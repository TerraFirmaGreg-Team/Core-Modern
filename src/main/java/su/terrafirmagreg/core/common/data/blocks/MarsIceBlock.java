package su.terrafirmagreg.core.common.data.blocks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.TFGBlocks;

public class MarsIceBlock extends IceBlock {

    public MarsIceBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(level, player, pos, state, te, stack);
        if (level.dimensionType().ultraWarm()) {
            level.removeBlock(pos, false);
            return;
        }

        final BlockState belowState = level.getBlockState(pos.below());
        if (belowState.blocksMotion() || belowState.liquid()) {
            level.setBlockAndUpdate(pos, TFGBlocks.MARS_WATER.get().defaultBlockState());
        }
    }

    @Override
    protected void melt(BlockState state, Level level, BlockPos pos) {
        if (level.dimensionType().ultraWarm()) {
            level.removeBlock(pos, false);
        } else {
            level.setBlockAndUpdate(pos, TFGBlocks.MARS_WATER.get().defaultBlockState());
            level.neighborChanged(pos, TFGBlocks.MARS_WATER.get(), pos);
        }
    }
}
