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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeConditions;

/**
 * Recipe condition that matches specific months or a month range
 * <p>
 * <p>Options:
 * <p>- Whitelist months
 * <p>- Start/end range. Supports wrap around (example: september-march)
 */
public class MonthCondition extends RecipeCondition {

    private static final Codec<Month> MONTH_CODEC = Codec.INT.xmap(Month::valueOf, Month::ordinal);

    /**
     * Codec for serializing recipes.
     * <p>- optional months list
     * <p>- optional start/end bounds
     */
    public static final Codec<MonthCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance)
            .and(Codec.list(MONTH_CODEC)
                    .optionalFieldOf("months")
                    .forGetter(c -> c.months.isEmpty() ? Optional.empty() : Optional.of(c.months)))
            .and(MONTH_CODEC.optionalFieldOf("start").forGetter(c -> Optional.ofNullable(c.start)))
            .and(MONTH_CODEC.optionalFieldOf("end").forGetter(c -> Optional.ofNullable(c.end)))
            .apply(instance, (isReverse, monthsOpt, startOpt, endOpt) -> new MonthCondition(isReverse, monthsOpt.orElse(List.of()), startOpt.orElse(null), endOpt.orElse(null))));

    private final List<Month> months;
    private final @Nullable Month start;
    private final @Nullable Month end;

    // Default template.
    public MonthCondition() {
        super(false);
        this.months = List.of();
        this.start = null;
        this.end = null;
    }

    /**
     * Constructor.
     *
     * @param isReverse invert result.
     * @param months month list.
     * @param start range start.
     * @param end range end.
     */
    public MonthCondition(boolean isReverse, List<Month> months, @Nullable Month start, @Nullable Month end) {
        super(isReverse);
        this.months = months == null ? List.of() : List.copyOf(months);
        this.start = start;
        this.end = end;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return TFGTRecipeConditions.MONTHS;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    // Tooltips.
    @Override
    public Component getTooltips() {
        if (!months.isEmpty()) {
            String names = months.stream()
                    .map(m -> Component.translatable(m.getTranslationKey(Month.Style.LONG_MONTH)).getString())
                    .collect(Collectors.joining(", "));
            return Component.literal(names);
        }
        String startName = start == null ? "" : Component.translatable(start.getTranslationKey(Month.Style.LONG_MONTH)).getString();
        String endName = end == null ? "" : Component.translatable(end.getTranslationKey(Month.Style.LONG_MONTH)).getString();
        return Component.literal(startName + " - " + endName);
    }

    /**
     * Gets current month from the TFC calendar.
     */
    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine.self();
        var level = machine.getLevel();
        if (!(level instanceof ServerLevel serverLevel))
            return false;

        var calendar = net.dries007.tfc.util.calendar.Calendars.get(serverLevel);
        Month current = net.dries007.tfc.util.calendar.ICalendar.getMonthOfYear(
                calendar.getCalendarTicks(),
                calendar.getCalendarDaysInMonth());

        boolean passes = matches(current);
        return isReverse != passes;
    }

    /**
     * Matches the current month against list or range.
     */
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

    @Override
    public RecipeCondition createTemplate() {
        return new MonthCondition();
    }

    // Template factories.

    public static MonthCondition ofMonths(boolean reverse, List<String> monthNames) {
        List<Month> list = new ArrayList<>();
        for (String s : monthNames) {
            list.add(parseMonth(s));
        }
        return new MonthCondition(reverse, list, null, null);
    }

    public static MonthCondition ofRange(boolean reverse, String startName, String endName) {
        Month s = parseMonth(startName);
        Month e = parseMonth(endName);
        return new MonthCondition(reverse, List.of(), s, e);
    }

    private static Month parseMonth(String name) {
        String key = name.trim().toUpperCase(Locale.ROOT);
        for (Month m : Month.values()) {
            if (m.name().equals(key)) {
                return m;
            }
        }
        return Month.JANUARY;
    }
}
