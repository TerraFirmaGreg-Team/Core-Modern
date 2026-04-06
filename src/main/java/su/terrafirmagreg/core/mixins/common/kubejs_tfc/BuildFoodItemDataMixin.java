package su.terrafirmagreg.core.mixins.common.kubejs_tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonObject;
import com.notenoughmail.kubejs_tfc.util.implementation.data.BuildFoodItemData;

import net.dries007.tfc.common.capabilities.food.Nutrient;

import su.terrafirmagreg.core.common.capabilities.food.TFGNutrients;

/**
 * Mixin to add support for new nutrients to KubeJS-TFC's BuildFoodItemData.
 */
@Mixin(BuildFoodItemData.class)
public class BuildFoodItemDataMixin {

    @Unique
    private float[] tfg$negativeNutrients;

    @Unique
    public BuildFoodItemData toxins(float value) {
        return tfg$setNegativeNutrient("toxins", value);
    }

    @Unique
    public BuildFoodItemData microplastics(float value) {
        return tfg$setNegativeNutrient("microplastics", value);
    }

    @Unique
    private BuildFoodItemData tfg$setNegativeNutrient(String name, float value) {
        for (Nutrient nutrient : Nutrient.values()) {
            if (TFGNutrients.isNegative(nutrient) && nutrient.getSerializedName().equals(name)) {
                if (tfg$negativeNutrients == null) {
                    tfg$negativeNutrients = new float[TFGNutrients.getNegativeCount()];
                }
                int index = nutrient.ordinal() - TFGNutrients.POSITIVE_COUNT;
                if (index >= 0 && index < tfg$negativeNutrients.length) {
                    tfg$negativeNutrients[index] = value;
                }
                break;
            }
        }
        return (BuildFoodItemData) (Object) this;
    }

    @Inject(method = "toJson", at = @At("RETURN"), remap = false)
    private void tfg$writeNegativeNutrients(CallbackInfoReturnable<JsonObject> cir) {
        if (tfg$negativeNutrients == null)
            return;
        JsonObject json = cir.getReturnValue();
        Nutrient[] values = Nutrient.values();
        for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
            int index = i - TFGNutrients.POSITIVE_COUNT;
            if (index < tfg$negativeNutrients.length && tfg$negativeNutrients[index] != 0) {
                json.addProperty(values[i].getSerializedName(), tfg$negativeNutrients[index]);
            }
        }
    }
}
