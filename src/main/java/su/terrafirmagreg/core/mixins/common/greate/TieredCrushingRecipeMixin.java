package su.terrafirmagreg.core.mixins.common.greate;

import electrolyte.greate.content.kinetics.crusher.TieredCrushingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TieredCrushingRecipe.class, remap = false)
public class TieredCrushingRecipeMixin {

	@Inject(method = "getMaxOutputCount", at = @At(value = "HEAD"), cancellable = true)
	private void tfg$getMaxOutputCount(CallbackInfoReturnable<Integer> cir)
	{
		cir.setReturnValue(9);
	}
}
