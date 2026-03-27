package su.terrafirmagreg.core.mixins.common.tfc;

import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT;
import static su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData.OVERWORLD_VERSION;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.OverworldClimateModel;
import net.minecraft.util.Mth;

@Mixin(value = OverworldClimateModel.class, remap = false)
public abstract class OverworldClimateModelMixin {

    @Shadow
    private float temperatureScale;

    @Shadow
    protected abstract float calculateMonthlyTemperature(int z, float monthTemperatureModifier);

    @Shadow
    protected abstract float adjustTemperatureByElevation(int y, float averageTemperature, float monthTemperature, float dailyTemperature);

    @Inject(method = "getAverageMonthlyTemperature", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getAverageMonthlyTemperature(int z, int y, float averageTemperature, float monthFactor, CallbackInfoReturnable<Float> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            if (!tfg$getInNorthernHemisphere(z, temperatureScale)) {
                monthFactor = -monthFactor;
            }
            final float monthlyTemperature = calculateMonthlyTemperature(z, monthFactor);
            cir.setReturnValue(adjustTemperatureByElevation(y, averageTemperature, monthlyTemperature, 0));
        }
    }

    @Inject(method = "calculateMonthlyTemperature", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$calculateMonthlyTemperature(int z, float monthTemperatureModifier, CallbackInfoReturnable<Float> cir) {
        if (OVERWORLD_VERSION == OVERWORLD_TFC_1_21_BACKPORT) {
            cir.setReturnValue(monthTemperatureModifier * (temperatureScale == 0 ? 0 : Helpers.triangle(-18f, 0f, 1f / (4f * temperatureScale), z - temperatureScale / 2)));
        }
    }

    /**
     * Return true if the position is in a Northern Hemisphere, false if Southern
     * (Originally from TFC 1.21's SolarCalculator class)
     */
    @Unique
    private static boolean tfg$getInNorthernHemisphere(int z, float hemisphereScale) {
        if (hemisphereScale == 0) {
            return true;
        }
        final int adjustedZ = z - (int) (hemisphereScale / 2);
        final int poleToPoleDistance = (int) (hemisphereScale * 2);
        final int normalizedZ = Mth.positiveModulo(adjustedZ, (poleToPoleDistance * 2));
        return normalizedZ > poleToPoleDistance;
    }
}
