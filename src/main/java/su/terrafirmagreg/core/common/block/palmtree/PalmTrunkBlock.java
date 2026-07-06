package su.terrafirmagreg.core.common.block.palmtree;

import javax.annotation.Nullable;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_PalmTrees;

@SuppressWarnings("deprecation")
public class PalmTrunkBlock extends Block implements IFluidLoggable {

    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 2);
    public static final Property<Boolean> BASE = BooleanProperty.create("base");
    public static final FluidProperty FLUID = TFCBlockStateProperties.WATER;

    public static final VoxelShape SHAPE0 = box(2, 0, 2, 14, 16, 14);
    public static final VoxelShape SHAPE1 = box(3, 0, 3, 13, 16, 13);
    public static final VoxelShape SHAPE2 = box(4, 0, 4, 12, 16, 12);

    public PalmTrunkBlock(Properties p) {
        super(p);
        this.registerDefaultState(this.stateDefinition.any().setValue(SIZE, 0).setValue(BASE, false).setValue(getFluidProperty(), getFluidProperty().keyFor(Fluids.EMPTY)));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        boolean persist = state.getValue(BASE);

        if (persist) {
            return true;
        } else {
            return below.isFaceSturdy(level, pos.below(), Direction.UP) || below.is(TFGBlocks_PalmTrees.PALM_TRUNK.get());
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            if (level instanceof Level l && !l.isClientSide) {
                l.destroyBlock(pos, true);
            }
            return Blocks.AIR.defaultBlockState();
        }
        FluidHelpers.tickFluid(level, pos, state);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return IFluidLoggable.super.getFluidLoggedState(state);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        final BlockState state = super.getStateForPlacement(context);
        final FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        if (state != null && !fluidState.isEmpty()) {
            return state.setValue(getFluidProperty(), getFluidProperty().keyForOrEmpty(fluidState.getType()));
        }
        return state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(SIZE)) {
            case 1 -> SHAPE1;
            case 2 -> SHAPE2;
            default -> SHAPE0;
        };
    }

    @Override
    public FluidProperty getFluidProperty() {
        return FLUID;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE, BASE, getFluidProperty());
    }
}
