package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.eerussianguy.firmalife.common.blockentities.HangingPlanterBlockEntity;
import com.eerussianguy.firmalife.common.blockentities.LargePlanterBlockEntity;
import com.eerussianguy.firmalife.common.blocks.greenhouse.LargePlanterBlock;
import com.google.common.collect.Sets;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.item.tool.behavior.HarvestCropsBehavior;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.crop.ClimbingCropBlock;
import net.dries007.tfc.common.blocks.crop.DeadCropBlock;
import net.dries007.tfc.common.blocks.crop.DoubleCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

@Mixin(value = HarvestCropsBehavior.class, remap = false)
public abstract class HarvestCropsBehaviorMixin {
    @Unique
    private static final Set<Class<?>> CROP_BLOCKS_SET = Set.of(
            DeadCropBlock.class,
            CropBlock.class);

    @Unique
    private static final Set<Class<?>> PLANTER_BLOCKS_SET = Set.of(
            LargePlanterBlock.class);

    @Unique
    private static final Set<Class<?>> HARVESTABLE_BLOCKS_SET = Sets.union(CROP_BLOCKS_SET, PLANTER_BLOCKS_SET);

    @Unique
    private final static BooleanProperty CROP_STICK_PROPERTY = ClimbingCropBlock.STICK;

    /** Harvest routine used for the scythe
     * @author Ujhik
     * @reason To adapt GregTech harvest routine to TerraFirmaCraft crops so they reset to growth 0 correctly
     *         And to customize harvest behavior for TerraFirmaGreg
     */
    @Overwrite
    private static boolean harvestBlockRoutine(BlockPos pos, UseOnContext context) {
        Level world = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (world.isClientSide())
            return false;

        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        boolean isProcessed = false;

        if (tfg$isCrop(block)) {
            isProcessed = tfg$processCrop(world, stack, player, blockState, pos);
        }

        if (tfg$isPlanter(block)) {
            isProcessed = tfg$processPlanters(world, player, blockState, pos);
        }

        if (isProcessed && player != null) {
            ToolHelper.damageItem(stack, player);
            player.swing(context.getHand(), true);

        }

        return isProcessed;
    }

    /**
     * @author Ujhik
     * @reason To adapt sound to successful scythe use
     */
    @Inject(method = "onItemUse", at = @At("RETURN"))
    private void tfg$onItemUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() == InteractionResult.CONSUME) {
            context.getLevel().playSound(null, context.getClickedPos(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.BLOCKS, 1.0f, 0.5f);
        }
    }

    @Unique
    private static boolean tfg$processPlanters(Level level, Player player, BlockState blockState, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof LargePlanterBlockEntity planterBlockEntity) {
            InteractionResult result = null;
            Direction direction;

            if (blockEntity instanceof HangingPlanterBlockEntity)
                direction = Direction.DOWN;
            else {
                direction = Direction.UP;
            }

            for (int i = 0; i < planterBlockEntity.slots(); i++) {
                int iFinal = i;
                InteractionResult resultTemp = LargePlanterBlock.takeSlot(level, planterBlockEntity, i, (item) -> {
                    if (item.is(Tags.Items.SEEDS)) {
                        if (blockState.getBlock() instanceof LargePlanterBlock planterBlock) {
                            planterBlock.insertSlot(level, planterBlockEntity, item, player, iFinal);
                        }
                    }

                    Block.popResourceFromFace(level, pos, direction, item);
                });

                if (result != InteractionResult.CONSUME) {
                    result = resultTemp;
                }
            }

            return result == InteractionResult.CONSUME;
        }

        return false;
    }

    @Unique
    private static boolean tfg$processCrop(Level level, ItemStack stack, Player player, BlockState blockState, BlockPos pos) {
        if ((blockState.getBlock() instanceof CropBlock cropBlock) && !cropBlock.isMaxAge(blockState))
            return false;

        // Getting the crop bottom block assuming crops are max 2 block tall
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        BlockPos targetPos;
        if (belowState.getBlock() == blockState.getBlock()) {
            targetPos = belowPos;
            blockState = belowState;
        } else {
            targetPos = pos;
        }

        // Safety check
        if (!tfg$isCrop(blockState))
            return false;

        // Not processing top blocks of plants
        belowPos = targetPos.below();
        belowState = level.getBlockState(belowPos);
        boolean isTfcFarmland = belowState.is(TFCTags.Blocks.FARMLAND);
        if (!isTfcFarmland)
            return false;

        BlockEntity be = level.getBlockEntity(targetPos);
        var drops = Block.getDrops(blockState, (ServerLevel) level, targetPos, be, player, stack);

        // Detecting plants with sticks like tomatoes or green beans
        boolean needsStick = blockState.hasProperty(CROP_STICK_PROPERTY);

        ItemStack cropSeed = null;
        boolean removedSeed = false;
        boolean removedStick = false;
        for (ItemStack drop : drops) {
            // Accounting for the replanted seed
            if (!removedSeed && drop.is(Tags.Items.SEEDS)) {
                cropSeed = drop.copy();
                cropSeed.setCount(1);
                drop.shrink(1);
                removedSeed = true;
            }

            // Accounting for the replanted stick
            if (!removedStick && needsStick && drop.is(Items.STICK)) {
                drop.shrink(1);
                removedStick = true;
            }
        }

        // If not removed stick, try to remove one from inventory
        if (!removedStick && needsStick && player != null) {
            int slot = player.getInventory().findSlotMatchingItem(new ItemStack(Items.STICK));
            if (slot != -1) {
                ItemStack sticksStack = player.getInventory().getItem(slot);
                if (!sticksStack.isEmpty()) {
                    sticksStack.shrink(1);
                    removedStick = true;
                }
            }
        }

        // Replacing block to force multiblock crops to break without spawning drops
        FluidState fluidState = level.getFluidState(targetPos);
        level.setBlockAndUpdate(targetPos, fluidState.createLegacyBlock());

        BlockState stateAfter = level.getBlockState(targetPos);
        Block blockAfter = stateAfter.getBlock();

        // If break failed we exit
        boolean broken = blockAfter != blockState.getBlock();
        if (!broken)
            return false;

        // Replanting in the next tick to ensure multiblock crop tops break
        if (cropSeed != null) {
            ItemStack finalCropSeed = cropSeed;
            UseOnContext plantCtx = new UseOnContext(
                    level,
                    player,
                    InteractionHand.MAIN_HAND,
                    cropSeed,
                    new BlockHitResult(
                            Vec3.atCenterOf(targetPos),
                            Direction.UP,
                            targetPos,
                            false));

            BlockState previousBlockState = blockState;
            int ticksDelay = 1;
            level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + ticksDelay,
                    () -> {
                        BlockState checkState = level.getBlockState(targetPos);
                        boolean trulyBroken = checkState.getBlock() != previousBlockState.getBlock();

                        if (!trulyBroken)
                            return;

                        // Spawning drops
                        for (ItemStack drop : drops) {
                            if (!drop.isEmpty())
                                Block.popResource(level, targetPos, drop);
                        }

                        finalCropSeed.useOn(plantCtx);
                    }));

            // Adding the stick when needed. Couldn't add it simulating player right click because the "use" method of ClimbingCropBlock internally gets the item in hand instead of the item in context
            ticksDelay += 1;
            if (removedStick) {
                level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + ticksDelay,
                        () -> tfg$addStickToClimbableCrop(level, targetPos)));
            }
        }

        return true;
    }

    @Unique
    private static void tfg$addStickToClimbableCrop(Level level, BlockPos targetPos) {
        BlockState cropsBlockState = level.getBlockState(targetPos);

        //Safety check
        if (!cropsBlockState.hasProperty(CROP_STICK_PROPERTY) || cropsBlockState.getValue(CROP_STICK_PROPERTY))
            return;

        level.setBlock(targetPos, cropsBlockState.setValue(CROP_STICK_PROPERTY, true), Block.UPDATE_CLIENTS);
        level.setBlock(targetPos.above(), cropsBlockState.setValue(CROP_STICK_PROPERTY, true).setValue(DoubleCropBlock.PART, DoubleCropBlock.Part.TOP), Block.UPDATE_ALL);
    }

    @Unique
    private static boolean tfg$isCrop(Block block) {
        return CROP_BLOCKS_SET.stream().anyMatch(c -> c.isInstance(block));
    }

    @Unique
    private static boolean tfg$isCrop(BlockState blockState) {
        return tfg$isCrop(blockState.getBlock());
    }

    @Unique
    private static boolean tfg$isPlanter(Block block) {
        return PLANTER_BLOCKS_SET.stream().anyMatch(c -> c.isInstance(block));
    }

    @Unique
    private static boolean tfg$isHarvestable(Block block) {
        return HARVESTABLE_BLOCKS_SET.stream().anyMatch(c -> c.isInstance(block));
    }

    /** Filters blocks used in harvest routine
     * @author Ujhik
     * @reason To allow new harvestable blocks not covered in gregTech
     */
    @Inject(method = "isBlockCrops", at = @At("RETURN"), cancellable = true)
    private static void tfg$allowHarvestableBlocks(UseOnContext context, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();

            Block block = level.getBlockState(pos).getBlock();
            if (tfg$isHarvestable(block)) {
                cir.setReturnValue(true);
            }
        }
    }
}
