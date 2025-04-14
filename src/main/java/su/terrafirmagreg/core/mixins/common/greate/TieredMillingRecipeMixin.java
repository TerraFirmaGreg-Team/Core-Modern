package su.terrafirmagreg.core.mixins.common.greate;

import electrolyte.greate.content.kinetics.millstone.TieredMillingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TieredMillingRecipe.class, remap = false)
public class TieredMillingRecipeMixin {

	@Inject(method = "getMaxOutputCount", at = @At(value = "HEAD"), cancellable = true)
	private void tfg$getMaxOutputCount(CallbackInfoReturnable<Integer> cir)
	{
		cir.setReturnValue(6);
	}
}
