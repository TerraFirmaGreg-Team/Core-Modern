package su.terrafirmagreg.core.mixins.common.steampowered;

import org.spongepowered.asm.mixin.Mixin;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.teammoeg.steampowered.content.flywheel.SteelSteamFlywheelBlock;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;

@Mixin(value = SteelSteamFlywheelBlock.class, remap = false)
public class SteelSteamFlywheelBlockMixin implements ITieredBlock {

    @Override
    public int getTier() {
        return 3;
    }

    @Override
    public void setTier(int i) {
    }

    @Override
    public Material getMaterial() {
        return GTMaterials.StainlessSteel;
    }
}
