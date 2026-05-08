package su.terrafirmagreg.core.common.data.blocks;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltMixBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadStairsBlock;
import su.terrafirmagreg.core.common.block.asphalt.HotAsphaltRoadBlock;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.utils.ModelUtils;

@SuppressWarnings("unused")
public final class TFGBlocksAsphalt {

    public static void init() {
    }

    public static final BlockEntry<LiquidBlock> ASPHALT_MIX_FLUID_BLOCK = TFGCore.REGISTRATE
            .block("fluid/asphalt_mix", p -> new LiquidBlock(TFGFluids.ASPHALT_MIX.source(), p))
            .initialProperties(() -> Blocks.WATER)
            .blockstate(ModelUtils.blockVariants(TFGCore.id("block/fluid/asphalt_mix")))
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK).noLootTable())
            .register();

    public static final BlockEntry<AsphaltMixBlock> ASPHALT_MIX = TFGCore.REGISTRATE.block("asphalt_mix", AsphaltMixBlock::new)
            .initialProperties(() -> Blocks.BLACK_CONCRETE)
            .properties(p -> p.strength(1.4f, 6).sound(SoundType.TUFF).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops())
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<HotAsphaltRoadBlock> HOT_ASPHALT_ROAD = TFGCore.REGISTRATE.block("asphalt_road_hot", HotAsphaltRoadBlock::new)
            .initialProperties(() -> Blocks.BLACK_CONCRETE)
            .properties(p -> p.strength(1.4f, 6).sound(SoundType.TUFF).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops())
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .onRegister(block -> TFGBlockEntities.addValidBEBlock(TFCBlockEntities.TICK_COUNTER, block))
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<AsphaltRoadBlock> ASPHALT_ROAD = TFGCore.REGISTRATE.block("asphalt_road", AsphaltRoadBlock::new)
            .initialProperties(() -> Blocks.BLACK_CONCRETE)
            .properties(p -> p.strength(1.6f, 6).sound(SoundType.DEEPSLATE_TILES).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops())
            .addLayer(() -> RenderType::cutout)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, TFGTags.Blocks.FUNCTIONAL_ASPHALT_ROADS)
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<AsphaltRoadStairsBlock> ASPHALT_ROAD_STAIRS = TFGCore.REGISTRATE.block("asphalt_road_stairs",
            p -> new AsphaltRoadStairsBlock(ASPHALT_ROAD.get().defaultBlockState(), p))
            .initialProperties(ASPHALT_ROAD)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.STAIRS, TFGTags.Blocks.FUNCTIONAL_ASPHALT_ROAD_STAIRS)
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();

    public static final BlockEntry<AsphaltRoadSlabBlock> ASPHALT_ROAD_SLAB = TFGCore.REGISTRATE.block("asphalt_road_slab", AsphaltRoadSlabBlock::new)
            .initialProperties(ASPHALT_ROAD)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .addLayer(() -> RenderType::cutout)
            .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.SLABS, TFGTags.Blocks.FUNCTIONAL_ASPHALT_ROAD_SLABS)
            .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
            .register();
}
