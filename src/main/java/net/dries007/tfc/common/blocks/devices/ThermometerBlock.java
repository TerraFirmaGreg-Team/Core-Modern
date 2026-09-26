/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.blocks.devices;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.ThermometerBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class ThermometerBlock extends DeviceBlock
{
    public static IntegerProperty POWER = BlockStateProperties.POWER;
    public static BooleanProperty ATTACHED = TFCBlockStateProperties.THERMOMETER_ATTACHED;
    public static DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE_NORTH = box(4D, 1D, 14D, 12D, 15D, 16D);
    private static final VoxelShape SHAPE_SOUTH = box(4D, 1D, 0D, 12D, 15D, 2D);
    private static final VoxelShape SHAPE_EAST = box(0D, 1D, 4D, 2D, 15D, 12D);
    private static final VoxelShape SHAPE_WEST = box(14D, 1D, 4D, 16D, 15D, 12D);

    public ThermometerBlock(ExtendedProperties properties)
    {
        super(properties, InventoryRemoveBehavior.NOOP);
        registerDefaultState(getStateDefinition().any().setValue(POWER, 0).setValue(FACING, Direction.NORTH).setValue(ATTACHED, Boolean.FALSE));

    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        super.onRemove(state, level, pos, newState, isMoving);
        level.updateNeighborsAt(pos.relative(state.getValue(FACING).getOpposite()), this); // needed for strong power update
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState state = Helpers.getSupportedDirectionalStateForPlacement(this, context, true);

        if (state == null)
        {
            return null;
        }
        final Direction direction = state.getValue(FACING);
        final LevelReader levelreader = context.getLevel();
        final BlockPos blockpos = context.getClickedPos();

        if (levelreader.getBlockState(blockpos.relative(direction.getOpposite())).is(TFCTags.Blocks.THERMOMETER_READABLE))
        {
            state = state.setValue(ATTACHED, true);
        }

        return state;
    }

    @Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (level.getBlockEntity(pos) instanceof ThermometerBlockEntity thermometer)
        {
            thermometer.needsInstantUpdate();
        }
        level.updateNeighborsAt(pos.relative(state.getValue(FACING).getOpposite()), this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(POWER).add(FACING).add(ATTACHED);
    }

    @Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return switch (state.getValue(FACING))
        {
            case NORTH, DOWN, UP -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
        };
    }

    @Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos blockpos, BlockPos facingPos)
    {
        if (facing == state.getValue(FACING).getOpposite() && !this.canSurvive(state, level, blockpos))
        {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, blockpos, facingPos);
    }

    @Override
	public boolean isSignalSource(BlockState state)
    {
        return true;
    }

    @Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
    {
        if (blockState.getValue(FACING) == side && !blockState.getValue(ATTACHED))
        {
            return getSignal(blockState, blockAccess, pos, side);
        }
        return 0;
    }

    @Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
    {
        if (Helpers.isBlock(blockState, TFCBlocks.THERMOMETER.get()))
        {
            return blockState.getValue(POWER);
        }
        return 0;
    }

    @Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
        return canSurvive(level, pos, state.getValue(FACING));
    }

    public static boolean canSurvive(LevelReader level, BlockPos pos, Direction facing)
    {
        BlockPos blockpos = pos.relative(facing.getOpposite());
        BlockState blockstate = level.getBlockState(blockpos);
        if (Helpers.isBlock(blockstate, TFCTags.Blocks.THERMOMETER_READABLE))
        {
            return true;
        }
        return blockstate.isFaceSturdy(level, blockpos, facing);
    }

    @Override
	public BlockState rotate(BlockState state, Rotation rot)
    {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
	public BlockState mirror(BlockState state, Mirror mirror)
    {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
}
