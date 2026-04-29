package su.terrafirmagreg.core.mixins.client.tfcgenviewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TFCGenViewer's private {@code ClimateSpace#check} used strict inequalities; TFC's
 * {@link net.dries007.tfc.world.placement.ClimatePlacement#isValid} uses inclusive bounds.
 */
@Mixin(targets = "com.notenoughmail.tfcgenviewer.color.FeatureColors$ClimateSpace", remap = false)
public abstract class FeatureColorsClimateSpaceMixin {

    @Accessor("minTemp")
    abstract float tfg$minTemp();

    @Accessor("maxTemp")
    abstract float tfg$maxTemp();

    @Accessor("minRain")
    abstract float tfg$minRain();

    @Accessor("maxRain")
    abstract float tfg$maxRain();

    @Inject(method = "check", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$inclusiveClimateBounds(float temperature, float rainfall, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(
                temperature >= tfg$minTemp() && temperature <= tfg$maxTemp()
                        && rainfall >= tfg$minRain() && rainfall <= tfg$maxRain());
    }
}
