package su.terrafirmagreg.core.mixins.common.create_addition;

import org.spongepowered.asm.mixin.Mixin;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillBlock;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;

@Mixin(value = RollingMillBlock.class, remap = false)
public abstract class RollingMillBlockMixin implements ITieredBlock {
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
