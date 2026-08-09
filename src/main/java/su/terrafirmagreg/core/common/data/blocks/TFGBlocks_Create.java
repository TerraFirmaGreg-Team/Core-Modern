package su.terrafirmagreg.core.common.data.blocks;

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

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.create.DieselEngineBlock;
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

    public static final BlockEntry<DieselEngineBlock> DIESEL_ENGINE = TFGCore.REGISTRATE.block("generators/diesel_engine", p -> new DieselEngineBlock(p, GTMaterials.Aluminium))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
            .transform(TagGen.pickaxeOnly())
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry())
                    .forAllStates(bs -> ConfiguredModel.builder()
                            .modelFile(AssetLookup.partialBaseModel(c, p, bs.getValue(DieselEngineBlock.FACING).getAxis().isVertical() ? "vertical" : ""))
                            .rotationY(bs.getValue(DieselEngineBlock.FACING).getAxis().isVertical() ? (bs.getValue(DieselEngineBlock.FACING) == Direction.UP ? 90 : 180)
                                    : (int) bs.getValue(DieselEngineBlock.FACING).toYRot())
                            .build()))
            .item()
            .model((c, p) -> p.blockItem(c, "/item"))
            .build()
            .register();
}
