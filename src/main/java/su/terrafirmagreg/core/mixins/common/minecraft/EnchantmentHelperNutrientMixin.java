package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import su.terrafirmagreg.core.common.food.nutrient.NutrientEffectsHandler;

/**
 * Applies nutrition-based enchantment equivalents.
 */
@Mixin(LivingEntity.class)
public class EnchantmentHelperNutrientMixin {

    /**
     * Feather Falling.
     */
    @ModifyVariable(method = "causeFallDamage(FFLnet/minecraft/world/damagesource/DamageSource;)Z", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float tfg$applyFeatherFalling(float fallDistance) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player))
            return fallDistance;
        int level = NutrientEffectsHandler.getFeatherFallingLevel(player.getUUID());
        if (level <= 0)
            return fallDistance;
        return Math.max(0, fallDistance - level * 3);
    }

    /**
     * Respiration.
     */
    @Inject(method = "decreaseAirSupply(I)I", at = @At("RETURN"), cancellable = true)
    private void tfg$applyRespiration(int air, CallbackInfoReturnable<Integer> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player))
            return;
        int level = NutrientEffectsHandler.getRespirationLevel(player.getUUID());
        if (level <= 0)
            return;
        if (cir.getReturnValue() < air && self.getRandom().nextInt(level + 1) > 0)
            cir.setReturnValue(air);
    }
}
