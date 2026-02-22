package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.item.tool.behavior.HarvestCropsBehavior;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.crop.ClimbingCropBlock;
import net.dries007.tfc.common.blocks.crop.DeadCropBlock;
import net.dries007.tfc.common.blocks.crop.DoubleCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
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
        boolean isDeadCrop = blockState.getBlock() instanceof DeadCropBlock;
        boolean isAliveCrop = blockState.getBlock() instanceof CropBlock;

        if (!isDeadCrop && !isAliveCrop)
            return false;

        if ((blockState.getBlock() instanceof CropBlock cropBlock) && !cropBlock.isMaxAge(blockState))
            return false;

        // Getting the crop bottom block assuming crops are max 2 block tall
        BlockPos belowPos = pos.below();
        BlockState belowState = world.getBlockState(belowPos);
        BlockPos targetPos;
        if (belowState.getBlock() == blockState.getBlock()) {
            targetPos = belowPos;
            blockState = belowState;
        } else {
            targetPos = pos;
        }

        // Not processing top blocks of plants
        belowPos = targetPos.below();
        belowState = world.getBlockState(belowPos);
        boolean isTfcFarmland = belowState.is(TFCTags.Blocks.FARMLAND);
        if (!isTfcFarmland)
            return false;

        BlockEntity be = world.getBlockEntity(targetPos);
        var drops = Block.getDrops(blockState, (ServerLevel) world, targetPos, be, player, stack);

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

            // Generating drops
            if (!drop.isEmpty())
                Block.popResource(world, targetPos, drop);
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
        FluidState fluidState = world.getFluidState(targetPos);
        world.setBlockAndUpdate(targetPos, fluidState.createLegacyBlock());
        world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, targetPos, Block.getId(blockState));

        // Replanting in the next tick to ensure multiblock crop tops break
        if (cropSeed != null) {
            ItemStack finalCropSeed = cropSeed;
            UseOnContext plantCtx = new UseOnContext(
                    world,
                    player,
                    InteractionHand.MAIN_HAND,
                    cropSeed,
                    new BlockHitResult(
                            Vec3.atCenterOf(targetPos),
                            Direction.UP,
                            targetPos,
                            false));
            int ticksDelay = 1;
            world.getServer().tell(new net.minecraft.server.TickTask(world.getServer().getTickCount() + ticksDelay, () -> {
                finalCropSeed.useOn(plantCtx);
            }));

            // Adding the stick when needed. Couldn't add it simulating player right click because the "use" method of ClimbingCropBlock internally gets the item in hand instead of the item in context
            ticksDelay += 1;
            if (removedStick) {
                world.getServer().tell(new net.minecraft.server.TickTask(world.getServer().getTickCount() + ticksDelay, () -> {
                    tfg$addStickToClimbableCrop(world, targetPos);
                }));
            }
        }

        ToolHelper.damageItem(stack, player);

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

    /** Filters blocks used in harvest routine
     * @author Ujhik
     * @reason To add dead crop blocks to the filter
     */
    @Inject(method = "isBlockCrops", at = @At("RETURN"), cancellable = true)
    private static void tfc$allowDeadCrops(UseOnContext context, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();

            if (level.getBlockState(pos.above()).isAir()) {
                Block block = level.getBlockState(pos).getBlock();
                if (block instanceof DeadCropBlock) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
