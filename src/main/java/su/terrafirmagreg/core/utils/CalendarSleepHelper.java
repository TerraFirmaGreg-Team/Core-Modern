/*
 * Backport of TerraFirmaCraft 4 non-overworld sleep calendar handling.
 * @see net.dries007.tfc.util.calendar.CalendarEventHandler#onPlayersFinishedSleeping
 */
package su.terrafirmagreg.core.utils;

import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class CalendarSleepHelper {

    /** TFC 3 morning after sleep ({@code Level#getDayTime()} where {@code 0 = 6:00 AM}). */
    private static final long WAKE_UP_DAY_TIME = 0L;

    private CalendarSleepHelper() {
    }

    /**
     * Advances the TFC calendar to the next morning when players finish sleeping outside the Overworld.
     * TFC 3 only syncs calendar time from {@code PlayerWakeUpEvent} using the current dimension's
     * {@code getDayTime()}, which does not change in Nether/Beneath when {@code doDaylightCycle} is off.
     */
    public static void onPlayersFinishedSleeping(ServerLevel level) {
        if (level.dimension() == Level.OVERWORLD) {
            return;
        }

        var calendar = Calendars.SERVER;
        long currentDayTime = calendar.getCalendarDayTime();
        if (currentDayTime == WAKE_UP_DAY_TIME) {
            return;
        }

        long jump = WAKE_UP_DAY_TIME - currentDayTime;
        if (jump <= 0) {
            jump += ICalendar.TICKS_IN_DAY;
        }

        calendar.setTimeFromCalendarTime(calendar.getCalendarTicks() + jump);

        float exhaustion = jump * TFCFoodData.PASSIVE_EXHAUSTION_PER_TICK
                * TFCConfig.SERVER.passiveExhaustionModifier.get().floatValue();
        for (ServerPlayer player : level.players()) {
            player.causeFoodExhaustion(exhaustion);
        }
    }
}
