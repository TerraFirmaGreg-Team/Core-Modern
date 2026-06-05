package su.terrafirmagreg.core.common.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.block.PalmFruitClusterBlock;
import su.terrafirmagreg.core.common.block.PalmHeadBlock;
import su.terrafirmagreg.core.common.data.PalmTrees;

public class PalmHeadBlockEntity extends TickCounterBlockEntity {

    private final PalmTrees tree;

    public PalmHeadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, PalmTrees tree) {
        super(type, pos, state);
        this.tree = tree;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PalmHeadBlockEntity palmHead) {
        if (level.getGameTime() % 400 == 0 && state.hasProperty(PalmHeadBlock.NATURAL) && state.getValue(PalmHeadBlock.NATURAL)) {
            palmHead.tryProduceFruit(level, pos, state);
        }
    }

    protected void tryProduceFruit(Level level, BlockPos pos, BlockState state) {
        if (isProductionMonth()) {
            List<Direction> directions = new ArrayList<>(List.of(Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new)));
            Collections.shuffle(directions);
            for (Direction direction : directions) {
                BlockPos fruitPos = pos.relative(direction);
                if (level.isEmptyBlock(fruitPos)) {
                    Block fruitBlock = getFruitBlock();
                    BlockState fruitState = fruitBlock.defaultBlockState();
                    if (fruitState.hasProperty(PalmFruitClusterBlock.FACING)) {
                        fruitState = fruitState.setValue(PalmFruitClusterBlock.FACING, direction.getOpposite());
                    }
                    if (fruitState.hasProperty(PalmFruitClusterBlock.ATTACHED)) {
                        fruitState = fruitState.setValue(PalmFruitClusterBlock.ATTACHED, true);
                    }
                    if (fruitState.hasProperty(PalmFruitClusterBlock.NATURAL)) {
                        fruitState = fruitState.setValue(PalmFruitClusterBlock.NATURAL, true);
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
        return tree.getFruitBlock().get();
    }
}
