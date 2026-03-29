package su.terrafirmagreg.core.common.data.blocks;

import static su.terrafirmagreg.core.common.data.TFGItems.*;
import static su.terrafirmagreg.core.common.data.blocks.TFGBlocks.dropBetween;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.therighthon.rnr.common.block.TampedMudBlock;
import com.therighthon.rnr.common.block.TampedSoilBlock;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.DryingBricksBlock;
import net.dries007.tfc.common.blocks.soil.*;
import net.dries007.tfc.util.Helpers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.*;
import su.terrafirmagreg.core.common.data.TFGPlant;

@SuppressWarnings("unused")
public class TFGBlocks_Earth {
    public static void init() {
    }

    // Thanks TFC
    public static TagKey<Item> TFCDirtItemTag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "dirt"));
    public static TagKey<Item> TFCGrassItemTag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "grass"));
    public static TagKey<Item> TFCFarmlandItemTag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "farmland"));
    public static TagKey<Item> TFCDryMudBricksItemTag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "dry_mud_bricks"));
    public static TagKey<Item> TFCPathItemTag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "paths"));
    public static TagKey<Item> TFCMudItemTag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "mud"));
    public static TagKey<Item> TFCClayItemTag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "clay"));

    public static TagKey<Block> TFCDirtBlockTag = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "dirt"));
    public static TagKey<Block> TFCMudBricksBlockTag = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "mud_bricks"));

    // New TFC Worldgen
    public static final BlockEntry<Block> TUFF_GRAVEL = TFGCore.REGISTRATE.block("tuff_gravel", Block::new)
            .initialProperties(() -> Blocks.GRAVEL)
            .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/tuff_gravel")))
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_GRAY))
            .tag(Tags.Blocks.GRAVEL, TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL)
            .item(BlockItem::new)
            .tag(Tags.Items.GRAVEL)
            .build()
            .register();

    public static final BlockEntry<Block> HARDENED_CLAY = TFGCore.REGISTRATE.block("hardened_clay", Block::new)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .strength(7.0F)
                    .sound(SoundType.PACKED_MUD)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops())
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(TFCTags.Blocks.CAN_CARVE, BlockTags.MINEABLE_WITH_SHOVEL)
            .item(BlockItem::new).build()
            .loot(dropBetween(() -> Items.CLAY_BALL, 1, 3))
            .register();

    public static final BlockEntry<Block> HALITE = TFGCore.REGISTRATE.block("halite", Block::new)
            .properties(p -> p
                    .mapColor(MapColor.QUARTZ)
                    .strength(6.0F)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops())
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(TFCTags.Blocks.CAN_CARVE, BlockTags.MINEABLE_WITH_PICKAXE)
            .item(BlockItem::new).build()
            .register();

    public static final BlockEntry<TierLockedBlock> CARBONATE_HORNFELS = TFGCore.REGISTRATE.block("carbonate_hornfels", TierLockedBlock::new)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_BROWN)
                    .sound(SoundType.DEEPSLATE)
                    .strength(20)
                    .explosionResistance(2)
                    .requiresCorrectToolForDrops()
                    .pushReaction(PushReaction.IGNORE))
            .tag(TFCTags.Blocks.CAN_CARVE, BlockTags.MINEABLE_WITH_PICKAXE, TFCTags.Blocks.CAN_COLLAPSE, Tags.Blocks.STONE, BlockTags.NEEDS_DIAMOND_TOOL)
            .simpleItem()
            .register();

    public static final BlockEntry<TierLockedBlock> PELITIC_HORNFELS = TFGCore.REGISTRATE.block("pelitic_hornfels", TierLockedBlock::new)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .sound(SoundType.DEEPSLATE)
                    .strength(20)
                    .explosionResistance(2)
                    .requiresCorrectToolForDrops()
                    .pushReaction(PushReaction.IGNORE))
            .tag(TFCTags.Blocks.CAN_CARVE, BlockTags.MINEABLE_WITH_PICKAXE, TFCTags.Blocks.CAN_COLLAPSE, Tags.Blocks.STONE, BlockTags.NEEDS_DIAMOND_TOOL)
            .simpleItem()
            .register();

    public static final BlockEntry<TierLockedBlock> MAFIC_HORNFELS = TFGCore.REGISTRATE.block("mafic_hornfels", TierLockedBlock::new)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_BROWN)
                    .sound(SoundType.DEEPSLATE)
                    .strength(20)
                    .explosionResistance(2)
                    .requiresCorrectToolForDrops()
                    .pushReaction(PushReaction.IGNORE))
            .tag(TFCTags.Blocks.CAN_CARVE, BlockTags.MINEABLE_WITH_PICKAXE, TFCTags.Blocks.CAN_COLLAPSE, Tags.Blocks.STONE, BlockTags.NEEDS_DIAMOND_TOOL)
            .simpleItem()
            .register();

    public static final Map<TFGPlant, BlockEntry<Block>> PLANTS = Helpers.mapOfKeys(TFGPlant.class,
            plant -> TFGCore.REGISTRATE.block("plant/" + plant.name().toLowerCase(Locale.ROOT), p -> plant.create())
                    .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                    .tag(TFCTags.Blocks.PLANTS, BlockTags.MINEABLE_WITH_HOE, TFCTags.Blocks.CAN_BE_ICE_PILED, TFCTags.Blocks.CAN_BE_SNOW_PILED, TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE,
                            BlockTags.REPLACEABLE)
                    .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
                    .register());

    // Coarse dirt for old soil types
    public static BlockEntry<CoarseDirtBlock> COARSE_SILTY_LOAM_DIRT;
    public static BlockEntry<CoarseDirtBlock> COARSE_SANDY_LOAM_DIRT;
    public static BlockEntry<CoarseDirtBlock> COARSE_SILT_DIRT;
    public static BlockEntry<CoarseDirtBlock> COARSE_LOAM_DIRT;
    // Duff for old soil types
    public static BlockEntry<ConnectedDuffBlock> SILTY_LOAM_DUFF;
    public static BlockEntry<ConnectedDuffBlock> SANDY_LOAM_DUFF;
    public static BlockEntry<ConnectedDuffBlock> SILT_DUFF;
    public static BlockEntry<ConnectedDuffBlock> LOAM_DUFF;
    // Alfisol
    public static final BlockEntry<MudBlock> ALFISOL_MUD = createMud("mud/alfisol");
    public static final BlockEntry<RotatedPillarBlock> ALFISOL_MUDDY_ROOTS = createMuddyRoots("muddy_roots/alfisol");
    public static BlockEntry<DirtBlock> ALFISOL_DIRT;
    public static BlockEntry<PathBlock> ALFISOL_PATH;
    public static BlockEntry<FarmlandBlock> ALFISOL_FARMLAND;
    public static BlockEntry<ConnectedGrassBlock> ALFISOL_GRASS;
    public static BlockEntry<TFCRootedDirtBlock> ALFISOL_ROOTED;
    public static BlockEntry<DirtBlock> ALFISOL_CLAY;
    public static BlockEntry<ConnectedGrassBlock> ALFISOL_CLAY_GRASS;
    public static BlockEntry<DryingBricksBlock> ALFISOL_DRYING_BRICKS;
    public static BlockEntry<CoarseDirtBlock> ALFISOL_COARSE;
    public static BlockEntry<ConnectedDuffBlock> ALFISOL_DUFF;
    public static BlockEntry<TampedSoilBlock> TAMPED_SOIL_ALFISOL = createTampedSoil("alfisol");
    public static BlockEntry<TampedMudBlock> TAMPED_MUD_ALFISOL = createTampedMud("alfisol");
    // Mollisol (normally Andisol is the volcanic one, but we're already using that texture for Silty Loam)
    public static final BlockEntry<MudBlock> MOLLISOL_MUD = createMud("mud/mollisol");
    public static final BlockEntry<RotatedPillarBlock> MOLLISOL_MUDDY_ROOTS = createMuddyRoots("muddy_roots/mollisol");
    public static BlockEntry<DirtBlock> MOLLISOL_DIRT;
    public static BlockEntry<PathBlock> MOLLISOL_PATH;
    public static BlockEntry<FarmlandBlock> MOLLISOL_FARMLAND;
    public static BlockEntry<ConnectedGrassBlock> MOLLISOL_GRASS;
    public static BlockEntry<TFCRootedDirtBlock> MOLLISOL_ROOTED;
    public static BlockEntry<DirtBlock> MOLLISOL_CLAY;
    public static BlockEntry<ConnectedGrassBlock> MOLLISOL_CLAY_GRASS;
    public static BlockEntry<DryingBricksBlock> MOLLISOL_DRYING_BRICKS;
    public static BlockEntry<CoarseDirtBlock> MOLLISOL_COARSE;
    public static BlockEntry<ConnectedDuffBlock> MOLLISOL_DUFF;
    public static BlockEntry<TampedSoilBlock> TAMPED_SOIL_MOLLISOL = createTampedSoil("mollisol");
    public static BlockEntry<TampedMudBlock> TAMPED_MUD_MOLLISOL = createTampedMud("mollisol");
    // Oxisol
    public static final BlockEntry<MudBlock> OXISOL_MUD = createMud("mud/oxisol");
    public static final BlockEntry<RotatedPillarBlock> OXISOL_MUDDY_ROOTS = createMuddyRoots("muddy_roots/oxisol");
    public static BlockEntry<DirtBlock> OXISOL_DIRT;
    public static BlockEntry<PathBlock> OXISOL_PATH;
    public static BlockEntry<FarmlandBlock> OXISOL_FARMLAND;
    public static BlockEntry<ConnectedGrassBlock> OXISOL_GRASS;
    public static BlockEntry<TFCRootedDirtBlock> OXISOL_ROOTED;
    public static BlockEntry<DirtBlock> OXISOL_CLAY;
    public static BlockEntry<ConnectedGrassBlock> OXISOL_CLAY_GRASS;
    public static BlockEntry<DryingBricksBlock> OXISOL_DRYING_BRICKS;
    public static BlockEntry<CoarseDirtBlock> OXISOL_COARSE;
    public static BlockEntry<ConnectedDuffBlock> OXISOL_DUFF;
    public static BlockEntry<TampedSoilBlock> TAMPED_SOIL_OXISOL = createTampedSoil("oxisol");
    public static BlockEntry<TampedMudBlock> TAMPED_MUD_OXISOL = createTampedMud("oxisol");
    // Podzol
    public static final BlockEntry<MudBlock> PODZOL_MUD = createMud("mud/podzol");
    public static final BlockEntry<RotatedPillarBlock> PODZOL_MUDDY_ROOTS = createMuddyRoots("muddy_roots/podzol");
    public static BlockEntry<DirtBlock> PODZOL_DIRT;
    public static BlockEntry<PathBlock> PODZOL_PATH;
    public static BlockEntry<FarmlandBlock> PODZOL_FARMLAND;
    public static BlockEntry<ConnectedGrassBlock> PODZOL_GRASS;
    public static BlockEntry<TFCRootedDirtBlock> PODZOL_ROOTED;
    public static BlockEntry<DirtBlock> PODZOL_CLAY;
    public static BlockEntry<ConnectedGrassBlock> PODZOL_CLAY_GRASS;
    public static BlockEntry<DryingBricksBlock> PODZOL_DRYING_BRICKS;
    public static BlockEntry<CoarseDirtBlock> PODZOL_COARSE;
    public static BlockEntry<ConnectedDuffBlock> PODZOL_DUFF;
    public static BlockEntry<TampedSoilBlock> TAMPED_SOIL_PODZOL = createTampedSoil("podzol");
    public static BlockEntry<TampedMudBlock> TAMPED_MUD_PODZOL = createTampedMud("podzol");

    // Oil-based blocks for fluid vein indicators

    public static final BlockEntry<Block> GILSONITE = TFGCore.REGISTRATE.block("gilsonite", Block::new)
            .properties(p -> p
                    .mapColor(MapColor.DIRT)
                    .strength(3.0F)
                    .sound(SoundType.BONE_BLOCK)
                    .requiresCorrectToolForDrops())
            .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/fluid_vein/gilsonite")))
            .tag(TFCTags.Blocks.CAN_CARVE, BlockTags.MINEABLE_WITH_PICKAXE)
            .item(BlockItem::new).build()
            .register();

    public static BlockEntry<OilTarBlock> OIL_TAR = createOilTar("oil");
    public static BlockEntry<OilTarBlock> RAW_OIL_TAR = createOilTar("raw_oil");
    public static BlockEntry<OilTarBlock> LIGHT_OIL_TAR = createOilTar("light_oil");
    public static BlockEntry<OilTarBlock> HEAVY_OIL_TAR = createOilTar("heavy_oil");

    public static BlockEntry<OilSlickBlock> OIL_SLICK = createOilSlick("oil");
    public static BlockEntry<OilSlickBlock> RAW_OIL_SLICK = createOilSlick("raw_oil");
    public static BlockEntry<OilSlickBlock> LIGHT_OIL_SLICK = createOilSlick("light_oil");
    public static BlockEntry<OilSlickBlock> HEAVY_OIL_SLICK = createOilSlick("heavy_oil");

    // These are done separately to avoid cyclic references
    static {
        COARSE_SILTY_LOAM_DIRT = createCoarse("coarse_dirt/silty_loam",
                TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(SoilBlockType.Variant.SILTY_LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH).get(SoilBlockType.Variant.SILTY_LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.FARMLAND).get(SoilBlockType.Variant.SILTY_LOAM));
        COARSE_SANDY_LOAM_DIRT = createCoarse("coarse_dirt/sandy_loam",
                TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(SoilBlockType.Variant.SANDY_LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH).get(SoilBlockType.Variant.SANDY_LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.FARMLAND).get(SoilBlockType.Variant.SANDY_LOAM));
        COARSE_SILT_DIRT = createCoarse("coarse_dirt/silt",
                TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(SoilBlockType.Variant.SILT),
                TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH).get(SoilBlockType.Variant.SILT),
                TFCBlocks.SOIL.get(SoilBlockType.FARMLAND).get(SoilBlockType.Variant.SILT));
        COARSE_LOAM_DIRT = createCoarse("coarse_dirt/loam",
                TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(SoilBlockType.Variant.LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH).get(SoilBlockType.Variant.LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.FARMLAND).get(SoilBlockType.Variant.LOAM));

        SILTY_LOAM_DUFF = createDuff("duff/silty_loam",
                TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(SoilBlockType.Variant.SILTY_LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH).get(SoilBlockType.Variant.SILTY_LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.FARMLAND).get(SoilBlockType.Variant.SILTY_LOAM));
        SANDY_LOAM_DUFF = createDuff("duff/sandy_loam",
                TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(SoilBlockType.Variant.SANDY_LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH).get(SoilBlockType.Variant.SANDY_LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.FARMLAND).get(SoilBlockType.Variant.SANDY_LOAM));
        SILT_DUFF = createDuff("duff/silt",
                TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(SoilBlockType.Variant.SILT),
                TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH).get(SoilBlockType.Variant.SILT),
                TFCBlocks.SOIL.get(SoilBlockType.FARMLAND).get(SoilBlockType.Variant.SILT));
        LOAM_DUFF = createDuff("duff/loam",
                TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(SoilBlockType.Variant.LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH).get(SoilBlockType.Variant.LOAM),
                TFCBlocks.SOIL.get(SoilBlockType.FARMLAND).get(SoilBlockType.Variant.LOAM));

        ALFISOL_DIRT = createDirt("dirt/alfisol", () -> ALFISOL_GRASS.get(), () -> ALFISOL_PATH.get(), () -> ALFISOL_FARMLAND.get(), () -> ALFISOL_ROOTED.get(), ALFISOL_MUD);
        ALFISOL_PATH = createPath("grass_path/alfisol", ALFISOL_DIRT);
        ALFISOL_FARMLAND = createFarmland("farmland/alfisol", ALFISOL_DIRT);
        ALFISOL_GRASS = createGrass("grass/alfisol", ALFISOL_DIRT, ALFISOL_PATH, ALFISOL_FARMLAND);
        ALFISOL_ROOTED = createRooted("rooted_dirt/alfisol", ALFISOL_DIRT, ALFISOL_MUD);
        ALFISOL_CLAY = createClay("clay/alfisol", ALFISOL_GRASS, ALFISOL_PATH, ALFISOL_FARMLAND, ALFISOL_ROOTED, ALFISOL_MUD);
        ALFISOL_CLAY_GRASS = createClayGrass("clay_grass/alfisol", ALFISOL_DIRT, ALFISOL_PATH, ALFISOL_FARMLAND);
        ALFISOL_DRYING_BRICKS = createDryingBricks("drying_bricks/alfisol", ALFISOL_MUD_BRICK);
        ALFISOL_COARSE = createCoarse("coarse_dirt/alfisol", ALFISOL_DIRT, ALFISOL_PATH, ALFISOL_FARMLAND);
        ALFISOL_DUFF = createDuff("duff/alfisol", ALFISOL_DIRT, ALFISOL_PATH, ALFISOL_FARMLAND);

        MOLLISOL_DIRT = createDirt("dirt/mollisol", () -> MOLLISOL_GRASS.get(), () -> MOLLISOL_PATH.get(), () -> MOLLISOL_FARMLAND.get(), () -> MOLLISOL_ROOTED.get(), MOLLISOL_MUD);
        MOLLISOL_PATH = createPath("grass_path/mollisol", MOLLISOL_DIRT);
        MOLLISOL_FARMLAND = createFarmland("farmland/mollisol", MOLLISOL_DIRT);
        MOLLISOL_GRASS = createGrass("grass/mollisol", MOLLISOL_DIRT, MOLLISOL_PATH, MOLLISOL_FARMLAND);
        MOLLISOL_ROOTED = createRooted("rooted_dirt/mollisol", MOLLISOL_DIRT, MOLLISOL_MUD);
        MOLLISOL_CLAY = createClay("clay/mollisol", MOLLISOL_GRASS, MOLLISOL_PATH, MOLLISOL_FARMLAND, MOLLISOL_ROOTED, MOLLISOL_MUD);
        MOLLISOL_CLAY_GRASS = createClayGrass("clay_grass/mollisol", MOLLISOL_DIRT, MOLLISOL_PATH, MOLLISOL_FARMLAND);
        MOLLISOL_DRYING_BRICKS = createDryingBricks("drying_bricks/mollisol", MOLLISOL_MUD_BRICK);
        MOLLISOL_COARSE = createCoarse("coarse_dirt/mollisol", MOLLISOL_DIRT, MOLLISOL_PATH, MOLLISOL_FARMLAND);
        MOLLISOL_DUFF = createDuff("duff/mollisol", MOLLISOL_DIRT, MOLLISOL_PATH, MOLLISOL_FARMLAND);

        OXISOL_DIRT = createDirt("dirt/oxisol", () -> OXISOL_GRASS.get(), () -> OXISOL_PATH.get(), () -> OXISOL_FARMLAND.get(), () -> OXISOL_ROOTED.get(), OXISOL_MUD);
        OXISOL_PATH = createPath("grass_path/oxisol", OXISOL_DIRT);
        OXISOL_FARMLAND = createFarmland("farmland/oxisol", OXISOL_DIRT);
        OXISOL_GRASS = createGrass("grass/oxisol", OXISOL_DIRT, OXISOL_PATH, OXISOL_FARMLAND);
        OXISOL_ROOTED = createRooted("rooted_dirt/oxisol", OXISOL_DIRT, OXISOL_MUD);
        OXISOL_CLAY = createClay("clay/oxisol", OXISOL_GRASS, OXISOL_PATH, OXISOL_FARMLAND, OXISOL_ROOTED, OXISOL_MUD);
        OXISOL_CLAY_GRASS = createClayGrass("clay_grass/oxisol", OXISOL_DIRT, OXISOL_PATH, OXISOL_FARMLAND);
        OXISOL_DRYING_BRICKS = createDryingBricks("drying_bricks/oxisol", OXISOL_MUD_BRICK);
        OXISOL_COARSE = createCoarse("coarse_dirt/oxisol", OXISOL_DIRT, OXISOL_PATH, OXISOL_FARMLAND);
        OXISOL_DUFF = createDuff("duff/oxisol", OXISOL_DIRT, OXISOL_PATH, OXISOL_FARMLAND);

        PODZOL_DIRT = createDirt("dirt/podzol", () -> PODZOL_GRASS.get(), () -> PODZOL_PATH.get(), () -> PODZOL_FARMLAND.get(), () -> PODZOL_ROOTED.get(), PODZOL_MUD);
        PODZOL_PATH = createPath("grass_path/podzol", PODZOL_DIRT);
        PODZOL_FARMLAND = createFarmland("farmland/podzol", PODZOL_DIRT);
        PODZOL_GRASS = createGrass("grass/podzol", PODZOL_DIRT, PODZOL_PATH, PODZOL_FARMLAND);
        PODZOL_ROOTED = createRooted("rooted_dirt/podzol", PODZOL_DIRT, PODZOL_MUD);
        PODZOL_CLAY = createClay("clay/podzol", PODZOL_GRASS, PODZOL_PATH, PODZOL_FARMLAND, PODZOL_ROOTED, PODZOL_MUD);
        PODZOL_CLAY_GRASS = createClayGrass("clay_grass/podzol", PODZOL_DIRT, PODZOL_PATH, PODZOL_FARMLAND);
        PODZOL_DRYING_BRICKS = createDryingBricks("drying_bricks/podzol", PODZOL_MUD_BRICK);
        PODZOL_COARSE = createCoarse("coarse_dirt/podzol", PODZOL_DIRT, PODZOL_PATH, PODZOL_FARMLAND);
        PODZOL_DUFF = createDuff("duff/podzol", PODZOL_DIRT, PODZOL_PATH, PODZOL_FARMLAND);
    }

    private static BlockEntry<CoarseDirtBlock> createCoarse(String id, Supplier<? extends Block> dirt, Supplier<? extends Block> path, Supplier<? extends Block> farmland) {
        return TFGCore.REGISTRATE.block(id,
                p -> new CoarseDirtBlock(p, dirt, path, farmland))
                .initialProperties(dirt::get)
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(BlockTags.DIRT, TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL, TFCDirtBlockTag)
                .item(BlockItem::new)
                .tag(ItemTags.DIRT, TFCDirtItemTag)
                .build()
                .register();
    }

    private static BlockEntry<ConnectedDuffBlock> createDuff(String id, Supplier<? extends Block> dirt, Supplier<? extends Block> path, Supplier<? extends Block> farmland) {
        return TFGCore.REGISTRATE.block(id,
                p -> new ConnectedDuffBlock(p.randomTicks(), dirt, path, farmland))
                .initialProperties(dirt::get)
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(BlockTags.DIRT, TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL, TFCDirtBlockTag)
                .loot((ctx, prov) -> ctx.dropOther(prov, dirt.get()))
                .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .tag(ItemTags.DIRT, TFCDirtItemTag)
                .build()
                .register();
    }

    public static BlockEntry<DirtBlock> createDirt(String id, Supplier<ConnectedGrassBlock> grass, Supplier<PathBlock> path,
            Supplier<FarmlandBlock> farmland, @Nullable Supplier<TFCRootedDirtBlock> rooted, @Nullable Supplier<MudBlock> mud) {
        return TFGCore.REGISTRATE.block(id,
                p -> new DirtBlock(p, grass, path, farmland, rooted, mud))
                .properties(p -> p.mapColor(MapColor.DIRT).strength(1.4f).sound(SoundType.GRAVEL))
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(BlockTags.DIRT, TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL, TFCDirtBlockTag)
                .item(BlockItem::new)
                .tag(ItemTags.DIRT, TFCDirtItemTag)
                .build()
                .register();
    }

    public static BlockEntry<PathBlock> createPath(String id, Supplier<DirtBlock> dirt) {
        return TFGCore.REGISTRATE.block(id, p -> new PathBlock(p, dirt))
                .properties(p -> p.mapColor(MapColor.DIRT).strength(1.5f).sound(SoundType.GRAVEL))
                .loot((ctx, prov) -> ctx.dropOther(prov, dirt.get()))
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL)
                .item(BlockItem::new)
                .tag(TFCPathItemTag)
                .build()
                .register();
    }

    public static BlockEntry<DirtBlock> createClay(String id, Supplier<ConnectedGrassBlock> grass, Supplier<PathBlock> path,
            BlockEntry<FarmlandBlock> farmland, @Nullable BlockEntry<TFCRootedDirtBlock> rooted, @Nullable BlockEntry<MudBlock> mud) {
        return TFGCore.REGISTRATE.block(id,
                p -> new DirtBlock(p, grass, path, farmland, rooted, mud))
                .properties(p -> p.mapColor(MapColor.DIRT).strength(1.5f).sound(SoundType.GRAVEL))
                .loot(dropBetween(() -> Items.CLAY_BALL, 1, 3))
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(BlockTags.DIRT, TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL, TFCDirtBlockTag)
                .item(BlockItem::new)
                .tag(TFCDirtItemTag, TFCClayItemTag)
                .build()
                .register();
    }

    public static BlockEntry<ConnectedGrassBlock> createClayGrass(String id, Supplier<DirtBlock> dirt, Supplier<PathBlock> path, Supplier<FarmlandBlock> farmland) {
        return createClayGrass(id, dirt, path, farmland, p -> p.mapColor(MapColor.GRASS).strength(1.8f).sound(SoundType.GRASS).randomTicks());
    }

    public static BlockEntry<ConnectedGrassBlock> createClayGrass(String id, Supplier<DirtBlock> dirt, Supplier<PathBlock> path, Supplier<FarmlandBlock> farmland,
            NonNullUnaryOperator<BlockBehaviour.Properties> props) {
        return TFGCore.REGISTRATE.block(id,
                p -> new ConnectedGrassBlock(p, dirt, path, farmland))
                .properties(props)
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(TFCTags.Blocks.GRASS, TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL)
                .loot(dropBetween(() -> Items.CLAY_BALL, 1, 3))
                .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .tag(TFCGrassItemTag, TFCClayItemTag)
                .build()
                .register();
    }

    private static BlockEntry<ConnectedGrassBlock> createGrass(String id, Supplier<DirtBlock> dirt, Supplier<PathBlock> path, Supplier<FarmlandBlock> farmland) {
        return createGrass(id, dirt, path, farmland, p -> p.mapColor(MapColor.GRASS).strength(1.8f).sound(SoundType.GRASS).randomTicks());
    }

    public static BlockEntry<ConnectedGrassBlock> createGrass(String id, Supplier<DirtBlock> dirt, Supplier<PathBlock> path, Supplier<FarmlandBlock> farmland,
            NonNullUnaryOperator<BlockBehaviour.Properties> props) {
        return TFGCore.REGISTRATE.block(id,
                p -> new ConnectedGrassBlock(p, dirt, path, farmland))
                .properties(props)
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(TFCTags.Blocks.GRASS, TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL)
                .loot((ctx, prov) -> ctx.dropOther(prov, dirt.get()))
                .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .tag(TFCGrassItemTag)
                .build()
                .register();
    }

    public static BlockEntry<FarmlandBlock> createFarmland(String id, Supplier<DirtBlock> dirt) {
        return TFGCore.REGISTRATE.block(id,
                p -> new FarmlandBlock(ExtendedProperties.of(p).mapColor(MapColor.DIRT).strength(1.3f).sound(SoundType.GRAVEL).isViewBlocking(TFCBlocks::always).isSuffocating(TFCBlocks::always)
                        .blockEntity(TFCBlockEntities.FARMLAND), dirt))
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(TFCTags.Blocks.FARMLAND, TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL)
                .loot((ctx, prov) -> ctx.dropOther(prov, dirt.get()))
                .item(BlockItem::new)
                .tag(TFCFarmlandItemTag)
                .build()
                .register();
    }

    private static BlockEntry<TFCRootedDirtBlock> createRooted(String id, Supplier<DirtBlock> dirt, Supplier<MudBlock> mud) {
        return TFGCore.REGISTRATE.block(id,
                p -> new TFCRootedDirtBlock(p.mapColor(MapColor.DIRT).strength(2f).sound(SoundType.ROOTED_DIRT), dirt, mud))
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(BlockTags.DIRT, TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL, TFCDirtBlockTag)
                .item(BlockItem::new)
                .tag(ItemTags.DIRT, TFCDirtItemTag)
                .build()
                .register();
    }

    private static BlockEntry<MudBlock> createMud(String id) {
        return TFGCore.REGISTRATE.block(id,
                p -> new MudBlock(p.mapColor(MapColor.DIRT).sound(SoundType.MUD).strength(2f).speedFactor(0.8f).isRedstoneConductor(TFCBlocks::always).isViewBlocking(TFCBlocks::always)
                        .isSuffocating(TFCBlocks::always).instrument(NoteBlockInstrument.BASEDRUM)))
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL)
                .item(BlockItem::new)
                .tag(TFCMudItemTag)
                .build()
                .register();
    }

    private static BlockEntry<DryingBricksBlock> createDryingBricks(String id, ItemEntry<Item> dryItem) {
        return TFGCore.REGISTRATE.block(id,
                p -> new DryingBricksBlock(
                        ExtendedProperties.of(p).mapColor(MapColor.DIRT).noCollission().noOcclusion().instabreak().sound(SoundType.STEM).randomTicks().blockEntity(TFCBlockEntities.TICK_COUNTER),
                        dryItem))
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(BlockTags.MINEABLE_WITH_SHOVEL, TFCMudBricksBlockTag)
                .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .tag(TFCDryMudBricksItemTag)
                .build()
                .register();
    }

    private static BlockEntry<RotatedPillarBlock> createMuddyRoots(String id) {
        return TFGCore.REGISTRATE.block(id, p -> new RotatedPillarBlock(p.strength(4f)))
                .initialProperties(() -> Blocks.MUDDY_MANGROVE_ROOTS)
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .simpleItem()
                .register();
    }

    private static BlockEntry<TampedSoilBlock> createTampedSoil(String dirtBlock) {
        return TFGCore.REGISTRATE.block("tamped/dirt/" + dirtBlock, TampedSoilBlock::new)
                .properties(p -> p
                        .mapColor(MapColor.DIRT)
                        .strength(3.0F)
                        .sound(SoundType.ROOTED_DIRT))
                .tag(TFCTags.Blocks.CAN_LANDSLIDE, TFCTags.Blocks.SUPPORTS_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL)
                .blockstate((blockTampedSoilBlockDataGenContext, registrateBlockstateProvider) -> {
                    var model = registrateBlockstateProvider.models()
                            .withExistingParent(blockTampedSoilBlockDataGenContext.getName(), ResourceLocation.fromNamespaceAndPath("rnr", "block/tamped_block"))
                            .texture("dirt", TFGCore.id("block/dirt/" + dirtBlock));
                    registrateBlockstateProvider.simpleBlock(blockTampedSoilBlockDataGenContext.getEntry(), model);
                })
                .simpleItem()
                .register();
    }

    private static BlockEntry<TampedMudBlock> createTampedMud(String mudBlock) {
        return TFGCore.REGISTRATE.block("tamped/mud/" + mudBlock, TampedMudBlock::new)
                .properties(p -> p
                        .mapColor(MapColor.DIRT)
                        .strength(3.0F)
                        .sound(SoundType.ROOTED_DIRT))
                .tag(TFCTags.Blocks.CAN_LANDSLIDE, TFCTags.Blocks.SUPPORTS_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL)
                .blockstate((blockTampedSoilBlockDataGenContext, registrateBlockstateProvider) -> {
                    var model = registrateBlockstateProvider.models()
                            .withExistingParent(blockTampedSoilBlockDataGenContext.getName(), ResourceLocation.fromNamespaceAndPath("rnr", "block/tamped_block"))
                            .texture("dirt", TFGCore.id("block/mud/" + mudBlock));
                    registrateBlockstateProvider.simpleBlock(blockTampedSoilBlockDataGenContext.getEntry(), model);
                })
                .simpleItem()
                .register();
    }

    private static BlockEntry<OilTarBlock> createOilTar(String oilType) {
        return TFGCore.REGISTRATE.block(oilType + "_tar", OilTarBlock::new)
                .properties(p -> p
                        .mapColor(MapColor.COLOR_BLACK)
                        .strength(2.0F)
                        .speedFactor(0.2F)
                        .jumpFactor(0.4F)
                        .sound(SoundType.HONEY_BLOCK))
                .exBlockstate(GTModels.cubeAllModel(TFGCore.id("block/fluid_vein/" + oilType + "_tar")))
                .tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .item(BlockItem::new).build()
                .register();
    }

    //I don't know how to get it to use my custom model, but putting it in kjs works
    private static BlockEntry<OilSlickBlock> createOilSlick(String oilType) {
        return TFGCore.REGISTRATE.block(oilType + "_slick", OilSlickBlock::new)
                .properties(p -> p
                        .mapColor(MapColor.COLOR_BLACK)
                        .strength(1.0F)
                        .speedFactor(0.6F)
                        .sound(SoundType.SLIME_BLOCK))
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .loot((ctx, p) -> ctx.add(p, LootTable.lootTable()))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .item(BlockItem::new).build()
                .register();
    }

}
