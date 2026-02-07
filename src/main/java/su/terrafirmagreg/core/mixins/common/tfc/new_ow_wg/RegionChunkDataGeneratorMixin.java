package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.chunkdata.RegionChunkDataGenerator;
import net.dries007.tfc.world.river.MidpointFractal;
import net.minecraft.util.Mth;

import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = RegionChunkDataGenerator.class, remap = false)
public class RegionChunkDataGeneratorMixin {

    @Final
    @Shadow
    private static float RIVER_INFLUENCE_SQ;

    @Inject(method = "adjustRiverRainfall", at = @At("HEAD"), cancellable = true)
    private void tfg$adjustRiverRainfall(float currentRainfall, float originalRainfall, float widthInfluence, MidpointFractal fractal, double gridX, double gridZ, CallbackInfoReturnable<Float> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            final float distance = (float) fractal.intersectDistance(gridX, gridZ);
            final float distanceInfluence = Mth.clampedMap(distance, 0f, RIVER_INFLUENCE_SQ, 1f, 0f);
            final float targetValue = Math.min(originalRainfall + 300f, 500f);

            // Take the max of any influence with adjacent rivers
            cir.setReturnValue(Math.max(originalRainfall, Mth.lerp(distanceInfluence * widthInfluence, originalRainfall, targetValue)));
        }
    }
}
