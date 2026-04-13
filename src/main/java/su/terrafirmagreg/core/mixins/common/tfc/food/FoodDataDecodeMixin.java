package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.minecraft.network.FriendlyByteBuf;

import su.terrafirmagreg.core.common.food.nutrient.FoodDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mixin to fix FoodData.decode() to read both positive and negative nutrients.
 */
@Mixin(FoodData.class)
public class FoodDataDecodeMixin {

    /**
     * Override decode to read positive nutrients followed by negative nutrients.
     * @author Redeix
     * @reason Fix network deserialization with extended Nutrient enum and support negative nutrients.
     */
    @Overwrite(remap = false)
    public static FoodData decode(FriendlyByteBuf buffer) {
        final int hunger = buffer.readVarInt();
        final float saturation = buffer.readFloat();
        final float water = buffer.readFloat();
        final float decayModifier = buffer.readFloat();

        // Read positive nutrients
        final float[] nutrition = new float[TFGNutrients.POSITIVE_COUNT];
        for (int i = 0; i < TFGNutrients.POSITIVE_COUNT; i++) {
            nutrition[i] = buffer.readFloat();
        }

        FoodData data = FoodData.create(hunger, water, saturation, nutrition, decayModifier);

        // Read extended nutrients
        int extendedCount = buffer.readVarInt();
        if (extendedCount > 0) {
            float[] extended = new float[extendedCount];
            for (int i = 0; i < extendedCount; i++) {
                extended[i] = buffer.readFloat();
            }
            FoodDataExtension.setExtendedNutrients(data, extended);
        }

        return data;
    }
}
