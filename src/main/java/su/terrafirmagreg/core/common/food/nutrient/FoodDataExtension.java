package su.terrafirmagreg.core.common.food.nutrient;

import java.util.IdentityHashMap;
import java.util.Map;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.minecraft.nbt.CompoundTag;

/**
 * Extension for FoodData to support additional nutrients.
 */
public final class FoodDataExtension {

    private static final Map<FoodData, float[]> EXTENDED_NUTRIENTS = new IdentityHashMap<>();

    /**
     * Get the extended nutrient values for a FoodData instance.
     * @return array of extended nutrient values.
     */
    public static float[] getExtendedNutrients(FoodData data) {
        float[] stored = EXTENDED_NUTRIENTS.get(data);
        if (stored != null) {
            return stored;
        }
        return new float[getExtendedCount()];
    }

    public static int getExtendedCount() {
        return Nutrient.VALUES.length - TFGNutrients.POSITIVE_COUNT;
    }

    public static boolean hasExtendedNutrients(FoodData data) {
        float[] extended = EXTENDED_NUTRIENTS.get(data);
        if (extended == null)
            return false;
        for (float v : extended) {
            if (v != 0)
                return true;
        }
        return false;
    }

    /**
     * Set the extended nutrient values for a FoodData instance.
     */
    public static void setExtendedNutrients(FoodData data, float[] extendedNutrients) {
        if (extendedNutrients != null && extendedNutrients.length > 0) {
            boolean hasAny = false;
            for (float v : extendedNutrients) {
                if (v != 0) {
                    hasAny = true;
                    break;
                }
            }
            if (hasAny) {
                EXTENDED_NUTRIENTS.put(data, extendedNutrients);
            } else {
                EXTENDED_NUTRIENTS.remove(data);
            }
        }
    }

    /**
     * Get a specific extended nutrient value.
     * @param nutrient must be an extended nutrient.
     * @return the nutrient value.
     */
    public static float getExtendedNutrient(FoodData data, Nutrient nutrient) {
        int index = nutrient.ordinal() - TFGNutrients.POSITIVE_COUNT;
        if (index < 0) {
            return 0;
        }
        float[] extended = EXTENDED_NUTRIENTS.get(data);
        if (extended == null || index >= extended.length) {
            return 0;
        }
        return extended[index];
    }

    /**
     * Get nutrient value for any nutrient.
     */
    public static float getNutrient(FoodData data, Nutrient nutrient) {
        if (nutrient.ordinal() < TFGNutrients.POSITIVE_COUNT) {
            return data.nutrient(nutrient);
        }
        return getExtendedNutrient(data, nutrient);
    }

    /**
     * Get all nutrient values.
     * @return array of all nutrient values in order.
     */
    public static float[] getAllNutrients(FoodData data) {
        float[] all = new float[Nutrient.VALUES.length];
        float[] original = data.nutrients();
        float[] extended = getExtendedNutrients(data);

        System.arraycopy(original, 0, all, 0, original.length);
        System.arraycopy(extended, 0, all, TFGNutrients.POSITIVE_COUNT, extended.length);

        return all;
    }

    /**
     * Write extended nutrients to NBT.
     */
    public static void writeToNbt(FoodData data, CompoundTag nbt) {
        float[] extended = EXTENDED_NUTRIENTS.get(data);
        if (extended == null) {
            return;
        }

        Nutrient[] values = Nutrient.VALUES;
        for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
            int index = i - TFGNutrients.POSITIVE_COUNT;
            if (index < extended.length && extended[index] != 0) {
                nbt.putFloat(values[i].getSerializedName(), extended[index]);
            }
        }
    }

    /**
     * Read extended nutrients from NBT and associate with FoodData.
     */
    public static void readFromNbt(FoodData data, CompoundTag nbt) {
        int extendedCount = getExtendedCount();
        if (extendedCount <= 0)
            return;

        float[] extended = new float[extendedCount];
        boolean hasAny = false;

        Nutrient[] values = Nutrient.VALUES;
        for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
            Nutrient nutrient = values[i];
            String key = nutrient.getSerializedName();
            if (nbt.contains(key)) {
                int index = i - TFGNutrients.POSITIVE_COUNT;
                extended[index] = nbt.getFloat(key);
                hasAny = true;
            }
        }

        if (hasAny) {
            EXTENDED_NUTRIENTS.put(data, extended);
        }
    }

    /**
     * Copy extended nutrients from one FoodData to another.
     */
    public static void copyExtendedNutrients(FoodData from, FoodData to) {
        float[] extended = EXTENDED_NUTRIENTS.get(from);
        if (extended != null) {
            float[] copy = new float[extended.length];
            System.arraycopy(extended, 0, copy, 0, extended.length);
            EXTENDED_NUTRIENTS.put(to, copy);
        }
    }

    /**
     * Remove extended nutrients for a FoodData.
     */
    public static void remove(FoodData data) {
        EXTENDED_NUTRIENTS.remove(data);
    }

    private FoodDataExtension() {
    }
}
