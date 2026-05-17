package su.terrafirmagreg.core.common.food.nutrient;

/**
 * Extension for TFC's Nutrient enum to support negative and transient nutrients.
 */
public interface INutrientExtension {

    boolean tfg$isNegative();

    boolean tfg$isTransient();

    default boolean tfg$isPositive() {
        return !tfg$isNegative() && !tfg$isTransient();
    }
}
