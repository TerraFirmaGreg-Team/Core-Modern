package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlock;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;

@Mixin(value = LargeWaterWheelBlock.class, remap = false)
public class LargeWaterWheelBlockMixin implements ITieredBlock {

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public void setTier(int i) {
    }
}
