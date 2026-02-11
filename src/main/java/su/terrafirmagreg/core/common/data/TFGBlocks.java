package su.terrafirmagreg.core.common.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import com.eerussianguy.firmalife.common.FLTags;
import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IcicleBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.*;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blockentity.LargeNestBoxBlockEntity;
import su.terrafirmagreg.core.common.data.blocks.*;
import su.terrafirmagreg.core.common.data.buds.BudIndicator;
import su.terrafirmagreg.core.common.data.buds.BudIndicatorItem;
import su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix;
import su.terrafirmagreg.core.utils.TFGModelUtils;

@SuppressWarnings({ "unused" })
public final class TFGBlocks {

    // Reference table builders
    static ImmutableMap.Builder<Material, BlockEntry<BudIndicator>> BUD_BLOCKS_BUILDER = ImmutableMap.builder();

    // Reference tables
    public static Map<Material, BlockEntry<BudIndicator>> BUD_BLOCKS;

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TFGCore.MOD_ID);

    // Decoration blocks

    public static final RegistryObject<Block> LUNAR_CHORUS_PLANT = register("lunar_chorus_plant",
            () -> new LunarChorusPlantBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_PURPLE)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .strength(0.2f)
                    .sound(SoundType.CHERRY_WOOD)));

    public static final RegistryObject<Block> LUNAR_CHORUS_FLOWER = register("lunar_chorus_flower",
            () -> new LunarChorusFlowerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_PURPLE)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .strength(0.2f)
                    .sound(SoundType.CHERRY_WOOD),
                    LUNAR_CHORUS_PLANT));

    // Connected texture grass blocks + dirt

    // This one's constructor needs to reference the others, so it's in the static constructor below
    public static RegistryObject<Block> MARS_DIRT;
    public static RegistryObject<Block> MARS_CLAY;

    public static final RegistryObject<Block> MARS_PATH = register("grass/mars_path",
            () -> new PathBlock(Block.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(1.4f)
                    .sound(SoundType.GRAVEL), MARS_DIRT));

    public static final RegistryObject<Block> MARS_FARMLAND = register("grass/mars_farmland",
            () -> new FarmlandBlock(ExtendedProperties.of(MapColor.DIRT)
                    .strength(1.3f)
                    .sound(SoundType.GRAVEL)
                    .isViewBlocking(TFCBlocks::always)
                    .isSuffocating(TFCBlocks::always)
                    .blockEntity(TFCBlockEntities.FARMLAND), MARS_DIRT));

    private static final BlockBehaviour.Properties amber_properties = BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_YELLOW)
            .strength(5.0f)
            .sound(SoundType.WART_BLOCK)
            .randomTicks();
    public static final RegistryObject<Block> AMBER_MYCELIUM = register("grass/amber_mycelium",
            () -> new ConnectedGrassBlock(amber_properties, MARS_DIRT, MARS_PATH, MARS_FARMLAND));
    public static final RegistryObject<Block> AMBER_CLAY_MYCELIUM = register("grass/amber_clay_mycelium",
            () -> new ConnectedGrassBlock(amber_properties, MARS_CLAY, MARS_PATH, MARS_FARMLAND));
    public static final RegistryObject<Block> AMBER_KAOLIN_MYCELIUM = register("grass/amber_kaolin_mycelium",
            () -> new ConnectedGrassBlock(amber_properties, TFCBlocks.RED_KAOLIN_CLAY, null, null));

    private static final BlockBehaviour.Properties rusticus_properties = BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .strength(5.0f)
            .sound(SoundType.WART_BLOCK)
            .randomTicks();
    public static final RegistryObject<Block> RUSTICUS_MYCELIUM = register("grass/rusticus_mycelium",
            () -> new ConnectedGrassBlock(rusticus_properties, MARS_DIRT, MARS_PATH, MARS_FARMLAND));
    public static final RegistryObject<Block> RUSTICUS_CLAY_MYCELIUM = register("grass/rusticus_clay_mycelium",
            () -> new ConnectedGrassBlock(rusticus_properties, MARS_CLAY, MARS_PATH, MARS_FARMLAND));
    public static final RegistryObject<Block> RUSTICUS_KAOLIN_MYCELIUM = register("grass/rusticus_kaolin_mycelium",
            () -> new ConnectedGrassBlock(rusticus_properties, TFCBlocks.RED_KAOLIN_CLAY, null, null));

    private static final BlockBehaviour.Properties sangnum_properties = BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_RED)
            .strength(5.0f)
            .sound(SoundType.WART_BLOCK)
            .randomTicks();
    public static final RegistryObject<Block> SANGNUM_MYCELIUM = register("grass/sangnum_mycelium",
            () -> new ConnectedGrassBlock(sangnum_properties, MARS_DIRT, MARS_PATH, MARS_FARMLAND));
    public static final RegistryObject<Block> SANGNUM_CLAY_MYCELIUM = register("grass/sangnum_clay_mycelium",
            () -> new ConnectedGrassBlock(sangnum_properties, MARS_CLAY, MARS_PATH, MARS_FARMLAND));
    public static final RegistryObject<Block> SANGNUM_KAOLIN_MYCELIUM = register("grass/sangnum_kaolin_mycelium",
            () -> new ConnectedGrassBlock(sangnum_properties, TFCBlocks.RED_KAOLIN_CLAY, null, null));

    //#region Martian sand piles and layer blocks, in order of color

    // Still in the pile folder because these are for the existing pre-0.11 layer blocks in peoples' worlds

    public static final RegistryObject<SandLayerBlock> ASH_LAYER_BLOCK = register("ash_pile",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> VOLCANIC_ASH_LAYER_BLOCK = register("pile/volcanic_ash",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.BLACK).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> BLACK_SAND_LAYER_BLOCK = register("pile/black_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.BLACK).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> WHITE_SAND_LAYER_BLOCK = register("pile/white_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.WHITE).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> BROWN_SAND_LAYER_BLOCK = register("pile/brown_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.BROWN).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> RED_SAND_LAYER_BLOCK = register("pile/red_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> YELLOW_SAND_LAYER_BLOCK = register("pile/yellow_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.YELLOW).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> PINK_SAND_LAYER_BLOCK = register("pile/pink_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.PINK).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> GREEN_SAND_LAYER_BLOCK = register("pile/green_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.GREEN).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> MOON_SAND_LAYER_BLOCK = register("pile/moon_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> HEMATITIC_SAND_LAYER_BLOCK = register("pile/hematitic_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> MARS_SAND_LAYER_BLOCK = register("pile/mars_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)));
    public static final RegistryObject<SandLayerBlock> VENUS_SAND_LAYER_BLOCK = register("pile/venus_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)));

    // The _covering suffix is to differentiate these from the other piles
    public static final RegistryObject<SandPileBlock> HEMATITIC_SAND_PILE_BLOCK = register("pile/hematitic_sand_covering",
            () -> new SandPileBlock(ExtendedProperties.of(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE).randomTicks().blockEntity(TFCBlockEntities.PILE)));
    public static final RegistryObject<SandPileBlock> MARS_SAND_PILE_BLOCK = register("pile/mars_sand_covering",
            () -> new SandPileBlock(ExtendedProperties.of(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE).randomTicks().blockEntity(TFCBlockEntities.PILE)));
    public static final RegistryObject<SandPileBlock> VENUS_SAND_PILE_BLOCK = register("pile/venus_sand_covering",
            () -> new SandPileBlock(ExtendedProperties.of(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE).randomTicks().blockEntity(TFCBlockEntities.PILE)));

    //#endregion

    // Fluid blocks

    public static final RegistryObject<LiquidBlock> MARS_WATER = registerNoItem("fluid/semiheavy_ammoniacal_water",
            () -> new LiquidBlock(TFGFluids.MARS_WATER.source(),
                    BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(MapColor.WARPED_WART_BLOCK).noLootTable()));
    public static final RegistryObject<LiquidBlock> SULFUR_FUMES = registerNoItem("fluid/sulfur_fumes",
            () -> new LiquidBlock(TFGFluids.SULFUR_FUMES.source(),
                    BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(MapColor.NONE).noLootTable().noCollission()));
    public static final RegistryObject<LiquidBlock> GEYSER_SLURRY = registerNoItem("fluid/geyser_slurry",
            () -> new LiquidBlock(TFGFluids.GEYSER_SLURRY.source(),
                    BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(MapColor.TERRACOTTA_LIGHT_BLUE).noLootTable()));

    // Misc blocks

    public static final RegistryObject<Block> PIGLIN_DISGUISE_BLOCK = register("piglin_disguise_block",
            () -> new PiglinDisguiseBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.1f)
                    .sound(SoundType.DRIPSTONE_BLOCK)
                    .pushReaction(PushReaction.DESTROY)
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)));

    public static final RegistryObject<Block> MARS_ICE = register("mars_ice",
            () -> new MarsIceBlock(BlockBehaviour.Properties.copy(Blocks.ICE)));

    public static final RegistryObject<Block> MARS_ICICLE = register("mars_icicle",
            () -> new IcicleBlock(BlockBehaviour.Properties.copy(TFCBlocks.ICICLE.get())));

    public static final RegistryObject<Block> DRY_ICE = register("dry_ice",
            () -> new DryIceBlock(BlockBehaviour.Properties.copy(Blocks.ICE)
                    .sound(SoundType.BONE_BLOCK)));
    public static final RegistryObject<Block> ARTISAN_TABLE = register("artisan_table",
            () -> new ArtisanTableBlock(ExtendedProperties.of(TFCBlocks.WOODS.get(Wood.HICKORY).get(Wood.BlockType.SEWING_TABLE).get())));

    // Multi block casings

    // Mars animal related
    public static final RegistryObject<Block> LARGE_NEST_BOX = register("large_nest_box",
            () -> new LargeNestBoxBlock(ExtendedProperties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(3f)
                    .noOcclusion()
                    .sound(TFCSounds.THATCH)
                    .blockEntity(TFGBlockEntities.LARGE_NEST_BOX)
                    .serverTicks(LargeNestBoxBlockEntity::serverTick)));
    public static final RegistryObject<Block> LARGE_NEST_BOX_WARPED = register("large_nest_box_warped",
            () -> new LargeNestBoxBlock(ExtendedProperties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(3f)
                    .noOcclusion()
                    .sound(TFCSounds.THATCH)
                    .blockEntity(TFGBlockEntities.LARGE_NEST_BOX)
                    .serverTicks(LargeNestBoxBlockEntity::serverTick)));

    // These are done separately to avoid cyclic references

    static {
        MARS_DIRT = register("grass/mars_dirt",
                () -> new DirtBlock(Block.Properties.of()
                        .mapColor(MapColor.DIRT)
                        .strength(1.4f)
                        .sound(SoundType.GRAVEL), RUSTICUS_MYCELIUM, MARS_PATH, MARS_FARMLAND, null, null));
        MARS_CLAY = register("grass/mars_clay_dirt",
                () -> new DirtBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.DIRT)
                        .strength(1.4f)
                        .sound(SoundType.GRAVEL), RUSTICUS_CLAY_MYCELIUM, MARS_PATH, MARS_FARMLAND, null, null));
    }

    //    public static final BlockEntry<ActiveCardinalBlock> SAMPLE_RACK = TFGCore.REGISTRATE.block("sample_rack", ActiveCardinalBlock::new)
    //            .properties(p -> p.sound(SoundType.COPPER).strength(5, 6).mapColor(MapColor.COLOR_LIGHT_GRAY).noOcclusion())
    //            .addLayer(() -> RenderType::cutout)
    //            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
    //            .item(BlockItem::new).build()
    //            .register();
    //
    //    public static final BlockEntry<ActiveCardinalBlock> GROWTH_MONITOR = TFGCore.REGISTRATE.block("growth_monitor", ActiveCardinalBlock::new)
    //            .properties(p -> p.sound(SoundType.COPPER).strength(5, 6).mapColor(MapColor.COLOR_LIGHT_GRAY).noOcclusion().lightLevel((state) -> (int) (0.8 * 15.0F)))
    //            .addLayer(() -> RenderType::cutout)
    //            .item(BlockItem::new).build()
    //            .register();
    //
    //    public static final BlockEntry<ActiveCardinalBlock> CULTIVATION_MONITOR = TFGCore.REGISTRATE.block("cultivation_monitor", ActiveCardinalBlock::new)
    //            .properties(p -> p.sound(SoundType.COPPER).strength(5, 6).mapColor(MapColor.COLOR_LIGHT_GRAY).noCollission().noOcclusion().lightLevel((state) -> (int) (0.8 * 15.0F)))
    //            .addLayer(() -> RenderType::cutout)
    //            .item(BlockItem::new).build()
    //            .register();

    //// Casings

    public static final BlockEntry<Block> CLEAN_STAINLESS_STEEL_DESH_CASING = createCasingBlock("machine_casing_clean_stainless_steel_desh",
            TFGCore.id("block/casings/machine_casing_clean_stainless_steel_desh"));
    public static final BlockEntry<Block> DESH_PTFE_CASING = createCasingBlock("machine_casing_desh_ptfe", TFGCore.id("block/casings/machine_casing_desh_ptfe"));
    public static final BlockEntry<Block> IRON_DESH_CASING = createCasingBlock("machine_casing_iron_desh", TFGCore.id("block/casings/machine_casing_iron_desh"));
    public static final BlockEntry<Block> PTFE_DESH_CASING = createCasingBlock("machine_casing_ptfe_desh", TFGCore.id("block/casings/machine_casing_ptfe_desh"));
    public static final BlockEntry<Block> STAINLESS_STEEL_DESH_CASING = createCasingBlock("machine_casing_stainless_steel_desh", TFGCore.id("block/casings/machine_casing_stainless_steel_desh"));
    public static final BlockEntry<Block> BLUE_SOLAR_PANEL_CASING = createCasingBlock("machine_casing_blue_solar_panel", TFGCore.id("block/casings/machine_casing_blue_solar_panel"));
    public static final BlockEntry<Block> GREEN_SOLAR_PANEL_CASING = createCasingBlock("machine_casing_green_solar_panel", TFGCore.id("block/casings/machine_casing_green_solar_panel"));
    public static final BlockEntry<Block> RED_SOLAR_PANEL_CASING = createCasingBlock("machine_casing_red_solar_panel", TFGCore.id("block/casings/machine_casing_red_solar_panel"));
    public static final BlockEntry<Block> MARS_CASING = createCasingBlock("machine_casing_mars", TFGCore.id("block/casings/machine_casing_mars"));
    public static final BlockEntry<Block> OSTRUM_CARBON_CASING = createCasingBlock("machine_casing_ostrum_carbon", TFGCore.id("block/casings/machine_casing_ostrum_carbon"));
    public static final BlockEntry<Block> STAINLESS_EVAPORATION_CASING = createCasingBlock("machine_casing_stainless_evaporation", TFGCore.id("block/casings/machine_casing_stainless_evaporation"));

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

    public static final BlockEntry<Block> SUPERCONDUCTOR_COIL_LARGE_BLOCK = createCasingBlock("superconductor_coil_large", TFGCore.id("block/casings/superconductor_coil_large"),
            p -> p
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(5.5f)
                    .sound(SoundType.COPPER));

    public static final BlockEntry<Block> SUPERCONDUCTOR_COIL_SMALL_BLOCK = createCasingBlock("superconductor_coil_small", TFGCore.id("block/casings/superconductor_coil_small"),
            p -> p
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(5.5f)
                    .sound(SoundType.COPPER));

    public static final RegistryObject<ReflectorBlock> REFLECTOR_BLOCK = register("reflector", ReflectorBlock::new);

    public static final BlockEntry<Block> MACHINE_CASING_ALUMINIUM_PLATED_STEEL = createCasingBlock(
            "machine_casing_aluminium_plated_steel", TFGCore.id("block/casings/machine_casing_aluminium_plated_steel"),
            p -> p
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(5.5f)
                    .sound(SoundType.COPPER));

    public static final BlockEntry<Block> MACHINE_CASING_POWER_CASING = createCasingBlock(
            "machine_casing_power_casing", TFGCore.id("block/casings/machine_casing_power_casing"),
            p -> p
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(5.5f)
                    .sound(SoundType.COPPER));

    public static final BlockEntry<Block> HEAT_PIPE_CASING = createCasingBlock("heat_pipe_casing", TFGCore.id("block/casings/heat_pipe_casing"),
            p -> p.isValidSpawn((state, level, pos, ent) -> false)
                    .sound(SoundType.COPPER).strength(5, 6)
                    .mapColor(MapColor.COLOR_BLACK));

    public static final BlockEntry<Block> BIOCULTURE_CASING = createCasingBlock("machine_casing_bioculture", TFGCore.id("block/casings/machine_casing_bioculture"),
            p -> p.isValidSpawn((state, level, pos, ent) -> false)
                    .sound(SoundType.COPPER).strength(5, 6)
                    .mapColor(MapColor.COLOR_RED));

    public static final BlockEntry<Block> BIOCULTURE_GLASS_CASING = TFGCore.REGISTRATE.block("machine_casing_bioculture_glass", Block::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false)
                    .sound(SoundType.GLASS).strength(5, 6)
                    .mapColor(MapColor.COLOR_ORANGE))
            .addLayer(() -> RenderType::translucent)
            .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_bioculture_glass")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings, TFCTags.Blocks.MINEABLE_WITH_GLASS_SAW)
            .item(BlockItem::new).tag(TFGTags.Items.Casings).build()
            .register();

    public static final BlockEntry<ActiveBlock> BIOCULTURE_ROTOR_SECONDARY = createActiveCasingBlock("bioculture_rotor_secondary",
            p -> p.strength(6, 5).sound(SoundType.COPPER).mapColor(MapColor.COLOR_LIGHT_GRAY));

    public static final BlockEntry<ActiveBlock> VACUUM_ENGINE_INTAKE = createActiveCasingBlock("machine_casing_vacuum_engine_intake",
            p -> p.strength(6, 5).sound(SoundType.METAL).mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops());

    public static final BlockEntry<ActiveBlock> ULTRAVIOLET_CASING = createActiveCasingBlock("machine_casing_ultraviolet",
            p -> p.strength(6, 5).sound(SoundType.GLASS).mapColor(MapColor.COLOR_LIGHT_GRAY));

    public static final BlockEntry<ActiveBlock> EGH_CASING = createActiveCasingBlock("machine_casing_egh",
            p -> p.strength(6, 5).sound(SoundType.METAL).mapColor(MapColor.COLOR_LIGHT_GRAY));

    public static final BlockEntry<ActiveCardinalBlock> STERILIZING_PIPE_CASING = TFGCore.REGISTRATE.block("machine_casing_sterilizing_pipes", ActiveCardinalBlock::new)
            .properties(p -> p.sound(SoundType.COPPER).strength(5, 6).mapColor(MapColor.COLOR_BROWN))
            .addLayer(() -> RenderType::cutout)
            .blockstate(TFGModelUtils.createActiveCardinalModel(TFGCore.id("block/casings/machine_casing_sterilizing_pipes")))
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

    public static BlockEntry<ActiveBlock> createActiveCasingBlock(String name, UnaryOperator<BlockBehaviour.Properties> properties) {
        return TFGCore.REGISTRATE.block(name, ActiveBlock::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties::apply)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate(TFGModelUtils.createActiveModel(TFGCore.id("block/casings/machine_casing_vacuum_engine_intake")))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings)
                .item(BlockItem::new).tag(TFGTags.Items.Casings)
                .build()
                .register();
    }

    public static BlockEntry<Block> createCasingBlock(String name, ResourceLocation texture, UnaryOperator<BlockBehaviour.Properties> properties) {
        return TFGCore.REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties::apply)
                .addLayer(() -> RenderType::solid)
                .exBlockstate(GTModels.cubeAllModel(texture))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings)
                .item(BlockItem::new).tag(TFGTags.Items.Casings)
                .build()
                .register();
    }

    public static BlockEntry<Block> createCasingBlock(String name, ResourceLocation texture) {
        return createCasingBlock(name, texture,
                p -> p.isValidSpawn((state, level, pos, ent) -> false)
                        .sound(SoundType.COPPER).strength(5, 6)
                        .mapColor(MapColor.COLOR_LIGHT_GRAY));
    }

    @SuppressWarnings("unchecked")
    public static BlockEntry<Block>[] createGreenhouseCasings(String tier, List<TagKey<Block>> blockTags, List<TagKey<Item>> itemTags) {
        List<BlockEntry<Block>> casings = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            String blockId = "%s_greenhouse_casing_%s".formatted(tier, i);
            var blockBuilder = TFGCore.REGISTRATE.block(blockId, Block::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.strength(0.3f, 0.3f).requiresCorrectToolForDrops().sound(SoundType.GLASS))
                    .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/casings/greenhouse/" + blockId)))
                    .tag(TFGTags.Blocks.Casings, TFCTags.Blocks.MINEABLE_WITH_GLASS_SAW, FLTags.Blocks.GREENHOUSE, FLTags.Blocks.GREENHOUSE_FULL_WALLS)
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

    // Buds are generated automatically

    public static void generateBudIndicators() {
        BUD_BLOCKS_BUILDER = ImmutableMap.builder();

        for (MaterialRegistry registry : GTCEuAPI.materialManager.getRegistries()) {
            GTRegistrate registrate = registry.getRegistrate();
            for (Material material : registry.getAllMaterials()) {
                if (material.hasProperty(PropertyKey.ORE) && material.hasProperty(PropertyKey.GEM)) {
                    registerBudIndicator(material, registrate, BUD_BLOCKS_BUILDER);
                }
            }
        }
        BUD_BLOCKS = BUD_BLOCKS_BUILDER.build();
    }

    @SuppressWarnings("removal")
    private static void registerBudIndicator(Material material, GTRegistrate registrate,
            ImmutableMap.Builder<Material, BlockEntry<BudIndicator>> builder) {
        TagPrefix budTag;
        int lightLevel;

        var entry = registrate
                .block("%s_bud_indicator".formatted(material.getName()), p -> new BudIndicator(p, material))
                .initialProperties(() -> Blocks.AMETHYST_CLUSTER)
                .properties(p -> p
                        .noLootTable()
                        .noOcclusion()
                        .noCollission()
                        .strength(0.25f)
                        .lightLevel(b -> 3)
                        .offsetType(BlockBehaviour.OffsetType.XZ))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .transform(GTBlocks.unificationBlock(TFGTagPrefix.budIndicator, material))
                // "deprecated" but gregtech uses it too
                .addLayer(() -> RenderType::cutoutMipped)
                .color(() -> BudIndicator::tintedBlockColor)
                .item((b, p) -> BudIndicatorItem.create(b, p, material))
                .color(() -> BudIndicator::tintedItemColor)
                .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .build()
                .register();
        builder.put(material, entry);
    }

    private static final VanillaBlockLoot BLOCK_LOOT = new VanillaBlockLoot();

    public static void generateBudIndicatorLoot(Map<ResourceLocation, LootTable> lootTables) {
        TFGBlocks.BUD_BLOCKS.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(blockEntry.getId().getNamespace(),
                    "blocks/" + blockEntry.getId().getPath());
            ((BlockBehaviourAccessor) blockEntry.get()).setDrops(lootTableId);
        });
    }

    // Helper registration methods

    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> blockSupplier) {
        return register(name, blockSupplier, (Function<T, ? extends BlockItem>) null);
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier) {
        return register(name, blockSupplier, b -> new BlockItem(b, new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier,
            Item.Properties blockItemProperties) {
        return register(name, blockSupplier, block -> new BlockItem(block, blockItemProperties));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier,
            @Nullable Function<T, ? extends BlockItem> blockItemFactory) {
        return RegistrationHelpers.registerBlock(BLOCKS, TFGItems.ITEMS, name, blockSupplier, blockItemFactory);
    }
}
