package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.layer.TFCLayers;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.world.new_ow_wg.TFGLayers;

@Mixin(value = TFCLayers.class, remap = false)
public class TFCLayersMixin {

    /**
     * Exists to ensure that biome layers are initialized at the right time
     */
    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void tfg$injected(CallbackInfo ci) {
        TFGLayers.init();
    }

    @Inject(method = "isOcean", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$isOcean(int value, CallbackInfoReturnable<Boolean> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            cir.setReturnValue(value == TFGLayers.OCEAN || value == TFGLayers.DEEP_OCEAN || value == TFGLayers.DEEP_OCEAN_TRENCH || value == TFGLayers.OCEAN_REEF);
        }
    }

    @Inject(method = "isLow", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$isLow(int value, CallbackInfoReturnable<Boolean> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            cir.setReturnValue(value == TFGLayers.MUD_FLATS || value == TFGLayers.SALT_FLATS || value == TFGLayers.DUNE_SEA);
        }
    }
}
