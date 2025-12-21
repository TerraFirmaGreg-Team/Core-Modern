package su.terrafirmagreg.core.common.data.blocks;

import java.util.Map;

import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.data.TFGTags;

// Mostly copied from TFC's EpiphytePlantBlock

public class DecorativeAttachedPlantBlock extends DecorativePlantBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private final boolean allowVertical;

    protected static final VoxelShape NORTH_SHAPE = box(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape SOUTH_SHAPE = box(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
    protected static final VoxelShape WEST_SHAPE = box(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EAST_SHAPE = box(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
    protected static final VoxelShape UP_SHAPE = box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    protected static final VoxelShape DOWN_SHAPE = box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);

    protected static final Map<Direction, VoxelShape> SHAPES = ImmutableMap.of(
            Direction.NORTH, NORTH_SHAPE,
            Direction.SOUTH, SOUTH_SHAPE,
            Direction.WEST, WEST_SHAPE,
            Direction.EAST, EAST_SHAPE,
            Direction.UP, UP_SHAPE,
            Direction.DOWN, DOWN_SHAPE);

    public DecorativeAttachedPlantBlock(ExtendedProperties properties, boolean allowVertical) {
        super(properties, DecorativePlantBlock.DEFAULT_SHAPE);
        this.allowVertical = allowVertical;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level,
            BlockPos currentPos, BlockPos facingPos) {
        // Must be attached to a log
        if (direction.getOpposite() == state.getValue(FACING)
                && !Helpers.isBlock(facingState, TFGTags.Blocks.DecorativePlantAttachable)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);

        if (!allowVertical && (facing == Direction.DOWN || facing == Direction.UP)) {
            return false;
        }

        BlockState attachedState = level.getBlockState(pos.relative(facing.getOpposite()));
        return Helpers.isBlock(attachedState, TFGTags.Blocks.DecorativePlantAttachable);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        BlockState state = this.defaultBlockState();
        if (getFluidProperty().canContain(fluidState.getType())) {
            state = state.setValue(getFluidProperty(), getFluidProperty().keyForOrEmpty(fluidState.getType()));
        }

        Direction direction = context.getClickedFace();
        if (!allowVertical && (direction == Direction.DOWN || direction == Direction.UP)) {
            return null;
        }

        return state.setValue(FACING, direction);
    }
}
