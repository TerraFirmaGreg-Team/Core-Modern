package su.terrafirmagreg.core.mixins.common.gtceu.jetpacks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.common.item.armor.AdvancedJetpack;

@Mixin(value = AdvancedJetpack.class, remap = false)
public class AdvancedJetpackMixin {

    @Inject(method = "getVerticalHoverSlowSpeed", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getVerticalHoverSlowSpeed(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0D);
    }

    @Inject(method = "getSidewaysSpeed", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getSidewaysSpeed(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0.1D);
    }

    @Inject(method = "getVerticalSpeed", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getVerticalSpeed(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0.35D);
    }

    @Inject(method = "getVerticalHoverSpeed", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getVerticalHoverSpeed(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(0.3D);
    }
}
