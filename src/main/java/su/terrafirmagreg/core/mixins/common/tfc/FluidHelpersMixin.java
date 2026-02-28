package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

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

    /**
     * Fix GT drums not draining from Firmalife barrels when there's fluid in the drum.
     * This method handles fluid interaction between a block and an item.
     * Previously it would only check if the item fluidholder was empty before transfering into the item.
     * With this mixin it also checks if the block fluidholder is full.
     */
    @ModifyExpressionValue(method = "transferBetweenBlockHandlerAndItem", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/FluidStack;isEmpty()Z"))
    private static boolean tfg$transferBetweenBlockHandlerAndItem(
            boolean isEmpty,
            @Local(argsOnly = true) IFluidHandler blockHandler,
            @Local FluidStack aggressiveDrained) {
        return isEmpty || blockHandler.fill(aggressiveDrained, IFluidHandler.FluidAction.SIMULATE) <= 0;
    }
}
