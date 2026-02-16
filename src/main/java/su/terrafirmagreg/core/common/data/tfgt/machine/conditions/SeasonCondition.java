package su.terrafirmagreg.core.common.data.tfgt.machine.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.calendar.Season;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeConditions;

/**
 * Recipe condition that matches specific seasons or a season range.
 * <p>
 * <p>Options:
 * <p>- Whitelist months.
 * <p>- start/end range. Supports wrap around (example: fall-spring).
 */
public class SeasonCondition extends RecipeCondition<SeasonCondition> {

    private static final Codec<Season> SEASON_CODEC = Codec.INT.xmap(i -> Season.values()[i], Season::ordinal);

    /**
     * Codec for serializing recipes.
     * <p>- optional seasons list
     * <p>- optional start/end bounds
     */
    public static final Codec<SeasonCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance)
            .and(Codec.list(SEASON_CODEC)
                    .optionalFieldOf("seasons")
                    .forGetter(c -> c.seasons.isEmpty() ? Optional.empty() : Optional.of(c.seasons)))
            .and(SEASON_CODEC.optionalFieldOf("start").forGetter(c -> Optional.ofNullable(c.start)))
            .and(SEASON_CODEC.optionalFieldOf("end").forGetter(c -> Optional.ofNullable(c.end)))
            .apply(instance, (isReverse, seasonsOpt, startOpt, endOpt) -> new SeasonCondition(isReverse, seasonsOpt.orElse(List.of()), startOpt.orElse(null), endOpt.orElse(null))));

    private final List<Season> seasons;
    private final @Nullable Season start;
    private final @Nullable Season end;

    // Default template.
    public SeasonCondition() {
        super(false);
        this.seasons = List.of();
        this.start = null;
        this.end = null;
    }

    /**
     * Constructor.
     *
     * @param isReverse invert result.
     * @param seasons seasons list.
     * @param start range start.
     * @param end range end.
     */
    public SeasonCondition(boolean isReverse, List<Season> seasons, @Nullable Season start, @Nullable Season end) {
        super(isReverse);
        this.seasons = seasons == null ? List.of() : List.copyOf(seasons);
        this.start = start;
        this.end = end;
    }

    @Override
    public RecipeConditionType<SeasonCondition> getType() {
        return TFGTRecipeConditions.SEASONS;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    // Tooltips.
    @Override
    public Component getTooltips() {
        if (!seasons.isEmpty()) {
            String names = seasons.stream()
                    .map(SeasonCondition::displayName)
                    .collect(Collectors.joining(", "));
            return Component.literal(names);
        }
        String startName = start == null ? "" : displayName(start);
        String endName = end == null ? "" : displayName(end);
        return Component.literal(startName + " - " + endName);
    }

    /**
     * Gets current season from the TFC calendar.
     */
    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine.self();
        var level = machine.getLevel();
        if (!(level instanceof ServerLevel serverLevel))
            return false;

        var calendar = net.dries007.tfc.util.calendar.Calendars.get(serverLevel);
        Month currentMonth = net.dries007.tfc.util.calendar.ICalendar.getMonthOfYear(
                calendar.getCalendarTicks(),
                calendar.getCalendarDaysInMonth());
        Season current = fromMonth(currentMonth);

        boolean passes = matches(current);
        return isReverse != passes;
    }

    /**
     * Matches the current season against list or range.
     */
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

    // Season map.
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

    /**
     * Returns a display name with a capitalized first letter.
     */
    private static String displayName(Season s) {
        String n = s.getSerializedName();
        return Character.toUpperCase(n.charAt(0)) + n.substring(1);
    }

    @Override
    public SeasonCondition createTemplate() {
        return new SeasonCondition();
    }

    // Template factories.

    public static SeasonCondition ofSeasons(boolean reverse, List<String> seasonNames) {
        List<Season> list = new ArrayList<>();
        for (String s : seasonNames) {
            list.add(parseSeason(s));
        }
        return new SeasonCondition(reverse, list, null, null);
    }

    public static SeasonCondition ofRange(boolean reverse, String startName, String endName) {
        Season s = parseSeason(startName);
        Season e = parseSeason(endName);
        return new SeasonCondition(reverse, List.of(), s, e);
    }

    private static Season parseSeason(String name) {
        String key = name.trim().toUpperCase(Locale.ROOT);
        if (key.equals("AUTUMN"))
            key = "FALL";
        for (Season s : Season.values()) {
            if (s.name().equals(key)) {
                return s;
            }
        }
        return Season.SPRING;
    }
}
