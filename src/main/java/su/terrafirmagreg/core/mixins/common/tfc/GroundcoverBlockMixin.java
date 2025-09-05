package su.terrafirmagreg.core.mixins.common.tfc;

import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.fluids.FluidProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.terrafirmagreg.core.compat.kjs.TFGBlockProperties;

@Mixin(value = GroundcoverBlock.class, remap = false)
public class GroundcoverBlockMixin  {

	@Inject(method = "getFluidProperty", at = @At("HEAD"), remap = false, cancellable = true)
	public void tfg$getFluidProperty(CallbackInfoReturnable<FluidProperty> cir)	{
		cir.setReturnValue(TFGBlockProperties.SPACE_WATER);
	}
}
