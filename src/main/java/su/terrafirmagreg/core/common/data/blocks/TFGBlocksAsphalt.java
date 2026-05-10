package su.terrafirmagreg.core.common.data.blocks;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadHotBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadPouringBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadStairsBlock;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public final class TFGBlocksAsphalt {

    public static void init() {
    }

    public static final BlockEntry<AsphaltRoadPouringBlock> ASPHALT_ROAD_POURING = TFGCore.REGISTRATE.block("asphalt_road_pouring",
            AsphaltRoadPouringBlock::new)
            .initialProperties(() -> Blocks.BLACK_CONCRETE)
            .properties(p -> p.strength(-1.0F, 3600000.0F).sound(SoundType.MUD).mapColor(MapColor.COLOR_BLACK))
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<AsphaltRoadHotBlock> ASPHALT_ROAD_HOT = TFGCore.REGISTRATE.block("asphalt_road_hot", AsphaltRoadHotBlock::new)
            .initialProperties(() -> Blocks.BLACK_CONCRETE)
            .properties(p -> p.strength(1.3f, 6).sound(SoundType.TUFF).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops())
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, TFCTags.Blocks.SUPPORTS_LANDSLIDE, TFCTags.Blocks.TOUGHNESS_2)
            .onRegister(block -> TFGBlockEntities.addValidBEBlock(TFCBlockEntities.TICK_COUNTER, block))
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<AsphaltRoadBlock> ASPHALT_ROAD = TFGCore.REGISTRATE.block("asphalt_road", AsphaltRoadBlock::new)
            .initialProperties(() -> Blocks.BLACK_CONCRETE)
            .properties(p -> p.strength(6f, 64).sound(SoundType.DEEPSLATE_TILES).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops())
            .addLayer(() -> RenderType::cutout)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, TFCTags.Blocks.SUPPORTS_LANDSLIDE, TFCTags.Blocks.TOUGHNESS_2)
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<AsphaltRoadStairsBlock> ASPHALT_ROAD_STAIRS = TFGCore.REGISTRATE.block("asphalt_road_stairs",
            p -> new AsphaltRoadStairsBlock(() -> ASPHALT_ROAD.get().defaultBlockState(), p))
            .initialProperties(ASPHALT_ROAD)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.STAIRS, TFCTags.Blocks.SUPPORTS_LANDSLIDE, TFCTags.Blocks.TOUGHNESS_2)
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<AsphaltRoadSlabBlock> ASPHALT_ROAD_SLAB = TFGCore.REGISTRATE.block("asphalt_road_slab", AsphaltRoadSlabBlock::new)
            .initialProperties(ASPHALT_ROAD)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .addLayer(() -> RenderType::cutout)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.SLABS, TFCTags.Blocks.SUPPORTS_LANDSLIDE, TFCTags.Blocks.TOUGHNESS_2)
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();
}
