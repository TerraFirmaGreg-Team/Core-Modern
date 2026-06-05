package su.terrafirmagreg.core.common.data.blocks;

import java.util.EnumMap;
import java.util.Map;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.PalmFruitClusterBlock;
import su.terrafirmagreg.core.common.block.PalmHeadBlock;
import su.terrafirmagreg.core.common.data.PalmTrees;

public class TFGBlocks_PalmTrees {

    public static final Map<PalmTrees, BlockEntry<PalmHeadBlock>> PALM_HEADS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<PalmFruitClusterBlock>> PALM_CLUSTERS = new EnumMap<>(PalmTrees.class);

    public static void init() {
    }

    static {
        for (PalmTrees tree : PalmTrees.values()) {
            String name = tree.getSerializedName();

            PALM_CLUSTERS.put(tree, TFGCore.REGISTRATE.block(name + "_tree_cluster", PalmFruitClusterBlock::new)
                    .properties(p -> p.mapColor(MapColor.PLANT)
                            .strength(0.5f)
                            .sound(SoundType.GRASS))
                    .item(BlockItem::new)
                    .build()
                    .register());

            PALM_HEADS.put(tree, TFGCore.REGISTRATE.block(name + "_tree_head",
                    p -> new PalmHeadBlock(p, tree))
                    .properties(p -> p.mapColor(MapColor.WOOD)
                            .strength(2.0f)
                            .sound(SoundType.WOOD))
                    .item(BlockItem::new)
                    .build()
                    .register());
        }
    }
}
