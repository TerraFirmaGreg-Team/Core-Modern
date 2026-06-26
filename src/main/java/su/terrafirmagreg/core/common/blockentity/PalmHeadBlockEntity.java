package su.terrafirmagreg.core.common.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
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

import su.terrafirmagreg.core.common.block.palmtree.CoconutClusterBlock;
import su.terrafirmagreg.core.common.block.palmtree.PalmHeadBlock;
import su.terrafirmagreg.core.common.data.PalmTrees;

/**
 * Generic block entity for palm tree heads.
 */
public class PalmHeadBlockEntity extends TickCounterBlockEntity {

    private final PalmTrees tree;

    /**
     * Creates a new PalmHeadBlockEntity.
     * @param type Block entity type.
     * @param pos Position.
     * @param state Blockstate.
     * @param tree The type of palm tree.
     */
    public PalmHeadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PalmTrees tree) {
        super(type, pos, state);
        this.tree = tree;
    }

    /**
     * Server tick method for PalmHeadBlockEntity.
     * If the palm head is natural, it will try to produce a cluster once a day.
     * @param level Level.
     * @param pos Position.
     * @param state Blockstate.
     * @param palmHead PalmHeadBlockEntity instance.
     */
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

    /**
     * Checks if the climate is correct for the palm tree.
     * @param level Level.
     * @param pos Position.
     * @return True if the climate is correct.
     */
    protected boolean isCorrectClimate(Level level, BlockPos pos) {
        ClimateRange range = tree.getClimateRange().get();
        int hydration = (int) (Climate.getRainfall(level, pos) / 5);
        float temperature = Climate.getAverageTemperature(level, pos);
        return range.checkBoth(hydration, temperature, false);
    }

    /**
     * Tries to produce a fruit cluster on the palm head.
     * @param level Level.
     * @param pos Position.
     */
    protected void tryProduceFruit(Level level, BlockPos pos) {
        if (isFruitingMonth()) {
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

    /**
     * Checks if the current month is a fruiting month for the palm tree.
     * @return True if the current month is a fruiting month.
     */
    protected boolean isFruitingMonth() {
        assert level != null;
        var calendar = Calendars.get(level);
        Month current = ICalendar.getMonthOfYear(calendar.getCalendarTicks(), calendar.getCalendarDaysInMonth());

        return tree.getStages()[current.ordinal()] == Lifecycle.FRUITING;
    }

    /**
     * Gets the fruit cluster block for the palm tree.
     * @return The fruit cluster block.
     */
    protected @NotNull Block getFruitBlock() {
        return tree.getFruitClusterBlock().get();
    }
}
