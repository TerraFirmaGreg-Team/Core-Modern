package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;

import su.terrafirmagreg.core.common.food.nutrient.FoodDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mixin to handle extended nutrients in FoodData.nutrient() method.
 */
@Mixin(FoodData.class)
public class FoodDataNutrientMixin {

    /**
     * Intercept nutrient() calls for extended nutrients and return from our extension.
     */
    @Inject(method = "nutrient", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$handleExtendedNutrient(Nutrient nutrient, CallbackInfoReturnable<Float> cir) {
        if (TFGNutrients.isExtended(nutrient)) {
            FoodData self = (FoodData) (Object) this;
            float value = FoodDataExtension.getExtendedNutrient(self, nutrient);
            cir.setReturnValue(value);
        }
    }
}
