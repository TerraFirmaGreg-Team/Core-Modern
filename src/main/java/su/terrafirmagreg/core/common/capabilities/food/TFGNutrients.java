package su.terrafirmagreg.core.common.capabilities.food;

import net.dries007.tfc.common.capabilities.food.Nutrient;

/**
 * Helper class for working with TFG's extended nutrient system.
 * Provides constants and utility methods for negative nutrients.
 */
public final class TFGNutrients {

    // GRAIN, FRUIT, VEGETABLES, PROTEIN, DAIRY
    public static final int POSITIVE_COUNT = 5;
    public static int getTotalCount() {
        return Nutrient.values().length;
    }
    public static int getNegativeCount() {
        return getTotalCount() - POSITIVE_COUNT;
    }

    public static boolean isNegative(Nutrient nutrient) {
        return ((INutrientExtension) (Object) nutrient).tfg$isNegative();
    }

    public static boolean isPositive(Nutrient nutrient) {
        return !isNegative(nutrient);
    }

    private TFGNutrients() {
    }
}
