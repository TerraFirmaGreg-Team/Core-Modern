package su.terrafirmagreg.core.mixins.common.kubejs_tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonObject;
import com.notenoughmail.kubejs_tfc.util.implementation.data.BuildFoodItemData;

import net.dries007.tfc.common.capabilities.food.Nutrient;

import su.terrafirmagreg.core.common.food.nutrient.IExtendedNutrientBuilder;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mixin to add support for new nutrients to KubeJS-TFC's BuildFoodItemData.
 */
@Mixin(value = BuildFoodItemData.class, remap = false)
public class BuildFoodItemDataMixin implements IExtendedNutrientBuilder<BuildFoodItemData> {

    @Unique
    private float[] tfg$extendedNutrients;

    // --------- Negative Nutrients -----------
    @Override
    public BuildFoodItemData toxins(float value) {
        return tfg$setExtendedNutrient("toxins", value);
    }

    @Override
    public BuildFoodItemData microplastics(float value) {
        return tfg$setExtendedNutrient("microplastics", value);
    }

    @Override
    public BuildFoodItemData parasites(float value) {
        return tfg$setExtendedNutrient("parasites", value);
    }

    // --------- Transient Nutrients -----------
    @Override
    public BuildFoodItemData deadly(float value) {
        return tfg$setExtendedNutrient("deadly", value);
    }

    @Override
    public BuildFoodItemData cooling(float value) {
        return tfg$setExtendedNutrient("cooling", value);
    }

    @Override
    public BuildFoodItemData warming(float value) {
        return tfg$setExtendedNutrient("warming", value);
    }

    @Override
    public BuildFoodItemData freezing(float value) {
        return tfg$setExtendedNutrient("freezing", value);
    }

    @Override
    public BuildFoodItemData blazing(float value) {
        return tfg$setExtendedNutrient("blazing", value);
    }

    @Override
    public BuildFoodItemData radiating(float value) {
        return tfg$setExtendedNutrient("radiating", value);
    }

    @Override
    public BuildFoodItemData nauseating(float value) {
        return tfg$setExtendedNutrient("nauseating", value);
    }

    @Override
    public BuildFoodItemData parching(float value) {
        return tfg$setExtendedNutrient("parching", value);
    }

    @Override
    public BuildFoodItemData quenching(float value) {
        return tfg$setExtendedNutrient("quenching", value);
    }

    @Override
    public BuildFoodItemData bolstering(float value) {
        return tfg$setExtendedNutrient("bolstering", value);
    }

    @Override
    public BuildFoodItemData hearty(float value) {
        return tfg$setExtendedNutrient("hearty", value);
    }

    @Override
    public BuildFoodItemData rejuvenating(float value) {
        return tfg$setExtendedNutrient("rejuvenating", value);
    }

    @Override
    public BuildFoodItemData sugary(float value) {
        return tfg$setExtendedNutrient("sugary", value);
    }

    @Override
    public BuildFoodItemData spicy(float value) {
        return tfg$setExtendedNutrient("spicy", value);
    }

    @Override
    public BuildFoodItemData fulfilling(float value) {
        return tfg$setExtendedNutrient("fulfilling", value);
    }

    @Unique
    private BuildFoodItemData tfg$setExtendedNutrient(String name, float value) {
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
        return (BuildFoodItemData) (Object) this;
    }

    @Inject(method = "toJson", at = @At("RETURN"), remap = false)
    private void tfg$writeExtendedNutrients(CallbackInfoReturnable<JsonObject> cir) {
        if (tfg$extendedNutrients == null)
            return;
        JsonObject json = cir.getReturnValue();
        if (json != null) {
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
