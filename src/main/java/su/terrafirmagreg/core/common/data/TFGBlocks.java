package su.terrafirmagreg.core.common.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.eerussianguy.firmalife.common.FLTags;
import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IcicleBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.*;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.Powder;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blockentity.LargeNestBoxBlockEntity;
import su.terrafirmagreg.core.common.data.blocks.*;
import su.terrafirmagreg.core.common.data.buds.BudIndicator;
import su.terrafirmagreg.core.common.data.buds.BudIndicatorItem;
import su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix;
import su.terrafirmagreg.core.compat.kjs.GTActiveParticleBuilder;
import su.terrafirmagreg.core.utils.ModelUtils;

@SuppressWarnings({ "unused" })
public final class TFGBlocks {

    // Reference table builders
    static ImmutableMap.Builder<Material, BlockEntry<BudIndicator>> BUD_BLOCKS_BUILDER = ImmutableMap.builder();

    // Reference tables
    public static Map<Material, BlockEntry<BudIndicator>> BUD_BLOCKS = new Object2ObjectOpenHashMap<>();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TFGCore.MOD_ID);

    public static void init() {
    }

    ////// Decoration blocks

    public static final BlockEntry<LunarChorusPlantBlock> LUNAR_CHORUS_PLANT = TFGCore.REGISTRATE.block("lunar_chorus_plant", LunarChorusPlantBlock::new)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_PURPLE)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .strength(0.2f)
                    .sound(SoundType.CHERRY_WOOD))
            .item(BlockItem::new).model(ModelUtils.blockItemModel(ResourceLocation.fromNamespaceAndPath("minecraft", "block/chorus_plant"))).build()
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .loot((prov, ctx) -> prov.add(ctx, new LootTable.Builder()))
            .register();

    public static final BlockEntry<LunarChorusFlowerBlock> LUNAR_CHORUS_FLOWER = TFGCore.REGISTRATE.block("lunar_chorus_flower", p -> new LunarChorusFlowerBlock(p, LUNAR_CHORUS_PLANT))
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_PURPLE)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)
                    .strength(0.2f)
                    .sound(SoundType.CHERRY_WOOD))
            .item(BlockItem::new).model(ModelUtils.blockItemModel(ResourceLocation.fromNamespaceAndPath("minecraft", "block/chorus_flower"))).build()
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .register();

    ////// Connected texture grass blocks + dirt

    // This one's constructor needs to reference the others, so it's in the static constructor below
    public static BlockEntry<DirtBlock> MARS_DIRT;
    public static BlockEntry<DirtBlock> MARS_CLAY;

    public static final BlockEntry<PathBlock> MARS_PATH = TFGCore.REGISTRATE.block("grass/mars_path", p -> new PathBlock(p, MARS_DIRT))
            .properties(p -> p.mapColor(MapColor.DIRT)
                    .strength(1.4f)
                    .sound(SoundType.GRAVEL))
            .simpleItem()
            .loot((ctx, prov) -> ctx.dropOther(prov, MARS_DIRT))
            .blockstate(ModelUtils.blockVariantsRotated(TFGCore.id("block/grass/mars_path")))
            .register();

    public static final BlockEntry<FarmlandBlock> MARS_FARMLAND = TFGCore.REGISTRATE.block("grass/mars_farmland",
            p -> new FarmlandBlock(ExtendedProperties.of(MapColor.DIRT)
                    .strength(1.3f)
                    .sound(SoundType.GRAVEL)
                    .isViewBlocking(TFCBlocks::always)
                    .isSuffocating(TFCBlocks::always)
                    .blockEntity(TFCBlockEntities.FARMLAND), MARS_DIRT))
            .simpleItem()
            .blockstate(ModelUtils.blockVariants(TFGCore.id("block/grass/mars_farmland")))
            .loot((ctx, prov) -> ctx.dropOther(prov, MARS_DIRT))
            .register();

    private static final BlockBehaviour.Properties amber_properties = BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_YELLOW)
            .strength(5.0f)
            .sound(SoundType.WART_BLOCK)
            .randomTicks();

    public static final BlockEntry<ConnectedGrassBlock> AMBER_MYCELIUM = TFGCore.REGISTRATE.block("grass/amber_mycelium",
            p -> new ConnectedGrassBlock(p, MARS_DIRT, MARS_PATH, MARS_FARMLAND))
            .properties(p -> amber_properties)
            .loot((ctx, b) -> ctx.dropOther(b, MARS_DIRT))
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<ConnectedGrassBlock> AMBER_CLAY_MYCELIUM = TFGCore.REGISTRATE.block("grass/amber_clay_mycelium",
            p -> new ConnectedGrassBlock(p, MARS_DIRT, MARS_PATH, MARS_FARMLAND))
            .properties(p -> amber_properties)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .loot(dropBetween(() -> Items.CLAY_BALL, 1, 3))
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<ConnectedGrassBlock> AMBER_KAOLIN_MYCELIUM = TFGCore.REGISTRATE.block("grass/amber_kaolin_mycelium",
            p -> new ConnectedGrassBlock(p, TFCBlocks.RED_KAOLIN_CLAY, null, null))
            .properties(p -> amber_properties)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .loot(dropBetween(TFCItems.KAOLIN_CLAY, 1, 3))
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    private static final BlockBehaviour.Properties rusticus_properties = BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .strength(5.0f)
            .sound(SoundType.WART_BLOCK)
            .randomTicks();

    public static final BlockEntry<ConnectedGrassBlock> RUSTICUS_MYCELIUM = TFGCore.REGISTRATE.block("grass/rusticus_mycelium",
            p -> new ConnectedGrassBlock(p, MARS_DIRT, MARS_PATH, MARS_FARMLAND))
            .properties(p -> rusticus_properties)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .loot((ctx, prov) -> ctx.dropOther(prov, MARS_DIRT))
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<ConnectedGrassBlock> RUSTICUS_CLAY_MYCELIUM = TFGCore.REGISTRATE.block("grass/rusticus_clay_mycelium",
            p -> new ConnectedGrassBlock(p, MARS_DIRT, MARS_PATH, MARS_FARMLAND))
            .properties(p -> rusticus_properties)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .loot(dropBetween(() -> Items.CLAY_BALL, 1, 3))
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<ConnectedGrassBlock> RUSTICUS_KAOLIN_MYCELIUM = TFGCore.REGISTRATE.block("grass/rusticus_kaolin_mycelium",
            p -> new ConnectedGrassBlock(p, TFCBlocks.RED_KAOLIN_CLAY, null, null))
            .properties(p -> rusticus_properties)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .loot(dropBetween(TFCItems.KAOLIN_CLAY, 1, 3))
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    private static final BlockBehaviour.Properties sangnum_properties = BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_RED)
            .strength(5.0f)
            .sound(SoundType.WART_BLOCK)
            .randomTicks();

    public static final BlockEntry<ConnectedGrassBlock> SANGNUM_MYCELIUM = TFGCore.REGISTRATE.block("grass/sangnum_mycelium",
            p -> new ConnectedGrassBlock(p, MARS_DIRT, MARS_PATH, MARS_FARMLAND))
            .properties(p -> sangnum_properties)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .loot((ctx, prov) -> ctx.dropOther(prov, MARS_DIRT))
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<ConnectedGrassBlock> SANGNUM_CLAY_MYCELIUM = TFGCore.REGISTRATE.block("grass/sangnum_clay_mycelium",
            p -> new ConnectedGrassBlock(p, MARS_DIRT, MARS_PATH, MARS_FARMLAND))
            .properties(p -> sangnum_properties)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .loot(dropBetween(() -> Items.CLAY_BALL, 1, 3))
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<ConnectedGrassBlock> SANGNUM_KAOLIN_MYCELIUM = TFGCore.REGISTRATE.block("grass/sangnum_kaolin_mycelium",
            p -> new ConnectedGrassBlock(p, TFCBlocks.RED_KAOLIN_CLAY, null, null))
            .properties(p -> sangnum_properties)
            .loot(dropBetween(TFCItems.KAOLIN_CLAY, 1, 3))
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    // These are done separately to avoid cyclic references

    static {
        MARS_DIRT = TFGCore.REGISTRATE.block("grass/mars_dirt",
                (p) -> new DirtBlock(p, RUSTICUS_MYCELIUM, MARS_PATH, MARS_FARMLAND, null, null))
                .properties(p -> p.mapColor(MapColor.DIRT).strength(1.4f).sound(SoundType.GRAVEL))
                .simpleItem()
                .blockstate(ModelUtils.blockVariantsRotated(TFGCore.id("block/grass/mars_dirt")))
                .register();

        MARS_CLAY = TFGCore.REGISTRATE.block("grass/mars_clay_dirt",
                (p) -> new DirtBlock(p, RUSTICUS_MYCELIUM, MARS_PATH, MARS_FARMLAND, null, null))
                .properties(p -> p.mapColor(MapColor.DIRT).strength(1.4f).sound(SoundType.GRAVEL))
                .simpleItem()
                .loot(dropBetween(() -> Items.CLAY_BALL, 1, 3))
                .blockstate(ModelUtils.blockVariantsRotated(TFGCore.id("block/grass/mars_clay_dirt")))
                .register();

    }

    private static <T extends Block> NonNullBiConsumer<RegistrateBlockLootTables, T> dropBetween(Supplier<Item> item, int min, int max) {
        return (ctx, b) -> ctx.add(b, LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(item.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))))));
    }

    ////#region Martian sand piles and layer blocks, in order of color

    // Still in the pile folder because these are for the existing pre-0.11 layer blocks in peoples' worlds

    public static final BlockEntry<SandLayerBlock> ASH_LAYER_BLOCK = TFGCore.REGISTRATE.block("ash_pile", SandLayerBlock::new)
            .initialProperties(TFCBlocks.SAND.get(SandBlockType.RED)::get)
            .properties(p -> p.noOcclusion().mapColor(MapColor.NONE))
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .loot((ctx, b) -> ctx.dropOther(b, TFCItems.POWDERS.get(Powder.WOOD_ASH).get()))
            .item(BlockItem::new).model(ModelUtils.layeredItemModel(ResourceLocation.fromNamespaceAndPath("tfc", "item/powder/wood_ash"))).build()
            .register();

    public static final BlockEntry<SandLayerBlock> VOLCANIC_ASH_LAYER_BLOCK = registerSandLayerBlock("pile/volcanic_ash",
            TFCBlocks.SAND.get(SandBlockType.RED)::get, TFGCore.id("block/planets/venus/volcanic_ash"));

    public static final BlockEntry<SandLayerBlock> BLACK_SAND_LAYER_BLOCK = registerSandLayerBlock("pile/black_sand",
            TFCBlocks.SAND.get(SandBlockType.BLACK)::get, ResourceLocation.fromNamespaceAndPath("tfc", "block/sand/black"));

    public static final BlockEntry<SandLayerBlock> WHITE_SAND_LAYER_BLOCK = registerSandLayerBlock("pile/white_sand",
            TFCBlocks.SAND.get(SandBlockType.WHITE)::get, ResourceLocation.fromNamespaceAndPath("tfc", "block/sand/white"));

    public static final BlockEntry<SandLayerBlock> BROWN_SAND_LAYER_BLOCK = registerSandLayerBlock("pile/brown_sand",
            TFCBlocks.SAND.get(SandBlockType.WHITE)::get, ResourceLocation.fromNamespaceAndPath("tfc", "block/sand/brown"));

    public static final BlockEntry<SandLayerBlock> RED_SAND_LAYER_BLOCK = registerSandLayerBlock("pile/red_sand",
            TFCBlocks.SAND.get(SandBlockType.RED)::get, ResourceLocation.fromNamespaceAndPath("tfc", "block/sand/red"));

    public static final BlockEntry<SandLayerBlock> YELLOW_SAND_LAYER_BLOCK = registerSandLayerBlock("pile/yellow_sand",
            TFCBlocks.SAND.get(SandBlockType.YELLOW)::get, ResourceLocation.fromNamespaceAndPath("tfc", "block/sand/yellow"));

    public static final BlockEntry<SandLayerBlock> PINK_SAND_LAYER_BLOCK = registerSandLayerBlock("pile/pink_sand",
            TFCBlocks.SAND.get(SandBlockType.PINK)::get, ResourceLocation.fromNamespaceAndPath("tfc", "block/sand/pink"));

    public static final BlockEntry<SandLayerBlock> GREEN_SAND_LAYER_BLOCK = registerSandLayerBlock("pile/green_sand",
            TFCBlocks.SAND.get(SandBlockType.PINK)::get, ResourceLocation.fromNamespaceAndPath("tfc", "block/sand/green"));

    public static final BlockEntry<SandLayerBlock> MOON_SAND_LAYER_BLOCK = registerSandLayerBlock("pile/moon_sand",
            TFCBlocks.SAND.get(SandBlockType.PINK)::get, ResourceLocation.fromNamespaceAndPath("ad_astra", "block/moon_sand"));

    public static final BlockEntry<SandLayerBlock> HEMATITIC_SAND_LAYER_BLOCK = registerSandLayerBlock("pile/hematitic_sand",
            TFCBlocks.SAND.get(SandBlockType.PINK)::get, ResourceLocation.fromNamespaceAndPath("minecraft", "block/red_sand"));

    public static final BlockEntry<SandLayerBlock> MARS_SAND_LAYER_BLOCK = registerSandLayerBlock("pile/mars_sand",
            TFCBlocks.SAND.get(SandBlockType.PINK)::get, ResourceLocation.fromNamespaceAndPath("ad_astra", "block/mars_sand"));

    public static final BlockEntry<SandLayerBlock> VENUS_SAND_LAYER_BLOCK = registerSandLayerBlock("pile/venus_sand",
            TFCBlocks.SAND.get(SandBlockType.PINK)::get, ResourceLocation.fromNamespaceAndPath("ad_astra", "block/venus_sand"));

    // The _covering suffix is to differentiate these from the other piles
    public static final BlockEntry<SandPileBlock> HEMATITIC_SAND_PILE_BLOCK = TFGCore.REGISTRATE.block("pile/hematitic_sand_covering",
            p -> new SandPileBlock(ExtendedProperties.of(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE).randomTicks().blockEntity(TFCBlockEntities.PILE)))
            .blockstate(ModelUtils.generateSandLayersFromBlock(ResourceLocation.fromNamespaceAndPath("minecraft", "block/red_sand")))
            .loot((ctx, p) -> ctx.add(p, LootTable.lootTable()))
            .item(BlockItem::new).model(ModelUtils.blockItemModel(ResourceLocation.fromNamespaceAndPath("minecraft", "block/red_sand"))).build()
            .register();

    public static final BlockEntry<SandPileBlock> MARS_SAND_PILE_BLOCK = TFGCore.REGISTRATE.block("pile/mars_sand_covering",
            p -> new SandPileBlock(ExtendedProperties.of(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE).randomTicks().blockEntity(TFCBlockEntities.PILE)))
            .blockstate(ModelUtils.generateSandLayersFromBlock(ResourceLocation.fromNamespaceAndPath("ad_astra", "block/mars_sand")))
            .loot((ctx, p) -> ctx.add(p, LootTable.lootTable()))
            .item(BlockItem::new).model(ModelUtils.blockItemModel(ResourceLocation.fromNamespaceAndPath("ad_astra", "block/mars_sand"))).build()
            .register();

    public static final BlockEntry<SandPileBlock> VENUS_SAND_PILE_BLOCK = TFGCore.REGISTRATE.block("pile/venus_sand_covering",
            p -> new SandPileBlock(ExtendedProperties.of(TFCBlocks.SAND.get(SandBlockType.RED).get()).noOcclusion().mapColor(MapColor.NONE).randomTicks().blockEntity(TFCBlockEntities.PILE)))
            .blockstate(ModelUtils.generateSandLayersFromBlock(ResourceLocation.fromNamespaceAndPath("ad_astra", "block/venus_sand")))
            .loot((ctx, p) -> ctx.add(p, LootTable.lootTable()))
            .item(BlockItem::new).model(ModelUtils.blockItemModel(ResourceLocation.fromNamespaceAndPath("ad_astra", "block/venus_sand"))).build()
            .register();

    private static BlockEntry<SandLayerBlock> registerSandLayerBlock(String name, NonNullSupplier<Block> initalProperties, ResourceLocation modelPath) {
        return registerSandLayerBlock(name, initalProperties, modelPath, modelPath);
    }

    private static BlockEntry<SandLayerBlock> registerSandLayerBlock(String name, NonNullSupplier<Block> initalProperties, ResourceLocation modelPath, ResourceLocation itemModelPath) {
        return TFGCore.REGISTRATE.block(name, SandLayerBlock::new)
                .initialProperties(initalProperties)
                .blockstate(ModelUtils.generateSandLayersFromBlock(modelPath))
                .item(BlockItem::new).model(ModelUtils.blockItemModel(modelPath)).build()
                .loot((ctx, p) -> ctx.add(p, LootTable.lootTable()))
                .properties(p -> p.noOcclusion().mapColor(MapColor.NONE))
                .register();
    }

    //#endregion

    /// Fluid blocks

    public static final BlockEntry<LiquidBlock> MARS_WATER = TFGCore.REGISTRATE.block("fluid/semiheavy_ammoniacal_water", p -> new LiquidBlock(TFGFluids.MARS_WATER.source(), p))
            .initialProperties(() -> Blocks.WATER)
            .blockstate(ModelUtils.blockVariants(TFGCore.id("block/fluid/semiheavy_ammoniacal_water")))
            .properties(p -> p.mapColor(MapColor.WARPED_WART_BLOCK).noLootTable())
            .register();

    public static final BlockEntry<LiquidBlock> SULFUR_FUMES = TFGCore.REGISTRATE.block("fluid/sulfur_fumes",
            p -> new LiquidBlock(TFGFluids.SULFUR_FUMES.source(), p))
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .initialProperties(() -> Blocks.WATER)
            .properties(p -> p.mapColor(MapColor.NONE).noLootTable().noCollission())
            .register();

    public static final BlockEntry<LiquidBlock> GEYSER_SLURRY = TFGCore.REGISTRATE.block("fluid/geyser_slurry", p -> new LiquidBlock(TFGFluids.GEYSER_SLURRY.source(), p))
            .initialProperties(() -> Blocks.WATER)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_BLUE).noLootTable())
            .register();

    ///// Misc blocks

    public static final BlockEntry<PiglinDisguiseBlock> PIGLIN_DISGUISE_BLOCK = TFGCore.REGISTRATE.block("piglin_disguise_block", PiglinDisguiseBlock::new)
            .properties(p -> p
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.1f)
                    .sound(SoundType.DRIPSTONE_BLOCK)
                    .pushReaction(PushReaction.DESTROY)
                    .isViewBlocking((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false))
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .blockstate((ctx, prov) -> ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().getExistingFile(TFGCore.id("block/piglin_disguise_block"))))
            .register();

    public static final BlockEntry<MarsIceBlock> MARS_ICE = TFGCore.REGISTRATE.block("mars_ice", MarsIceBlock::new)
            .initialProperties(() -> Blocks.ICE)
            .simpleItem()
            .register();

    public static final BlockEntry<IcicleBlock> MARS_ICICLE = TFGCore.REGISTRATE.block("mars_icicle", IcicleBlock::new)
            .initialProperties(TFCBlocks.ICICLE::get)
            .properties(BlockBehaviour.Properties::noLootTable)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build().register();

    public static final BlockEntry<DryIceBlock> DRY_ICE = TFGCore.REGISTRATE.block("dry_ice", DryIceBlock::new)
            .initialProperties(() -> Blocks.ICE)
            .properties(p -> p.sound(SoundType.BONE_BLOCK))
            .simpleItem()
            .register();

    public static final BlockEntry<ArtisanTableBlock> ARTISAN_TABLE = TFGCore.REGISTRATE.block("artisan_table",
            (p) -> new ArtisanTableBlock(ExtendedProperties.of(TFCBlocks.WOODS.get(Wood.HICKORY).get(Wood.BlockType.SEWING_TABLE).get())))
            .blockstate((ctx, prov) -> ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().getExistingFile(TFGCore.id("block/artisan_table"))))
            .item(BlockItem::new).build()
            .register();

    ///// Mars animal related

    public static final BlockEntry<LargeNestBoxBlock> LARGE_NEST_BOX = TFGCore.REGISTRATE.block("large_nest_box",
            p -> new LargeNestBoxBlock(ExtendedProperties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(3f)
                    .noOcclusion()
                    .sound(TFCSounds.THATCH)
                    .blockEntity(TFGBlockEntities.LARGE_NEST_BOX)
                    .serverTicks(LargeNestBoxBlockEntity::serverTick)))
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<LargeNestBoxBlock> LARGE_NEST_BOX_WARPED = TFGCore.REGISTRATE.block("large_nest_box_warped",
            p -> new LargeNestBoxBlock(ExtendedProperties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(3f)
                    .noOcclusion()
                    .sound(TFCSounds.THATCH)
                    .blockEntity(TFGBlockEntities.LARGE_NEST_BOX)
                    .serverTicks(LargeNestBoxBlockEntity::serverTick)))
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<ActiveCardinalBlock> SAMPLE_RACK = TFGCore.REGISTRATE.block("sample_rack", ActiveCardinalBlock::new)
            .properties(p -> p.sound(SoundType.COPPER).strength(5, 6).mapColor(MapColor.COLOR_LIGHT_GRAY).noOcclusion())
            .addLayer(() -> RenderType::cutout)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new).model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("item/sample_rack"))).build()
            .register();

    public static final BlockEntry<ActiveCardinalBlock> GROWTH_MONITOR = TFGCore.REGISTRATE.block("growth_monitor", ActiveCardinalBlock::new)
            .properties(p -> p.sound(SoundType.COPPER).strength(5, 6).mapColor(MapColor.COLOR_LIGHT_GRAY).noOcclusion().lightLevel((state) -> (int) (0.8 * 15.0F)))
            .addLayer(() -> RenderType::cutout)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .item(BlockItem::new).model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("item/growth_monitor"))).build()
            .register();

    public static final BlockEntry<ActiveCardinalBlock> CULTIVATION_MONITOR = TFGCore.REGISTRATE.block("cultivation_monitor", ActiveCardinalBlock::new)
            .properties(p -> p.sound(SoundType.COPPER).strength(5, 6).mapColor(MapColor.COLOR_LIGHT_GRAY).noCollission().noOcclusion().lightLevel((state) -> (int) (0.8 * 15.0F)))
            .blockstate(ModelUtils.existingActiveCardinalModel(TFGCore.id("block/machines/cultivation_monitor/cultivation_monitor")))
            .addLayer(() -> RenderType::cutout)
            .item(BlockItem::new).model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("item/cultivation_monitor"))).build()
            .register();

    //// Casings

    public static final BlockEntry<ActiveParticleBlock> BIOCULTURE_ROTOR_PRIMARY = TFGCore.REGISTRATE
            .block("bioculture_rotor_primary", p -> new ActiveParticleBlock(p.sound(SoundType.COPPER).strength(5f, 6f).mapColor(MapColor.COLOR_LIGHT_GRAY),
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
            .blockstate(ModelUtils.existingActiveParticleModel(TFGCore.id("block/machines/egh_planter/egh_planter")))
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

    public static final BlockEntry<Block> CLEAN_STAINLESS_STEEL_DESH_CASING = createCasingBlock("machine_casing_clean_stainless_steel_desh",
            GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_clean_stainless_steel_desh")));

    public static final BlockEntry<Block> DESH_PTFE_CASING = createCasingBlock("machine_casing_desh_ptfe", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_desh_ptfe")));

    public static final BlockEntry<Block> IRON_DESH_CASING = createCasingBlock("machine_casing_iron_desh", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_iron_desh")));

    public static final BlockEntry<Block> PTFE_DESH_CASING = createCasingBlock("machine_casing_ptfe_desh", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_ptfe_desh")));

    public static final BlockEntry<Block> STAINLESS_STEEL_DESH_CASING = createCasingBlock("machine_casing_stainless_steel_desh",
            GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_stainless_steel_desh")));

    public static final BlockEntry<Block> MARS_CASING = createCasingBlock("machine_casing_mars", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_mars")));

    public static final BlockEntry<Block> OSTRUM_CARBON_CASING = createCasingBlock("machine_casing_ostrum_carbon", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_ostrum_carbon")));

    public static final BlockEntry<Block> STAINLESS_EVAPORATION_CASING = createCasingBlock("machine_casing_stainless_evaporation",
            GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_stainless_evaporation")));

    public static final BlockEntry<Block> BLUE_SOLAR_PANEL_CASING = createCasingBlock("machine_casing_blue_solar_panel",
            (ctx, prov) -> prov.models().cubeBottomTop(ctx.getName(), GTCEu.id("block/casings/steam/steel/side"), GTCEu.id("block/casings/steam/steel/bottom"),
                    TFGCore.id("block/casings/machine_casing_blue_solar_panel")));

    public static final BlockEntry<Block> GREEN_SOLAR_PANEL_CASING = createCasingBlock("machine_casing_green_solar_panel",
            (ctx, prov) -> prov.models().cubeBottomTop(ctx.getName(), GTCEu.id("block/casings/steam/steel/side"), GTCEu.id("block/casings/steam/steel/bottom"),
                    TFGCore.id("block/casings/machine_casing_green_solar_panel")));

    public static final BlockEntry<Block> RED_SOLAR_PANEL_CASING = createCasingBlock("machine_casing_red_solar_panel",
            (ctx, prov) -> prov.models().cubeBottomTop(ctx.getName(), GTCEu.id("block/casings/steam/steel/side"), GTCEu.id("block/casings/steam/steel/bottom"),
                    TFGCore.id("block/casings/machine_casing_red_solar_panel")));

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

    public static final BlockEntry<Block> SUPERCONDUCTOR_COIL_LARGE_BLOCK = createCasingBlock("superconductor_coil_large", GTModels.cubeAllModel(TFGCore.id("block/casings/superconductor_coil_large")),
            SoundType.COPPER, 5.5f, 5.5f, MapColor.COLOR_ORANGE, false);

    public static final BlockEntry<Block> SUPERCONDUCTOR_COIL_SMALL_BLOCK = createCasingBlock("superconductor_coil_small", GTModels.cubeAllModel(TFGCore.id("block/casings/superconductor_coil_small")),
            SoundType.COPPER, 5.5f, 5.5f, MapColor.COLOR_ORANGE, false);

    public static final BlockEntry<ReflectorBlock> REFLECTOR_BLOCK = TFGCore.REGISTRATE.block("reflector", ReflectorBlock::new)
            .properties(p -> p.mapColor(MapColor.SNOW).strength(5.5F).sound(SoundType.AMETHYST)
                    .noOcclusion().isViewBlocking((state, level, pos) -> false))
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .register();

    public static final BlockEntry<Block> MACHINE_CASING_ALUMINIUM_PLATED_STEEL = createCasingBlock(
            "machine_casing_aluminium_plated_steel", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_aluminium_plated_steel")),
            SoundType.COPPER, 5.5f, 5.5f, MapColor.COLOR_LIGHT_BLUE, false);

    public static final BlockEntry<Block> MACHINE_CASING_POWER_CASING = createCasingBlock(
            "machine_casing_power_casing", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_power_casing")),
            SoundType.COPPER, 5.5f, 5.5f, MapColor.COLOR_LIGHT_BLUE, false);

    public static final BlockEntry<Block> HEAT_PIPE_CASING = createCasingBlock("heat_pipe_casing", GTModels.cubeAllModel(TFGCore.id("block/casings/heat_pipe_casing")),
            SoundType.COPPER, 5.5f, 6f, MapColor.COLOR_BLACK, false);

    public static final BlockEntry<Block> BIOCULTURE_CASING = createCasingBlock("machine_casing_bioculture", GTModels.cubeAllModel(TFGCore.id("block/casings/machine_casing_bioculture")),
            SoundType.COPPER, 5.5f, 6f, MapColor.COLOR_RED, false);

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

    public static final BlockEntry<ActiveBlock> BIOCULTURE_ROTOR_SECONDARY = TFGCore.REGISTRATE.block("bioculture_rotor_secondary", ActiveBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.sound(SoundType.COPPER).strength(6f, 5f).mapColor(MapColor.COLOR_LIGHT_GRAY).isValidSpawn((s, l, ps, e) -> false))
            .addLayer(() -> RenderType::cutoutMipped)
            .blockstate(ModelUtils.existingActiveModel(TFGCore.id("block/casings/bioculture_rotor_secondary")))
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, TFGTags.Blocks.Casings)
            .item(BlockItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/casings/bioculture_rotor_secondary")))
            .tag(TFGTags.Items.Casings)
            .build().register();

    public static final BlockEntry<ActiveBlock> VACUUM_ENGINE_INTAKE = createActiveCasingBlock("machine_casing_vacuum_engine_intake",
            ModelUtils.createActiveModel(TFGCore.id("block/casings/machine_casing_vacuum_engine_intake")),
            SoundType.METAL, 6, 5, MapColor.COLOR_LIGHT_GRAY, true);

    public static final BlockEntry<ActiveBlock> ULTRAVIOLET_CASING = createActiveCasingBlock("machine_casing_ultraviolet",
            ModelUtils.createActiveCasingModel(TFGCore.id("block/casings/machine_casing_ultraviolet")),
            SoundType.GLASS, 6, 5, MapColor.COLOR_LIGHT_GRAY, false);

    public static final BlockEntry<ActiveBlock> EGH_CASING = createActiveCasingBlock("machine_casing_egh",
            ModelUtils.createActiveCasingModel(TFGCore.id("block/casings/machine_casing_egh")),
            SoundType.METAL, 6, 5, MapColor.COLOR_LIGHT_GRAY, false);

    public static final BlockEntry<ActiveCardinalBlock> STERILIZING_PIPE_CASING = TFGCore.REGISTRATE.block("machine_casing_sterilizing_pipes", ActiveCardinalBlock::new)
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
}
