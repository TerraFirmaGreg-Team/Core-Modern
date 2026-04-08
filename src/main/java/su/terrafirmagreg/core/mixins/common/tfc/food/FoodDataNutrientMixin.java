package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;

import su.terrafirmagreg.core.common.capabilities.food.FoodDataExtension;
import su.terrafirmagreg.core.common.capabilities.food.TFGNutrients;

/**
 * Mixin to handle negative nutrients in FoodData.nutrient() method.
 */
@Mixin(FoodData.class)
public class FoodDataNutrientMixin {

    /**
     * Intercept nutrient() calls for negative nutrients and return from our extension.
     */
    @Inject(method = "nutrient", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$handleNegativeNutrient(Nutrient nutrient, CallbackInfoReturnable<Float> cir) {
        if (TFGNutrients.isNegative(nutrient)) {
            FoodData self = (FoodData) (Object) this;
            float value = FoodDataExtension.getNegativeNutrient(self, nutrient);
            cir.setReturnValue(value);
        }
    }
}
