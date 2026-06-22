package su.terrafirmagreg.core.common.food.nutrient;

import java.util.IdentityHashMap;
import java.util.Map;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.NutritionData;
import net.minecraft.nbt.CompoundTag;

/**
 * Extension system for NutritionData to track extended nutrients.
 */
public final class NutritionDataExtension {

    private static final Map<NutritionData, float[]> EXTENDED_NUTRIENTS = new IdentityHashMap<>();

    /**
     * Get the extended nutrients array for a NutritionData instance.
     */
    public static float[] getOrCreateExtendedNutrients(NutritionData data) {
        return EXTENDED_NUTRIENTS.computeIfAbsent(data, k -> new float[TFGNutrients.getExtendedCount()]);
    }

    /**
     * Get the extended nutrients array, or null if not set.
     */
    public static float[] getExtendedNutrients(NutritionData data) {
        return EXTENDED_NUTRIENTS.get(data);
    }

    /**
     * Get a specific extended nutrient value.
     */
    public static float getExtendedNutrient(NutritionData data, Nutrient nutrient) {
        if (TFGNutrients.isOriginal(nutrient)) {
            return 0;
        }
        float[] extended = EXTENDED_NUTRIENTS.get(data);
        if (extended == null) {
            return 0;
        }
        int index = nutrient.ordinal() - TFGNutrients.ORIGINAL_COUNT;
        return index >= 0 && index < extended.length ? extended[index] : 0;
    }

    /**
     * Set a specific extended nutrient value.
     */
    public static void setExtendedNutrient(NutritionData data, Nutrient nutrient, float value) {
        if (TFGNutrients.isOriginal(nutrient)) {
            return;
        }
        float[] extended = getOrCreateExtendedNutrients(data);
        int index = nutrient.ordinal() - TFGNutrients.ORIGINAL_COUNT;
        if (index >= 0 && index < extended.length) {
            if (TFGNutrients.isTransient(nutrient)) {
                extended[index] = Math.max(0f, value);
            } else {
                extended[index] = Math.min(1f, Math.max(0f, value));
            }
        }
    }

    /**
     * Add extended nutrient values from food data.
     * Transient nutrients accumulate their raw food value so that
     * the effect handler receives the exact amount declared in the food recipe.
     */
    public static void addExtendedNutrients(NutritionData data, FoodData foodData, float weight) {
        float[] extended = getOrCreateExtendedNutrients(data);

        for (Nutrient nutrient : Nutrient.VALUES) {
            if (TFGNutrients.isExtended(nutrient)) {
                int index = nutrient.ordinal() - TFGNutrients.ORIGINAL_COUNT;
                if (index >= 0 && index < extended.length) {
                    float foodNutrient = FoodDataExtension.getExtendedNutrient(foodData, nutrient);
                    if (foodNutrient > 0) {
                        if (TFGNutrients.isTransient(nutrient)) {
                            extended[index] += foodNutrient;
                        } else {
                            extended[index] = Math.min(1f, extended[index] + foodNutrient * weight);
                        }
                    }
                }
            }
        }
    }

    /**
     * Decay extended nutrients over time.
     */
    public static void decayExtendedNutrients(NutritionData data, float decayAmount) {
        float[] extended = EXTENDED_NUTRIENTS.get(data);
        if (extended == null) {
            return;
        }
        for (int i = 0; i < extended.length; i++) {
            extended[i] = Math.max(0f, extended[i] - decayAmount);
        }
    }

    /**
     * Reset all extended nutrients to zero.
     */
    public static void reset(NutritionData data) {
        float[] extended = EXTENDED_NUTRIENTS.get(data);
        if (extended != null) {
            java.util.Arrays.fill(extended, 0f);
        }
    }

    /**
     * Write extended nutrients to NBT.
     */
    public static void writeToNbt(NutritionData data, CompoundTag nbt) {
        float[] extended = EXTENDED_NUTRIENTS.get(data);
        if (extended != null) {
            CompoundTag extendedNbt = new CompoundTag();
            Nutrient[] values = Nutrient.VALUES;
            boolean hasAny = false;

            for (int i = TFGNutrients.ORIGINAL_COUNT; i < values.length; i++) {
                Nutrient nutrient = values[i];
                int index = i - TFGNutrients.ORIGINAL_COUNT;
                if (index < extended.length && extended[index] > 0) {
                    extendedNbt.putFloat(nutrient.getSerializedName(), extended[index]);
                    hasAny = true;
                }
            }

            if (hasAny) {
                nbt.put("tfg_extended_nutrients", extendedNbt);
            }
        }

    }

    /**
     * Read extended nutrients from NBT.
     */
    public static void readFromNbt(NutritionData data, CompoundTag nbt) {
        CompoundTag extendedNbt = null;
        if (nbt.contains("tfg_extended_nutrients")) {
            extendedNbt = nbt.getCompound("tfg_extended_nutrients");
        } else if (nbt.contains("tfg_negative_nutrients")) {
            extendedNbt = nbt.getCompound("tfg_negative_nutrients");
        }

        if (extendedNbt == null) {
            return;
        }

        float[] extended = getOrCreateExtendedNutrients(data);

        Nutrient[] values = Nutrient.VALUES;
        for (int i = TFGNutrients.ORIGINAL_COUNT; i < values.length; i++) {
            Nutrient nutrient = values[i];
            int index = i - TFGNutrients.ORIGINAL_COUNT;
            if (index < extended.length && extendedNbt.contains(nutrient.getSerializedName())) {
                extended[index] = extendedNbt.getFloat(nutrient.getSerializedName());
            }
        }
    }

    /**
     * Update extended nutrients from client packet.
     */
    public static void onClientUpdate(NutritionData data, float[] extendedNutrients) {
        if (extendedNutrients == null || extendedNutrients.length == 0) {
            return;
        }
        float[] extended = getOrCreateExtendedNutrients(data);
        System.arraycopy(extendedNutrients, 0, extended, 0, Math.min(extended.length, extendedNutrients.length));
    }

    /**
     * Copy extended nutrients from one NutritionData to another.
     */
    public static void copyFrom(NutritionData from, NutritionData to) {
        float[] fromExtended = EXTENDED_NUTRIENTS.get(from);
        if (fromExtended != null) {
            float[] toExtended = getOrCreateExtendedNutrients(to);
            System.arraycopy(fromExtended, 0, toExtended, 0, Math.min(fromExtended.length, toExtended.length));
        }
    }

    private NutritionDataExtension() {
    }
}
