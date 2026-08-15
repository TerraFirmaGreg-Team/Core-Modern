package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.dries007.tfc.util.climate.OverworldClimateModel;

@Mixin(value = OverworldClimateModel.class, remap = false)
public interface OverworldClimateModelAccessor {

    @Accessor("temperatureScale")
    float tfg$temperatureScale();
}
