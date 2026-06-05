package su.terrafirmagreg.core.common.loot.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import su.terrafirmagreg.core.common.data.TFGLootConditions;

/**
 * Loot condition that checks if the current month matches any of the specified months.
 */
public class MonthLootCondition implements LootItemCondition {

    private final boolean isReverse;
    private final List<Month> months;
    private final @Nullable Month start;
    private final @Nullable Month end;

    public MonthLootCondition(boolean isReverse, List<Month> months, @Nullable Month start, @Nullable Month end) {
        this.isReverse = isReverse;
        this.months = months == null ? List.of() : List.copyOf(months);
        this.start = start;
        this.end = end;
    }

    @Override
    public @NotNull LootItemConditionType getType() {
        return TFGLootConditions.MONTH_LOOT_CONDITION.get();
    }

    @Override
    public boolean test(LootContext context) {
        var level = context.getLevel();
        var calendar = Calendars.get(level);
        Month current = ICalendar.getMonthOfYear(
                calendar.getCalendarTicks(),
                calendar.getCalendarDaysInMonth());

        boolean passes = matches(current);
        return isReverse != passes;
    }

    private boolean matches(Month current) {
        if (!months.isEmpty()) {
            return months.contains(current);
        }
        if (start != null && end != null) {
            int s = start.ordinal();
            int e = end.ordinal();
            int c = current.ordinal();
            if (s <= e) {
                return c >= s && c <= e;
            } else {
                return c >= s || c <= e;
            }
        }
        return false;
    }

    public static class ConditionSerializer implements Serializer<MonthLootCondition> {
        @Override
        public void serialize(@NotNull JsonObject json, @NotNull MonthLootCondition instance, @NotNull JsonSerializationContext ctx) {
            json.addProperty("isReverse", instance.isReverse);
            if (!instance.months.isEmpty()) {
                JsonArray monthsArray = new JsonArray();
                for (Month m : instance.months) {
                    monthsArray.add(m.ordinal());
                }
                json.add("months", monthsArray);
            }
            if (instance.start != null) {
                json.addProperty("start", instance.start.ordinal());
            }
            if (instance.end != null) {
                json.addProperty("end", instance.end.ordinal());
            }
        }

        @Override
        public @NotNull MonthLootCondition deserialize(@NotNull JsonObject json, @NotNull JsonDeserializationContext ctx) {
            boolean isReverse = GsonHelper.getAsBoolean(json, "isReverse", false);
            List<Month> months = new ArrayList<>();
            if (json.has("months")) {
                JsonArray monthsArray = json.getAsJsonArray("months");
                for (JsonElement e : monthsArray) {
                    months.add(parseMonth(e));
                }
            }
            Month start = json.has("start") ? parseMonth(json.get("start")) : null;
            Month end = json.has("end") ? parseMonth(json.get("end")) : null;
            return new MonthLootCondition(isReverse, months, start, end);
        }

        private static Month parseMonth(JsonElement e) {
            if (e.isJsonPrimitive()) {
                if (e.getAsJsonPrimitive().isNumber()) {
                    return Month.valueOf(e.getAsInt());
                } else {
                    String name = e.getAsString().trim().toUpperCase(Locale.ROOT);
                    for (Month m : Month.values()) {
                        if (m.name().equals(name)) {
                            return m;
                        }
                    }
                }
            }
            return Month.JANUARY;
        }
    }
}
