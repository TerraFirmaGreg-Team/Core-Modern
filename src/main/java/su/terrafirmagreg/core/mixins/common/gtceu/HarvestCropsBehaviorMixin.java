package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.item.tool.behavior.HarvestCropsBehavior;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.crop.DeadCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

@Mixin(value = HarvestCropsBehavior.class, remap = false)
public abstract class HarvestCropsBehaviorMixin {

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

        ItemStack cropSeed = null;
        boolean removedSeed = false;
        for (ItemStack drop : drops) {
            // Accounting for the replanted seed
            if (!removedSeed && drop.is(Tags.Items.SEEDS)) {
                cropSeed = drop.copy();
                cropSeed.setCount(1);
                drop.shrink(1);
                removedSeed = true;
            }

            Block.popResource(world, targetPos, drop);
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

            world.getServer().execute(() -> finalCropSeed.useOn(plantCtx));
        }

        ToolHelper.damageItem(stack, player);

        return true;
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
