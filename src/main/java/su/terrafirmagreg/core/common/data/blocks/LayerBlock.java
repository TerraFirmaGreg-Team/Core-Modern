package su.terrafirmagreg.core.common.data.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class LayerBlock extends AbstractLayerBlock {
	private final Supplier<ItemLike> m_cloneSupplier;

    public LayerBlock(Properties properties) {
        this(null, properties);
    }

	public LayerBlock(Supplier<ItemLike> item, Properties properties)
	{
		super(properties);
		m_cloneSupplier = item;
	}

    @Override
    public float getSpeedFactor() {
        return 1.0f;
    }

    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {}

    @Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player)
	{
        if (m_cloneSupplier == null) {
            return new ItemStack(this);
        }
		return new ItemStack(m_cloneSupplier.get());
	}

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
//        if (!level.isClientSide() && facing == Direction.DOWN)
//        {
//            if (Helpers.isBlock(facingState, this))
//            {
//                int layersAt = stateIn.getValue(LAYERS);
//                int layersUnder = facingState.getValue(LAYERS);
//                if (layersUnder < 8)
//                {
//                    if (layersUnder + layersAt <= 8)
//                    {
//                        level.setBlock(facingPos, facingState.setValue(LAYERS, layersAt + layersUnder), 3);
//                        level.destroyBlock(currentPos, false); // Have to destroy the block to prevent it from dropping an additional wood ash
//                        return Blocks.AIR.defaultBlockState();
//                    }
//                    else
//                    {
//                        level.setBlock(facingPos, facingState.setValue(LAYERS, 8), 3);
//                        return stateIn.setValue(LAYERS, layersAt + layersUnder - 8);
//                    }
//                }
//            }
//        }
//        return canSurvive(stateIn, level, currentPos) ? stateIn : Blocks.AIR.defaultBlockState();
        return super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos);
    }

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder.add(LAYERS));
	}
}
