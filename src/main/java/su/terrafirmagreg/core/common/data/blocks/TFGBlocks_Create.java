package su.terrafirmagreg.core.common.data.blocks;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.teammoeg.steampowered.oldcreatestuff.OldFlywheelGenerator;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.core.Direction;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.model.generators.ConfiguredModel;

import electrolyte.greate.infrastructure.config.GStress;
import electrolyte.greate.registry.GreateMaterials;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.create.CombustionEngineBlock;
import su.terrafirmagreg.core.common.block.create.TitaniumSteamEngineBlock;
import su.terrafirmagreg.core.common.block.create.TitaniumSteamFlywheelBlock;

public class TFGBlocks_Create {
    public static void init() {
    }

    public static final BlockEntry<TitaniumSteamFlywheelBlock> TITANIUM_FLYWHEEL = TFGCore.REGISTRATE.block("titanium_flywheel", TitaniumSteamFlywheelBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(properties -> properties.noOcclusion().mapColor(MapColor.COLOR_PURPLE))
            .transform(TagGen.axeOrPickaxe())
            .blockstate(new OldFlywheelGenerator()::generate)
            .item()
            .transform(ModelGen.customItemModel())
            .register();

    public static final BlockEntry<TitaniumSteamEngineBlock> TITANIUM_STEAM_ENGINE = TFGCore.REGISTRATE.block("titanium_steam_engine", TitaniumSteamEngineBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .transform(TagGen.pickaxeOnly())
            .item()
            .transform(ModelGen.customItemModel())
            .register();

    public static BlockEntry<CombustionEngineBlock> STEEL_COMBUSTION_ENGINE = combustionEngine(1, "steel", GTMaterials.Steel),
            ALUMINIUM_COMBUSTION_ENGINE = combustionEngine(2, "aluminium", GTMaterials.Aluminium),
            STAINLESS_STEEL_COMBUSTION_ENGINE = combustionEngine(3, "stainless_steel", GTMaterials.StainlessSteel),
            TITANIUM_COMBUSTION_ENGINE = combustionEngine(4, "titanium", GTMaterials.Titanium);

    // Have to pass the material name as a separate parameter because the materials aren't initialised yet
    public static BlockEntry<CombustionEngineBlock> combustionEngine(int tier, String name, Material material) {
        return TFGCore.REGISTRATE.block("generators/" + name + "_combustion_engine", p -> new CombustionEngineBlock(p, material))
                .initialProperties(SharedProperties::softMetal)
                .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
                .transform(TagGen.pickaxeOnly())
                .transform(GStress.setCapacity(1))
                .onRegister(c -> c.setTier(tier))
                .blockstate((c, p) -> p.getVariantBuilder(c.getEntry())
                        .forAllStates(bs -> ConfiguredModel.builder()
                                .modelFile(AssetLookup.partialBaseModel(c, p, bs.getValue(CombustionEngineBlock.FACING).getAxis().isVertical() ? "vertical" : ""))
                                .rotationY(bs.getValue(CombustionEngineBlock.FACING).getAxis().isVertical() ? (bs.getValue(CombustionEngineBlock.FACING) == Direction.UP ? 90 : 180)
                                        : (int) bs.getValue(CombustionEngineBlock.FACING).toYRot())
                                .build()))
                .item()
                .model((c, p) -> p.blockItem(c, "/item"))
                .build()
                .register();
    }
}
