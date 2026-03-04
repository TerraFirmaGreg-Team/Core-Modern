package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import static net.dries007.tfc.util.climate.OverworldClimateModel.SEA_LEVEL;
import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT;
import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_VERSION;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.OverworldClimateModel;

import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGNoiseHelpers;

@Mixin(value = OverworldClimateModel.class, remap = false)
public class OverworldClimateModelMixin {

    @Shadow
    private float temperatureScale;

    @Inject(method = "adjustTemperatureByElevation", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$adjustTemperatureByElevation(int y, float averageTemperature, float monthTemperature, float dailyTemperature, CallbackInfoReturnable<Float> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            if (y > SEA_LEVEL) {
                // -1.6 C / 10 blocks above sea level
                final float averageElevationTemperature = TFGNoiseHelpers.adjustAverageTemperatureByElevation(y, averageTemperature, SEA_LEVEL);
                cir.setReturnValue(averageElevationTemperature + monthTemperature + dailyTemperature);
            }
        }
    }

    @Inject(method = "calculateMonthlyTemperature", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$calculateMonthlyTemperature(int z, float monthTemperatureModifier, CallbackInfoReturnable<Float> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            cir.setReturnValue(monthTemperatureModifier * (temperatureScale == 0 ? 0 : Helpers.triangle(-18f, 0f, 1f / (4f * temperatureScale), z - temperatureScale / 2)));
        }
    }
}
