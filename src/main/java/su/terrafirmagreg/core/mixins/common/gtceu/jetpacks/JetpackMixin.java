package su.terrafirmagreg.core.mixins.common.gtceu.jetpacks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.common.item.armor.Jetpack;

@Mixin(value = Jetpack.class, remap = false)
public class JetpackMixin {

    @Inject(method = "getVerticalHoverSlowSpeed", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getVerticalHoverSlowSpeed(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0.005D);
    }

    @Inject(method = "getSidewaysSpeed", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getSidewaysSpeed(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0.04D);
    }

    @Inject(method = "getVerticalSpeed", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getVerticalSpeed(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0.22D);
    }

    @ModifyVariable(method = "drainEnergy", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int tfg$drainEnergy(int amount) {
        return amount * 2;
    }
}
