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

import su.terrafirmagreg.core.common.food.nutrient.IExtendedNutrientBuilder;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mixin to add support for new nutrients to KubeJS-TFC's FoodComponent.FoodData.
 */
@Mixin(value = FoodComponent.FoodData.class, remap = false)
public class FoodComponentFoodDataMixin implements IExtendedNutrientBuilder<FoodComponent.FoodData> {

    @Unique
    private float[] tfg$extendedNutrients;

    // --------- Negative Nutrients -----------
    @Override
    public FoodComponent.FoodData toxins(float value) {
        return tfg$setExtendedNutrient("toxins", value);
    }

    @Override
    public FoodComponent.FoodData microplastics(float value) {
        return tfg$setExtendedNutrient("microplastics", value);
    }

    @Override
    public FoodComponent.FoodData parasites(float value) {
        return tfg$setExtendedNutrient("parasites", value);
    }

    // --------- Transient Nutrients -----------
    @Override
    public FoodComponent.FoodData deadly(float value) {
        return tfg$setExtendedNutrient("deadly", value);
    }

    @Override
    public FoodComponent.FoodData cooling(float value) {
        return tfg$setExtendedNutrient("cooling", value);
    }

    @Override
    public FoodComponent.FoodData warming(float value) {
        return tfg$setExtendedNutrient("warming", value);
    }

    @Override
    public FoodComponent.FoodData freezing(float value) {
        return tfg$setExtendedNutrient("freezing", value);
    }

    @Override
    public FoodComponent.FoodData blazing(float value) {
        return tfg$setExtendedNutrient("blazing", value);
    }

    @Override
    public FoodComponent.FoodData radiating(float value) {
        return tfg$setExtendedNutrient("radiating", value);
    }

    @Override
    public FoodComponent.FoodData nauseating(float value) {
        return tfg$setExtendedNutrient("nauseating", value);
    }

    @Override
    public FoodComponent.FoodData parching(float value) {
        return tfg$setExtendedNutrient("parching", value);
    }

    @Override
    public FoodComponent.FoodData quenching(float value) {
        return tfg$setExtendedNutrient("quenching", value);
    }

    @Override
    public FoodComponent.FoodData bolstering(float value) {
        return tfg$setExtendedNutrient("bolstering", value);
    }

    @Override
    public FoodComponent.FoodData hearty(float value) {
        return tfg$setExtendedNutrient("hearty", value);
    }

    @Override
    public FoodComponent.FoodData rejuvenating(float value) {
        return tfg$setExtendedNutrient("rejuvenating", value);
    }

    @Override
    public FoodComponent.FoodData sugary(float value) {
        return tfg$setExtendedNutrient("sugary", value);
    }

    @Override
    public FoodComponent.FoodData spicy(float value) {
        return tfg$setExtendedNutrient("spicy", value);
    }

    @Override
    public FoodComponent.FoodData fulfilling(float value) {
        return tfg$setExtendedNutrient("fulfilling", value);
    }

    @Unique
    private FoodComponent.FoodData tfg$setExtendedNutrient(String name, float value) {
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (TFGNutrients.isExtended(nutrient) && nutrient.getSerializedName().equals(name)) {
                if (tfg$extendedNutrients == null) {
                    tfg$extendedNutrients = new float[TFGNutrients.getExtendedCount()];
                }
                int index = nutrient.ordinal() - TFGNutrients.POSITIVE_COUNT;
                if (index >= 0 && index < tfg$extendedNutrients.length) {
                    tfg$extendedNutrients[index] = value;
                }
                break;
            }
        }
        return (FoodComponent.FoodData) (Object) this;
    }

    @Inject(method = "<init>(Lcom/google/gson/JsonObject;)V", at = @At("TAIL"), remap = false)
    private void tfg$readExtendedNutrients(JsonObject json, CallbackInfo ci) {
        Nutrient[] values = Nutrient.VALUES;
        for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
            Nutrient nutrient = values[i];
            String name = nutrient.getSerializedName();
            if (json.has(name)) {
                tfg$setExtendedNutrient(name, json.get(name).getAsFloat());
            }
        }
    }

    @Inject(method = "toJson", at = @At("RETURN"), remap = false)
    private void tfg$writeExtendedNutrients(CallbackInfoReturnable<JsonElement> cir) {
        if (tfg$extendedNutrients == null)
            return;
        if (cir.getReturnValue() instanceof JsonObject json) {
            Nutrient[] values = Nutrient.VALUES;
            for (int i = TFGNutrients.POSITIVE_COUNT; i < values.length; i++) {
                int index = i - TFGNutrients.POSITIVE_COUNT;
                if (index < tfg$extendedNutrients.length && tfg$extendedNutrients[index] != 0) {
                    json.addProperty(values[i].getSerializedName(), tfg$extendedNutrients[index]);
                }
            }
        }
        tfg$extendedNutrients = null;
    }
}
