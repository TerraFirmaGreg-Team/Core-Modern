package su.terrafirmagreg.core.common.food.nutrient;

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

    T microplastics(float value);

    T parasites(float value);
}
