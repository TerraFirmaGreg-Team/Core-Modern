package su.terrafirmagreg.core.compat.kjs;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema.GTRecipeJS;

import su.terrafirmagreg.core.common.data.tfgt.machine.conditions.OxygenatedCondition;

public final class TFGRecipeSchemaBindings {
    public static GTRecipeJS isOxygenated(GTRecipeJS recipe, boolean isOxygenated) {
        return recipe.addCondition(new OxygenatedCondition(false, isOxygenated));
    }

    public static GTRecipeJS isOxygenated(GTRecipeJS recipe, boolean isOxygenated, boolean reverse) {
        return recipe.addCondition(new OxygenatedCondition(reverse, isOxygenated));
    }
}
