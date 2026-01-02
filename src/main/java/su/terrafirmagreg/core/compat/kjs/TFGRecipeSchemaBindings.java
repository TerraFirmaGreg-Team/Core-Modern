package su.terrafirmagreg.core.compat.kjs;

import java.util.Arrays;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema.GTRecipeJS;

import su.terrafirmagreg.core.common.data.tfgt.machine.conditions.*;

/**
 * KubeJS recipe schema bindings for TFG.
 */
@SuppressWarnings("unused")
public final class TFGRecipeSchemaBindings {

    // Oxygenated Conditions.
    public static GTRecipeJS isOxygenated(GTRecipeJS recipe, boolean isOxygenated) {
        return recipe.addCondition(new OxygenatedCondition(false, isOxygenated));
    }

    public static GTRecipeJS isOxygenated(GTRecipeJS recipe, boolean isOxygenated, boolean reverse) {
        return recipe.addCondition(new OxygenatedCondition(reverse, isOxygenated));
    }

    // Month Conditions.
    public static GTRecipeJS months(GTRecipeJS recipe, String... monthNames) {
        return recipe.addCondition(MonthCondition.ofMonths(false, Arrays.asList(monthNames)));
    }

    public static GTRecipeJS months(GTRecipeJS recipe, boolean reverse, String... monthNames) {
        return recipe.addCondition(MonthCondition.ofMonths(reverse, Arrays.asList(monthNames)));
    }

    public static GTRecipeJS monthsRange(GTRecipeJS recipe, String start, String end) {
        return recipe.addCondition(MonthCondition.ofRange(false, start, end));
    }

    public static GTRecipeJS monthsRange(GTRecipeJS recipe, String start, String end, boolean reverse) {
        return recipe.addCondition(MonthCondition.ofRange(reverse, start, end));
    }

    // Season Conditions.
    public static GTRecipeJS seasons(GTRecipeJS recipe, String... seasonNames) {
        return recipe.addCondition(SeasonCondition.ofSeasons(false, Arrays.asList(seasonNames)));
    }

    public static GTRecipeJS seasons(GTRecipeJS recipe, boolean reverse, String... seasonNames) {
        return recipe.addCondition(SeasonCondition.ofSeasons(reverse, Arrays.asList(seasonNames)));
    }

    public static GTRecipeJS seasonsRange(GTRecipeJS recipe, String start, String end) {
        return recipe.addCondition(SeasonCondition.ofRange(false, start, end));
    }

    public static GTRecipeJS seasonsRange(GTRecipeJS recipe, String start, String end, boolean reverse) {
        return recipe.addCondition(SeasonCondition.ofRange(reverse, start, end));
    }

    // Temperature Conditions.
    public static GTRecipeJS climateAvgTemperatureRange(GTRecipeJS recipe, float start, float end) {
        return recipe.addCondition(AverageTemperatureCondition.ofRange(false, start, end));
    }

    public static GTRecipeJS climateAvgTemperatureRange(GTRecipeJS recipe, float start, float end, boolean reverse) {
        return recipe.addCondition(AverageTemperatureCondition.ofRange(reverse, start, end));
    }

    public static GTRecipeJS climateAvgTemperatureGreaterThan(GTRecipeJS recipe, float value) {
        return recipe.addCondition(AverageTemperatureCondition.greaterThan(value));
    }

    public static GTRecipeJS climateAvgTemperatureLessThan(GTRecipeJS recipe, float value) {
        return recipe.addCondition(AverageTemperatureCondition.lessThan(value));
    }

    // Rainfall Conditions.
    public static GTRecipeJS climateAvgRainfallRange(GTRecipeJS recipe, float start, float end) {
        return recipe.addCondition(AverageRainfallCondition.ofRange(false, start, end));
    }

    public static GTRecipeJS climateAvgRainfallRange(GTRecipeJS recipe, float start, float end, boolean reverse) {
        return recipe.addCondition(AverageRainfallCondition.ofRange(reverse, start, end));
    }

    public static GTRecipeJS climateAvgRainfallGreaterThan(GTRecipeJS recipe, float value) {
        return recipe.addCondition(AverageRainfallCondition.greaterThan(value));
    }

    public static GTRecipeJS climateAvgRainfallLessThan(GTRecipeJS recipe, float value) {
        return recipe.addCondition(AverageRainfallCondition.lessThan(value));
    }

    // Gravity Conditions.
    public static GTRecipeJS gravityGreaterThan(GTRecipeJS recipe, float value) {
        return recipe.addCondition(GravityCondition.greaterThan(value));
    }

    public static GTRecipeJS gravityLessThan(GTRecipeJS recipe, float value) {
        return recipe.addCondition(GravityCondition.lessThan(value));
    }

    public static GTRecipeJS gravityRange(GTRecipeJS recipe, float start, float end) {
        return recipe.addCondition(GravityCondition.ofRange(false, start, end));
    }

    public static GTRecipeJS gravityRange(GTRecipeJS recipe, float start, float end, boolean reverse) {
        return recipe.addCondition(GravityCondition.ofRange(reverse, start, end));
    }
}
