package su.terrafirmagreg.core.common.data.blocks;

import java.util.Map;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Helpers;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.common.Tags;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

public class TFGBlocks_Venus {
    public static void init() {
    }

    public static Map<FluorapatiteBlockType, BlockEntry<Block>> FLUORAPATITE_SAND = Helpers.mapOfKeys(FluorapatiteBlockType.class,
            type -> TFGCore.REGISTRATE.block("sand/fluorapatite/" + type.nameLower(), Block::new)
                    .properties(p -> p
                            .mapColor(type.getMapColor())
                            .strength(0.5F)
                            .sound(SoundType.SAND))
                    .tag(TFCTags.Blocks.CAN_LANDSLIDE, TFCTags.Blocks.SUPPORTS_LANDSLIDE, BlockTags.MINEABLE_WITH_SHOVEL, Tags.Blocks.SAND)
                    .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry()))
                    .item(BlockItem::new)
                    .tag(Tags.Items.SAND, TFGTags.Items.FLUORAPATITE_SAND)
                    .build()
                    .register());

    public static Map<FluorapatiteBlockType, BlockEntry<Block>> FLUORAPATITE_RAW_SANDSTONE = Helpers.mapOfKeys(FluorapatiteBlockType.class,
            type -> TFGCore.REGISTRATE.block("sandstone/raw/fluorapatite/" + type.nameLower(), Block::new)
                    .properties(p -> p
                            .mapColor(type.getMapColor())
                            .strength(0.8f)
                            .explosionResistance(0.8f)
                            .requiresCorrectToolForDrops())
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE, Tags.Blocks.SANDSTONE)
                    .blockstate((ctx, prov) -> {
                        var side = TFGCore.id("block/sandstone/fluorapatite/bottom_" + type.nameLower());
                        var top = TFGCore.id("block/sandstone/fluorapatite/top_" + type.nameLower());
                        prov.simpleBlock(ctx.getEntry(),
                                prov.models().cubeBottomTop(ctx.getName(), side, top, top));
                    })
                    .item(BlockItem::new)
                    .tag(Tags.Items.SANDSTONE, TFGTags.Items.FLUORAPATITE_SANDSTONE)
                    .build()
                    .register());

    public static Map<FluorapatiteBlockType, BlockEntry<Block>> FLUORAPATITE_SMOOTH_SANDSTONE = Helpers.mapOfKeys(FluorapatiteBlockType.class,
            type -> TFGCore.REGISTRATE.block("sandstone/smooth/fluorapatite/" + type.nameLower(), Block::new)
                    .properties(p -> p
                            .mapColor(type.getMapColor())
                            .strength(0.8f)
                            .explosionResistance(0.8f)
                            .requiresCorrectToolForDrops())
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE, Tags.Blocks.SANDSTONE)
                    .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().cubeAll(ctx.getName(),
                            TFGCore.id("block/sandstone/fluorapatite/top_" + type.nameLower()))))
                    .item(BlockItem::new)
                    .tag(Tags.Items.SANDSTONE, TFGTags.Items.FLUORAPATITE_SANDSTONE)
                    .build()
                    .register());

    public static Map<FluorapatiteBlockType, BlockEntry<Block>> FLUORAPATITE_CHISELED_SANDSTONE = Helpers.mapOfKeys(FluorapatiteBlockType.class,
            type -> TFGCore.REGISTRATE.block("sandstone/chiseled/fluorapatite/" + type.nameLower(), Block::new)
                    .properties(p -> p
                            .mapColor(type.getMapColor())
                            .strength(0.8f)
                            .explosionResistance(0.8f)
                            .requiresCorrectToolForDrops())
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE, Tags.Blocks.SANDSTONE)
                    .blockstate((ctx, prov) -> {
                        prov.simpleBlock(ctx.getEntry(),
                                prov.models().cubeBottomTop(ctx.getName(),
                                        TFGCore.id("block/sandstone/fluorapatite/chiseled_" + type.nameLower()),
                                        TFGCore.id("block/sandstone/fluorapatite/bottom_" + type.nameLower()),
                                        TFGCore.id("block/sandstone/fluorapatite/top_" + type.nameLower())));
                    })
                    .item(BlockItem::new)
                    .tag(Tags.Items.SANDSTONE, TFGTags.Items.FLUORAPATITE_SANDSTONE)
                    .build()
                    .register());

    public static Map<FluorapatiteBlockType, BlockEntry<Block>> FLUORAPATITE_CUT_SANDSTONE = Helpers.mapOfKeys(FluorapatiteBlockType.class,
            type -> TFGCore.REGISTRATE.block("sandstone/cut/fluorapatite/" + type.nameLower(), Block::new)
                    .properties(p -> p
                            .mapColor(type.getMapColor())
                            .strength(0.8f)
                            .explosionResistance(0.8f)
                            .requiresCorrectToolForDrops())
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE, Tags.Blocks.SANDSTONE)
                    .blockstate((ctx, prov) -> {
                        prov.simpleBlock(ctx.getEntry(),
                                prov.models().cubeBottomTop(ctx.getName(),
                                        TFGCore.id("block/sandstone/fluorapatite/cut_" + type.nameLower()),
                                        TFGCore.id("block/sandstone/fluorapatite/bottom_" + type.nameLower()),
                                        TFGCore.id("block/sandstone/fluorapatite/top_" + type.nameLower())));
                    })
                    .item(BlockItem::new)
                    .tag(Tags.Items.SANDSTONE, TFGTags.Items.FLUORAPATITE_SANDSTONE)
                    .build()
                    .register());
}
