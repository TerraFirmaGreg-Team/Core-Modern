package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.minecraft.network.FriendlyByteBuf;

import su.terrafirmagreg.core.common.capabilities.food.TFGNutrients;

/**
 * Mixin to fix FoodData network encoding/decoding to only handle positive nutrients.
 */
@Mixin(FoodData.class)
public abstract class FoodDataEncodeMixin {

    @Shadow(remap = false)
    public abstract float nutrient(Nutrient nutrient);

    @Shadow(remap = false)
    @Final
    private int hunger;

    @Shadow(remap = false)
    @Final
    private float saturation;

    @Shadow(remap = false)
    @Final
    private float water;

    @Shadow(remap = false)
    @Final
    private float decayModifier;

    /**
     * Override encode to only write positive nutrients.
     * @author Redeix
     * @reason Fix network serialization with extended Nutrient enum.
     */
    @Overwrite(remap = false)
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(hunger);
        buffer.writeFloat(saturation);
        buffer.writeFloat(water);
        buffer.writeFloat(decayModifier);

        for (Nutrient nutrient : Nutrient.VALUES) {
            if (TFGNutrients.isPositive(nutrient)) {
                buffer.writeFloat(nutrient(nutrient));
            }
        }
    }
}
