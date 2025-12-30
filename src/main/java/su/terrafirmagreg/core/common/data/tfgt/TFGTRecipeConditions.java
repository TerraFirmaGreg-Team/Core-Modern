package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;

import su.terrafirmagreg.core.common.data.tfgt.machine.conditions.*;

public class TFGTRecipeConditions {

    private TFGTRecipeConditions() {
    }

    public static RecipeConditionType<OxygenatedCondition> OXYGENATED;
    public static RecipeConditionType<MonthCondition> MONTHS;
    public static RecipeConditionType<SeasonCondition> SEASONS;
    public static RecipeConditionType<AverageTemperatureCondition> CLIMATE_AVG_TEMPERATURE;
    public static RecipeConditionType<AverageRainfallCondition> CLIMATE_AVG_RAINFALL;
    public static RecipeConditionType<GravityCondition> GRAVITY;

    public static void init() {
        OXYGENATED = register("oxygenated", OxygenatedCondition::new, OxygenatedCondition.CODEC);
        MONTHS = register("months", MonthCondition::new, MonthCondition.CODEC);
        SEASONS = register("seasons", SeasonCondition::new, SeasonCondition.CODEC);
        CLIMATE_AVG_TEMPERATURE = register("climate_avg_temperature", AverageTemperatureCondition::new, AverageTemperatureCondition.CODEC);
        CLIMATE_AVG_RAINFALL = register("climate_avg_rainfall", AverageRainfallCondition::new, AverageRainfallCondition.CODEC);
        GRAVITY = register("gravity", GravityCondition::new, GravityCondition.CODEC);
    }

    private static <T extends RecipeCondition> RecipeConditionType<T> register(
            String name,
            RecipeConditionType.ConditionFactory<T> factory,
            Codec<T> codec) {
        return GTRegistries.RECIPE_CONDITIONS.register(name, new RecipeConditionType<>(factory, codec));
    }
}
