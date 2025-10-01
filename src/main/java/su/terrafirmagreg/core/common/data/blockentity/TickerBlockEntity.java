package su.terrafirmagreg.core.common.data.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.blocks.ParticleEmitterBlock;

public class TickerBlockEntity extends BlockEntity {
    public TickerBlockEntity(BlockPos pos, BlockState state) {
        super(TFGBlockEntities.TICKER_ENTITY.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TickerBlockEntity be) {
        if (level.isClientSide && state.getBlock() instanceof ParticleEmitterBlock block) {
            block.animateTick(state, level, pos, level.random);
        }
    }
}
