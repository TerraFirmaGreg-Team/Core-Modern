package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.minecraft.network.FriendlyByteBuf;

import su.terrafirmagreg.core.common.food.nutrient.FoodDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mixin to fix FoodData network encoding/decoding to handle both positive and negative nutrients.
 */
@Mixin(FoodData.class)
public abstract class FoodDataEncodeMixin {

    /**
     * Override encode to write positive nutrients followed by negative nutrients.
     * @author Redeix
     * @reason Fix network serialization with extended Nutrient enum and support negative nutrients.
     */
    @Overwrite(remap = false)
    public void encode(FriendlyByteBuf buffer) {
        FoodData self = (FoodData) (Object) this;

        buffer.writeVarInt(self.hunger());
        buffer.writeFloat(self.saturation());
        buffer.writeFloat(self.water());
        buffer.writeFloat(self.decayModifier());

        // Write positive nutrients.
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (TFGNutrients.isPositive(nutrient)) {
                buffer.writeFloat(self.nutrient(nutrient));
            }
        }

        // Write extended nutrients
        float[] extended = FoodDataExtension.getExtendedNutrients(self);
        buffer.writeVarInt(extended.length);
        for (float value : extended) {
            buffer.writeFloat(value);
        }
    }
}
