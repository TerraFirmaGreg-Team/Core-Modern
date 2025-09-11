package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.negodya1.vintageimprovements.content.kinetics.vibration.VibratingRecipe;

@Mixin(value = VibratingRecipe.class, remap = false)
public class VibratingRecipeMixin {

    @Inject(method = "getMaxOutputCount", at = @At("HEAD"), cancellable = true)
    protected void tfg$getMaxOutputCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(6);
    }
}
