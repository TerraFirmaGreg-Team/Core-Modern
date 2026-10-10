package su.terrafirmagreg.core.mixins.common.opposing_force;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unusualmodding.opposing_force.entity.utils.EliteVariant;

@Mixin(value = EliteVariant.class, remap = false)
public interface EliteVariantMixin {

    @Inject(method = "getEliteSpawnChance", at = @At("HEAD"), remap = false, cancellable = true)
    default void tfg$getEliteSpawnChance(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(20);
    }
}
