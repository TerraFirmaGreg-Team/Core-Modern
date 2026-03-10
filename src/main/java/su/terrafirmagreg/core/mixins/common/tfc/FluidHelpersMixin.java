package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.world.entity.Entity;

import su.terrafirmagreg.core.common.data.TFGFluids;

@Mixin(value = FluidHelpers.class, remap = false)
public class FluidHelpersMixin {

    /**
     * TFC's FluidHelpers class is used by its own mixins to handle various fluid-related things for river water, sea
     * water, etc, so this mixin is to add our own mars water so it's handled in the same way
     */

    @Inject(method = "isInWaterLikeFluid", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$isInWaterLikeFluid(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.isInFluidType(((fluidType, aDouble) -> fluidType == TFGFluids.MARS_WATER.type().get()))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isEyeInWaterLikeFluid", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$isEyeInWaterLikeFluid(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.isEyeInFluidType(TFGFluids.MARS_WATER.type().get())) {
            cir.setReturnValue(true);
        }
    }
}
