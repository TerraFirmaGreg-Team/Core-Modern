package su.terrafirmagreg.core.compat.kjs;

import static com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema.*;

import java.util.Arrays;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;

import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import lombok.experimental.Accessors;

import su.terrafirmagreg.core.common.data.tfgt.machine.conditions.AverageRainfallCondition;
import su.terrafirmagreg.core.common.data.tfgt.machine.conditions.AverageTemperatureCondition;
import su.terrafirmagreg.core.common.data.tfgt.machine.conditions.MonthCondition;
import su.terrafirmagreg.core.common.data.tfgt.machine.conditions.OxygenatedCondition;
import su.terrafirmagreg.core.common.data.tfgt.machine.conditions.SeasonCondition;

public interface TFGRecipeSchema {

    @SuppressWarnings({ "unused", "UnusedReturnValue" })
    @Accessors(chain = true, fluent = true)
    class TFGRecipeJS extends GTRecipeSchema.GTRecipeJS {

        public GTRecipeSchema.GTRecipeJS isOxygenated(boolean isOxygenated) {
            return this.addCondition(new OxygenatedCondition(false, isOxygenated));
        }

        public GTRecipeSchema.GTRecipeJS isOxygenated(boolean isOxygenated, boolean reverse) {
            return this.addCondition(new OxygenatedCondition(reverse, isOxygenated));
        }

        public GTRecipeSchema.GTRecipeJS months(String... monthNames) {
            return this.addCondition(MonthCondition.ofMonths(false, Arrays.asList(monthNames)));
        }

        public GTRecipeSchema.GTRecipeJS months(boolean reverse, String... monthNames) {
            return this.addCondition(MonthCondition.ofMonths(reverse, Arrays.asList(monthNames)));
        }

        public GTRecipeSchema.GTRecipeJS monthsRange(String start, String end) {
            return this.addCondition(MonthCondition.ofRange(false, start, end));
        }

        public GTRecipeSchema.GTRecipeJS monthsRange(String start, String end, boolean reverse) {
            return this.addCondition(MonthCondition.ofRange(reverse, start, end));
        }

        public GTRecipeSchema.GTRecipeJS seasons(String... seasonNames) {
            return this.addCondition(SeasonCondition.ofSeasons(false, Arrays.asList(seasonNames)));
        }

        public GTRecipeSchema.GTRecipeJS seasons(boolean reverse, String... seasonNames) {
            return this.addCondition(SeasonCondition.ofSeasons(reverse, Arrays.asList(seasonNames)));
        }

        public GTRecipeSchema.GTRecipeJS seasonsRange(String start, String end) {
            return this.addCondition(SeasonCondition.ofRange(false, start, end));
        }

        public GTRecipeSchema.GTRecipeJS seasonsRange(String start, String end, boolean reverse) {
            return this.addCondition(SeasonCondition.ofRange(reverse, start, end));
        }

        public GTRecipeSchema.GTRecipeJS climateAvgTemperatureRange(float start, float end) {
            return this.addCondition(AverageTemperatureCondition.ofRange(false, start, end));
        }

        public GTRecipeSchema.GTRecipeJS climateAvgTemperatureRange(float start, float end, boolean reverse) {
            return this.addCondition(AverageTemperatureCondition.ofRange(reverse, start, end));
        }

        public GTRecipeSchema.GTRecipeJS climateAvgTemperatureGreaterThan(float value) {
            return this.addCondition(AverageTemperatureCondition.greaterThan(value));
        }

        public GTRecipeSchema.GTRecipeJS climateAvgTemperatureLessThan(float value) {
            return this.addCondition(AverageTemperatureCondition.lessThan(value));
        }

        public GTRecipeSchema.GTRecipeJS climateAvgRainfallRange(float start, float end) {
            return this.addCondition(AverageRainfallCondition.ofRange(false, start, end));
        }

        public GTRecipeSchema.GTRecipeJS climateAvgRainfallRange(float start, float end, boolean reverse) {
            return this.addCondition(AverageRainfallCondition.ofRange(reverse, start, end));
        }

        public GTRecipeSchema.GTRecipeJS climateAvgRainfallGreaterThan(float value) {
            return this.addCondition(AverageRainfallCondition.greaterThan(value));
        }

        public GTRecipeSchema.GTRecipeJS climateAvgRainfallLessThan(float value) {
            return this.addCondition(AverageRainfallCondition.lessThan(value));
        }
    }

    RecipeSchema SCHEMA = new RecipeSchema(
            TFGRecipeJS.class,
            TFGRecipeJS::new,
            DURATION, DATA, CONDITIONS,
            ALL_INPUTS, ALL_TICK_INPUTS, ALL_OUTPUTS, ALL_TICK_OUTPUTS,
            INPUT_CHANCE_LOGICS, OUTPUT_CHANCE_LOGICS, TICK_INPUT_CHANCE_LOGICS, TICK_OUTPUT_CHANCE_LOGICS, CATEGORY)
            .constructor((recipe, schemaType, keys, from) -> recipe.id(from.getValue(recipe, ID)), ID)
            .constructor(DURATION, CONDITIONS, ALL_INPUTS, ALL_OUTPUTS, ALL_TICK_INPUTS, ALL_TICK_OUTPUTS);
}
