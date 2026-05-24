package su.terrafirmagreg.core.common.food.nutrient;

import net.dries007.tfc.common.capabilities.food.Nutrient;

/**
 * Helper class for working with TFG's extended nutrient system.
 * <p>
 * The nutrient system is organized as:
 * - Original nutrients (0-4): GRAIN, FRUIT, VEGETABLES, PROTEIN, DAIRY.
 * - Extended nutrients (5+): new nutrients added.
 */
@SuppressWarnings("unused")
public final class TFGNutrients {

    public static final int ORIGINAL_COUNT = 5;
    public static final int POSITIVE_COUNT = ORIGINAL_COUNT;

    public static int getTotalCount() {
        return Nutrient.VALUES.length;
    }

    public static int getExtendedCount() {
        return getTotalCount() - ORIGINAL_COUNT;
    }

    public static int getNegativeCount() {
        int count = 0;
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (isNegative(nutrient))
                count++;
        }
        return count;
    }

    public static int getPositiveCount() {
        return POSITIVE_COUNT;
    }

    public static boolean isNegative(Nutrient nutrient) {
        return ((INutrientExtension) (Object) nutrient).tfg$isNegative();
    }

    public static boolean isTransient(Nutrient nutrient) {
        return ((INutrientExtension) (Object) nutrient).tfg$isTransient();
    }

    public static boolean isPositive(Nutrient nutrient) {
        return ((INutrientExtension) (Object) nutrient).tfg$isPositive();
    }

    public static boolean isOriginal(Nutrient nutrient) {
        return nutrient.ordinal() < ORIGINAL_COUNT;
    }

    public static boolean isExtended(Nutrient nutrient) {
        return nutrient.ordinal() >= ORIGINAL_COUNT;
    }

    public static Nutrient getByName(String name) {
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (nutrient.name().equalsIgnoreCase(name)) {
                return nutrient;
            }
        }
        return null;
    }

    public static int getTransientCount() {
        int count = 0;
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (isTransient(nutrient))
                count++;
        }
        return count;
    }

    private TFGNutrients() {
    }
}
