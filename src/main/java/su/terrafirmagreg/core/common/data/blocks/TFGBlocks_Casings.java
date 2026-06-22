package su.terrafirmagreg.core.common.data.blocks;

import java.util.ArrayList;
import java.util.List;

import com.eerussianguy.firmalife.common.FLTags;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.simibubi.create.content.decoration.palettes.ConnectedGlassBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.ActiveCardinalBlock;
import su.terrafirmagreg.core.common.block.ActiveParticleBlock;
import su.terrafirmagreg.core.common.block.ElectromagneticAcceleratorBlock;
import su.terrafirmagreg.core.common.block.ReflectorBlock;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.compat.kjs.GTActiveParticleBuilder;
import su.terrafirmagreg.core.utils.ModelUtils;

@SuppressWarnings("unused")
public class TFGBlocks_Casings {
    public static void init() {
    }

    //// Casings

    public static final BlockEntry<ActiveParticleBlock> BIOCULTURE_ROTOR_PRIMARY = TFGCore.REGISTRATE
            .block("casings/bioculture_rotor_primary", p -> new ActiveParticleBlock(p.sound(SoundType.COPPER).strength(5f, 6f).mapColor(MapColor.COLOR_LIGHT_GRAY),
                    ActiveParticleBlock.DEFAULT_SHAPE,
                    null,
                    null,
                    List.of(new GTActiveParticleBuilder.ParticleSetBuilder()
                            .particle("minecraft:landing_lava")
                            .range(1.6, 2, 1.6)
                            .velocity(0, 0, 0)
                            .count(10)
                            .forced(false)
                            .build())))
            .blockstate(ModelUtils.existingActiveParticleModel(TFGCore.id("block/casings/bioculture_rotor_primary")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new).model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/casings/bioculture_rotor_primary"))).build().register();

    public static final BlockEntry<ActiveParticleBlock> EGH_PLANTER = TFGCore.REGISTRATE
            .block("egh_planter", p -> new ActiveParticleBlock(p.sound(SoundType.COPPER).strength(5f, 6f).mapColor(MapColor.GRASS).noOcclusion(),
                    Block.box(0, 12, 0, 16, 16, 16),
                    null,
                    null,
                    List.of(new GTActiveParticleBuilder.ParticleSetBuilder()
                            .particle("minecraft:dripping_water")
                            .range(0.2, 0.0, 0.2)
                            .velocity(0, 0, 0)
                            .position(0.5, -0.1, 0.5)
                            .count(1)
                            .forced(false)
                            .build()),
                    true, 200, 0, 12))
            .blockstate((ctx, prov) -> ModelUtils.activeCardinalBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().getExistingFile(TFGCore.id("block/machines/egh_planter/egh_planter")),
                    prov.models().getExistingFile(TFGCore.id("block/machines/egh_planter/egh_planter_active"))))
            .addLayer(() -> RenderType::cutout)
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFCTags.Blocks.FARMLAND, TFCTags.Blocks.TREE_GROWS_ON,
                    TFCTags.Blocks.BUSH_PLANTABLE_ON, TFCTags.Blocks.WILD_CROP_GROWS_ON, TFCTags.Blocks.SPREADING_FRUIT_GROWS_ON,
                    TFCTags.Blocks.CREEPING_PLANTABLE_ON, TFCTags.Blocks.GRASS_PLANTABLE_ON, BlockTags.MUSHROOM_GROW_BLOCK, BlockTags.BAMBOO_PLANTABLE_ON)
            .item(BlockItem::new).model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/machines/egh_planter/egh_planter"))).build().register();

    public static final BlockEntry<ActiveParticleBlock> GROW_LIGHT = TFGCore.REGISTRATE
            .block("grow_light", p -> new ActiveParticleBlock(p.sound(SoundType.COPPER).strength(5f, 6f).mapColor(MapColor.GRASS).noOcclusion(),
                    Block.box(0, 12, 0, 16, 16, 16),
                    null,
                    null,
                    List.of(new GTActiveParticleBuilder.ParticleSetBuilder()
                            .particle("minecraft:dripping_water")
                            .range(0.2, 0.0, 0.2)
                            .velocity(0, 0, 0)
                            .position(0.5, -0.1, 0.5)
                            .count(1)
                            .forced(false)
                            .build()),
                    true, 200, 0, 12))
            .blockstate(ModelUtils.existingActiveParticleModel(TFGCore.id("block/machines/egh_planter/grow_light")))
            .addLayer(() -> RenderType::cutout)
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/machines/egh_planter/grow_light"))).build().register();

    public static final BlockEntry<ActiveParticleBlock> PISCICULTURE_CORE = TFGCore.REGISTRATE
            .block("pisciculture_core", p -> new ActiveParticleBlock(p.sound(SoundType.COPPER).strength(5f, 6f).mapColor(MapColor.GRASS).noOcclusion(),
                    Block.box(0, 12, 0, 16, 16, 16),
                    null,
                    null,
                    List.of(new GTActiveParticleBuilder.ParticleSetBuilder()
                            .particle("tfg:fish_school")
                            .position(0.5, 1.5, 0.5)
                            .range(0.0, 2.0, 0.0)
                            .velocity(0.0, 0.0, 0.0)
                            .count(5)
                            .forced(false)
                            .build(),
                            new GTActiveParticleBuilder.ParticleSetBuilder()
                                    .particle("minecraft:current_down")
                                    .position(0.0, 3.8, 0.0)
                                    .range(5.0, 0.0, 5.0)
                                    .velocity(0.0, 0.1, 0.0)
                                    .count(5)
                                    .forced(false)
                                    .build(),
                            new GTActiveParticleBuilder.ParticleSetBuilder()
                                    .particle("minecraft:current_down")
                                    .position(0.0, 3.8, 0.0)
                                    .range(0.5, 0.0, 0.5)
                                    .velocity(0.0, 0.1, 0.0)
                                    .count(5)
                                    .forced(false)
                                    .build()),
                    true, 20, 0, 12))
            .blockstate(ModelUtils.existingActiveParticleModel(TFGCore.id("block/casings/pisciculture_core")))
            .addLayer(() -> RenderType::cutout)
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new).model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/casings/pisciculture_core"))).build().register();

    public static final BlockEntry<Block> CLEAN_STAINLESS_STEEL_DESH_CASING = createCasingBlock("casings/machine_casing_clean_stainless_steel_desh",
            GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_clean_stainless_steel_desh")));

    public static final BlockEntry<Block> DESH_PTFE_CASING = createCasingBlock("casings/machine_casing_desh_ptfe", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_desh_ptfe")));

    public static final BlockEntry<Block> IRON_DESH_CASING = createCasingBlock("casings/machine_casing_iron_desh", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_iron_desh")));

    public static final BlockEntry<Block> PTFE_DESH_CASING = createCasingBlock("casings/machine_casing_ptfe_desh", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_ptfe_desh")));

    public static final BlockEntry<Block> STAINLESS_STEEL_DESH_CASING = createCasingBlock("casings/machine_casing_stainless_steel_desh",
            GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_stainless_steel_desh")));

    public static final BlockEntry<Block> MARS_CASING = createCasingBlock("casings/machine_casing_mars", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_mars")));

    public static final BlockEntry<Block> OSTRUM_CARBON_CASING = createCasingBlock("casings/machine_casing_ostrum_carbon",
            GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_ostrum_carbon")));

    public static final BlockEntry<Block> STAINLESS_EVAPORATION_CASING = createCasingBlock("casings/machine_casing_stainless_evaporation",
            GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_stainless_evaporation")));

    public static final BlockEntry<Block> PRESSURE_CASING = createCasingBlock("casings/machine_casing_pressure",
            GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_pressure")));

    public static final BlockEntry<Block> BLUE_SOLAR_PANEL_CASING = createCasingBlock("casings/machine_casing_blue_solar_panel",
            (ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    GTCEu.id("block/casings/steam/steel/side"),
                    GTCEu.id("block/casings/steam/steel/bottom"),
                    TFGCore.id("block/casings/machine_casing_blue_solar_panel"))));

    public static final BlockEntry<Block> GREEN_SOLAR_PANEL_CASING = createCasingBlock("casings/machine_casing_green_solar_panel",
            (ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    GTCEu.id("block/casings/steam/steel/side"),
                    GTCEu.id("block/casings/steam/steel/bottom"),
                    TFGCore.id("block/casings/machine_casing_green_solar_panel"))));

    public static final BlockEntry<Block> RED_SOLAR_PANEL_CASING = createCasingBlock("casings/machine_casing_red_solar_panel",
            (ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    GTCEu.id("block/casings/steam/steel/side"),
                    GTCEu.id("block/casings/steam/steel/bottom"),
                    TFGCore.id("block/casings/machine_casing_red_solar_panel"))));

    public static final BlockEntry<ElectromagneticAcceleratorBlock> ELECTROMAGNETIC_ACCELERATOR_BLOCK = TFGCore.REGISTRATE.block("electromagnetic_accelerator", ElectromagneticAcceleratorBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(5.5f)
                    .sound(SoundType.COPPER)
                    .lightLevel(state -> 15)
                    .speedFactor(1.5f))
            .addLayer(() -> RenderType::solid)
            .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/casings/electromagnetic_accelerator")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings)
            .item(BlockItem::new).tag(TFGTags.Items.Casings)
            .build()
            .register();

    public static final BlockEntry<Block> TEST_CASING = TFGCore.REGISTRATE.block("casings/test_casing", Block::new)
            .properties(p -> p.sound(SoundType.COPPER).strength(5f, 6f))
            .tag(TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath("c", "hidden_from_recipe_viewers")))
            .defaultBlockstate()
            .item(BlockItem::new).tag(TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath("c", "hidden_from_recipe_viewers"))).build()
            .register();

    public static final BlockEntry<Block> SUPERCONDUCTOR_COIL_LARGE_BLOCK = createCasingBlock("superconductor_coil_large", GTModels.cubeAllModel(TFGCore.id("block/casings/superconductor_coil_large")),
            SoundType.COPPER, 5.5f, 5.5f, MapColor.COLOR_ORANGE, false);

    public static final BlockEntry<Block> SUPERCONDUCTOR_COIL_SMALL_BLOCK = createCasingBlock("superconductor_coil_small", GTModels.cubeAllModel(TFGCore.id("block/casings/superconductor_coil_small")),
            SoundType.COPPER, 5.5f, 5.5f, MapColor.COLOR_ORANGE, false);

    public static final BlockEntry<ReflectorBlock> REFLECTOR_BLOCK = TFGCore.REGISTRATE.block("reflector", ReflectorBlock::new)
            .properties(p -> p.mapColor(MapColor.SNOW).strength(5.5F).sound(SoundType.AMETHYST)
                    .noOcclusion().isViewBlocking((state, level, pos) -> false))
            .item(BlockItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/reflector_night"))).build()
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .register();

    public static final BlockEntry<Block> MACHINE_CASING_ALUMINIUM_PLATED_STEEL = createCasingBlock(
            "machine_casing_aluminium_plated_steel", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_aluminium_plated_steel")),
            SoundType.COPPER, 5.5f, 5.5f, MapColor.COLOR_LIGHT_BLUE, false);

    public static final BlockEntry<Block> MACHINE_CASING_POWER_CASING = createCasingBlock(
            "machine_casing_power_casing", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_power_casing")),
            SoundType.COPPER, 5.5f, 5.5f, MapColor.COLOR_LIGHT_BLUE, false);

    public static final BlockEntry<Block> HEAT_PIPE_CASING = createCasingBlock("casings/heat_pipe_casing", GTModels.cubeAllModel(TFGCore.id("block/casings/heat_pipe_casing")),
            SoundType.COPPER, 5.5f, 6f, MapColor.COLOR_BLACK, false);

    public static final BlockEntry<Block> BIOCULTURE_CASING = createCasingBlock("casings/machine_casing_bioculture", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_bioculture")),
            SoundType.COPPER, 5.5f, 6f, MapColor.COLOR_RED, false);

    public static final BlockEntry<ConnectedGlassBlock> BIOCULTURE_GLASS_CASING = TFGCore.REGISTRATE.block("casings/machine_casing_bioculture_glass", ConnectedGlassBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false)
                    .sound(SoundType.GLASS).strength(5, 6).noOcclusion()
                    .mapColor(MapColor.COLOR_ORANGE))
            .addLayer(() -> RenderType::translucent)
            .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_bioculture_glass")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings, TFCTags.Blocks.MINEABLE_WITH_GLASS_SAW)
            .item(BlockItem::new).tag(TFGTags.Items.Casings).build()
            .register();

    public static final BlockEntry<ActiveBlock> BIOCULTURE_ROTOR_SECONDARY = TFGCore.REGISTRATE.block("casings/bioculture_rotor_secondary", ActiveBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.sound(SoundType.COPPER).strength(6f, 5f).mapColor(MapColor.COLOR_LIGHT_GRAY).isValidSpawn((s, l, ps, e) -> false))
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate(ModelUtils.existingActiveModel(TFGCore.id("block/casings/bioculture_rotor_secondary")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings)
            .item(BlockItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/casings/bioculture_rotor_secondary")))
            .tag(TFGTags.Items.Casings)
            .build().register();

    public static final BlockEntry<ActiveBlock> VACUUM_ENGINE_INTAKE = createActiveCasingBlock("casings/machine_casing_vacuum_engine_intake",
            ModelUtils.createActiveModel(TFGCore.id("block/casings/machine_casing_vacuum_engine_intake")),
            SoundType.METAL, 6, 5, MapColor.COLOR_LIGHT_GRAY, true);

    public static final BlockEntry<ActiveBlock> ULTRAVIOLET_CASING = createActiveCasingBlock("casings/machine_casing_ultraviolet",
            ModelUtils.createActiveCasingModel(TFGCore.id("block/casings/machine_casing_ultraviolet")),
            SoundType.GLASS, 6, 5, MapColor.COLOR_LIGHT_GRAY, false);

    public static final BlockEntry<ActiveBlock> EGH_CASING = createActiveCasingBlock("casings/machine_casing_egh",
            ModelUtils.createActiveCasingModel(TFGCore.id("block/casings/machine_casing_egh")),
            SoundType.METAL, 6, 5, MapColor.COLOR_LIGHT_GRAY, false);

    public static final BlockEntry<ActiveCardinalBlock> STERILIZING_PIPE_CASING = TFGCore.REGISTRATE.block("casings/machine_casing_sterilizing_pipes", ActiveCardinalBlock::new)
            .properties(p -> p.sound(SoundType.COPPER).strength(5, 6).mapColor(MapColor.COLOR_BROWN))
            .addLayer(() -> RenderType::cutout)
            .blockstate(ModelUtils.createActiveCardinalCasingModel(TFGCore.id("block/casings/machine_casing_sterilizing_pipes")))
            .tag(TFGTags.Blocks.Casings)
            .item(BlockItem::new).tag(TFGTags.Items.Casings).build()
            .register();

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

    public static final BlockEntry<Block> STERLING_SILVER_CASING = createCasingBlock("casings/sterling_silver_casing",
            GTModels.cubeAllModel(TFGCore.id("block/casings/sterling_silver_casing")));

    public static BlockEntry<ActiveBlock> createActiveCasingBlock(String name, NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockstateProvider> modelProvider,
            SoundType sound, float strength, float explosionResist, MapColor mapColor, boolean onlyDropWithTool) {
        return TFGCore.REGISTRATE.block(name, ActiveBlock::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> {
                    p.sound(sound).strength(strength, explosionResist).mapColor(mapColor).isValidSpawn((s, l, ps, e) -> false);
                    if (onlyDropWithTool)
                        p.requiresCorrectToolForDrops();
                    return p;
                })
                .addLayer(() -> RenderType::solid)
                .blockstate(modelProvider)
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings)
                .item(BlockItem::new).tag(TFGTags.Items.Casings)
                .build()
                .register();
    }

    public static BlockEntry<Block> createCasingBlock(String name, NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> modelProvider,
            SoundType sound, float strength, float explosionResist, MapColor mapColor, boolean onlyDropWithTool) {
        return TFGCore.REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> {
                    p.sound(sound).strength(strength, explosionResist).mapColor(mapColor).isValidSpawn((s, l, pos, e) -> false);
                    if (onlyDropWithTool)
                        p.requiresCorrectToolForDrops();
                    return p;
                })
                .addLayer(() -> RenderType::solid)
                .exBlockstate(modelProvider)
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings)
                .item(BlockItem::new).tag(TFGTags.Items.Casings)
                .build()
                .register();
    }

    public static BlockEntry<Block> createCasingBlock(String name, NonNullBiConsumer<DataGenContext<Block, ? extends Block>, GTBlockstateProvider> modelProvider) {
        return createCasingBlock(name, modelProvider, SoundType.COPPER, 5, 6, MapColor.COLOR_LIGHT_GRAY, false);
    }

    @SuppressWarnings({ "unchecked", "removal" })
    public static BlockEntry<Block>[] createGreenhouseCasings(String tier, List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        List<BlockEntry<ConnectedGlassBlock>> casings = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            String blockId = "casings/greenhouse/%s_greenhouse_casing_%s".formatted(tier, i);
            var blockBuilder = TFGCore.REGISTRATE.block(blockId, ConnectedGlassBlock::new)
                    .initialProperties(() -> Blocks.GLASS)
                    .properties(p -> p.strength(0.3f, 0.3f).requiresCorrectToolForDrops().sound(SoundType.GLASS).noOcclusion())
                    .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/" + blockId)))
                    .tag(TFGTags.Blocks.Casings, TFCTags.Blocks.MINEABLE_WITH_GLASS_SAW, CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, FLTags.Blocks.GREENHOUSE,
                            FLTags.Blocks.GREENHOUSE_FULL_WALLS)
                    .addLayer(i > 2 ? () -> RenderType::translucent : () -> RenderType::cutout);
            blockTags.forEach(blockBuilder::tag);

            var blockItemBuilder = blockBuilder.item(BlockItem::new);
            blockItemBuilder.tag(TFGTags.Items.GreenhouseCasings, TFGTags.Items.Casings);
            itemTags.forEach(blockItemBuilder::tag);
            blockItemBuilder.build();
            casings.add(blockBuilder.register());
        }
        return (BlockEntry<Block>[]) casings.toArray(BlockEntry[]::new);
    }
}
