package su.terrafirmagreg.core.mixins.common.fowlplay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import aqario.fowlplay.common.entity.bird.BirdEntity;
import aqario.fowlplay.common.entity.bird.penguin.PenguinEntity;

@Mixin(value = BirdEntity.class, remap = false)
public class BirdEntityMixin {

    // Redirects birds to use the ambient mob category instead of two new ones that fowlplay adds

    @Inject(method = "shouldBeAmbient", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$shouldBeAmbient(CallbackInfoReturnable<Boolean> cir) {
        if ((BirdEntity) (Object) this instanceof PenguinEntity) {
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(true);
        }
    }
}
