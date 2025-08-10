package su.terrafirmagreg.core.mixins.common.ad_astra;

import earth.terrarium.adastra.common.entities.vehicles.Rocket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Rocket.class, remap = false)
public class RocketMixin {

	@Inject(method = "explode", at = @At("HEAD"), remap = false, cancellable = true)
	public void tfg$explode(CallbackInfo ci)
	{
		ci.cancel();
	}

}
