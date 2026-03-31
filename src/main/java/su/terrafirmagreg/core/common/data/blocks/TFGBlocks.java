package su.terrafirmagreg.core.common.data.blocks;

import java.util.function.Supplier;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.Powder;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
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

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.*;
import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.utils.ModelUtils;

@SuppressWarnings({ "unused" })
public final class TFGBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TFGCore.MOD_ID);

    public static void init() {
        TFGBlocks_Earth.init();
        TFGBlocks_Mars.init();
        TFGBlocks_Casings.init();
        TFGBlocks_Buds.init();
        TFGBlocks_Wood.init();
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

    public static final BlockEntry<Block> VOLCANIC_ASH = TFGCore.REGISTRATE.block("volcanic_ash", Block::new)
            .initialProperties(TFCBlocks.SAND.get(SandBlockType.RED)::get)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
            .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/volcanic_ash")))
            .loot((ctx, p) -> ctx.add(p, LootTable.lootTable()))
            .item(BlockItem::new).build()
            .register();

    ////#region Martian sand piles and layer blocks, in order of color

    // Still in the pile folder because these are for the existing pre-0.11 layer blocks in peoples' worlds

    public static final BlockEntry<SandLayerBlock> ASH_LAYER_BLOCK = TFGCore.REGISTRATE.block("ash_pile", SandLayerBlock::new)
            .initialProperties(TFCBlocks.SAND.get(SandBlockType.RED)::get)
            .properties(p -> p.noOcclusion().mapColor(MapColor.NONE))
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .loot((ctx, b) -> ctx.dropOther(b, TFCItems.POWDERS.get(Powder.WOOD_ASH).get()))
            .item(BlockItem::new).model(ModelUtils.layeredItemModel(ResourceLocation.fromNamespaceAndPath("tfc", "item/powder/wood_ash"))).build()
            .register();

    public static final BlockEntry<SandLayerBlock> VOLCANIC_ASH_LAYER_BLOCK = TFGCore.REGISTRATE.block("pile/volcanic_ash", SandLayerBlock::new)
            .initialProperties(TFCBlocks.SAND.get(SandBlockType.RED)::get)
            .properties(p -> p.noOcclusion().mapColor(MapColor.NONE))
            .blockstate(ModelUtils.generateSandLayersFromBlock(TFGCore.id("block/volcanic_ash")))
            .loot((ctx, b) -> ctx.dropOther(b, ChemicalHelper.get(TagPrefix.dustSmall, GTMaterials.DarkAsh, 1).getItem()))
            .register();

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

    public static final BlockEntry<LiquidBlock> MUDDY_WATER = TFGCore.REGISTRATE.block("fluid/muddy_water", p -> new LiquidBlock(TFGFluids.MUDDY_WATER.source(), p))
            .initialProperties(() -> Blocks.WATER)
            .blockstate(ModelUtils.blockVariants(TFGCore.id("block/fluid/muddy_water")))
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN).noLootTable())
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

    public static final BlockEntry<DryIceBlock> DRY_ICE = TFGCore.REGISTRATE.block("dry_ice", DryIceBlock::new)
            .initialProperties(() -> Blocks.ICE)
            .properties(p -> p.sound(SoundType.BONE_BLOCK))
            .item(BlockItem::new).model(ModelUtils.layeredItemModel(TFGCore.id("item/dry_ice"))).build()
            .register();

    public static final BlockEntry<ArtisanTableBlock> ARTISAN_TABLE = TFGCore.REGISTRATE.block("artisan_table",
            (p) -> new ArtisanTableBlock(ExtendedProperties.of(TFCBlocks.WOODS.get(Wood.HICKORY).get(Wood.BlockType.SEWING_TABLE).get())))
            .blockstate((ctx, prov) -> ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().getExistingFile(TFGCore.id("block/artisan_table"))))
            .item(BlockItem::new).build()
            .register();

    public static final BlockEntry<ActiveCardinalBlock> SAMPLE_RACK = TFGCore.REGISTRATE.block("sample_rack", ActiveCardinalBlock::new)
            .properties(p -> p.sound(SoundType.COPPER).strength(5, 6).mapColor(MapColor.COLOR_LIGHT_GRAY).noOcclusion())
            .addLayer(() -> RenderType::cutout)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<ActiveCardinalBlock> GROWTH_MONITOR = TFGCore.REGISTRATE.block("growth_monitor", ActiveCardinalBlock::new)
            .properties(p -> p.sound(SoundType.COPPER).strength(5, 6).mapColor(MapColor.COLOR_LIGHT_GRAY).noOcclusion().lightLevel((state) -> (int) (0.8 * 15.0F)))
            .addLayer(() -> RenderType::cutout)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<ActiveCardinalBlock> CULTIVATION_MONITOR = TFGCore.REGISTRATE.block("cultivation_monitor", ActiveCardinalBlock::new)
            .properties(p -> p.sound(SoundType.COPPER).strength(5, 6).mapColor(MapColor.COLOR_LIGHT_GRAY).noCollission().noOcclusion().lightLevel((state) -> (int) (0.8 * 15.0F)))
            .blockstate(ModelUtils.existingActiveCardinalModel(TFGCore.id("block/machines/cultivation_monitor/cultivation_monitor")))
            .addLayer(() -> RenderType::cutout)
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<Block> QUARTZ_CRUCIBLE = TFGCore.REGISTRATE.block("quartz_crucible", Block::new)
            .properties(p -> p.sound(SoundType.STONE).strength(3).mapColor(MapColor.QUARTZ).noOcclusion())
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static <T extends Block> NonNullBiConsumer<RegistrateBlockLootTables, T> dropBetween(Supplier<Item> item, int min, int max) {
        return (ctx, b) -> ctx.add(b, LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(item.get()).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))))));
    }

}
