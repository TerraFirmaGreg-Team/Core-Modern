package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonObject;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.util.JsonHelpers;
import net.minecraft.nbt.CompoundTag;

import su.terrafirmagreg.core.common.capabilities.food.FoodDataExtension;
import su.terrafirmagreg.core.common.capabilities.food.TFGNutrients;

/**
 * Mixin to handle reading negative nutrients from NBT and JSON when FoodData is deserialized.
 */
@Mixin(FoodData.class)
public class FoodDataReadMixin {

    /**
     * After FoodData.read(CompoundTag) returns, read negative nutrients from NBT.
     */
    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;)Lnet/dries007/tfc/common/capabilities/food/FoodData;", at = @At("RETURN"), remap = false)
    private static void tfg$readNegativeNutrientsFromNbt(CompoundTag nbt, CallbackInfoReturnable<FoodData> cir) {
        FoodData data = cir.getReturnValue();
        if (data != null) {
            FoodDataExtension.readFromNbt(data, nbt);
        }
    }

    /**
     * After FoodData.read(JsonObject) returns, read negative nutrients from JSON.
     * This supports KubeJS TFC addon food definitions with negative nutrients.
     */
    @Inject(method = "read(Lcom/google/gson/JsonObject;)Lnet/dries007/tfc/common/capabilities/food/FoodData;", at = @At("RETURN"), remap = false)
    private static void tfg$readNegativeNutrientsFromJson(JsonObject json, CallbackInfoReturnable<FoodData> cir) {
        FoodData data = cir.getReturnValue();
        if (data != null) {
            int negativeCount = TFGNutrients.getNegativeCount();
            if (negativeCount <= 0)
                return;

            float[] negatives = new float[negativeCount];
            boolean hasAny = false;

            Nutrient[] values = Nutrient.VALUES;
            for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
                Nutrient nutrient = values[i];
                int index = i - TFGNutrients.POSITIVE_COUNT;
                if (index < negatives.length) {
                    float value = JsonHelpers.getAsFloat(json, nutrient.getSerializedName(), 0);
                    if (value != 0) {
                        negatives[index] = value;
                        hasAny = true;
                    }
                }
            }

            if (hasAny) {
                FoodDataExtension.setNegativeNutrients(data, negatives);
            }
        }
    }
}
