package su.terrafirmagreg.core.mixins.common.steampowered;

import org.spongepowered.asm.mixin.Mixin;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.teammoeg.steampowered.content.flywheel.CastIronSteamFlywheelBlock;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;

@Mixin(value = CastIronSteamFlywheelBlock.class, remap = false)
public class CastIronSteamFlywheelBlockMixin implements ITieredBlock {

    @Override
    public int getTier() {
        return 2;
    }

    @Override
    public void setTier(int i) {
    }

    @Override
    public Material getMaterial() {
        return GTMaterials.Aluminium;
    }
}
