package su.terrafirmagreg.core.common.data.blocks;

import java.util.EnumMap;
import java.util.Map;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.CoconutBlock;
import su.terrafirmagreg.core.common.block.CoconutClusterBlock;
import su.terrafirmagreg.core.common.block.PalmHeadBlock;
import su.terrafirmagreg.core.common.data.PalmTrees;

public class TFGBlocks_PalmTrees {

    public static final Map<PalmTrees, BlockEntry<PalmHeadBlock>> PALM_HEADS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<CoconutClusterBlock>> PALM_CLUSTERS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<CoconutBlock>> PALM_FRUITS = new EnumMap<>(PalmTrees.class);

    public static void init() {
    }

    static {
        for (PalmTrees tree : PalmTrees.values()) {
            String name = tree.getSerializedName();

            PALM_HEADS.put(tree, TFGCore.REGISTRATE.block("palm_tree/" + name + "_tree_head",
                    p -> new PalmHeadBlock(p, tree))
                    .properties(p -> p.mapColor(MapColor.WOOD)
                            .strength(2.0f)
                            .sound(SoundType.WOOD))
                    .blockstate((ctx, prov) -> {
                        var model = prov.models().withExistingParent("palm_tree/" + name + "_tree_head", TFGCore.id("block/palm_tree/palm_head"))
                                .texture("fuzz", TFGCore.id("block/palm_tree/" + name + "_head_fuzz"))
                                .texture("side", TFGCore.id("block/palm_tree/" + name + "_head_side"))
                                .texture("top", TFGCore.id("block/palm_tree/" + name + "_head_top"));
                        prov.simpleBlock(ctx.getEntry(), model);
                    })
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .item(BlockItem::new)
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .build()
                    .register());
        }

        PALM_FRUITS.put(PalmTrees.COCONUT, TFGCore.REGISTRATE.block("palm_tree/coconut_fruit_brown", p -> new CoconutBlock(ExtendedProperties.of(p), CoconutBlock.DEFAULT_SHAPE))
                .properties(p -> p.mapColor(MapColor.DIRT)
                        .offsetType(BlockBehaviour.OffsetType.XZ)
                        .pushReaction(PushReaction.DESTROY)
                        .instabreak()
                        .strength(0.5f)
                        .dynamicShape()
                        .sound(SoundType.BAMBOO))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().withExistingParent(ctx.getName(), "tfg:block/palm_tree/square_palm_fruit")
                                .texture("0", TFGCore.id("block/palm_tree/coconut_fruit_brown"))))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .item(BlockItem::new)
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .build()
                .register());

        PALM_CLUSTERS.put(PalmTrees.COCONUT, TFGCore.REGISTRATE.block("palm_tree/coconut_tree_cluster", p -> new CoconutClusterBlock(p, PalmTrees.COCONUT))
                .properties(p -> p.mapColor(MapColor.PLANT)
                        .strength(0.5f)
                        .sound(SoundType.BAMBOO)
                        .pushReaction(PushReaction.DESTROY)
                        .noCollission()
                        .noOcclusion())
                .blockstate((ctx, prov) -> {
                    var builder = prov.getVariantBuilder(ctx.getEntry());
                    for (int age = 0; age <= 7; age++) {
                        var model = prov.models().withExistingParent("palm_tree/coconut_tree_cluster_" + age, TFGCore.id("block/palm_tree/square_palm_cluster_" + age))
                                .texture("0", TFGCore.id("block/palm_tree/coconut_cluster_" + age));
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            builder.partialState()
                                    .with(CoconutClusterBlock.AGE, age)
                                    .with(CoconutClusterBlock.FACING, dir)
                                    .modelForState()
                                    .modelFile(model)
                                    .rotationY((int) dir.toYRot())
                                    .addModel();
                        }
                    }
                })
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .loot((prov, block) -> prov.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(PalmTrees.COCONUT.getDroppedFruitBlock())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(CoconutClusterBlock.AGE, 4)))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0F))))
                        .add(LootItem.lootTableItem(PalmTrees.COCONUT.getDroppedFruitBlock())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(CoconutClusterBlock.AGE, 5)))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))
                        .add(LootItem.lootTableItem(PalmTrees.COCONUT.getDroppedFruitBlock())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(CoconutClusterBlock.AGE, 6)))))))
                .item(BlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/palm_tree/coconut_tree_cluster_7")))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .build()
                .register());
    }
}
