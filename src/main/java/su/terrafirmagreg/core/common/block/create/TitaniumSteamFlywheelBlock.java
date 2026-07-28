package su.terrafirmagreg.core.common.block.create;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.teammoeg.steampowered.content.flywheel.SteamFlywheelBlock;
import com.teammoeg.steampowered.content.flywheel.SteamFlywheelTileEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;

import electrolyte.greate.content.kinetics.simpleRelays.ITieredBlock;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class TitaniumSteamFlywheelBlock extends SteamFlywheelBlock implements ITieredBlock {

    public TitaniumSteamFlywheelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends SteamFlywheelTileEntity> getBlockEntityType() {
        return TFGBlockEntities.TITANIUM_STEAM_FLYWHEEL.get();
    }

    @Override
    public int getTier() {
        return 4;
    }

    @Override
    public void setTier(int i) {
    }

    @Override
    public Material getMaterial() {
        return GTMaterials.Titanium;
    }
}
