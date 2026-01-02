package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.monster.Blaze;

@Mixin(value = Blaze.class)
public class BlazeMixin {

    // Change blaze's stupid loud annoying breathing noise

    @Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
    protected void tfg$getAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SoundEvents.FIRE_AMBIENT);
    }

    // Acid rain still counts as rain apparently

    @Inject(method = "isSensitiveToWater", at = @At("HEAD"), cancellable = true)
    public void tfg$isSensitiveToWater(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
