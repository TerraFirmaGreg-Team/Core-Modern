package su.terrafirmagreg.core.common.data;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IcicleBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.*;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
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
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> BLACK_SAND_LAYER_BLOCK = register("pile/black_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.BLACK).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> WHITE_SAND_LAYER_BLOCK = register("pile/white_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.WHITE).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> BROWN_SAND_LAYER_BLOCK = register("pile/brown_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.BROWN).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> RED_SAND_LAYER_BLOCK = register("pile/red_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> YELLOW_SAND_LAYER_BLOCK = register("pile/yellow_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.YELLOW).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> PINK_SAND_LAYER_BLOCK = register("pile/pink_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.PINK).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> GREEN_SAND_LAYER_BLOCK = register("pile/green_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.GREEN).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> MOON_SAND_LAYER_BLOCK = register("pile/moon_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> HEMATITIC_SAND_LAYER_BLOCK = register("pile/hematitic_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> MARS_SAND_LAYER_BLOCK = register("pile/mars_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> VENUS_SAND_LAYER_BLOCK = register("pile/venus_sand",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));
    public static final RegistryObject<SandLayerBlock> MARS_SNOW_LAYER_BLOCK = register("pile/mars_snow",
            () -> new SandLayerBlock(BlockBehaviour.Properties.copy(TFCBlocks.SNOW_PILE.get()).sound(SoundType.SNOW).noOcclusion().mapColor(MapColor.NONE)
            //TODO
            ));

    // The _covering suffix is to differentiate these from the other piles
    public static final RegistryObject<SandPileBlock> HEMATITIC_SAND_PILE_BLOCK = register("pile/hematitic_sand_covering",
            () -> new SandPileBlock(
                    ExtendedProperties.of(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE).randomTicks().blockEntity(TFCBlockEntities.PILE)));
    public static final RegistryObject<SandPileBlock> MARS_SAND_PILE_BLOCK = register("pile/mars_sand_covering",
            () -> new SandPileBlock(
                    ExtendedProperties.of(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE).randomTicks().blockEntity(TFCBlockEntities.PILE)));
    public static final RegistryObject<SandPileBlock> VENUS_SAND_PILE_BLOCK = register("pile/venus_sand_covering",
            () -> new SandPileBlock(
                    ExtendedProperties.of(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE).randomTicks().blockEntity(TFCBlockEntities.PILE)));
    public static final RegistryObject<SandPileBlock> MARS_SNOW_PILE_BLOCK = register("pile/mars_snow_covering",
            () -> new SandPileBlock(
                    ExtendedProperties.of(TFCBlocks.SNOW_PILE.get()).noOcclusion().sound(SoundType.SNOW).randomTicks().blockEntity(TFCBlockEntities.PILE)));

    //#endregion

    // Fluid blocks

    public static final RegistryObject<LiquidBlock> MARS_WATER = registerNoItem("fluid/semiheavy_ammoniacal_water",
            () -> new LiquidBlock(TFGFluids.MARS_WATER.source(),
                    BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(MapColor.WARPED_WART_BLOCK).noLootTable()));

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

    // Multi block casings

    public static final RegistryObject<Block> ELECTROMAGNETIC_ACCELERATOR_BLOCK = register(
            "electromagnetic_accelerator",
            () -> new ElectromagneticAcceleratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(5.5f)
                    .sound(SoundType.COPPER)
                    .lightLevel(state -> 15)
                    .speedFactor(1.5f)));

    public static final RegistryObject<Block> SUPERCONDUCTOR_COIL_LARGE_BLOCK = register("superconductor_coil_large",
            () -> new SimpleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(5.5f)
                    .sound(SoundType.COPPER)));

    public static final RegistryObject<Block> SUPERCONDUCTOR_COIL_SMALL_BLOCK = register("superconductor_coil_small",
            () -> new SimpleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(5.5f)
                    .sound(SoundType.COPPER)));

    public static final RegistryObject<Block> MACHINE_CASING_ALUMINIUM_PLATED_STEEL = register(
            "machine_casing_aluminium_plated_steel",
            () -> new SimpleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(5.5f)
                    .sound(SoundType.COPPER)));

    public static final RegistryObject<ReflectorBlock> REFLECTOR_BLOCK = register("reflector", ReflectorBlock::new);

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
