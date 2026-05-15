package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.layer.TFCLayers;

import su.terrafirmagreg.core.world.new_ow_wg.TFGLayers;
import su.terrafirmagreg.core.world.new_ow_wg.TfgClientPreviewState;

@Mixin(value = TFCLayers.class, remap = false)
public class TFCLayersMixin {

    /**
     * Exists to ensure that biome layers are initialized at the right time
     */
    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void tfg$injected(CallbackInfo ci) {
        TFGLayers.init();
    }

    /**
     * Preview and tools resolve {@code TFCLayers.getFromLayerId}(int) to a {@link BiomeExtension}. TFG
     * overworld uses ids from the extended {@link TFGLayers} table; vanilla TFC's registration list is
     * shorter, so high ids miss and TFCGenViewer visualization can NPE.
     */
    @Inject(method = "getFromLayerId", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$delegateLayerLookupToTfgTable(int id, CallbackInfoReturnable<BiomeExtension> cir) {
        if (TfgClientPreviewState.useTfgOverworldPipeline()) {
            cir.setReturnValue(TFGLayers.getFromLayerId(id));
        }
    }
}
