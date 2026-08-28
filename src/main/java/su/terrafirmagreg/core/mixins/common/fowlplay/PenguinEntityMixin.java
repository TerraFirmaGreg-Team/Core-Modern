package su.terrafirmagreg.core.mixins.common.fowlplay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.animal.Animal;

import aqario.fowlplay.common.entity.bird.penguin.PenguinEntity;

@Mixin(value = PenguinEntity.class, remap = false)
public class PenguinEntityMixin {
    /**
     * @author Pyritie
     * @reason Prevent vanilla-like breeding
     */
    @Inject(method = "canMate", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$canMate(Animal other, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
