package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.negodya1.vintageimprovements.content.kinetics.vacuum_chamber.VacuumChamberBlock;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;

@Mixin(value = VacuumChamberBlock.class, remap = false)
public class VacuumChamberBlockMixin implements ITieredBlock {
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
