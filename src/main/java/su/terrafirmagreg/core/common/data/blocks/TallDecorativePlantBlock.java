package su.terrafirmagreg.core.common.data.blocks;

import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import su.terrafirmagreg.core.compat.kjs.TFGBlockProperties;

public class TallDecorativePlantBlock extends ExtendedBlock implements IFluidLoggable {

	public static final FluidProperty FLUID = TFGBlockProperties.SPACE_WATER;
	public static final VoxelShape DEFAULT_SHAPE = Block.box(2.0F, 0.0F, 2.0F, 14.0F, 16.0F, 14.0F);
	public static final IntegerProperty HEIGHT = TFGBlockProperties.HEIGHT;
	private final VoxelShape shape;
	private final int maxHeight;

	public TallDecorativePlantBlock(ExtendedProperties properties, VoxelShape shape, int maxHeight) {
		super(properties);
		this.shape = shape;
		this.maxHeight = maxHeight;

		this.registerDefaultState(this.stateDefinition.any()
									  .setValue(TFGBlockProperties.HEIGHT, 0)
									  .setValue(getFluidProperty(), getFluidProperty().keyFor(Fluids.EMPTY)));

		getExtendedProperties().offsetType(OffsetType.XZ);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(TFGBlockProperties.HEIGHT);
		builder.add(getFluidProperty());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		FluidState fluidState = context.getLevel().getFluidState(pos);

		BlockState state = super.getStateForPlacement(context);
		if (getFluidProperty().canContain(fluidState.getType()))
		{
			state = state.setValue(getFluidProperty(), getFluidProperty().keyForOrEmpty(fluidState.getType()));
		}
		return state;
	}

	@Override
	public FluidProperty getFluidProperty() {
		return FLUID;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shape != null ? shape : super.getShape(state, level, pos, context);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		int h = state.getValue(TFGBlockProperties.HEIGHT);

		if (h == 0) {
			BlockPos below = pos.below();
			BlockState belowState = level.getBlockState(below);
			return belowState.isFaceSturdy(level, below, Direction.UP);
		} else {
			BlockState belowState = level.getBlockState(pos.below());
			return belowState.is(this) && belowState.getValue(TFGBlockProperties.HEIGHT) - 1 == h;
		}
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getValue(TFGBlockProperties.HEIGHT) != 0) {
			return;
		}

		BlockPos test = pos.above();
		for (int i = 0; i < maxHeight; i++) {
			if (!level.getBlockState(test).canBeReplaced()) {
				return;
			}
			test = test.above();
		}

		test = pos.above();
		for (int i = 1; i < maxHeight; i++) {
			level.setBlock(test, this.defaultBlockState().setValue(TFGBlockProperties.HEIGHT, i), 3);
			test = test.above();
		}
	}

	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
		playerWillDestroy(level, pos, state, player);

		BlockPos toDestroyPos = pos.below();
		BlockState nextState = level.getBlockState(toDestroyPos);

		while (nextState.is(this)) {
			level.destroyBlock(toDestroyPos, false, player);
			toDestroyPos = toDestroyPos.below();
			nextState = level.getBlockState(toDestroyPos);
		}

		toDestroyPos = pos.above();
		nextState = level.getBlockState(toDestroyPos);

		while (nextState.is(this)) {
			level.destroyBlock(toDestroyPos, false, player);
			toDestroyPos = toDestroyPos.above();
			nextState = level.getBlockState(toDestroyPos);
		}

		return level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
	}

	@Override
	@SuppressWarnings("deprecation")
	public FluidState getFluidState(BlockState state) {
		return IFluidLoggable.super.getFluidLoggedState(state);
	}
}
