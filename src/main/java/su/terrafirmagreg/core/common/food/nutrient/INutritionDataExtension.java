package su.terrafirmagreg.core.common.food.nutrient;

import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.NutritionData;

/**
 * Extension interface for NutritionData to access negative nutrients.
 */
public interface INutritionDataExtension {

    /**
     * Get a negative nutrient value for the player.
     * @param nutrient must be a negative nutrient.
     * @return the nutrient value.
     */
    float tfg$getNegativeNutrient(Nutrient nutrient);

    /**
     * Get all negative nutrient values.
     * @return array of negative nutrient values.
     */
    float[] tfg$getNegativeNutrients();

    /**
     * Helper to cast NutritionData to this interface.
     */
    static INutritionDataExtension of(NutritionData data) {
        return (INutritionDataExtension) data;
    }
}
