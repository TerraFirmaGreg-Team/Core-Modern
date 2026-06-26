package su.terrafirmagreg.core.common.data.blocks;

import java.util.EnumMap;
import java.util.Map;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.DecayingBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.palmtree.CoconutClusterBlock;
import su.terrafirmagreg.core.common.block.palmtree.PalmFruitBlock;
import su.terrafirmagreg.core.common.block.palmtree.PalmHeadBlock;
import su.terrafirmagreg.core.common.block.palmtree.PalmTrunkBlock;
import su.terrafirmagreg.core.common.data.PalmTrees;
import su.terrafirmagreg.core.common.data.TFGTags;

public class TFGBlocks_PalmTrees {

    public static final Map<PalmTrees, BlockEntry<PalmHeadBlock>> PALM_HEADS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<CoconutClusterBlock>> PALM_CLUSTERS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<PalmFruitBlock>> PALM_FRUITS = new EnumMap<>(PalmTrees.class);

    public static void init() {
        PalmTrees.init();
    }

    public static final BlockEntry<PalmTrunkBlock> PALM_TRUNK = TFGCore.REGISTRATE.block("palm_tree/trunk", PalmTrunkBlock::new)
            .properties(p -> p.mapColor(MapColor.WOOD)
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.WOOD))
            .blockstate((ctx, prov) -> {
                var builder = prov.getVariantBuilder(ctx.getEntry());
                for (int size = 0; size <= 2; size++) {
                    var model = prov.models().withExistingParent("palm_tree/trunk_" + size, TFGCore.id("block/palm_tree/palm_trunk_" + size))
                            .texture("0", ResourceLocation.fromNamespaceAndPath("tfc", "block/wood/log/palm"))
                            .texture("1", TFGCore.id("block/palm_tree/trunk_top_" + size));
                    builder.partialState()
                            .with(PalmTrunkBlock.SIZE, size)
                            .modelForState()
                            .modelFile(model)
                            .addModel();
                }
            })
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, BlockTags.MINEABLE_WITH_AXE)
            .loot((prov, block) -> prov.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(TFCBlocks.WOODS.get(Wood.PALM).get(Wood.BlockType.LOG).get())
                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.AXES)))))))
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .item(BlockItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/palm_tree/trunk_2")))
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .build()
            .register();

    // Palm Husk
    public static final BlockEntry<GroundcoverBlock> PALM_HUSK = TFGCore.REGISTRATE.block("groundcover/palm_husk", p -> GroundcoverBlock.twig(ExtendedProperties.of(p)))
            .properties(p -> p.mapColor(MapColor.DIRT)
                    .pushReaction(PushReaction.DESTROY)
                    .instabreak()
                    .strength(0.05F, 0.0F)
                    .sound(SoundType.SPORE_BLOSSOM)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .dynamicShape()
                    .noCollission()
                    .noOcclusion())
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().withExistingParent(ctx.getName(), "tfg:block/palm_tree/palm_husk")
                            .texture("0", TFGCore.id("block/palm_tree/palm_husk"))))
            .tag(TFCTags.Blocks.TOUGHNESS_1)
            .loot((provider, block) -> provider.add(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(1.0F, 1.0F))
                            .add(AlternativesEntry.alternatives(
                                    LootItem.lootTableItem(block)
                                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TFCTags.Items.KNIVES))),
                                    LootItem.lootTableItem(TFCItems.STRAW.get())
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))))))
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .item(BlockItem::new)
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .build()
            .register();

    // Green Coconut
    public static final BlockEntry<PalmFruitBlock> GREEN_COCONUT = TFGCore.REGISTRATE.block("palm_tree/coconut_fruit_green", p -> new PalmFruitBlock(ExtendedProperties.of(p)
            .blockEntity(TFCBlockEntities.DECAYING)
            .serverTicks(DecayingBlockEntity::serverTick), PALM_HUSK, PalmFruitBlock.DEFAULT_SHAPE))
            .properties(p -> p.mapColor(MapColor.DIRT)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .pushReaction(PushReaction.DESTROY)
                    .instabreak()
                    .strength(0.05F, 0.0F)
                    .dynamicShape()
                    .sound(SoundType.BAMBOO)
                    .noOcclusion())
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().withExistingParent(ctx.getName(), "tfg:block/palm_tree/square_palm_fruit")
                            .texture("0", TFGCore.id("block/palm_tree/coconut_fruit_green"))))
            .tag(TFGTags.Blocks.FALLING_CONCUSSIVE, TFCTags.Blocks.TOUGHNESS_1)
            //Not loot table since drops come from the decaying property.
            .loot((provider, block) -> provider.add(block, LootTable.lootTable()))
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .item(BlockItem::new)
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .build()
            .register();

    // Loop to register heads, clusters, and fruits for each PalmTrees entry.
    static {
        for (PalmTrees tree : PalmTrees.values()) {
            String name = tree.getSerializedName();

            // Palm Heads
            PALM_HEADS.put(tree, TFGCore.REGISTRATE.block("palm_tree/" + name + "_tree_head",
                    p -> new PalmHeadBlock(p, tree))
                    .properties(p -> p.mapColor(MapColor.WOOD)
                            .randomTicks()
                            .strength(2.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.WOOD))
                    .blockstate((ctx, prov) -> {
                        var model = prov.models().withExistingParent("palm_tree/" + name + "_tree_head", TFGCore.id("block/palm_tree/palm_head"))
                                .texture("fuzz", TFGCore.id("block/palm_tree/" + name + "_head_fuzz"))
                                .texture("side", TFGCore.id("block/palm_tree/" + name + "_head_side"))
                                .texture("top", TFGCore.id("block/palm_tree/" + name + "_head_top"));
                        prov.simpleBlock(ctx.getEntry(), model);
                    })
                    .loot((prov, block) -> prov.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(TFCBlocks.WOODS.get(Wood.PALM).get(Wood.BlockType.LOG).get())
                                    .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.AXES)))))))
                    .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, BlockTags.MINEABLE_WITH_AXE)
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .item(BlockItem::new)
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .build()
                    .register());
        }

        // Brown Coconut
        PALM_FRUITS.put(PalmTrees.COCONUT, TFGCore.REGISTRATE.block("palm_tree/coconut_fruit_brown", p -> new PalmFruitBlock(ExtendedProperties.of(p)
                .blockEntity(TFCBlockEntities.DECAYING)
                .serverTicks(DecayingBlockEntity::serverTick), PALM_HUSK, PalmFruitBlock.DEFAULT_SHAPE))
                .properties(p -> p.mapColor(MapColor.DIRT)
                        .offsetType(BlockBehaviour.OffsetType.XZ)
                        .pushReaction(PushReaction.DESTROY)
                        .instabreak()
                        .strength(0.05F, 0.0F)
                        .dynamicShape()
                        .sound(SoundType.BAMBOO)
                        .noOcclusion())
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().withExistingParent(ctx.getName(), "tfg:block/palm_tree/square_palm_fruit")
                                .texture("0", TFGCore.id("block/palm_tree/coconut_fruit_brown"))))
                .tag(TFGTags.Blocks.FALLING_CONCUSSIVE, TFCTags.Blocks.TOUGHNESS_1)
                //Not loot table since drops come from the decaying property.
                .loot((provider, block) -> provider.add(block, LootTable.lootTable()))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .item(BlockItem::new)
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .build()
                .register());

        // Coconut Cluster
        PALM_CLUSTERS.put(PalmTrees.COCONUT, TFGCore.REGISTRATE.block("palm_tree/coconut_tree_cluster", p -> new CoconutClusterBlock(p, PalmTrees.COCONUT))
                .properties(p -> p.mapColor(MapColor.PLANT)
                        .randomTicks()
                        .strength(0.5f, 0.0F)
                        .sound(SoundType.BAMBOO)
                        .pushReaction(PushReaction.DESTROY)
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
                        .add(LootItem.lootTableItem(TFGBlocks_PalmTrees.GREEN_COCONUT.get())
                                .when(AnyOfCondition.anyOf(
                                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CoconutClusterBlock.AGE, 2)),
                                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CoconutClusterBlock.AGE, 3))))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0F))))
                        .add(LootItem.lootTableItem(PalmTrees.COCONUT.getDroppedFruitBlock())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CoconutClusterBlock.AGE, 4)))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0F))))
                        .add(LootItem.lootTableItem(PalmTrees.COCONUT.getDroppedFruitBlock())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CoconutClusterBlock.AGE, 5)))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))
                        .add(LootItem.lootTableItem(PalmTrees.COCONUT.getDroppedFruitBlock())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CoconutClusterBlock.AGE, 6)))))))
                .item(BlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/palm_tree/coconut_tree_cluster_7")))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .build()
                .register());
    }
}
