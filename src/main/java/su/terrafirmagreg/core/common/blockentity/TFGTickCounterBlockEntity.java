package su.terrafirmagreg.core.common.blockentity;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;

/**
 * A tick counter block entity that uses TFG's own block entity type instead of TFC's.
 * TFC hard codes the tick counter block entity type in its fruit tree code, so we need to create our own.
 */
public class TFGTickCounterBlockEntity extends TickCounterBlockEntity {

    /**
     * Resets the tick counter for the block entity.
     *
     * @param level Level.
     * @param pos The position of the block entity to reset.
     */
    public static void reset(Level level, @NotNull BlockPos pos) {
        level.getBlockEntity(pos, TFGBlockEntities.FRUIT_TREE_TICK_COUNTER.get())
                .ifPresent(TickCounterBlockEntity::resetCounter);
    }

    /**
     * Constructs a new {@link TFGTickCounterBlockEntity}.
     *
     * @param pos The position of the block entity.
     * @param state The block state of the block entity.
     */
    public TFGTickCounterBlockEntity(BlockPos pos, BlockState state) {
        super(TFGBlockEntities.FRUIT_TREE_TICK_COUNTER.get(), pos, state);
    }

    @Override
    public @NotNull BlockEntityType<?> getType() {
        return TFGBlockEntities.FRUIT_TREE_TICK_COUNTER.get();
    }
}
