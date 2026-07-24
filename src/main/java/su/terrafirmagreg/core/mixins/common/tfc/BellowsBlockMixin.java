package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.content.kinetics.base.IRotate;

import net.dries007.tfc.common.blocks.devices.BellowsBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = BellowsBlock.class, remap = false)
public abstract class BellowsBlockMixin implements IRotate {
    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(BellowsBlock.FACING).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(BellowsBlock.FACING).getAxis();
    }
}
