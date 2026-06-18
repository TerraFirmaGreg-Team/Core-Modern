package su.terrafirmagreg.core.common.food.nutrient;

/**
 * Interface for builders that support extended nutrients to expose methods to KubeJS.
 */
public interface IExtendedNutrientBuilder<T> {

    // --------- Negative Nutrients -----------
    T toxins(float value);

    T microplastics(float value);

    T parasites(float value);

    // --------- Transient Nutrients -----------
    T deadly(float value);

    T cooling(float value);

    T warming(float value);

    T freezing(float value);

    T blazing(float value);

    T radiating(float value);

    T nauseating(float value);

    T parching(float value);

    T quenching(float value);

    T bolstering(float value);

    T hearty(float value);

    T rejuvenating(float value);

    T sugary(float value);

    T spicy(float value);

    T fulfilling(float value);
}
