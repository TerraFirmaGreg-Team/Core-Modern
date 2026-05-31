package su.terrafirmagreg.core.mixins.common.tfc.food;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.ToDoubleFunction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.NutritionData;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.nbt.CompoundTag;

import su.terrafirmagreg.core.common.food.nutrient.INutritionDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.NutritionDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mixin to modify NutritionData to track extended nutrients when eating food.
 */
@Mixin(NutritionData.class)
public abstract class NutritionDataMixin implements INutritionDataExtension {

    @Shadow(remap = false)
    @Final
    private float[] nutrients;

    @Shadow(remap = false)
    private float averageNutrients;

    @Shadow(remap = false)
    @Final
    private LinkedList<FoodData> records;

    @Shadow(remap = false)
    @Final
    private float defaultNutritionValue;

    @Shadow(remap = false)
    @Final
    private float defaultDairyNutritionValue;

    @Shadow(remap = false)
    private int hungerWindow;

    @Shadow(remap = false)
    private int hunger;

    @Shadow(remap = false)
    protected abstract void updateAverageNutrients();

    /**
     * Overwrite calculateNutrition to only iterate over positive nutrients.
     * @author Redeix
     * @reason Fix array index issues with extended Nutrient enum.
     */
    @Overwrite(remap = false)
    private void calculateNutrition() {
        Arrays.fill(this.nutrients, 0);

        int runningHungerTotal = Math.max(TFCFoodData.MAX_HUNGER - hunger, 0);

        hungerWindow = TFCConfig.SERVER.nutritionRotationHungerWindow.get();
        for (int i = 0; i < records.size(); i++) {
            FoodData record = records.get(i);
            int nextHunger = record.hunger() + runningHungerTotal;
            if (nextHunger <= this.hungerWindow) {
                updateAllNutrients(nutrients, j -> nutrients[j.ordinal()] + record.nutrient(j) * Math.max(record.hunger(), 4));
                runningHungerTotal = nextHunger;
            } else {
                float actualHunger = hungerWindow - runningHungerTotal;
                updateAllNutrients(nutrients, j -> nutrients[j.ordinal()] + record.nutrient(j) * actualHunger);

                while (records.size() > i + 1) {
                    records.remove(i + 1);
                }
            }
        }

        updateAllNutrients(nutrients, j -> nutrients[j.ordinal()] / hungerWindow);
        if (runningHungerTotal < hungerWindow) {
            float defaultModifier = 1 - (float) runningHungerTotal / hungerWindow;
            for (Nutrient nutrient : Nutrient.VALUES) {
                if (TFGNutrients.isPositive(nutrient) && nutrient.ordinal() < nutrients.length) {
                    if (nutrient == Nutrient.DAIRY) {
                        nutrients[nutrient.ordinal()] += defaultDairyNutritionValue * defaultModifier;
                    } else {
                        nutrients[nutrient.ordinal()] += defaultNutritionValue * defaultModifier;
                    }
                }
            }
        }
        updateAllNutrients(nutrients, j -> Math.min(1, nutrients[j.ordinal()]));
        updateAverageNutrients();
    }

    /**
     * Redirect the updateAverageNutrients method to only consider positive nutrients.
     * Negative nutrients are tracked separately.
     */
    @Inject(method = "updateAverageNutrients", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$updateAverageNutrientsOnlyPositive(CallbackInfo ci) {
        averageNutrients = 0;

        for (int i = 0; i < TFGNutrients.POSITIVE_COUNT && i < nutrients.length; i++) {
            averageNutrients += nutrients[i];
        }

        averageNutrients /= TFGNutrients.POSITIVE_COUNT;

        ci.cancel();
    }

    /**
     * Override updateAllNutrients to skip negative nutrients.
     * @author Redeix
     * @reason Skip negative nutrients to avoid array index issues.
     */
    @Overwrite(remap = false)
    private void updateAllNutrients(float[] array, ToDoubleFunction<Nutrient> operator) {
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (TFGNutrients.isPositive(nutrient) && nutrient.ordinal() < array.length) {
                array[nutrient.ordinal()] = (float) operator.applyAsDouble(nutrient);
            }
        }
    }

    /**
     * When nutrients are added from food, also track extended nutrients.
     */
    @Inject(method = "addNutrients", at = @At("HEAD"), remap = false)
    private void tfg$addExtendedNutrients(FoodData data, CallbackInfo ci) {
        float weight = Math.max(data.hunger(), 4) / 100f;
        NutritionDataExtension.addExtendedNutrients((NutritionData) (Object) this, data, weight);
    }

    /**
     * When nutrition is reset, also reset extended nutrients.
     */
    @Inject(method = "reset", at = @At("HEAD"), remap = false)
    private void tfg$resetExtendedNutrients(CallbackInfo ci) {
        NutritionDataExtension.reset((NutritionData) (Object) this);
    }

    /**
     * Save extended nutrients to NBT.
     */
    @Inject(method = "writeToNbt", at = @At("RETURN"), remap = false)
    private void tfg$writeExtendedNutrients(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag nbt = cir.getReturnValue();
        NutritionDataExtension.writeToNbt((NutritionData) (Object) this, nbt);
    }

    /**
     * Load extended nutrients from NBT.
     */
    @Inject(method = "readFromNbt", at = @At("TAIL"), remap = false)
    private void tfg$readExtendedNutrients(CompoundTag nbt, CallbackInfo ci) {
        NutritionDataExtension.readFromNbt((NutritionData) (Object) this, nbt);
    }

    /**
     * Intercept getNutrient calls for extended nutrients to prevent ArrayIndexOutOfBoundsException.
     */
    @Inject(method = "getNutrient", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$handleExtendedNutrientAccess(Nutrient nutrient, CallbackInfoReturnable<Float> cir) {
        if (TFGNutrients.isExtended(nutrient)) {
            cir.setReturnValue(NutritionDataExtension.getExtendedNutrient((NutritionData) (Object) this, nutrient));
        }
    }

    @Override
    public float tfg$getExtendedNutrient(Nutrient nutrient) {
        return NutritionDataExtension.getExtendedNutrient((NutritionData) (Object) this, nutrient);
    }

    @Override
    public float[] tfg$getExtendedNutrients() {
        float[] extended = NutritionDataExtension.getExtendedNutrients((NutritionData) (Object) this);
        return extended != null ? extended : new float[TFGNutrients.getExtendedCount()];
    }
}
