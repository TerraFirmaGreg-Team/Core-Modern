package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.minecraft.nbt.CompoundTag;

import su.terrafirmagreg.core.common.capabilities.food.FoodDataExtension;

/**
 * Mixin to FoodHandler.Dynamic to persist negative nutrients.
 */
@Mixin(FoodHandler.Dynamic.class)
public abstract class FoodHandlerDynamicMixin implements IFood {

    /**
     * Cached negative nutrients for this handler.
     */
    @Unique
    private float[] tfg$negativeNutrients;

    /**
     * Helper method to copy a float array without using clone().
     */
    @Unique
    private static float[] tfg$copyArray(float[] source) {
        if (source == null)
            return null;
        float[] copy = new float[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    /**
     * Helper method to check if array has any empty values.
     */
    @Unique
    private static boolean tfg$hasNonZero(float[] arr) {
        if (arr == null)
            return false;
        for (float v : arr) {
            if (v != 0)
                return true;
        }
        return false;
    }

    /**
     * When setFood is called, get negative nutrients from the FoodData.
     */
    @Inject(method = "setFood", at = @At("TAIL"), remap = false)
    private void tfg$captureNegativeNutrients(FoodData data, CallbackInfo ci) {
        float[] negatives = FoodDataExtension.getNegativeNutrients(data);
        if (tfg$hasNonZero(negatives)) {
            this.tfg$negativeNutrients = tfg$copyArray(negatives);
        } else {
            this.tfg$negativeNutrients = null;
        }
    }

    /**
     * Before serializing, ensure the current FoodData has negative nutrients in WeakHashMap.
     */
    @Inject(method = "serializeNBT()Lnet/minecraft/nbt/CompoundTag;", at = @At("HEAD"), remap = false)
    private void tfg$ensureNegativeNutrientsBeforeSerialize(CallbackInfoReturnable<CompoundTag> cir) {
        FoodData currentData = this.getData();
        if (this.tfg$negativeNutrients != null) {
            if (!FoodDataExtension.hasNegativeNutrients(currentData)) {
                FoodDataExtension.setNegativeNutrients(currentData, tfg$copyArray(this.tfg$negativeNutrients));
            }
        }
    }

    /**
     * After deserializing, capture negative nutrients from the newly loaded FoodData.
     */
    @Inject(method = "deserializeNBT(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"), remap = false)
    private void tfg$captureNegativeNutrientsAfterDeserialize(CompoundTag nbt, CallbackInfo ci) {
        FoodData currentData = this.getData();
        float[] negatives = FoodDataExtension.getNegativeNutrients(currentData);
        if (tfg$hasNonZero(negatives)) {
            this.tfg$negativeNutrients = tfg$copyArray(negatives);
        }
    }
}
