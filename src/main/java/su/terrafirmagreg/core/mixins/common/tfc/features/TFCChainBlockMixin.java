package su.terrafirmagreg.core.mixins.common.tfc.features;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.blocks.TFCChainBlock;
import net.dries007.tfc.common.fluids.FluidProperty;

import su.terrafirmagreg.core.common.data.TFGBlockProperties;

@Mixin(value = TFCChainBlock.class, remap = false)
public class TFCChainBlockMixin {

    // Lets chains be waterlogged with mars water.
    // Who's going to do that? Nobody, but TFCChainBlock and RockSpikeBlock both use
    // TFCBlocks.lavaLoggedBlockEmission, so if we want rock spikes to be mars-waterloggable,
    // chains need to be as well.

    @Inject(method = "getFluidProperty", at = @At("HEAD"), remap = false, cancellable = true)
    public void tfg$getFluidProperty(CallbackInfoReturnable<FluidProperty> cir) {
        cir.setReturnValue(TFGBlockProperties.SPACE_WATER_AND_LAVA);
    }
}
