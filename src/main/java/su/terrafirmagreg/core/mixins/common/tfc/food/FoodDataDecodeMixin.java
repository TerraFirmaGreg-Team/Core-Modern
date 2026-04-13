package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.minecraft.network.FriendlyByteBuf;

import su.terrafirmagreg.core.common.capabilities.food.FoodDataExtension;
import su.terrafirmagreg.core.common.capabilities.food.TFGNutrients;

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

        // Read negative nutrients
        int negativeCount = buffer.readVarInt();
        if (negativeCount > 0) {
            float[] negatives = new float[negativeCount];
            for (int i = 0; i < negativeCount; i++) {
                negatives[i] = buffer.readFloat();
            }
            FoodDataExtension.setNegativeNutrients(data, negatives);
        }

        return data;
    }
}
