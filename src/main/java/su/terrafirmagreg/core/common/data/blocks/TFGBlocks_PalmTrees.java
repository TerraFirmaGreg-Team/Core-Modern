package su.terrafirmagreg.core.common.data.blocks;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.DecayingBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.wood.TFCLeavesBlock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.PalmColorProvider;
import su.terrafirmagreg.core.common.block.palmtree.*;
import su.terrafirmagreg.core.common.blockentity.PalmHeadBlockEntity;
import su.terrafirmagreg.core.common.data.PalmTrees;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.utils.ModelUtils;

public class TFGBlocks_PalmTrees {

    private static final TagKey<Item> TFC_ITEM_SHARP_TOOLS = TagKey.create(ForgeRegistries.Keys.ITEMS,
            ResourceLocation.fromNamespaceAndPath("tfc", "sharp_tools"));
    private static final TagKey<Block> TFC_BLOCK_SHARP_MINEABLE = TagKey.create(ForgeRegistries.Keys.BLOCKS,
            ResourceLocation.fromNamespaceAndPath("tfc", "mineable_with_sharp_tool"));

    public static final Map<PalmTrees, BlockEntry<PalmHeadBlock>> PALM_HEADS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<GrowingPalmHeadBlock>> GROWING_PALM_HEADS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<? extends PalmClusterBlock>> PALM_CLUSTERS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<PalmTreeSaplingBlock>> PALM_SAPLINGS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<FlowerPotBlock>> POTTED_SAPLINGS = new EnumMap<>(PalmTrees.class);

    public static final Map<PalmTrees, BlockEntry<TFCLeavesBlock>> PALM_LEAVES = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, ItemEntry<Item>> PALM_FRUITS = new EnumMap<>(PalmTrees.class);

    private static final Wood PALM_REGISTER = Wood.PALM;

    public static void init() {
        PalmTrees.init();
    }

    // Palm Trunk
    public static final BlockEntry<PalmTrunkBlock> PALM_TRUNK = TFGCore.REGISTRATE.block("palm_tree/trunk", PalmTrunkBlock::new)
            .properties(p -> p.mapColor(MapColor.WOOD)
                    .strength(8.0f)
                    .sound(SoundType.WOOD))
            .blockstate((ctx, prov) -> {
                var builder = prov.getVariantBuilder(ctx.getEntry());
                for (int size = 0; size <= 2; size++) {
                    var model = prov.models().withExistingParent(ctx.getName() + "_" + size, TFGCore.id("block/palm_tree/palm_trunk_" + size))
                            .texture("0", ResourceLocation.fromNamespaceAndPath("tfc", "block/wood/log/palm"))
                            .texture("1", TFGCore.id("block/palm_tree/trunk_top_" + size));
                    builder.partialState()
                            .with(PalmTrunkBlock.SIZE, size)
                            .modelForState()
                            .modelFile(model)
                            .addModel();
                }
            })
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, TFCTags.Blocks.LOGS_THAT_LOG, BlockTags.MINEABLE_WITH_AXE)
            .loot((prov, block) -> prov.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(TFCBlocks.WOODS.get(Wood.PALM).get(Wood.BlockType.LOG).get())))))
            .item(BlockItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/palm_tree/trunk_2")))
            .build()
            .register();

    // Palm Husk
    public static final BlockEntry<PalmHuskBlock> PALM_HUSK = TFGCore.REGISTRATE.block("groundcover/palm_husk", p -> PalmHuskBlock.twig(ExtendedProperties.of(p)))
            .properties(p -> p.mapColor(MapColor.DIRT)
                    .pushReaction(PushReaction.DESTROY)
                    .instabreak()
                    .strength(0.05F, 0.0F)
                    .sound(SoundType.SPORE_BLOSSOM)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
                    .dynamicShape()
                    .noCollission()
                    .noOcclusion())
            .blockstate((ctx, prov) -> {
                for (int i = 0; i <= 2; i++) {
                    var model = prov.models().withExistingParent(ctx.getName() + "_" + i, TFGCore.id("block/palm_tree/palm_husk_" + i))
                            .texture("0", TFGCore.id("block/palm_tree/palm_husk"));
                    ModelUtils.blockVariantsRotated(prov.getVariantBuilder(ctx.getEntry()), model);
                }
            })
            .tag(TFCTags.Blocks.TOUGHNESS_1, TFCTags.Blocks.LIT_BY_DROPPED_TORCH, TFC_BLOCK_SHARP_MINEABLE, BlockTags.MINEABLE_WITH_HOE, TFCTags.Blocks.SINGLE_BLOCK_REPLACEABLE,
                    BlockTags.REPLACEABLE_BY_TREES)
            .loot((provider, block) -> provider.add(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(1.0F, 1.0F))
                            .add(AlternativesEntry.alternatives(
                                    LootItem.lootTableItem(block)
                                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TFCTags.Items.KNIVES))),
                                    LootItem.lootTableItem(TFCItems.STRAW.get())
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))))))
            .item(BlockItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/" + ctx.getName() + "_0")))
            .tag(TFCTags.Items.COMPOST_BROWNS_LOW, TFCTags.Items.FIREPIT_FUEL, TFCTags.Items.FIREPIT_KINDLING)
            .build()
            .register();

    // Green & Brown Coconuts
    public static final Map<String, BlockEntry<PalmFruitBlock>> COCONUTS = Stream.of("green", "brown")
            .collect(Collectors.toMap(
                    color -> color,
                    color -> TFGCore.REGISTRATE.block("palm_tree/coconut_fruit_" + color, p -> new PalmFruitBlock(ExtendedProperties.of(p)
                            .blockEntity(TFCBlockEntities.DECAYING)
                            .serverTicks(DecayingBlockEntity::serverTick), PALM_HUSK))
                            .properties(p -> p.mapColor(color.equals("green") ? MapColor.PLANT : MapColor.DIRT)
                                    .offsetType(BlockBehaviour.OffsetType.XZ)
                                    .pushReaction(PushReaction.DESTROY)
                                    .instabreak()
                                    .strength(0.05F, 0.0F)
                                    .dynamicShape()
                                    .sound(SoundType.BAMBOO)
                                    .noOcclusion())
                            .blockstate((ctx, prov) -> {
                                for (int i = 0; i <= 2; i++) {
                                    var model = prov.models().withExistingParent(ctx.getName() + "_" + i, "tfg:block/palm_tree/square_palm_fruit_" + i)
                                            .texture("0", TFGCore.id("block/" + ctx.getName()));
                                    ModelUtils.blockVariantsRotated(prov.getVariantBuilder(ctx.getEntry()), model);
                                }
                            })
                            .tag(TFGTags.Blocks.FALLING_CONCUSSIVE, TFCTags.Blocks.TOUGHNESS_1)
                            .loot((provider, block) -> provider.add(block, LootTable.lootTable()))
                            .item(BlockItem::new)
                            .model((ctx, prov) -> prov.basicItem(TFGCore.id(ctx.getName())))
                            .build()
                            .register()));

    public static final BlockEntry<PalmFruitBlock> GREEN_COCONUT = COCONUTS.get("green");
    public static final BlockEntry<PalmFruitBlock> BROWN_COCONUT = COCONUTS.get("brown");

    // Coconut Cluster
    public static final BlockEntry<CoconutClusterBlock> COCONUT_CLUSTER = TFGCore.REGISTRATE.block("palm_tree/coconut_tree_cluster", p -> new CoconutClusterBlock(p, PalmTrees.COCONUT))
            .properties(p -> p.mapColor(MapColor.PLANT)
                    .randomTicks()
                    .strength(0.5f, 0.0F)
                    .sound(SoundType.BAMBOO)
                    .pushReaction(PushReaction.DESTROY)
                    .noOcclusion())
            .blockstate((ctx, prov) -> {
                var builder = prov.getVariantBuilder(ctx.getEntry());
                for (int age = 0; age <= (PalmTrees.COCONUT.getClusterAges() - 1); age++) {
                    var model = prov.models().withExistingParent(ctx.getName() + "_" + age, TFGCore.id("block/palm_tree/" + PalmTrees.COCONUT.getClusterModelShape() + "_palm_cluster_" + age))
                            .texture("0", TFGCore.id("block/palm_tree/coconut_cluster_" + age));
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        builder.partialState()
                                .with(ctx.getEntry().clusterAge, age)
                                .with(PalmClusterBlock.FACING, dir)
                                .modelForState()
                                .modelFile(model)
                                .rotationY((int) dir.toYRot())
                                .addModel();
                    }
                }
            })
            .loot((prov, block) -> prov.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(TFGBlocks_PalmTrees.GREEN_COCONUT)
                            .when(AnyOfCondition.anyOf(
                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(block.clusterAge, 2)),
                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(block.clusterAge, 3))))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0F))))
                    .add(LootItem.lootTableItem(TFGBlocks_PalmTrees.BROWN_COCONUT)
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(block.clusterAge, 4)))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0F))))
                    .add(LootItem.lootTableItem(TFGBlocks_PalmTrees.BROWN_COCONUT)
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(block.clusterAge, 5)))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))
                    .add(LootItem.lootTableItem(TFGBlocks_PalmTrees.BROWN_COCONUT)
                            .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(block.clusterAge, 6)))))))
            .item(BlockItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/palm_tree/coconut_tree_cluster_7")))
            .build()
            .register();

    // Loop to register heads, clusters, and fruits for each PalmTrees entry.
    static {
        for (PalmTrees tree : PalmTrees.values()) {
            String name = tree.getSerializedName();

            // Palm Heads
            PALM_HEADS.put(tree, TFGCore.REGISTRATE.block("palm_tree/" + name + "_tree_head",
                    p -> new PalmHeadBlock(ExtendedProperties.of(p)
                            .blockEntity(TFGBlockEntities.PALM_HEADS.get(tree))
                            .serverTicks(PalmHeadBlockEntity::serverTick), tree))
                    .properties(p -> p.mapColor(MapColor.WOOD)
                            .randomTicks()
                            .strength(2.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.WOOD))
                    .blockstate((ctx, prov) -> {
                        var model = prov.models().withExistingParent(ctx.getName(), TFGCore.id("block/palm_tree/palm_head_" + name))
                                .texture("fuzz", TFGCore.id("block/palm_tree/" + name + "_head_fuzz"))
                                .texture("side", TFGCore.id("block/palm_tree/" + name + "_head_side"))
                                .texture("top", TFGCore.id("block/palm_tree/" + name + "_head_top"));
                        prov.simpleBlock(ctx.getEntry(), model);
                    })
                    .loot((prov, block) -> prov.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(PALM_SAPLINGS.get(tree))
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                                    .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.AXES)))))))
                    .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, TFCTags.Blocks.LOGS_THAT_LOG, BlockTags.MINEABLE_WITH_AXE)
                    .item(BlockItem::new)
                    .build()
                    .register());

            // Growing Palm Heads
            GROWING_PALM_HEADS.put(tree, TFGCore.REGISTRATE.block("palm_tree/" + name + "_growing_tree_head",
                    p -> new GrowingPalmHeadBlock(ExtendedProperties.of(p).blockEntity(TFCBlockEntities.TICK_COUNTER), tree, tree.getClimateRange(), tree.getStages()))
                    .properties(p -> p.mapColor(MapColor.WOOD)
                            .randomTicks()
                            .strength(2.0f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.WOOD))
                    .blockstate((ctx, prov) -> {
                        var builder = prov.getVariantBuilder(ctx.getEntry());
                        for (int stage = 0; stage <= 3; stage++) {
                            var model = prov.models().getExistingFile(TFGCore.id("block/palm_tree/growing_palm_head_" + stage));
                            builder.partialState()
                                    .with(GrowingPalmHeadBlock.STAGE, stage)
                                    .modelForState()
                                    .modelFile(model)
                                    .addModel();
                        }
                    })
                    .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, TFCTags.Blocks.LOGS_THAT_LOG, BlockTags.MINEABLE_WITH_AXE)
                    .loot((prov, block) -> prov.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(PALM_SAPLINGS.get(tree))
                                    .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.AXES)))))))
                    .item(BlockItem::new)
                    .model((ctx, prov) -> prov.withExistingParent(ctx.getName(),
                            ResourceLocation.fromNamespaceAndPath("tfg", "block/palm_tree/growing_palm_head_0")))
                    .build()
                    .register());

            // Palm Saplings
            PALM_SAPLINGS.put(tree, TFGCore.REGISTRATE.block("palm_tree/" + name + "_sapling", p -> (PalmTreeSaplingBlock) tree.createSapling())
                    .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                            prov.models().withExistingParent(ctx.getName(), "tfg:block/palm_tree/palm_sapling")
                                    .texture("cross", TFGCore.id("block/" + ctx.getName()))))
                    .tag(BlockTags.SAPLINGS, TFCTags.Blocks.FRUIT_TREE_SAPLING)
                    .item(BlockItem::new)
                    .model((ctx, prov) -> prov.basicItem(TFGCore.id(ctx.getName())))
                    .tag(ItemTags.SAPLINGS)
                    .build()
                    .register());

            // Palm Leaves
            PALM_LEAVES.put(tree, TFGCore.REGISTRATE.block("palm_tree/" + name + "_leaves", p -> new TFCLeavesBlock(ExtendedProperties.of(p)
                    .mapColor(MapColor.PLANT)
                    .strength(0.5F)
                    .sound(SoundType.GRASS)
                    .defaultInstrument()
                    .randomTicks()
                    .noOcclusion()
                    .isViewBlocking(TFCBlocks::never)
                    .flammableLikeLeaves(),
                    tree.getFoliageColorIndex(), PALM_REGISTER.getBlock(Wood.BlockType.FALLEN_LEAVES), PALM_REGISTER.getBlock(Wood.BlockType.TWIG)))
                    .blockstate((ctx, prov) -> prov.simpleBlock(
                            ctx.getEntry(), prov.models().withExistingParent(ctx.getName(), "tfc:block/wood/leaves/palm")
                                    .texture("side", TFGCore.id("block/" + ctx.getName() + "_side"))
                                    .texture("end", TFGCore.id("block/" + ctx.getName() + "_top"))))
                    .addLayer(() -> RenderType::cutoutMipped)
                    .color(() -> () -> (state, level, pos, tintIndex) -> PalmColorProvider.getPalmFoliageColor(tree, pos, tintIndex))
                    .tag(BlockTags.LEAVES)
                    .loot((prov, block) -> prov.add(block, LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1.0F))
                                    .add(LootItem.lootTableItem(TFCBlocks.WOODS.get(Wood.PALM).get(Wood.BlockType.LEAVES).get())
                                            .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(Tags.Items.SHEARS)))))
                            .withPool(LootPool.lootPool()
                                    .name("loot_pool")
                                    .setRolls(ConstantValue.exactly(1.0F))
                                    .add(AlternativesEntry.alternatives(
                                            LootItem.lootTableItem(Items.STICK)
                                                    .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TFC_ITEM_SHARP_TOOLS)))
                                                    .when(LootItemRandomChanceCondition.randomChance(0.2F))
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))),
                                            LootItem.lootTableItem(Items.STICK)
                                                    .when(LootItemRandomChanceCondition.randomChance(0.05F))
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))))))
                    .item(BlockItem::new)
                    .tag(ItemTags.LEAVES)
                    .color(() -> () -> (stack, tintIndex) -> PalmColorProvider.getPalmFoliageColor(tree, null, tintIndex))
                    .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/" + ctx.getName())))
                    .build()
                    .register());

            // Potted Saplings
            POTTED_SAPLINGS.put(tree, TFGCore.REGISTRATE.block("palm_tree/potted_" + name + "_sapling", p -> (FlowerPotBlock) tree.createPottedSapling())
                    .blockstate((ctx, prov) -> {
                        ResourceLocation saplingTex = TFGCore.id("block/palm_tree/" + name + "_sapling");
                        prov.simpleBlock(ctx.getEntry(), prov.models().withExistingParent(ctx.getName(), "minecraft:block/flower_pot_cross")
                                .texture("plant", saplingTex)
                                .texture("dirt", ResourceLocation.fromNamespaceAndPath("tfc", "block/dirt/loam")));
                    })
                    .loot((prov, block) -> prov.add(block, LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1.0F))
                                    .add(LootItem.lootTableItem(PALM_SAPLINGS.get(tree))))
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1.0F))
                                    .add(LootItem.lootTableItem(Blocks.FLOWER_POT)))))
                    .tag(BlockTags.FLOWER_POTS)
                    .register());

            // Fruit
            if (!tree.isSpecialFruit()) {
                TagKey<Item> tfgFoodProductTag = TagKey.create(ForgeRegistries.Keys.ITEMS, ResourceLocation.fromNamespaceAndPath("tfg", "foods/" + name));

                PALM_FRUITS.put(tree, TFGCore.REGISTRATE.item("food/" + name, p -> new Item(p.food(new FoodProperties.Builder().nutrition(4).saturationMod(0.3F).build())))
                        .model((ctx, prov) -> prov.basicItem(TFGCore.id(ctx.getName())))
                        .tag(TFCTags.Items.FOODS, tfgFoodProductTag)
                        .register());
            }

            // Clusters
            if (!tree.isSpecialCluster()) {

                PALM_CLUSTERS.put(tree, TFGCore.REGISTRATE.block("palm_tree/" + name + "_tree_cluster", p -> new PalmClusterBlock(p, tree))
                        .properties(p -> p.mapColor(MapColor.PLANT)
                                .randomTicks()
                                .strength(0.3f, 0.0F)
                                .sound(SoundType.CORAL_BLOCK)
                                .pushReaction(PushReaction.DESTROY)
                                .noOcclusion())
                        .blockstate((ctx, prov) -> {
                            var builder = prov.getVariantBuilder(ctx.getEntry());
                            for (int age = 0; age <= (tree.getClusterAges() - 1); age++) {
                                var model = prov.models().withExistingParent(ctx.getName() + "_" + age, TFGCore.id("block/palm_tree/" + tree.getClusterModelShape() + "_palm_cluster_" + age))
                                        .texture("0", TFGCore.id("block/palm_tree/" + name + "_cluster_" + age));
                                for (Direction dir : Direction.Plane.HORIZONTAL) {
                                    builder.partialState()
                                            .with(ctx.getEntry().clusterAge, age)
                                            .with(PalmClusterBlock.FACING, dir)
                                            .modelForState()
                                            .modelFile(model)
                                            .rotationY((int) dir.toYRot())
                                            .addModel();
                                }
                            }
                        })
                        .loot((prov, block) -> prov.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(TFGBlocks_PalmTrees.PALM_FRUITS.get(tree))
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(block.clusterAge, tree.getClusterAges() - 1)))
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(tree.getMinDrops(), tree.getMaxDrops())))))))
                        .item(BlockItem::new)
                        .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/" + ctx.getName() + "_" + (tree.getClusterAges() - 1))))
                        .build()
                        .register());
            } else if (tree == PalmTrees.COCONUT) {
                PALM_CLUSTERS.put(tree, COCONUT_CLUSTER);
            }
        }
    }
}
