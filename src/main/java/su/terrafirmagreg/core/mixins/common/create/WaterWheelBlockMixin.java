package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlock;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;

@Mixin(value = WaterWheelBlock.class, remap = false)
public class WaterWheelBlockMixin implements ITieredBlock {

    @Override
    public int getTier() {
        return 0;
    }

    @Override
    public void setTier(int i) {
    }
}
