package su.terrafirmagreg.core.mixins.common.tfc.features;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.blocks.rock.RockSpikeBlock;
import net.dries007.tfc.common.fluids.FluidProperty;

import su.terrafirmagreg.core.common.data.TFGBlockProperties;

@Mixin(value = RockSpikeBlock.class, remap = false)
public class RockSpikeBlockMixin {

    @Inject(method = "getFluidProperty", at = @At("HEAD"), remap = false, cancellable = true)
    public void tfg$getFluidProperty(CallbackInfoReturnable<FluidProperty> cir) {
        cir.setReturnValue(TFGBlockProperties.SPACE_WATER_AND_LAVA);
    }
}
