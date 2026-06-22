package su.terrafirmagreg.core.common.blockentity;

import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.block.CoconutClusterBlock;

public class PalmClusterBlockEntity extends TickCounterBlockEntity {

    public PalmClusterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PalmClusterBlockEntity cluster) {
        if (state.getBlock() instanceof CoconutClusterBlock coconutCluster && state.getValue(CoconutClusterBlock.NATURAL)) {
            if (cluster.getLastUpdateTick() == Integer.MIN_VALUE) {
                cluster.resetCounter();
            }
            while (cluster.getTicksSinceUpdate() >= ICalendar.TICKS_IN_DAY) {
                cluster.reduceCounter(ICalendar.TICKS_IN_DAY);
                coconutCluster.onUpdate(level, pos, state);
                state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof CoconutClusterBlock)) {
                    break;
                }
            }
        }
    }
}
