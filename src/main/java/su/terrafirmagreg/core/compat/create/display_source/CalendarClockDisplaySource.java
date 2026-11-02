package su.terrafirmagreg.core.compat.create.display_source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.CalendarClockBlock;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Month;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings({ "NoTranslation", "deprecation" })
public class CalendarClockDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        Level level = context.level();
        BlockPos pos = context.getSourcePos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (!state.is(TFCBlocks.CALENDAR_CLOCK.get())) {
            return ZERO.copy();
        }

        int mode = context.sourceConfig().getInt("Mode");
        return switch (mode) {
            case 0 ->
                // Weekday (Monday, Tuesday, etc.)
                Calendars.SERVER.getCalendarDayOfYear();
            case 1 ->
                // Time and Date (24:24 month dd, yyyy)
                Calendars.SERVER.getCalendarTimeAndDate();
            case 2 ->
                // Time and Date (12:12 AM/PM month dd, yyyy)
                Calendars.SERVER.getCalendarTimeAndDate12();
            case 3 ->
                // Weekday and Date (weekday month dd, yyyy)
                Calendars.SERVER.getCalendarWeekdayAndDate();
            case 4 ->
                // Date (mm/dd/yyyy)
                Calendars.SERVER.getCalendarDateUS();
            case 5 ->
                // Date (dd/mm/yyyy)
                Calendars.SERVER.getCalendarDateEU();
            case 6 ->
                // Date (yyyy-mm-dd)
                Calendars.SERVER.getCalendarDateISO();
            case 7 ->
                // Month and Day (month-dd)
                Calendars.SERVER.getCalendarMonthAndDay();
            case 8 ->
                // Season
                Component.translatable(Calendars.SERVER.getCalendarMonthOfYear().getTranslationKey(Month.Style.SEASON));
            default -> {
                // Redstone Power Output
                int power = block.getSignal(state, level, pos, state.getValue(CalendarClockBlock.FACING));
                yield Component.literal(String.valueOf(power));
            }
        };
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }

    @Override
    protected String getTranslationKey() {
        return "calendar_clock";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (!isFirstLine) {
            builder.addSelectionScrollInput(0, 120, (si, l) -> si
                    .forOptions(CreateLang.translatedOptions("display_source.calendar_clock",
                            "day_of_week",
                            "time_and_date_full_24",
                            "time_and_date_full_12",
                            "weekday_and_date",
                            "date_us",
                            "date_eu",
                            "date_iso",
                            "month_and_day",
                            "season",
                            "redstone_power"))
                    .titled(Component.translatable("tfg.display_source.calendar_clock.display_mode")), "Mode");
        }
    }
}
