package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.world.layer.TFCLayers;

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
}
