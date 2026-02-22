package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;

@Mixin(value = BoilerHeaters.class, remap = false)
public class BoilerHeatersMixin {
    /**
     * @author Ujhik
     * @reason To remove passive heating from Blaze Burners to boilers
     */
    @ModifyReturnValue(method = "blazeBurner", at = @At("RETURN"))
    private static int noPassiveHeat(int original) {
        if (original == BoilerHeater.PASSIVE_HEAT)
            return BoilerHeater.NO_HEAT;
        return original;
    }
}
