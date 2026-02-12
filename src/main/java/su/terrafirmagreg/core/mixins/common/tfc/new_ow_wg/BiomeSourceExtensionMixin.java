package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.biome.TFCBiomes;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;

@Mixin(value = BiomeSourceExtension.class, remap = false)
public interface BiomeSourceExtensionMixin {

    @Inject(method = "getBiomeExtension", at = @At("RETURN"), cancellable = true)
    private void tfg$getBiomeExtension(int quartX, int quartZ, CallbackInfoReturnable<BiomeExtension> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            var biome = cir.getReturnValue();
            if (biome == TFCBiomes.RIVER) {
                cir.setReturnValue(TFGBiomes.RIVER);
            }
        }
    }
}
