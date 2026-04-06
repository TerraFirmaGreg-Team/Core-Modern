package su.terrafirmagreg.core.common.capabilities.food;

/**
 * Extension for TFC's Nutrient enum to support negative nutrients.
 */
public interface INutrientExtension {

    boolean tfg$isNegative();

    default boolean tfg$isPositive() {
        return !tfg$isNegative();
    }
}
