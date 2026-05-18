package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.minecraft.world.entity.player.Player;

import su.terrafirmagreg.core.common.data.TFGEffects;
import su.terrafirmagreg.core.common.food.nutrient.NutrientEffectsHandler;

/**
 * Mixin to apply nutrient effect multipliers to TFCFoodData.
 */
@Mixin(TFCFoodData.class)
public class TFCFoodDataNutrientEffectsMixin {

    @Shadow(remap = false)
    @Final
    private Player sourcePlayer;

    /**
     * Applies the protein exhaustion reduction.
     */
    @ModifyVariable(method = "addExhaustion(F)V", at = @At("HEAD"), argsOnly = true)
    private float tfg$applyExhaustionMultiplier(float exhaustion) {
        return exhaustion * NutrientEffectsHandler.getProteinExhaustionMultiplier(sourcePlayer.getUUID());
    }

    /**
     * Applies the parasites passive exhaustion increase.
     */
    @ModifyArg(method = "tick(Lnet/minecraft/world/entity/player/Player;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private float tfg$applyPassiveExhaustionModifier(float exhaustion) {
        return exhaustion * NutrientEffectsHandler.getParasitesPassiveExhaustionModifier(sourcePlayer.getUUID());
    }

    /**
     * Applies the fruit thirst reduction and the quenched mob effect.
     */
    @Inject(method = "getThirstModifier(Lnet/minecraft/world/entity/player/Player;)F", at = @At("RETURN"), remap = false, cancellable = true)
    private void tfg$applyThirstMultiplier(Player player, CallbackInfoReturnable<Float> cir) {
        if (player.hasEffect(TFGEffects.QUENCHED.get())) {
            cir.setReturnValue(0f);
        } else {
            cir.setReturnValue(cir.getReturnValue() * NutrientEffectsHandler.getThirstModifierMultiplier(sourcePlayer.getUUID()));
        }
    }

    /**
     * Applies the microplastics thirst temperature multiplier.
     */
    @Inject(method = "getThirstContributionFromTemperature(Lnet/minecraft/world/entity/player/Player;)F", at = @At("RETURN"), remap = false, cancellable = true)
    private void tfg$applyMicroplasticsThirstTemperatureMultiplier(Player player, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * NutrientEffectsHandler.getMicroplasticsThirstTemperatureModifier(sourcePlayer.getUUID()));
    }

    /**
     * Redirects the player.heal() call inside tick() to apply the average nutrition healing multiplier.
     */
    @Redirect(method = "tick(Lnet/minecraft/world/entity/player/Player;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"))
    private void tfg$redirectHeal(Player player, float amount) {
        player.heal(amount * NutrientEffectsHandler.getHealingModifierMultiplier(player.getUUID()));
    }
}
