package su.terrafirmagreg.core.common.data.blocks;

import net.dries007.tfc.common.blocks.CharcoalPileBlock;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class LayerBlock extends Block {
	public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;

	private final Supplier<ItemLike> m_cloneSupplier;


	public LayerBlock(Supplier<ItemLike> item, Properties properties)
	{
		super(properties);
		m_cloneSupplier = item;
	}

    public LayerBlock(Properties properties) {
        super(properties);
        m_cloneSupplier = null;
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1)));
    }


	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid)
	{
		playerWillDestroy(level, pos, state, player);

		if (player.isCreative())
		{
			return level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		}

		int prevLayers = state.getValue(LAYERS);
		if (prevLayers == 1)
		{
			return level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		}
		return level.setBlock(pos, state.setValue(LAYERS, prevLayers - 1), level.isClientSide ? 11 : 3);
	}

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
	public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type)
	{
		return type == PathComputationType.LAND && state.getValue(LAYERS) < 5;
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
	{
		if (!level.isClientSide() && facing == Direction.DOWN)
		{
			if (Helpers.isBlock(facingState, this))
			{
				int layersAt = stateIn.getValue(LAYERS);
				int layersUnder = facingState.getValue(LAYERS);
				if (layersUnder < 8)
				{
					if (layersUnder + layersAt <= 8)
					{
						level.setBlock(facingPos, facingState.setValue(LAYERS, layersAt + layersUnder), 3);
						level.destroyBlock(currentPos, false); // Have to destroy the block to prevent it from dropping an additional wood ash
						return Blocks.AIR.defaultBlockState();
					}
					else
					{
						level.setBlock(facingPos, facingState.setValue(LAYERS, 8), 3);
						return stateIn.setValue(LAYERS, layersAt + layersUnder - 8);
					}
				}
			}
		}
		return canSurvive(stateIn, level, currentPos) ? stateIn : Blocks.AIR.defaultBlockState();
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean useShapeForLightOcclusion(BlockState state)
	{
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos)
	{
		return CharcoalPileBlock.SHAPE_BY_LAYER[state.getValue(LAYERS)];
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
	{
		BlockState blockstate = level.getBlockState(pos.below());
		return Block.isFaceFull(blockstate.getCollisionShape(level, pos.below()), Direction.UP) || (blockstate.getBlock() == this && blockstate.getValue(LAYERS) == 8);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return CharcoalPileBlock.SHAPE_BY_LAYER[state.getValue(LAYERS)];
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		return CharcoalPileBlock.SHAPE_BY_LAYER[state.getValue(LAYERS) - 1];
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context)
	{
		return CharcoalPileBlock.SHAPE_BY_LAYER[state.getValue(LAYERS)];
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder.add(LAYERS));
	}
}
