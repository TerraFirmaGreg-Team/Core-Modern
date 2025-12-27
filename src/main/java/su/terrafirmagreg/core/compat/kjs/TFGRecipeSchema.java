package su.terrafirmagreg.core.compat.kjs;

import static com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema.*;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;

import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import lombok.experimental.Accessors;

import su.terrafirmagreg.core.common.data.tfgt.machine.conditions.OxygenatedCondition;

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
