package su.terrafirmagreg.core.common.capabilities.food;

import java.util.Map;
import java.util.WeakHashMap;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.NutritionData;
import net.minecraft.nbt.CompoundTag;

/**
 * Extension system for NutritionData to track negative nutrients separately.
 */
public final class NutritionDataExtension {

    private static final Map<NutritionData, float[]> NEGATIVE_NUTRIENTS = new WeakHashMap<>();

    /**
     * Get the negative nutrients array for a NutritionData instance.
     */
    public static float[] getOrCreateNegativeNutrients(NutritionData data) {
        return NEGATIVE_NUTRIENTS.computeIfAbsent(data, k -> new float[TFGNutrients.getNegativeCount()]);
    }

    /**
     * Get the negative nutrients array, or null.
     */
    public static float[] getNegativeNutrients(NutritionData data) {
        return NEGATIVE_NUTRIENTS.get(data);
    }

    /**
     * Get a specific negative nutrient value.
     * @param nutrient must be a negative nutrient.
     * @return the nutrient value.
     */
    public static float getNegativeNutrient(NutritionData data, Nutrient nutrient) {
        if (!TFGNutrients.isNegative(nutrient)) {
            return 0;
        }
        float[] negatives = NEGATIVE_NUTRIENTS.get(data);
        if (negatives == null) {
            return 0;
        }
        int index = nutrient.ordinal() - TFGNutrients.POSITIVE_COUNT;
        return index >= 0 && index < negatives.length ? negatives[index] : 0;
    }

    /**
     * Set a specific negative nutrient value.
     */
    public static void setNegativeNutrient(NutritionData data, Nutrient nutrient, float value) {
        if (!TFGNutrients.isNegative(nutrient)) {
            return;
        }
        float[] negatives = getOrCreateNegativeNutrients(data);
        int index = nutrient.ordinal() - TFGNutrients.POSITIVE_COUNT;
        if (index >= 0 && index < negatives.length) {
            negatives[index] = Math.min(1f, Math.max(0f, value));
        }
    }

    /**
     * Add negative nutrient values from food data.
     */
    public static void addNegativeNutrients(NutritionData data, FoodData foodData, float weight) {
        float[] negatives = getOrCreateNegativeNutrients(data);

        for (Nutrient nutrient : Nutrient.values()) {
            if (TFGNutrients.isNegative(nutrient)) {
                int index = nutrient.ordinal() - TFGNutrients.POSITIVE_COUNT;
                if (index >= 0 && index < negatives.length) {
                    float foodNutrient = FoodDataExtension.getNegativeNutrient(foodData, nutrient);
                    if (foodNutrient > 0) {
                        // Add weighted nutrient value, cap at 1.0
                        negatives[index] = Math.min(1f, negatives[index] + foodNutrient * weight);
                    }
                }
            }
        }
    }

    /**
     * Decay negative nutrients over time.
     * @param decayAmount amount to decay all negative nutrients by.
     */
    public static void decayNegativeNutrients(NutritionData data, float decayAmount) {
        float[] negatives = NEGATIVE_NUTRIENTS.get(data);
        if (negatives == null) {
            return;
        }
        for (int i = 0; i < negatives.length; i++) {
            negatives[i] = Math.max(0f, negatives[i] - decayAmount);
        }
    }

    /**
     * Reset all negative nutrients to zero.
     */
    public static void reset(NutritionData data) {
        float[] negatives = NEGATIVE_NUTRIENTS.get(data);
        if (negatives != null) {
            java.util.Arrays.fill(negatives, 0f);
        }
    }

    /**
     * Write negative nutrients to NBT.
     */
    public static void writeToNbt(NutritionData data, CompoundTag nbt) {
        float[] negatives = NEGATIVE_NUTRIENTS.get(data);
        if (negatives == null) {
            return;
        }

        CompoundTag negativeNbt = new CompoundTag();
        Nutrient[] values = Nutrient.values();
        boolean hasAny = false;

        for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
            Nutrient nutrient = values[i];
            int index = i - TFGNutrients.POSITIVE_COUNT;
            if (index < negatives.length && negatives[index] > 0) {
                negativeNbt.putFloat(nutrient.getSerializedName(), negatives[index]);
                hasAny = true;
            }
        }

        if (hasAny) {
            nbt.put("tfg_negative_nutrients", negativeNbt);
        }
    }

    /**
     * Read negative nutrients from NBT.
     */
    public static void readFromNbt(NutritionData data, CompoundTag nbt) {
        if (!nbt.contains("tfg_negative_nutrients")) {
            return;
        }

        CompoundTag negativeNbt = nbt.getCompound("tfg_negative_nutrients");
        float[] negatives = getOrCreateNegativeNutrients(data);

        Nutrient[] values = Nutrient.values();
        for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
            Nutrient nutrient = values[i];
            int index = i - TFGNutrients.POSITIVE_COUNT;
            if (index < negatives.length && negativeNbt.contains(nutrient.getSerializedName())) {
                negatives[index] = negativeNbt.getFloat(nutrient.getSerializedName());
            }
        }
    }

    /**
     * Update negative nutrients from client packet.
     */
    public static void onClientUpdate(NutritionData data, float[] negativeNutrients) {
        if (negativeNutrients == null || negativeNutrients.length == 0) {
            return;
        }
        float[] negatives = getOrCreateNegativeNutrients(data);
        System.arraycopy(negativeNutrients, 0, negatives, 0, Math.min(negatives.length, negativeNutrients.length));
    }

    /**
     * Copy negative nutrients from one NutritionData to another.
     */
    public static void copyFrom(NutritionData from, NutritionData to) {
        float[] fromNegatives = NEGATIVE_NUTRIENTS.get(from);
        if (fromNegatives != null) {
            float[] toNegatives = getOrCreateNegativeNutrients(to);
            System.arraycopy(fromNegatives, 0, toNegatives, 0, Math.min(fromNegatives.length, toNegatives.length));
        }
    }

    private NutritionDataExtension() {
    }
}
