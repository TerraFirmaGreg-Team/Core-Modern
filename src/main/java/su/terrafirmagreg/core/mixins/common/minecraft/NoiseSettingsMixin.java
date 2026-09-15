package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.levelgen.NoiseSettings;

import su.terrafirmagreg.core.world.WorldgenData;

@Mixin(value = NoiseSettings.class, remap = true)
public class NoiseSettingsMixin {

    @Shadow
    @Final
    protected static NoiseSettings OVERWORLD_NOISE_SETTINGS;

    @Inject(method = "height", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$height(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == OVERWORLD_NOISE_SETTINGS) {
            cir.setReturnValue(WorldgenData.MOUNTAIN_SCALING.dimHeight());
        }
    }
}
