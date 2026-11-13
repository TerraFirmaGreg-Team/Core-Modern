package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.levelgen.NoiseSettings;

@Mixin(value = NoiseSettings.class)
public abstract class NoiseSettingsMixin {

    // Mixin to use a cell size of 1x1x1 as the minimum for density functions instead of 4x4x4.
    // Set the size_horizontal and size_vertical on noise settings to 4 (16x16x16),
    // and this mixin will override that to 1x1x1.
    // Intended for mercury only - smaller cells means more noise sampling which is slow!

    @Shadow
    public abstract int noiseSizeVertical();

    @Shadow
    public abstract int noiseSizeHorizontal();

    @Inject(method = "getCellHeight", at = @At("HEAD"), cancellable = true)
    private void tfg$getCellHeight(CallbackInfoReturnable<Integer> cir) {
        if (noiseSizeVertical() == 4) {
            cir.setReturnValue(1);
        }
    }

    @Inject(method = "getCellWidth", at = @At("HEAD"), cancellable = true)
    private void tfg$getCellWidth(CallbackInfoReturnable<Integer> cir) {
        if (noiseSizeHorizontal() == 4) {
            cir.setReturnValue(1);
        }
    }
}
