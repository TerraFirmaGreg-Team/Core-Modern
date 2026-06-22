package su.terrafirmagreg.core.common.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.block.CoconutClusterBlock;
import su.terrafirmagreg.core.common.block.PalmHeadBlock;
import su.terrafirmagreg.core.common.data.PalmTrees;

public class PalmHeadBlockEntity extends TickCounterBlockEntity {

    private final PalmTrees tree;

    public PalmHeadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PalmTrees tree) {
        super(type, pos, state);
        this.tree = tree;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PalmHeadBlockEntity palmHead) {
        if (state.hasProperty(PalmHeadBlock.NATURAL) && state.getValue(PalmHeadBlock.NATURAL)) {
            if (palmHead.getLastUpdateTick() == Integer.MIN_VALUE) {
                palmHead.resetCounter();
            }
            while (palmHead.getTicksSinceUpdate() >= ICalendar.TICKS_IN_DAY) {
                palmHead.reduceCounter(ICalendar.TICKS_IN_DAY);
                if (palmHead.isCorrectClimate(level, pos)) {
                    palmHead.tryProduceFruit(level, pos);
                }
            }
        }
    }

    protected boolean isCorrectClimate(Level level, BlockPos pos) {
        ClimateRange range = tree.getClimateRange().get();
        int hydration = (int) (Climate.getRainfall(level, pos) / 5);
        float temperature = Climate.getAverageTemperature(level, pos);
        return range.checkBoth(hydration, temperature, false);
    }

    protected void tryProduceFruit(Level level, BlockPos pos) {
        if (isProductionMonth()) {
            List<Direction> directions = new ArrayList<>(List.of(Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new)));
            Collections.shuffle(directions);
            for (Direction direction : directions) {
                BlockPos fruitPos = pos.relative(direction);
                if (level.isEmptyBlock(fruitPos)) {
                    Block fruitBlock = getFruitBlock();
                    BlockState fruitState = fruitBlock.defaultBlockState();
                    if (fruitState.hasProperty(CoconutClusterBlock.FACING)) {
                        fruitState = fruitState.setValue(CoconutClusterBlock.FACING, direction.getOpposite());
                    }
                    if (fruitState.hasProperty(CoconutClusterBlock.ATTACHED)) {
                        fruitState = fruitState.setValue(CoconutClusterBlock.ATTACHED, true);
                    }
                    if (fruitState.hasProperty(CoconutClusterBlock.NATURAL)) {
                        fruitState = fruitState.setValue(CoconutClusterBlock.NATURAL, true);
                    }
                    level.setBlockAndUpdate(fruitPos, fruitState);
                    break;
                }
            }
        }
    }

    protected boolean isProductionMonth() {
        assert level != null;
        var calendar = Calendars.get(level);
        Month current = ICalendar.getMonthOfYear(calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());
        Month start = getStartMonth();
        Month end = getEndMonth();

        if (start.ordinal() <= end.ordinal()) {
            return current.ordinal() >= start.ordinal() && current.ordinal() <= end.ordinal();
        } else {
            return current.ordinal() >= start.ordinal() || current.ordinal() <= end.ordinal();
        }
    }

    protected @NotNull Month getStartMonth() {
        return tree.getStartMonth();
    }

    protected @NotNull Month getEndMonth() {
        return tree.getEndMonth();
    }

    protected @NotNull Block getFruitBlock() {
        return tree.getFruitClusterBlock().get();
    }
}
