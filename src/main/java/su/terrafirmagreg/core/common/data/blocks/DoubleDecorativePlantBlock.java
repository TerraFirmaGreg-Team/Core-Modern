package su.terrafirmagreg.core.common.data.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class DoubleDecorativePlantBlock extends DoublePlantBlock {

	public static final VoxelShape DEFAULT_SHAPE = Block.box(2.0F, 0.0F, 2.0F, 14.0F, 16.0F, 14.0F);
	private final VoxelShape shape;

	public DoubleDecorativePlantBlock(Properties properties, VoxelShape shape, Supplier<Item> itemSupplier) {
		super(properties);
		this.shape = shape;
		this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shape != null ? shape : super.getShape(state, level, pos, context);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);

		if (half == DoubleBlockHalf.LOWER) {
			BlockPos below = pos.below();
			BlockState belowState = level.getBlockState(below);
			return belowState.isFaceSturdy(level, below, Direction.UP);
		} else {
			BlockState belowState = level.getBlockState(pos.below());
			return belowState.is(this) && belowState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
		}
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
			BlockPos above = pos.above();
			if (level.getBlockState(above).canBeReplaced()) {
				level.setBlock(above, this.defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), 3);
			}
		}
	}
}
