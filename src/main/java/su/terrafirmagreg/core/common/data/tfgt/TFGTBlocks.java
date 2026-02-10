package su.terrafirmagreg.core.common.data.tfgt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.eerussianguy.firmalife.common.FLTags;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

    public static final BlockEntry<Block> HEAT_PIPE_CASING = createCasingBlock("casings/heat_pipe_casing",
            p -> p.isValidSpawn((state, level, pos, ent) -> false)
                    .sound(SoundType.COPPER).strength(5, 6)
                    .mapColor(MapColor.COLOR_BLACK));

    public static final BlockEntry<Block> BIOCULTURE_CASING = createCasingBlock("casings/machine_casing_bioculture",
            p -> p.isValidSpawn((state, level, pos, ent) -> false)
                    .sound(SoundType.COPPER).strength(5, 6)
                    .mapColor(MapColor.COLOR_RED));

    public static final BlockEntry<Block> BIOCULTURE_GLASS_CASING = TFGCore.REGISTRATE.block("casings/machine_casing_bioculture_glass", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false)
                    .sound(SoundType.GLASS).strength(5, 6)
                    .mapColor(MapColor.COLOR_ORANGE))
            .addLayer(() -> RenderType::translucent)
            .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_bioculture_glass/")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings, TFCTags.Blocks.MINEABLE_WITH_GLASS_SAW)
            .item(BlockItem::new).tag(TFGTags.Items.Casings).build()
            .register();

    public static final BlockEntry<ActiveBlock> BIOCULTURE_ROTOR_SECONDARY = createActiveCasingBlock("casings/bioculture_rotor_secondary",
            p -> p.strength(6, 5).sound(SoundType.COPPER).mapColor(MapColor.COLOR_LIGHT_GRAY));

    public static final BlockEntry<ActiveBlock> VACUUM_ENGINE_INTAKE = createActiveCasingBlock("casings/machine_casing_vacuum_engine_intake",
            p -> p.strength(6, 5).sound(SoundType.METAL).mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops());

    public static final BlockEntry<ActiveBlock> ULTRAVIOLET_CASING = createActiveCasingBlock("casings/machine_casing_ultraviolet",
            p -> p.strength(6, 5).sound(SoundType.GLASS).mapColor(MapColor.COLOR_LIGHT_GRAY));

    public static final BlockEntry<ActiveBlock> EGH_CASING = createActiveCasingBlock("casings/machine_casing_egh",
            p -> p.strength(6, 5).sound(SoundType.METAL).mapColor(MapColor.COLOR_LIGHT_GRAY));

    public static final BlockEntry<Block>[] TREATED_WOOD_GREENHOUSE_CASINGS = createGreenhouseCasings("treated_wood",
            List.of(FLTags.Blocks.ALL_TREATED_WOOD_GREENHOUSE, TFGTags.Blocks.TreatedWoodGreenhouseCasings, BlockTags.MINEABLE_WITH_AXE),
            List.of(TFGTags.Items.TreatedWoodGreenhouseCasings));

    public static final BlockEntry<Block>[] COPPER_GREENHOUSE_CASINGS = createGreenhouseCasings("copper",
            List.of(FLTags.Blocks.ALL_COPPER_GREENHOUSE, TFGTags.Blocks.CopperGreenhouseCasings, BlockTags.MINEABLE_WITH_PICKAXE),
            List.of(TFGTags.Items.CopperGreenhouseCasings));

    public static final BlockEntry<Block>[] IRON_GREENHOUSE_CASINGS = createGreenhouseCasings("iron",
            List.of(FLTags.Blocks.ALL_IRON_GREENHOUSE, TFGTags.Blocks.IronGreenhouseCasings, BlockTags.MINEABLE_WITH_PICKAXE),
            List.of(TFGTags.Items.IronGreenhouseCasings));

    public static final BlockEntry<Block>[] STAINLESS_GREENHOUSE_CASINGS = createGreenhouseCasings("stainless",
            List.of(FLTags.Blocks.STAINLESS_STEEL_GREENHOUSE, TFGTags.Blocks.StainlessSteelGreenhouseCasings, BlockTags.MINEABLE_WITH_PICKAXE),
            List.of(TFGTags.Items.StainlessSteelGreenhouseCasings));


    public static BlockEntry<ActiveBlock> createActiveCasingBlock(String name, UnaryOperator<BlockBehaviour.Properties> properties) {
        return TFGCore.REGISTRATE.block("casings/machine_casing_vacuum_engine_intake", ActiveBlock::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties::apply)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(GTModels.createActiveModel(TFGCore.id("block/" + name)))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings)
                .item(BlockItem::new).tag(TFGTags.Items.Casings)
                .model((ctx, prov) -> prov.withExistingParent(prov.name(ctx), TFGCore.id("block/" + name)))
                .build()
                .register();
    }

    public static BlockEntry<Block> createCasingBlock(String name, UnaryOperator<BlockBehaviour.Properties> properties) {
        return TFGCore.REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties::apply)
                .addLayer(() -> RenderType::solid)
                .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/casings/" + name)))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings)
                .item(BlockItem::new).tag(TFGTags.Items.Casings)
                .build()
                .register();
    }

    public static BlockEntry<Block> createCasingBlock(String name) {
        return createCasingBlock(name,
                p -> p.isValidSpawn((state, level, pos, ent) -> false)
                .sound(SoundType.COPPER).strength(5, 6)
                .mapColor(MapColor.COLOR_LIGHT_GRAY));
    }

    @SuppressWarnings("unchecked")
    public static BlockEntry<Block>[] createGreenhouseCasings(String tier, List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        List<BlockEntry<Block>> casings = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            var blockBuilder = TFGCore.REGISTRATE.block("casings/greenhouse/%s_greenhouse_casing_%s".formatted(tier, i), Block::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.strength(0.3f, 0.3f).requiresCorrectToolForDrops().sound(SoundType.GLASS))
                    .tag(TFGTags.Blocks.Casings, TFCTags.Blocks.MINEABLE_WITH_GLASS_SAW, FLTags.Blocks.GREENHOUSE, FLTags.Blocks.GREENHOUSE_FULL_WALLS)
                    .addLayer(i > 2 ? () -> RenderType::translucent : () -> RenderType::cutout);
            blockTags.forEach(blockBuilder::tag);

            var blockItemBuilder = blockBuilder.item(BlockItem::new);
            blockItemBuilder.tag(TFGTags.Items.GreenhouseCasings, TFGTags.Items.Casings);
            itemTags.forEach(blockItemBuilder::tag);
            blockItemBuilder.build();
            casings.add(blockBuilder.register());
        }
        return (BlockEntry<Block>[])casings.toArray(BlockEntry[]::new);
    }
}
