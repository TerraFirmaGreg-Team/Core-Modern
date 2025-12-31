package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
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

    @Override
    public Material getMaterial() {
        return GTMaterials.Steel;
    }
}
