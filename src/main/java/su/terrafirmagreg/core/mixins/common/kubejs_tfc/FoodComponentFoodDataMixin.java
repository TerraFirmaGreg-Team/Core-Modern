package su.terrafirmagreg.core.mixins.common.kubejs_tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.notenoughmail.kubejs_tfc.addons.firmalife.recipe.component.FoodComponent;

import net.dries007.tfc.common.capabilities.food.Nutrient;

import su.terrafirmagreg.core.common.food.nutrient.INegativeNutrientBuilder;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mixin to add support for new nutrients to KubeJS-TFC's FoodComponent.FoodData.
 */
@Mixin(value = FoodComponent.FoodData.class, remap = false)
public class FoodComponentFoodDataMixin implements INegativeNutrientBuilder<FoodComponent.FoodData> {

    @Unique
    private float[] tfg$negativeNutrients;

    @Override
    public FoodComponent.FoodData toxins(float value) {
        return tfg$setNegativeNutrient("toxins", value);
    }

    @Override
    public FoodComponent.FoodData microplastics(float value) {
        return tfg$setNegativeNutrient("microplastics", value);
    }

    @Override
    public FoodComponent.FoodData parasites(float value) {
        return tfg$setNegativeNutrient("parasites", value);
    }

    @Unique
    private FoodComponent.FoodData tfg$setNegativeNutrient(String name, float value) {
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
        return (FoodComponent.FoodData) (Object) this;
    }

    @Inject(method = "<init>(Lcom/google/gson/JsonObject;)V", at = @At("TAIL"), remap = false)
    private void tfg$readNegativeNutrients(JsonObject json, CallbackInfo ci) {
        Nutrient[] values = Nutrient.VALUES;
        for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
            Nutrient nutrient = values[i];
            String name = nutrient.getSerializedName();
            if (json.has(name)) {
                tfg$setNegativeNutrient(name, json.get(name).getAsFloat());
            }
        }
    }

    @Inject(method = "toJson", at = @At("RETURN"), remap = false)
    private void tfg$writeNegativeNutrients(CallbackInfoReturnable<JsonElement> cir) {
        if (tfg$negativeNutrients == null)
            return;
        if (cir.getReturnValue() instanceof JsonObject json) {
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
