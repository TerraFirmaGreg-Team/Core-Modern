package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.entities.livestock.TFCAnimal;

@Mixin(value = TFCAnimal.class, remap = false)
public class TFCAnimalBrainMixin {

    /**
     * Throttle the TFC animal brain tick to once every 3 ticks.
     * The actual movement physics run independently in Minecraft aiStep/moveControl and are unaffected.
     * Interactive behaviors (temptation, following) should still be fast enough at max 3 ticks delay.
     */
    @Inject(method = "customServerAiStep", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$throttleBrainTick(CallbackInfo ci) {
        TFCAnimal self = (TFCAnimal) (Object) this;
        if ((self.tickCount + self.getId()) % 3 != 0) {
            ci.cancel();
        }
    }
}
