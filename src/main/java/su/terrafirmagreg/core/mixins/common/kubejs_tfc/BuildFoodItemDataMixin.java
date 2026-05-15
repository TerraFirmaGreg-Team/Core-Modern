package su.terrafirmagreg.core.mixins.common.kubejs_tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonObject;
import com.notenoughmail.kubejs_tfc.util.implementation.data.BuildFoodItemData;

import net.dries007.tfc.common.capabilities.food.Nutrient;

import su.terrafirmagreg.core.common.food.nutrient.INegativeNutrientBuilder;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mixin to add support for new nutrients to KubeJS-TFC's BuildFoodItemData.
 */
@Mixin(value = BuildFoodItemData.class, remap = false)
public class BuildFoodItemDataMixin implements INegativeNutrientBuilder<BuildFoodItemData> {

    @Unique
    private float[] tfg$negativeNutrients;

    @Override
    public BuildFoodItemData toxins(float value) {
        return tfg$setNegativeNutrient("toxins", value);
    }

    @Override
    public BuildFoodItemData microplastics(float value) {
        return tfg$setNegativeNutrient("microplastics", value);
    }

    @Override
    public BuildFoodItemData parasites(float value) {
        return tfg$setNegativeNutrient("parasites", value);
    }

    @Unique
    private BuildFoodItemData tfg$setNegativeNutrient(String name, float value) {
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (TFGNutrients.isExtended(nutrient) && nutrient.getSerializedName().equals(name)) {
                if (tfg$negativeNutrients == null) {
                    tfg$negativeNutrients = new float[TFGNutrients.getExtendedCount()];
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
        if (json != null) {
            Nutrient[] values = Nutrient.VALUES;
            for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
                int index = i - TFGNutrients.POSITIVE_COUNT;
                if (index < tfg$negativeNutrients.length && tfg$negativeNutrients[index] != 0) {
                    json.addProperty(values[i].getSerializedName(), tfg$negativeNutrients[index]);
                }
            }
        }
        tfg$negativeNutrients = null;
    }
}
