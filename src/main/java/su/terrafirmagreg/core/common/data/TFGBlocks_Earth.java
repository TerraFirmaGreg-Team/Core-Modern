package su.terrafirmagreg.core.common.data;

import static su.terrafirmagreg.core.common.data.TFGBlocks.dropBetween;

import java.util.Locale;
import java.util.Map;

import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.*;
import net.dries007.tfc.util.Helpers;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.Tags;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.CoarseDirtBlock;
import su.terrafirmagreg.core.common.data.blocks.ConnectedDuffBlock;

public class TFGBlocks_Earth {
    public static void init() {
    }

    ////// Connected texture grass blocks + dirt

    public static BlockEntry<CoarseDirtBlock> COARSE_SILTY_LOAM_DIRT;
    public static BlockEntry<CoarseDirtBlock> COARSE_SANDY_LOAM_DIRT;
    public static BlockEntry<CoarseDirtBlock> COARSE_SILT_DIRT;
    public static BlockEntry<CoarseDirtBlock> COARSE_LOAM_DIRT;
    public static BlockEntry<ConnectedDuffBlock> SILTY_LOAM_DUFF;
    public static BlockEntry<ConnectedDuffBlock> SANDY_LOAM_DUFF;
    public static BlockEntry<ConnectedDuffBlock> SILT_DUFF;
    public static BlockEntry<ConnectedDuffBlock> LOAM_DUFF;

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

    public static final Map<TFGPlant, BlockEntry<Block>> PLANTS = Helpers.mapOfKeys(TFGPlant.class,
            plant -> TFGCore.REGISTRATE.block("plant/" + plant.name().toLowerCase(Locale.ROOT), p -> plant.create())
                    .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                    .tag(TFCTags.Blocks.PLANTS, BlockTags.MINEABLE_WITH_HOE, TFCTags.Blocks.CAN_BE_ICE_PILED, TFCTags.Blocks.CAN_BE_SNOW_PILED, TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE,
                            BlockTags.REPLACEABLE)
                    .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
                    .register());

    // These are done separately to avoid cyclic references

    static {
        COARSE_SILTY_LOAM_DIRT = createCoarse("coarse_silty_loam_dirt", SoilBlockType.Variant.SILTY_LOAM);
        COARSE_SANDY_LOAM_DIRT = createCoarse("coarse_sandy_loam_dirt", SoilBlockType.Variant.SANDY_LOAM);
        COARSE_SILT_DIRT = createCoarse("coarse_silt_dirt", SoilBlockType.Variant.SILT);
        COARSE_LOAM_DIRT = createCoarse("coarse_loam_dirt", SoilBlockType.Variant.LOAM);

        SILTY_LOAM_DUFF = createDuff("silty_loam_duff", SoilBlockType.Variant.SILTY_LOAM);
        SANDY_LOAM_DUFF = createDuff("sandy_loam_duff", SoilBlockType.Variant.SANDY_LOAM);
        SILT_DUFF = createDuff("silt_duff", SoilBlockType.Variant.SILT);
        LOAM_DUFF = createDuff("loam_duff", SoilBlockType.Variant.LOAM);
    }

    private static BlockEntry<CoarseDirtBlock> createCoarse(String id, SoilBlockType.Variant tfcSoilType) {
        return TFGCore.REGISTRATE.block(id,
                p -> new CoarseDirtBlock(p,
                        () -> TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(tfcSoilType).get(),
                        () -> TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH).get(tfcSoilType).get(),
                        () -> TFCBlocks.SOIL.get(SoilBlockType.FARMLAND).get(tfcSoilType).get()))
                .initialProperties(() -> TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(tfcSoilType).get())
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(BlockTags.DIRT, TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL)
                .item(BlockItem::new)
                .tag(ItemTags.DIRT)
                .build()
                .register();
    }

    private static BlockEntry<ConnectedDuffBlock> createDuff(String id, SoilBlockType.Variant tfcSoilType) {
        return TFGCore.REGISTRATE.block(id,
                p -> new ConnectedDuffBlock(p.randomTicks(),
                        () -> TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(tfcSoilType).get(),
                        () -> TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH).get(tfcSoilType).get(),
                        () -> TFCBlocks.SOIL.get(SoilBlockType.FARMLAND).get(tfcSoilType).get()))
                .initialProperties(() -> TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(tfcSoilType).get())
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(BlockTags.DIRT, TFCTags.Blocks.CAN_CARVE, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL)
                .loot((ctx, prov) -> ctx.dropOther(prov, TFCBlocks.SOIL.get(SoilBlockType.DIRT).get(tfcSoilType).get()))
                .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .tag(ItemTags.DIRT)
                .build()
                .register();
    }
}
