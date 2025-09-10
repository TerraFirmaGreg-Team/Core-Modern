package su.terrafirmagreg.core.common.data.blocks;

import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.compat.kjs.TFGBlockProperties;

public class DecorativePlantBlock extends ExtendedBlock implements IFluidLoggable {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final FluidProperty FLUID = TFGBlockProperties.SPACE_WATER;
    public static final VoxelShape DEFAULT_SHAPE = Block.box(3.0F, 0.0F, 3.0F, 13.0F, 7.0F, 13.0F);

    private final VoxelShape shape;

    public DecorativePlantBlock(ExtendedProperties properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(getFluidProperty(), getFluidProperty().keyFor(Fluids.EMPTY)));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(getFluidProperty());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);

        BlockState state = this.defaultBlockState();
        if (getFluidProperty().canContain(fluidState.getType())) {
            state = state.setValue(getFluidProperty(), getFluidProperty().keyForOrEmpty(fluidState.getType()));
        }

        state = state.setValue(FACING, context.getHorizontalDirection().getOpposite());
        return state;
    }

    @Override
    public FluidProperty getFluidProperty() {
        return FLUID;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        var attachedBlock = pos.relative(Direction.DOWN);
        return level.getBlockState(attachedBlock).isFaceSturdy(level, attachedBlock, Direction.UP);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (!canSurvive(state, level, pos)) {
            Block.updateOrDestroy(state, Blocks.AIR.defaultBlockState(), level, pos, Block.UPDATE_ALL);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return IFluidLoggable.super.getFluidLoggedState(state);
    }
}
