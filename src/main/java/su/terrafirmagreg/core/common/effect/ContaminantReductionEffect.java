package su.terrafirmagreg.core.common.effect;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.NutritionData;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import su.terrafirmagreg.core.common.data.TFGEffects;
import su.terrafirmagreg.core.common.food.nutrient.NutritionDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mob effect that decreases a player's contaminant level.
 */
public class ContaminantReductionEffect extends MobEffect {
    public ContaminantReductionEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player) || !(player.getFoodData() instanceof TFCFoodData tfcFoodData)) {
            return;
        }

        NutritionData nutritionData = tfcFoodData.getNutrition();
        float reductionAmount = (amplifier + 1) * 0.001f;

        Nutrient targetNutrient = null;
        if (this == TFGEffects.CURE_PARASITES.get())
            targetNutrient = TFGNutrients.getByName("PARASITES");
        else if (this == TFGEffects.CURE_MICROPLASTICS.get())
            targetNutrient = TFGNutrients.getByName("MICROPLASTICS");
        else if (this == TFGEffects.CURE_TOXINS.get())
            targetNutrient = TFGNutrients.getByName("TOXINS");

        if (targetNutrient != null) {
            float currentValue = NutritionDataExtension.getExtendedNutrient(nutritionData, targetNutrient);
            if (currentValue > 0) {
                float newValue = Math.max(0, currentValue - reductionAmount);
                NutritionDataExtension.setExtendedNutrient(nutritionData, targetNutrient, newValue);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
