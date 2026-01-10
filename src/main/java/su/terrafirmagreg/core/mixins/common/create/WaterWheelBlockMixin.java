package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlock;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;
import electrolyte.greate.registry.GreateMaterials;

@Mixin(value = WaterWheelBlock.class, remap = false)
public class WaterWheelBlockMixin implements ITieredBlock {

    @Override
    public int getTier() {
        return 0;
    }

    @Override
    public void setTier(int i) {
    }

    @Override
    public Material getMaterial() {
        return GreateMaterials.AndesiteAlloy;
    }
}
