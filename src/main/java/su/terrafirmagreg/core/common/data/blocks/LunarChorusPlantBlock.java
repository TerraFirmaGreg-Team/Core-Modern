package su.terrafirmagreg.core.common.data.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.TFGBlocks;

/**
 * Basically a copy of the vanilla Chorus Plant except that it grows on any rock/cobble/gravel/sand instead of being
 * limited to end stone
 */

public class LunarChorusPlantBlock extends ChorusPlantBlock {

    public LunarChorusPlantBlock(Properties props) {
        super(props);
    }

    @Override
    public BlockState getStateForPlacement(BlockGetter pLevel, BlockPos pPos) {
        BlockState below = pLevel.getBlockState(pPos.below());
        BlockState above = pLevel.getBlockState(pPos.above());
        BlockState north = pLevel.getBlockState(pPos.north());
        BlockState east = pLevel.getBlockState(pPos.east());
        BlockState south = pLevel.getBlockState(pPos.south());
        BlockState west = pLevel.getBlockState(pPos.west());

        Block lunarChorusFlower = TFGBlocks.LUNAR_CHORUS_FLOWER.get();

        return this.defaultBlockState()
                .setValue(DOWN,
                        below.is(this) || below.is(lunarChorusFlower) || LunarChorusFlowerBlock.isGroundBlock(below))
                .setValue(UP, above.is(this) || above.is(lunarChorusFlower))
                .setValue(NORTH, north.is(this) || north.is(lunarChorusFlower))
                .setValue(EAST, east.is(this) || east.is(lunarChorusFlower))
                .setValue(SOUTH, south.is(this) || south.is(lunarChorusFlower))
                .setValue(WEST, west.is(this) || west.is(lunarChorusFlower));
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
            BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (!pState.canSurvive(pLevel, pCurrentPos)) {
            pLevel.scheduleTick(pCurrentPos, this, 1);
            return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
        } else {
            boolean isFacing = pFacingState.is(this)
                    || pFacingState.is(TFGBlocks.LUNAR_CHORUS_FLOWER.get())
                    || pFacing == Direction.DOWN && LunarChorusFlowerBlock.isGroundBlock(pFacingState);
            return pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), isFacing);
        }
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockState belowState = pLevel.getBlockState(pPos.below());
        BlockState aboveState = pLevel.getBlockState(pPos.above());
        boolean isAboveAndBelowNotAir = !aboveState.isAir() && !belowState.isAir();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos relativePos = pPos.relative(dir);
            BlockState relativeState = pLevel.getBlockState(relativePos);
            if (relativeState.is(this)) {
                if (isAboveAndBelowNotAir) {
                    return false;
                }

                BlockState relativeBelowState = pLevel.getBlockState(relativePos.below());
                if (relativeBelowState.is(this) || LunarChorusFlowerBlock.isGroundBlock(relativeBelowState)) {
                    return true;
                }
            }
        }

        return belowState.is(this) || LunarChorusFlowerBlock.isGroundBlock(belowState);
    }
}
