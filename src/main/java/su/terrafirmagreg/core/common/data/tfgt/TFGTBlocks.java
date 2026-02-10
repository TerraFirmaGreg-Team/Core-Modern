package su.terrafirmagreg.core.common.data.tfgt;

import java.util.function.Supplier;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

public class TFGTBlocks {

    public static void init() {
    }

    public static final BlockEntry<Block> CLEAN_STAINLESS_STEEL_DESH_CASING = createCasingBlock("casings/casing_clean_stainless_steel_desh");
    public static final BlockEntry<Block> DESH_PTFE_CASING = createCasingBlock("casings/machine_casing_desh_ptfe");
    public static final BlockEntry<Block> IRON_DESH_CASING = createCasingBlock("casings/machine_casing_iron_desh");
    public static final BlockEntry<Block> PTFE_DESH_CASING = createCasingBlock("casings/machine_casing_ptfe_desh");
    public static final BlockEntry<Block> STAINLESS_STEEL_DESH_CASING = createCasingBlock("casings/machine_casing_stainless_steel_desh");
    public static final BlockEntry<Block> BLUE_SOLAR_PANEL_CASING = createCasingBlock("casings/machine_casing_blue_solar_panel");
    public static final BlockEntry<Block> GREEN_SOLAR_PANEL_CASING = createCasingBlock("casings/machine_casing_green_solar_panel");
    public static final BlockEntry<Block> RED_SOLAR_PANEL_CASING = createCasingBlock("casings/machine_casing_red_solar_panel");
    public static final BlockEntry<Block> MARS_CASING = createCasingBlock("casings/machine_casing_mars");
    public static final BlockEntry<Block> OSTRUM_CARBON_CASING = createCasingBlock("casings/machine_casing_ostrum_carbon");
    public static final BlockEntry<Block> STAINLESS_EVAPORATION_CASING = createCasingBlock("casings/machine_casing_stainless_evaporation");

    public static final BlockEntry<ActiveBlock> VACUUM_ENGINE_INTAKE = TFGCore.REGISTRATE.block("tfg:casings/machine_casing_vacuum_engine_intake", ActiveBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.strength(6, 5).sound(SoundType.METAL).mapColor(MapColor.COLOR_LIGHT_GRAY))
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate(GTModels.createActiveModel(TFGCore.id("block/casings/machine_casing_vacuum_engine_intake")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings)
            .item(BlockItem::new).tag(TFGTags.Items.Casings)
            .model((ctx, prov) -> prov.withExistingParent(prov.name(ctx), TFGCore.id("block/casings/machine_casing_vacuum_engine_intake")))
            .build()
            .register();

    public static BlockEntry<Block> createCasingBlock(String name,
            Supplier<Supplier<RenderType>> type) {
        return TFGCore.REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false)
                        .sound(SoundType.COPPER).strength(5, 6)
                        .mapColor(MapColor.COLOR_LIGHT_GRAY))
                .addLayer(type)
                .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/casings/" + name)))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings)
                .item(BlockItem::new).tag(TFGTags.Items.Casings)
                .build()
                .register();
    }

    public static BlockEntry<Block> createCasingBlock(String name) {
        return createCasingBlock(name, () -> RenderType::solid);
    }
}
