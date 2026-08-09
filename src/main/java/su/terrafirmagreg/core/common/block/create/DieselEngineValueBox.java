/*
 * Originally from [Create Diesel Generators] (https://github.com/george8188625/Create-Diesel-Generators)
 * Licensed under the MIT license.
 */

package su.terrafirmagreg.core.common.block.create;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DieselEngineValueBox extends ValueBoxTransform.Sided {

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }

    @Override
    protected boolean isSideActive(BlockState state, Direction side) {
        if (state.getValue(DieselEngineBlock.FACING) == Direction.UP)
            return side == Direction.WEST;
        if (state.getValue(DieselEngineBlock.FACING) == Direction.DOWN)
            return side == Direction.NORTH;
        return side == Direction.UP;
    }

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        if (state.getValue(DieselEngineBlock.FACING) == Direction.UP)
            return VecHelper.voxelSpace(3, 8, 8);
        if (state.getValue(DieselEngineBlock.FACING) == Direction.DOWN)
            return VecHelper.voxelSpace(8, 8, 3);
        return VecHelper.voxelSpace(8, 13, 8);
    }

    @Override
    public float getScale() {
        return 0.5f;
    }
}
