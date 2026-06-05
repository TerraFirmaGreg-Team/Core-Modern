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
import net.dries007.tfc.util.calendar.Season;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import su.terrafirmagreg.core.common.data.TFGLootConditions;

/**
 * Loot condition that checks if the current season matches any of the specified seasons.
 */
public class SeasonLootCondition implements LootItemCondition {

    private final boolean isReverse;
    private final List<Season> seasons;
    private final @Nullable Season start;
    private final @Nullable Season end;

    public SeasonLootCondition(boolean isReverse, List<Season> seasons, @Nullable Season start, @Nullable Season end) {
        this.isReverse = isReverse;
        this.seasons = seasons == null ? List.of() : List.copyOf(seasons);
        this.start = start;
        this.end = end;
    }

    @Override
    public @NotNull LootItemConditionType getType() {
        return TFGLootConditions.SEASON_LOOT_CONDITION.get();
    }

    @Override
    public boolean test(LootContext context) {
        var level = context.getLevel();
        var calendar = Calendars.get(level);
        Month currentMonth = ICalendar.getMonthOfYear(
                calendar.getCalendarTicks(),
                calendar.getCalendarDaysInMonth());
        Season current = fromMonth(currentMonth);

        boolean passes = matches(current);
        return isReverse != passes;
    }

    private boolean matches(Season current) {
        if (!seasons.isEmpty()) {
            return seasons.contains(current);
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

    private static Season fromMonth(Month m) {
        switch (m) {
            case DECEMBER, JANUARY, FEBRUARY -> {
                return Season.WINTER;
            }
            case MARCH, APRIL, MAY -> {
                return Season.SPRING;
            }
            case JUNE, JULY, AUGUST -> {
                return Season.SUMMER;
            }
            case SEPTEMBER, OCTOBER, NOVEMBER -> {
                return Season.FALL;
            }
        }
        return Season.SPRING;
    }

    public static class ConditionSerializer implements Serializer<SeasonLootCondition> {
        @Override
        public void serialize(@NotNull JsonObject json, @NotNull SeasonLootCondition instance, @NotNull JsonSerializationContext ctx) {
            json.addProperty("isReverse", instance.isReverse);
            if (!instance.seasons.isEmpty()) {
                JsonArray seasonsArray = new JsonArray();
                for (Season s : instance.seasons) {
                    seasonsArray.add(s.ordinal());
                }
                json.add("seasons", seasonsArray);
            }
            if (instance.start != null) {
                json.addProperty("start", instance.start.ordinal());
            }
            if (instance.end != null) {
                json.addProperty("end", instance.end.ordinal());
            }
        }

        @Override
        public @NotNull SeasonLootCondition deserialize(@NotNull JsonObject json, @NotNull JsonDeserializationContext ctx) {
            boolean isReverse = GsonHelper.getAsBoolean(json, "isReverse", false);
            List<Season> seasons = new ArrayList<>();
            if (json.has("seasons")) {
                JsonArray seasonsArray = json.getAsJsonArray("seasons");
                for (JsonElement e : seasonsArray) {
                    seasons.add(parseSeason(e));
                }
            }
            Season start = json.has("start") ? parseSeason(json.get("start")) : null;
            Season end = json.has("end") ? parseSeason(json.get("end")) : null;
            return new SeasonLootCondition(isReverse, seasons, start, end);
        }

        private static Season parseSeason(JsonElement e) {
            if (e.isJsonPrimitive()) {
                if (e.getAsJsonPrimitive().isNumber()) {
                    int ordinal = e.getAsInt();
                    if (ordinal >= 0 && ordinal < Season.values().length) {
                        return Season.values()[ordinal];
                    }
                } else {
                    String name = e.getAsString().trim().toUpperCase(Locale.ROOT);
                    if (name.equals("AUTUMN")) {
                        name = "FALL";
                    }
                    for (Season s : Season.values()) {
                        if (s.name().equals(name)) {
                            return s;
                        }
                    }
                }
            }
            return Season.SPRING;
        }
    }
}
