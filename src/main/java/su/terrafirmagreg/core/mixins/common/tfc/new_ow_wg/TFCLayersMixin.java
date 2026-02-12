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

    //    @Inject(method = "hasLake", at = @At("HEAD"), remap = false, cancellable = true)
    //    private static void tfg$hasLake(int value, CallbackInfoReturnable<Boolean> cir) {
    //        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
    //            cir.setReturnValue(!TFGLayers.isOcean(value) && value != TFGLayers.BADLANDS);
    //            // TODO: change to the 1.21 method
    //        }
    //    }
    //
    //    @Inject(method = "lakeFor", at = @At("HEAD"), remap = false, cancellable = true)
    //    private static void tfg$lakeFor(int value, CallbackInfoReturnable<Integer> cir) {
    //        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
    //            if (value == TFGLayers.MOUNTAINS) {
    //                cir.setReturnValue(TFGLayers.MOUNTAIN_LAKE);
    //            } else if (value == TFGLayers.VOLCANIC_MOUNTAINS) {
    //                cir.setReturnValue(TFGLayers.VOLCANIC_MOUNTAIN_LAKE);
    //            } else if (value == TFGLayers.OLD_MOUNTAINS) {
    //                cir.setReturnValue(TFGLayers.OLD_MOUNTAIN_LAKE);
    //            } else if (value == TFGLayers.OCEANIC_MOUNTAINS) {
    //                cir.setReturnValue(TFGLayers.OCEANIC_MOUNTAIN_LAKE);
    //            } else if (value == TFGLayers.VOLCANIC_OCEANIC_MOUNTAINS) {
    //                cir.setReturnValue(TFGLayers.VOLCANIC_OCEANIC_MOUNTAIN_LAKE);
    //            } else if (value == TFGLayers.PLATEAU) {
    //                cir.setReturnValue(TFGLayers.PLATEAU_LAKE);
    //            }
    //            // TODO: isFlatIceSheet
    //            // TODO: ICE_SHEET_EDGE
    //            else {
    //                cir.setReturnValue(TFGLayers.LAKE);
    //            }
    //        }
    //    }
}
