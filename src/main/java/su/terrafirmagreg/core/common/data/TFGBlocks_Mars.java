package su.terrafirmagreg.core.common.data;

import static su.terrafirmagreg.core.common.data.TFGBlocks.dropBetween;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IcicleBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.dries007.tfc.common.blocks.soil.DirtBlock;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.PathBlock;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blockentity.LargeNestBoxBlockEntity;
import su.terrafirmagreg.core.common.data.blocks.LargeNestBoxBlock;
import su.terrafirmagreg.core.common.data.blocks.MarsIceBlock;

public class TFGBlocks_Mars {
    public static void init() {
    }

    // This one's constructor needs to reference the others, so it's in the static constructor below
    public static BlockEntry<DirtBlock> MARS_DIRT;
    public static BlockEntry<DirtBlock> MARS_CLAY;

    public static final BlockEntry<PathBlock> MARS_PATH = TFGCore.REGISTRATE.block("grass/mars_path", p -> new PathBlock(p, MARS_DIRT))
            .properties(p -> p.mapColor(MapColor.DIRT)
                    .strength(1.4f)
                    .sound(SoundType.GRAVEL))
            .simpleItem()
            .loot((ctx, prov) -> ctx.dropOther(prov, MARS_DIRT))
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .register();

    public static final BlockEntry<FarmlandBlock> MARS_FARMLAND = TFGCore.REGISTRATE.block("grass/mars_farmland",
            p -> new FarmlandBlock(ExtendedProperties.of(MapColor.DIRT)
                    .strength(1.3f)
                    .sound(SoundType.GRAVEL)
                    .isViewBlocking(TFCBlocks::always)
                    .isSuffocating(TFCBlocks::always)
                    .blockEntity(TFCBlockEntities.FARMLAND), MARS_DIRT))
            .simpleItem()
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
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

    public static final BlockEntry<MarsIceBlock> MARS_ICE = TFGCore.REGISTRATE.block("mars_ice", MarsIceBlock::new)
            .initialProperties(() -> Blocks.ICE)
            .simpleItem()
            .register();

    public static final BlockEntry<IcicleBlock> MARS_ICICLE = TFGCore.REGISTRATE.block("mars_icicle", IcicleBlock::new)
            .initialProperties(TFCBlocks.ICICLE::get)
            .properties(BlockBehaviour.Properties::noLootTable)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build().register();

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

    // These are done separately to avoid cyclic references
    static {

        MARS_DIRT = TFGCore.REGISTRATE.block("grass/mars_dirt",
                (p) -> new DirtBlock(p, RUSTICUS_MYCELIUM, MARS_PATH, MARS_FARMLAND, null, null))
                .properties(p -> p.mapColor(MapColor.DIRT).strength(1.4f).sound(SoundType.GRAVEL))
                .simpleItem()
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .register();

        MARS_CLAY = TFGCore.REGISTRATE.block("grass/mars_clay_dirt",
                (p) -> new DirtBlock(p, RUSTICUS_MYCELIUM, MARS_PATH, MARS_FARMLAND, null, null))
                .properties(p -> p.mapColor(MapColor.DIRT).strength(1.4f).sound(SoundType.GRAVEL))
                .simpleItem()
                .loot(dropBetween(() -> Items.CLAY_BALL, 1, 3))
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .register();
    }
}
