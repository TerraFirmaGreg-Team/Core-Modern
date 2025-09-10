package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.negodya1.vintageimprovements.content.kinetics.curving_press.CurvingRecipe;

@Mixin(value = CurvingRecipe.class, remap = false)
public class CurvingRecipeMixin {

    @Inject(method = "getMaxInputCount", at = @At("HEAD"), cancellable = true)
    protected void tfg$getMaxInputCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(2);
    }
}
