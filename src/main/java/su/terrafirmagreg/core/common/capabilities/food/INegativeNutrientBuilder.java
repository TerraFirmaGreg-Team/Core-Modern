package su.terrafirmagreg.core.common.capabilities.food;

/**
 * Interface for builders that support negative nutrients to expose methods to KubeJS.
 */
public interface INegativeNutrientBuilder<T> {
    
    /**
     * Sets the toxins value for this food.
     * @param value the toxins amount.
     * @return this builder.
     */
    T toxins(float value);
    
    /**
     * Sets the microplastics value for this food.
     * @param value the microplastics amount.
     * @return this builder.
     */
    T microplastics(float value);
}
