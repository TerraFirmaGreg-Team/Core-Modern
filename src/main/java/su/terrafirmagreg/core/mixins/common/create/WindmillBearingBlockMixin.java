package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlock;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;

@Mixin(value = WindmillBearingBlock.class, remap = false)
public class WindmillBearingBlockMixin implements ITieredBlock {

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public void setTier(int i) {
    }
}
