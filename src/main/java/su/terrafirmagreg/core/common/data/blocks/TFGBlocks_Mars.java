package su.terrafirmagreg.core.common.data.blocks;

import static su.terrafirmagreg.core.common.data.blocks.TFGBlocks.dropBetween;
import static su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth.*;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IcicleBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.dries007.tfc.common.blocks.soil.DirtBlock;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.PathBlock;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.LargeNestBoxBlock;
import su.terrafirmagreg.core.common.block.MarsIceBlock;
import su.terrafirmagreg.core.common.blockentity.LargeNestBoxBlockEntity;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class TFGBlocks_Mars {
    public static void init() {
    }

    public static BlockEntry<DirtBlock> MARS_DIRT;
    public static BlockEntry<DirtBlock> MARS_CLAY;
    public static BlockEntry<PathBlock> MARS_PATH;
    public static BlockEntry<FarmlandBlock> MARS_FARMLAND;

    public static BlockEntry<ConnectedGrassBlock> AMBER_MYCELIUM;
    public static BlockEntry<ConnectedGrassBlock> AMBER_CLAY_MYCELIUM;
    public static BlockEntry<ConnectedGrassBlock> AMBER_KAOLIN_MYCELIUM;
    public static BlockEntry<ConnectedGrassBlock> RUSTICUS_MYCELIUM;
    public static BlockEntry<ConnectedGrassBlock> RUSTICUS_CLAY_MYCELIUM;
    public static BlockEntry<ConnectedGrassBlock> RUSTICUS_KAOLIN_MYCELIUM;
    public static BlockEntry<ConnectedGrassBlock> SANGNUM_MYCELIUM;
    public static BlockEntry<ConnectedGrassBlock> SANGNUM_CLAY_MYCELIUM;
    public static BlockEntry<ConnectedGrassBlock> SANGNUM_KAOLIN_MYCELIUM;

    private static final NonNullUnaryOperator<BlockBehaviour.Properties> amber_properties = p -> p
            .mapColor(MapColor.TERRACOTTA_YELLOW)
            .strength(5.0f)
            .sound(SoundType.WART_BLOCK)
            .randomTicks();

    private static final NonNullUnaryOperator<BlockBehaviour.Properties> rusticus_properties = p -> p
            .mapColor(MapColor.TERRACOTTA_ORANGE)
            .strength(5.0f)
            .sound(SoundType.WART_BLOCK)
            .randomTicks();

    private static final NonNullUnaryOperator<BlockBehaviour.Properties> sangnum_properties = p -> p
            .mapColor(MapColor.TERRACOTTA_RED)
            .strength(5.0f)
            .sound(SoundType.WART_BLOCK)
            .randomTicks();

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

    static {
        MARS_DIRT = createDirt("grass/mars_dirt", () -> RUSTICUS_MYCELIUM.get(), () -> MARS_PATH.get(), () -> MARS_FARMLAND.get(), null, null);

        MARS_CLAY = createClay("grass/mars_clay_dirt", RUSTICUS_MYCELIUM, MARS_PATH, MARS_FARMLAND, null, null);
        MARS_PATH = createPath("grass/mars_path", MARS_DIRT);
        MARS_FARMLAND = createFarmland("grass/mars_farmland", MARS_DIRT);

        AMBER_MYCELIUM = createGrass("grass/amber_mycelium", MARS_DIRT, MARS_PATH, MARS_FARMLAND, amber_properties);
        AMBER_CLAY_MYCELIUM = createClayGrass("grass/amber_clay_mycelium", MARS_DIRT, MARS_PATH, MARS_FARMLAND, amber_properties);
        AMBER_KAOLIN_MYCELIUM = createKaolin("grass/amber_kaolin_mycelium", amber_properties);

        RUSTICUS_MYCELIUM = createGrass("grass/rusticus_mycelium", MARS_DIRT, MARS_PATH, MARS_FARMLAND, rusticus_properties);
        RUSTICUS_CLAY_MYCELIUM = createClayGrass("grass/rusticus_clay_mycelium", MARS_DIRT, MARS_PATH, MARS_FARMLAND, rusticus_properties);
        RUSTICUS_KAOLIN_MYCELIUM = createKaolin("grass/rusticus_kaolin_mycelium", rusticus_properties);

        SANGNUM_MYCELIUM = createGrass("grass/sangnum_mycelium", MARS_DIRT, MARS_PATH, MARS_FARMLAND, sangnum_properties);
        SANGNUM_CLAY_MYCELIUM = createClayGrass("grass/sangnum_clay_mycelium", MARS_DIRT, MARS_PATH, MARS_FARMLAND, sangnum_properties);
        SANGNUM_KAOLIN_MYCELIUM = createKaolin("grass/sangnum_kaolin_mycelium", sangnum_properties);
    }

    private static BlockEntry<ConnectedGrassBlock> createKaolin(String id, NonNullUnaryOperator<BlockBehaviour.Properties> props) {
        return TFGCore.REGISTRATE.block(id,
                p -> new ConnectedGrassBlock(p, TFCBlocks.RED_KAOLIN_CLAY, null, null))
                .properties(props)
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .loot(dropBetween(TFCItems.KAOLIN_CLAY, 1, 3))
                .tag(BlockTags.DIRT, TFCTags.Blocks.CAN_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL, TFCTags.Blocks.GRASS)
                .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .tag(ItemTags.DIRT, TFCGrassItemTag, TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "kaolin_clay")))
                .build()
                .register();
    }
}
