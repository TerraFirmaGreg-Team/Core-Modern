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

import su.terrafirmagreg.core.common.food.nutrient.FoodDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mixin to handle reading negative nutrients from NBT and JSON when FoodData is deserialized.
 */
@Mixin(FoodData.class)
public class FoodDataReadMixin {

    /**
     * After FoodData.read(CompoundTag) returns, read extended nutrients from NBT.
     */
    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;)Lnet/dries007/tfc/common/capabilities/food/FoodData;", at = @At("RETURN"), remap = false)
    private static void tfg$readExtendedNutrientsFromNbt(CompoundTag nbt, CallbackInfoReturnable<FoodData> cir) {
        FoodData data = cir.getReturnValue();
        if (data != null) {
            FoodDataExtension.readFromNbt(data, nbt);
        }
    }

    /**
     * After FoodData.read(JsonObject) returns, read extended nutrients from JSON.
     * This supports KubeJS TFC addon food definitions with extended nutrients.
     */
    @Inject(method = "read(Lcom/google/gson/JsonObject;)Lnet/dries007/tfc/common/capabilities/food/FoodData;", at = @At("RETURN"), remap = false)
    private static void tfg$readExtendedNutrientsFromJson(JsonObject json, CallbackInfoReturnable<FoodData> cir) {
        FoodData data = cir.getReturnValue();
        if (data != null) {
            int extendedCount = TFGNutrients.getExtendedCount();
            if (extendedCount <= 0)
                return;

            float[] extended = new float[extendedCount];
            boolean hasAny = false;

            Nutrient[] values = Nutrient.VALUES;
            for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
                Nutrient nutrient = values[i];
                int index = i - TFGNutrients.POSITIVE_COUNT;
                if (index < extended.length) {
                    float value = JsonHelpers.getAsFloat(json, nutrient.getSerializedName(), 0);
                    if (value != 0) {
                        extended[index] = value;
                        hasAny = true;
                    }
                }
            }

            if (hasAny) {
                FoodDataExtension.setExtendedNutrients(data, extended);
            }
        }
    }
}
