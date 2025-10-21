package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.entities.vehicles.Lander;

@Mixin(value = Lander.class, remap = false)
public abstract class LanderMixin extends Entity {

    public LanderMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    /**
     * Prevents the rocket from exploding when landing due to server lag
     */

    @Inject(method = "explode", at = @At("HEAD"), remap = false, cancellable = true)
    public void tfg$explode(CallbackInfo ci) {
        if (this.getY() < 400) {
            ci.cancel();
        }
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), remap = true, cancellable = true)
    public void tfg$causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource,
            CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
