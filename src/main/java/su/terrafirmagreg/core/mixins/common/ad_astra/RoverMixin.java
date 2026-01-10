package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import earth.terrarium.adastra.common.entities.vehicles.Rover;

@Mixin(value = Rover.class, remap = false)
public class RoverMixin {

    // Increases the rover's step height by a little, so it doesn't get stuck on mars sand piles as often

    @ModifyConstant(method = "<init>", constant = @Constant(floatValue = 1.0f, ordinal = 0), remap = false)
    public float tfg$increaseStepHeight(float constant) {
        return 1.2f;
    }
}
