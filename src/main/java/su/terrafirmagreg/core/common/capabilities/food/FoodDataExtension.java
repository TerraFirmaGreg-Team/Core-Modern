package su.terrafirmagreg.core.common.capabilities.food;

import java.util.Map;
import java.util.WeakHashMap;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.minecraft.nbt.CompoundTag;

/**
 * Extension for FoodData to support negative nutrients.
 */
public final class FoodDataExtension {

    private static final Map<FoodData, float[]> NEGATIVE_NUTRIENTS = new WeakHashMap<>();

    /**
     * Get the negative nutrient values for a FoodData instance.
     * @return array of negative nutrient values.
     */
    public static float[] getNegativeNutrients(FoodData data) {
        return NEGATIVE_NUTRIENTS.getOrDefault(data, new float[TFGNutrients.getNegativeCount()]);
    }

    /**
     * Set the negative nutrient values for a FoodData instance.
     */
    public static void setNegativeNutrients(FoodData data, float[] negativeNutrients) {
        if (negativeNutrients != null && negativeNutrients.length > 0) {
            NEGATIVE_NUTRIENTS.put(data, negativeNutrients);
        }
    }

    /**
     * Get a specific negative nutrient value.
     * @param nutrient must be a negative nutrient.
     * @return the nutrient value.
     */
    public static float getNegativeNutrient(FoodData data, Nutrient nutrient) {
        if (!TFGNutrients.isNegative(nutrient)) {
            return 0;
        }
        float[] negatives = getNegativeNutrients(data);
        int index = nutrient.ordinal() - TFGNutrients.POSITIVE_COUNT;
        return index >= 0 && index < negatives.length ? negatives[index] : 0;
    }

    /**
     * Get nutrient value for any nutrient.
     */
    public static float getNutrient(FoodData data, Nutrient nutrient) {
        if (TFGNutrients.isPositive(nutrient)) {
            return data.nutrient(nutrient);
        }
        return getNegativeNutrient(data, nutrient);
    }

    /**
     * Get all nutrient values.
     * @return array of all nutrient values in order.
     */
    public static float[] getAllNutrients(FoodData data) {
        float[] positive = data.nutrients();
        float[] negative = getNegativeNutrients(data);
        float[] all = new float[Nutrient.VALUES.length];

        System.arraycopy(positive, 0, all, 0, positive.length);
        System.arraycopy(negative, 0, all, TFGNutrients.POSITIVE_COUNT, negative.length);

        return all;
    }

    /**
     * Write negative nutrients to NBT.
     */
    public static void writeToNbt(FoodData data, CompoundTag nbt) {
        float[] negatives = getNegativeNutrients(data);
        Nutrient[] values = Nutrient.VALUES;

        for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
            Nutrient nutrient = values[i];
            float value = negatives[i - TFGNutrients.POSITIVE_COUNT];
            if (value != 0) {
                nbt.putFloat(nutrient.getSerializedName(), value);
            }
        }
    }

    /**
     * Read negative nutrients from NBT and associate with FoodData.
     */
    public static void readFromNbt(FoodData data, CompoundTag nbt) {
        Nutrient[] values = Nutrient.VALUES;
        int negativeCount = values.length - TFGNutrients.POSITIVE_COUNT;

        if (negativeCount <= 0)
            return;

        float[] negatives = new float[negativeCount];
        boolean hasAny = false;

        for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
            Nutrient nutrient = values[i];
            if (nbt.contains(nutrient.getSerializedName())) {
                negatives[i - TFGNutrients.POSITIVE_COUNT] = nbt.getFloat(nutrient.getSerializedName());
                hasAny = true;
            }
        }

        if (hasAny) {
            setNegativeNutrients(data, negatives);
        }
    }

    /**
     * Copy negative nutrients from one FoodData to another.
     */
    public static void copyNegativeNutrients(FoodData from, FoodData to) {
        float[] negatives = NEGATIVE_NUTRIENTS.get(from);
        if (negatives != null) {
            setNegativeNutrients(to, negatives.clone());
        }
    }

    private FoodDataExtension() {
    }
}
