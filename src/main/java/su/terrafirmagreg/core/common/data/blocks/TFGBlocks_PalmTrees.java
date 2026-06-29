package su.terrafirmagreg.core.common.data.blocks;

import java.util.EnumMap;
import java.util.Map;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.DecayingBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
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
import su.terrafirmagreg.core.common.block.palmtree.CoconutClusterBlock;
import su.terrafirmagreg.core.common.block.palmtree.GrowingPalmHeadBlock;
import su.terrafirmagreg.core.common.block.palmtree.PalmFruitBlock;
import su.terrafirmagreg.core.common.block.palmtree.PalmHeadBlock;
import su.terrafirmagreg.core.common.block.palmtree.PalmTreeSaplingBlock;
import su.terrafirmagreg.core.common.block.palmtree.PalmTrunkBlock;
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
    public static final Map<PalmTrees, BlockEntry<CoconutClusterBlock>> PALM_CLUSTERS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<PalmFruitBlock>> PALM_FRUITS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<PalmTreeSaplingBlock>> PALM_SAPLINGS = new EnumMap<>(PalmTrees.class);
    public static final Map<PalmTrees, BlockEntry<FlowerPotBlock>> POTTED_SAPLINGS = new EnumMap<>(PalmTrees.class);

    public static void init() {
        PalmTrees.init();
    }

    public static final BlockEntry<PalmTrunkBlock> PALM_TRUNK = TFGCore.REGISTRATE.block("palm_tree/trunk", PalmTrunkBlock::new)
            .properties(p -> p.mapColor(MapColor.WOOD)
                    .strength(8.0f)
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
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, TFCTags.Blocks.LOGS_THAT_LOG, BlockTags.MINEABLE_WITH_AXE)
            .loot((prov, block) -> prov.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .add(LootItem.lootTableItem(TFCBlocks.WOODS.get(Wood.PALM).get(Wood.BlockType.LOG).get())))))
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
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .item(BlockItem::new)
            .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/" + ctx.getName() + "_0")))
            .tag(TFCTags.Items.COMPOST_BROWNS_LOW, TFCTags.Items.FIREPIT_FUEL, TFCTags.Items.FIREPIT_KINDLING)
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

    // Fruit Palm Leaves
    // Copying the regular Palm Leaves, but it's different to prevent sapling drops.
    public static final BlockEntry<TFCLeavesBlock> FRUIT_PALM_LEAVES = TFGCore.REGISTRATE.block("palm_tree/fruit_palm_leaves", p -> {
        var wood = Wood.PALM;
        return new TFCLeavesBlock(ExtendedProperties.of(p)
                .mapColor(MapColor.PLANT)
                .strength(0.5F)
                .sound(SoundType.GRASS)
                .defaultInstrument()
                .randomTicks()
                .noOcclusion()
                .isViewBlocking(TFCBlocks::never)
                .flammableLikeLeaves(),
                wood.autumnIndex(), wood.getBlock(Wood.BlockType.FALLEN_LEAVES), wood.getBlock(Wood.BlockType.TWIG));
    })
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                    prov.models().getExistingFile(ResourceLocation.fromNamespaceAndPath("tfc", "block/wood/leaves/palm"))))
            .addLayer(() -> RenderType::cutoutMipped)
            .color(() -> () -> (state, level, pos, tintIndex) -> TFCColors.getFoliageColor(pos, tintIndex))
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
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .item(BlockItem::new)
            .color(() -> () -> (stack, tintIndex) -> TFCColors.getFoliageColor(null, tintIndex))
            .model((ctx, prov) -> prov.withExistingParent("palm_tree/fruit_palm_leaves",
                    ResourceLocation.fromNamespaceAndPath("tfc", "block/wood/leaves/palm")))
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
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
                        var model = prov.models().withExistingParent(ctx.getName(), TFGCore.id("block/palm_tree/palm_head"))
                                .texture("fuzz", TFGCore.id("block/palm_tree/" + name + "_head_fuzz"))
                                .texture("side", TFGCore.id("block/palm_tree/" + name + "_head_side"))
                                .texture("top", TFGCore.id("block/palm_tree/" + name + "_head_top"));
                        prov.simpleBlock(ctx.getEntry(), model);
                    })
                    .loot((prov, block) -> prov.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(TFGBlocks_PalmTrees.PALM_SAPLINGS.get(tree).get())
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                                    .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.AXES)))))))
                    .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, TFCTags.Blocks.LOGS_THAT_LOG, BlockTags.MINEABLE_WITH_AXE)
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .item(BlockItem::new)
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
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
                        var model = prov.models().withExistingParent(ctx.getName(), TFGCore.id("block/palm_tree/" + name + "_tree_head"));
                        prov.simpleBlock(ctx.getEntry(), model);
                    })
                    .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN, TFCTags.Blocks.LOGS_THAT_LOG, BlockTags.MINEABLE_WITH_AXE)
                    .loot((prov, block) -> prov.add(block, LootTable.lootTable().withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(TFGBlocks_PalmTrees.PALM_SAPLINGS.get(tree).get())
                                    .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.AXES)))))))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .item(BlockItem::new)
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                    .build()
                    .register());

            // Palm Saplings
            PALM_SAPLINGS.put(tree, TFGCore.REGISTRATE.block("palm_tree/" + name + "_sapling", p -> (PalmTreeSaplingBlock) tree.createSapling())
                    .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                            prov.models().withExistingParent(ctx.getName(), "tfg:block/palm_tree/palm_sapling")
                                    .texture("cross", TFGCore.id("block/palm_tree/" + name + "_sapling"))))
                    .tag(BlockTags.SAPLINGS, TFCTags.Blocks.FRUIT_TREE_SAPLING)
                    .item(BlockItem::new)
                    .model((ctx, prov) -> prov.basicItem(TFGCore.id("palm_tree/" + name + "_sapling")))
                    .tag(ItemTags.SAPLINGS)
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
                    .tag(BlockTags.FLOWER_POTS)
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
                        var model = prov.models().withExistingParent(ctx.getName() + "_" + age, TFGCore.id("block/palm_tree/square_palm_cluster_" + age))
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
