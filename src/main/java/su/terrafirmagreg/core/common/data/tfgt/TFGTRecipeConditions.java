package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;

import su.terrafirmagreg.core.common.data.tfgt.machine.conditions.OxygenatedCondition;

/**
 * Registers custom GTCEu recipe conditions for TFG.
 */
public class TFGTRecipeConditions {

    private TFGTRecipeConditions() {
    }

    public static RecipeConditionType<OxygenatedCondition> OXYGENATED;

    public static void init() {
        OXYGENATED = register("oxygenated", OxygenatedCondition::new, OxygenatedCondition.CODEC);
    }

    private static <T extends RecipeCondition> RecipeConditionType<T> register(
            String name,
            RecipeConditionType.ConditionFactory<T> factory,
            Codec<T> codec) {
        return GTRegistries.RECIPE_CONDITIONS.register(name, new RecipeConditionType<>(factory, codec));
    }
}
