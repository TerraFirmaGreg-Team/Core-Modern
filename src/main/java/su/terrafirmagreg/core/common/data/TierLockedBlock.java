package su.terrafirmagreg.core.common.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class TierLockedBlock extends Block {

    public TierLockedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        ItemStack stack = player.getMainHandItem();

        if (isCorrectTool(stack)) {
            return super.getDestroyProgress(state, player, level, pos);
        }

        return 0F;
    }

    private boolean isCorrectTool(ItemStack stack) {
        return stack.isCorrectToolForDrops(this.defaultBlockState());
    }
}
