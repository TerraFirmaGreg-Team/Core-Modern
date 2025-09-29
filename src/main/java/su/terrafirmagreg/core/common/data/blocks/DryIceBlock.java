package su.terrafirmagreg.core.common.data.blocks;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DryIceBlock extends IceBlock {
    public DryIceBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        level.removeBlock(pos, false);
    }

    @Override
    protected void melt(BlockState state, Level level, BlockPos pos) {
        level.removeBlock(pos, false);
    }
}
