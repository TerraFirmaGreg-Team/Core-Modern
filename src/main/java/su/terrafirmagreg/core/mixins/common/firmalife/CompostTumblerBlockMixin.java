package su.terrafirmagreg.core.mixins.common.firmalife;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.eerussianguy.firmalife.common.blocks.CompostTumblerBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
@Mixin(value = CompostTumblerBlock.class, remap = false)
public abstract class CompostTumblerBlockMixin extends Block implements IRotate {

    public CompostTumblerBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(CompostTumblerBlock.FACING).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(CompostTumblerBlock.FACING).getAxis();
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        tfg$notifyCreateNeighbors(level, pos);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            tfg$notifyCreateNeighbors(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Unique
    private void tfg$notifyCreateNeighbors(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            for (Direction direction : Direction.values()) {
                if (level.getBlockEntity(pos.relative(direction)) instanceof KineticBlockEntity kbe) {
                    if (kbe.hasNetwork()) {
                        kbe.getOrCreateNetwork().updateStressFor(kbe, kbe.calculateStressApplied());
                    } else {
                        kbe.updateSpeed = true;
                    }
                }
            }
        }
    }
}
