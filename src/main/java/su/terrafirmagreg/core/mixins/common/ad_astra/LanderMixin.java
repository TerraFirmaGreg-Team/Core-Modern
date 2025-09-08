package su.terrafirmagreg.core.mixins.common.ad_astra;

import earth.terrarium.adastra.common.entities.vehicles.Lander;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Lander.class, remap = false)
public class LanderMixin {

	/**
	 *  Prevents the rocket from exploding, since the auto lander mod was causing issues
	 */

	@Inject(method = "explode", at = @At("HEAD"), remap = false, cancellable = true)
	public void tfg$explode(CallbackInfo ci)
	{
		ci.cancel();
	}

	@Inject(method = "causeFallDamage", at = @At("HEAD"), remap = true, cancellable = true)
	public void tfg$causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir)
	{
		cir.setReturnValue(false);
	}
}
