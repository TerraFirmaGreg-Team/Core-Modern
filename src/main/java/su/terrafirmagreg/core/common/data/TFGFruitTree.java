package su.terrafirmagreg.core.common.data;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.BerryBushBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeBranchBlock;
import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeLeavesBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.fruittree.TFGFruitTreeSaplingBlock;
import su.terrafirmagreg.core.common.block.fruittree.TFGGrowingFruitTreeBranchBlock;

/**
 * Registration of custom TFG fruit tree blocks and items.
 */
public final class TFGFruitTree {

    /**
     * Custom fruit trees added by TFG.
     * Add new entries here to create additional fruit trees with full datagen support.
     * Then you need to add climate data in Kubejs, and finally textures in the core asset location.
     */
    @Getter
    public enum FruitTreeType implements StringRepresentable {

        LAVACADO(
                10,
                new Lifecycle[] {
                        Lifecycle.HEALTHY, Lifecycle.HEALTHY, Lifecycle.HEALTHY, Lifecycle.HEALTHY, Lifecycle.FLOWERING, Lifecycle.FLOWERING,
                        Lifecycle.FRUITING, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.HEALTHY
                },
                new Color(249, 255, 123).getRGB(),
                ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether")),
        MAGMANGO(
                10,
                new Lifecycle[] {
                        Lifecycle.HEALTHY, Lifecycle.HEALTHY, Lifecycle.FLOWERING, Lifecycle.FLOWERING, Lifecycle.FRUITING, Lifecycle.DORMANT,
                        Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.DORMANT, Lifecycle.HEALTHY, Lifecycle.HEALTHY, Lifecycle.HEALTHY
                },
                new Color(205, 76, 59).getRGB(),
                ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether"));

        public static final FoodProperties FRUIT_FOOD = new FoodProperties.Builder().nutrition(4).saturationMod(0.3F).build();

        private final String serializedName;
        private final int defaultGrowthDays;
        private final Lifecycle[] stages;
        private final int floweringLeavesColor;
        private final ResourceLocation dimension;

        /**
         * Constructor for {@link FruitTreeType}.
         *
         * @param defaultGrowthDays Default number of days required for growth.
         * @param stages Lifecycle stages is a 12-month cycle. Jan - Dec.
         * @param floweringLeavesColor RGB color of the leaf particles.
         * @param dimension The dimension this fruit tree is found in (Only information for the tooltip).
         */
        FruitTreeType(int defaultGrowthDays, Lifecycle[] stages, int floweringLeavesColor, ResourceLocation dimension) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.defaultGrowthDays = defaultGrowthDays;
            this.stages = stages;
            this.floweringLeavesColor = floweringLeavesColor;
            this.dimension = dimension;
        }
    }

    // Shout out to TFC for not having all their tags registered in java.
    private static final TagKey<Item> AXES = ItemTags.AXES;
    private static final TagKey<Item> TFC_ITEM_SHARP_TOOLS = TagKey.create(ForgeRegistries.Keys.ITEMS,
            ResourceLocation.fromNamespaceAndPath("tfc", "sharp_tools"));
    private static final TagKey<Item> TFC_ITEM_FRUIT_TREE_LEAVES = TagKey.create(ForgeRegistries.Keys.ITEMS,
            ResourceLocation.fromNamespaceAndPath("tfc", "fruit_tree_leaves"));
    private static final TagKey<Item> TFC_ITEM_WILD_FRUITS = TagKey.create(ForgeRegistries.Keys.ITEMS,
            ResourceLocation.fromNamespaceAndPath("tfc", "wild_fruits"));
    private static final TagKey<Item> TFC_ITEM_PLANTS = TagKey.create(ForgeRegistries.Keys.ITEMS,
            ResourceLocation.fromNamespaceAndPath("tfc", "plants"));

    private static final TagKey<Block> TFC_BLOCK_WILD_FRUITS = TagKey.create(ForgeRegistries.Keys.BLOCKS,
            ResourceLocation.fromNamespaceAndPath("tfc", "wild_fruits"));
    private static final TagKey<Block> TFC_BLOCK_MINEABLE_SHARP_TOOL = TagKey.create(ForgeRegistries.Keys.BLOCKS,
            ResourceLocation.fromNamespaceAndPath("tfc", "mineable_with_sharp_tool"));

    public static final Map<FruitTreeType, BlockEntry<Block>> FRUIT_TREE_SAPLINGS = new EnumMap<>(FruitTreeType.class);
    public static final Map<FruitTreeType, BlockEntry<Block>> FRUIT_TREE_POTTED_SAPLINGS = new EnumMap<>(FruitTreeType.class);
    public static final Map<FruitTreeType, BlockEntry<Block>> FRUIT_TREE_LEAVES = new EnumMap<>(FruitTreeType.class);
    public static final Map<FruitTreeType, BlockEntry<Block>> FRUIT_TREE_BRANCHES = new EnumMap<>(FruitTreeType.class);
    public static final Map<FruitTreeType, BlockEntry<Block>> FRUIT_TREE_GROWING_BRANCHES = new EnumMap<>(FruitTreeType.class);
    public static final Map<FruitTreeType, ItemEntry<Item>> FRUIT_TREE_PRODUCTS = new EnumMap<>(FruitTreeType.class);

    static {
        for (FruitTreeType tree : FruitTreeType.values()) {
            register(tree);
        }
    }

    public static void init() {
    }

    /**
     * Registers all components of a specific fruit tree type.
     *
     * @param tree The fruit tree type to register.
     */
    private static void register(FruitTreeType tree) {
        String name = tree.getSerializedName();
        Supplier<ClimateRange> climate = climateSupplier(tree);
        TagKey<Item> tfgFoodProductTag = TagKey.create(ForgeRegistries.Keys.ITEMS, ResourceLocation.fromNamespaceAndPath("tfg", "foods/" + name));

        // Product item (edible fruit).
        ItemEntry<Item> productItem = TFGCore.REGISTRATE.item("food/" + name,
                p -> new Item(p.food(FruitTreeType.FRUIT_FOOD)))
                .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .tag(TFCTags.Items.FOODS, tfgFoodProductTag)
                .register();

        // Growing Branch.
        BlockEntry<Block> growingBranch = TFGCore.REGISTRATE.<Block>block("fruit_trees/" + name + "_growing_branch",
                p -> new TFGGrowingFruitTreeBranchBlock(
                        ExtendedProperties.of(MapColor.WOOD)
                                .sound(SoundType.SCAFFOLDING)
                                .randomTicks()
                                .strength(1.0F)
                                .pushReaction(PushReaction.DESTROY)
                                .blockEntity(TFGBlockEntities.FRUIT_TREE_TICK_COUNTER)
                                .flammableLikeLogs(),
                        FRUIT_TREE_BRANCHES.get(tree),
                        FRUIT_TREE_LEAVES.get(tree),
                        climate))
                .setData(ProviderType.BLOCKSTATE, branchBlockstate(tree))
                .loot(TFGFruitTree::growingBranchLoot)
                .tag(TFCTags.Blocks.FRUIT_TREE_BRANCH, BlockTags.MINEABLE_WITH_AXE)
                .register();

        // Branch.
        BlockEntry<Block> branch = TFGCore.REGISTRATE.<Block>block("fruit_trees/" + name + "_branch",
                p -> new FruitTreeBranchBlock(
                        ExtendedProperties.of(MapColor.WOOD)
                                .sound(SoundType.SCAFFOLDING)
                                .randomTicks()
                                .strength(1.0F)
                                .pushReaction(PushReaction.DESTROY)
                                .flammableLikeLogs(),
                        climate))
                .setData(ProviderType.BLOCKSTATE, branchBlockstate(tree))
                .loot((prov, block) -> branchLoot(prov, block, FRUIT_TREE_SAPLINGS.get(tree)))
                .tag(TFCTags.Blocks.FRUIT_TREE_BRANCH, BlockTags.MINEABLE_WITH_AXE)
                .register();

        // Leaves.
        BlockEntry<Block> leaves = TFGCore.REGISTRATE.<Block>block("fruit_trees/" + name + "_leaves",
                p -> new FruitTreeLeavesBlock(
                        ExtendedProperties.of()
                                .mapColor(FruitTreeLeavesBlock::getMapColor)
                                .strength(0.5F)
                                .sound(SoundType.GRASS)
                                .randomTicks()
                                .noOcclusion()
                                .blockEntity(TFGBlockEntities.FRUIT_TREE_BERRY_BUSH)
                                .serverTicks(BerryBushBlockEntity::serverTick)
                                .flammableLikeLeaves(),
                        productItem,
                        tree.getStages(),
                        climate,
                        tree.getFloweringLeavesColor()))
                .setData(ProviderType.BLOCKSTATE, leavesBlockstate(tree))
                .loot((prov, block) -> leavesLoot(prov, block, productItem))
                .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).tag(TFC_ITEM_FRUIT_TREE_LEAVES, ItemTags.LEAVES).build()
                .tag(TFCTags.Blocks.FRUIT_TREE_LEAVES, TFCTags.Blocks.MINEABLE_WITH_SCYTHE, BlockTags.LEAVES)
                .register();

        // Sapling.
        BlockEntry<Block> sapling = TFGCore.REGISTRATE.<Block>block("fruit_trees/" + name + "_sapling",
                p -> new TFGFruitTreeSaplingBlock(
                        ExtendedProperties.of(MapColor.PLANT)
                                .noCollission()
                                .randomTicks()
                                .strength(0.0F)
                                .sound(SoundType.GRASS)
                                .blockEntity(TFGBlockEntities.FRUIT_TREE_TICK_COUNTER)
                                .flammableLikeLeaves(),
                        growingBranch,
                        tree::getDefaultGrowthDays,
                        climate,
                        tree.getStages()))
                .setData(ProviderType.BLOCKSTATE, saplingBlockstate(tree))
                .loot(TFGFruitTree::saplingLoot)
                .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).tag(ItemTags.SAPLINGS, TFC_ITEM_WILD_FRUITS, TFC_ITEM_PLANTS).build()
                .tag(TFCTags.Blocks.FRUIT_TREE_SAPLING, TFC_BLOCK_MINEABLE_SHARP_TOOL, BlockTags.SAPLINGS, TFC_BLOCK_WILD_FRUITS)
                .register();

        // Potted Sapling.
        BlockEntry<Block> potted = TFGCore.REGISTRATE.<Block>block("fruit_trees/potted_" + name + "_sapling",
                p -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, sapling,
                        Block.Properties.copy(Blocks.POTTED_ACACIA_SAPLING)))
                .setData(ProviderType.BLOCKSTATE, pottedSaplingBlockstate(tree))
                .loot((prov, block) -> prov.add(block, LootTable.lootTable()))
                .register();

        FRUIT_TREE_SAPLINGS.put(tree, sapling);
        FRUIT_TREE_POTTED_SAPLINGS.put(tree, potted);
        FRUIT_TREE_LEAVES.put(tree, leaves);
        FRUIT_TREE_BRANCHES.put(tree, branch);
        FRUIT_TREE_GROWING_BRANCHES.put(tree, growingBranch);
        FRUIT_TREE_PRODUCTS.put(tree, productItem);
    }

    /**
     * Returns a blockstate callback that generates a branch multipart for the given tree type.
     */
    private static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> branchBlockstate(FruitTreeType tree) {
        return (ctx, prov) -> {
            String name = tree.getSerializedName();
            String prefix = "block/fruit_trees/" + name;
            ResourceLocation branchTex = TFGCore.id("block/fruit_tree/" + name + "_branch");

            BlockModelBuilder branchCore = prov.models().withExistingParent(prefix + "_branch_core",
                    ResourceLocation.fromNamespaceAndPath("tfc", "block/plant/branch_core")).texture("bark", branchTex);
            BlockModelBuilder branchDown = prov.models().withExistingParent(prefix + "_branch_down",
                    ResourceLocation.fromNamespaceAndPath("tfc", "block/plant/branch_down")).texture("bark", branchTex);
            BlockModelBuilder branchUp = prov.models().withExistingParent(prefix + "_branch_up",
                    ResourceLocation.fromNamespaceAndPath("tfc", "block/plant/branch_up")).texture("bark", branchTex);
            BlockModelBuilder branchSide = prov.models().withExistingParent(prefix + "_branch_side",
                    ResourceLocation.fromNamespaceAndPath("tfc", "block/plant/branch_side")).texture("bark", branchTex);

            buildBranchMultipart(prov, ctx.get(), branchCore, branchDown, branchUp, branchSide);
        };
    }

    /**
     * Returns a blockstate callback that generates leaf variant states for the given tree type.
     * Also generates the leaf item model.
     */
    private static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> leavesBlockstate(FruitTreeType tree) {
        return (ctx, prov) -> {
            String name = tree.getSerializedName();
            String prefix = "block/fruit_trees/" + name;

            ResourceLocation leavesTex = TFGCore.id("block/fruit_tree/" + name + "_leaves");
            ResourceLocation dryTex = TFGCore.id("block/fruit_tree/" + name + "_dry_leaves");
            ResourceLocation flowerTex = TFGCore.id("block/fruit_tree/" + name + "_flowering_leaves");
            ResourceLocation fruitTex = TFGCore.id("block/fruit_tree/" + name + "_fruiting_leaves");

            BlockModelBuilder leavesModel = prov.models().withExistingParent(prefix + "_leaves", "block/leaves").texture("all", leavesTex);
            BlockModelBuilder dryModel = prov.models().withExistingParent(prefix + "_dry_leaves", "block/leaves").texture("all", dryTex);
            BlockModelBuilder floweringModel = prov.models().withExistingParent(prefix + "_flowering_leaves", "block/leaves").texture("all", flowerTex);
            BlockModelBuilder fruitingModel = prov.models().withExistingParent(prefix + "_fruiting_leaves", "block/leaves").texture("all", fruitTex);

            prov.getVariantBuilder(ctx.get())
                    .partialState().with(FruitTreeLeavesBlock.LIFECYCLE, Lifecycle.HEALTHY).addModels(new ConfiguredModel(leavesModel))
                    .partialState().with(FruitTreeLeavesBlock.LIFECYCLE, Lifecycle.DORMANT).addModels(new ConfiguredModel(dryModel))
                    .partialState().with(FruitTreeLeavesBlock.LIFECYCLE, Lifecycle.FLOWERING).addModels(new ConfiguredModel(floweringModel))
                    .partialState().with(FruitTreeLeavesBlock.LIFECYCLE, Lifecycle.FRUITING).addModels(new ConfiguredModel(fruitingModel));

            // Leaf item model.
            prov.itemModels().withExistingParent("fruit_trees/" + name + "_leaves", TFGCore.id(prefix + "_leaves"));
        };
    }

    /**
     * Returns a blockstate callback that generates sapling variant states for the given tree type.
     * Also generates the sapling item model and the food item model.
     */
    private static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> saplingBlockstate(FruitTreeType tree) {
        return (ctx, prov) -> {
            String name = tree.getSerializedName();
            String prefix = "block/fruit_trees/" + name;
            ResourceLocation saplingTex = TFGCore.id("block/fruit_tree/" + name + "_sapling");

            String[] saplingParents = {
                    "block/cross",
                    "tfc:block/plant/cross_2",
                    "tfc:block/plant/cross_3",
                    "tfc:block/plant/cross_4"
            };
            BlockModelBuilder[] saplingModels = new BlockModelBuilder[4];
            for (int i = 0; i < 4; i++) {
                saplingModels[i] = prov.models().withExistingParent(prefix + "_sapling_" + (i + 1), saplingParents[i]).texture("cross", saplingTex);
            }

            IntegerProperty SAPLINGS = IntegerProperty.create("saplings", 1, 4);
            var saplingBuilder = prov.getVariantBuilder(ctx.get());
            for (int i = 0; i < 4; i++) {
                saplingBuilder.partialState().with(SAPLINGS, i + 1).addModels(new ConfiguredModel(saplingModels[i]));
            }

            // Sapling item model.
            prov.itemModels().withExistingParent("fruit_trees/" + name + "_sapling", "item/generated").texture("layer0", saplingTex);

            // Food item model.
            prov.itemModels().withExistingParent("food/" + name, "item/generated").texture("layer0", TFGCore.id("item/food/" + name));
        };
    }

    /**
     * Returns a blockstate callback that generates the potted sapling model.
     */
    private static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> pottedSaplingBlockstate(FruitTreeType tree) {
        return (ctx, prov) -> {
            String name = tree.getSerializedName();
            String prefix = "block/fruit_trees/" + name;
            ResourceLocation saplingTex = TFGCore.id("block/fruit_tree/" + name + "_sapling");

            BlockModelBuilder pottedModel = prov.models().withExistingParent(prefix + "/potted_" + name + "_sapling",
                    "minecraft:block/flower_pot_cross")
                    .texture("plant", saplingTex)
                    .texture("dirt", ResourceLocation.fromNamespaceAndPath("tfc", "block/dirt/loam"));
            prov.simpleBlock(ctx.get(), pottedModel);
        };
    }

    /**
     * Builds a multipart block state for a branch block.
     */
    private static void buildBranchMultipart(RegistrateBlockstateProvider prov, Block block,
            ModelFile core, ModelFile down, ModelFile up, ModelFile side) {
        MultiPartBlockStateBuilder builder = prov.getMultipartBuilder(block);
        builder.part().modelFile(core).addModel().end();
        builder.part().modelFile(down).addModel().condition(PipeBlock.DOWN, true).end();
        builder.part().modelFile(up).addModel().condition(PipeBlock.UP, true).end();
        builder.part().modelFile(side).rotationY(90).addModel().condition(PipeBlock.NORTH, true).end();
        builder.part().modelFile(side).rotationY(270).addModel().condition(PipeBlock.SOUTH, true).end();
        builder.part().modelFile(side).addModel().condition(PipeBlock.WEST, true).end();
        builder.part().modelFile(side).rotationY(180).addModel().condition(PipeBlock.EAST, true).end();
    }

    private static void branchLoot(RegistrateBlockLootTables prov, Block block, Supplier<? extends Block> saplingSupplier) {
        var elbowCondition = AllOfCondition.allOf(
                AnyOfCondition.anyOf(
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(PipeBlock.UP, true).hasProperty(PipeBlock.WEST, true)),
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(PipeBlock.UP, true).hasProperty(PipeBlock.EAST, true)),
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(PipeBlock.UP, true).hasProperty(PipeBlock.NORTH, true)),
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(PipeBlock.UP, true).hasProperty(PipeBlock.SOUTH, true))),
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(AXES)));

        prov.add(block, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(saplingSupplier.get().asItem()).when(elbowCondition))
                        .when(ExplosionCondition.survivesExplosion()))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.STICK)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                        .when(ExplosionCondition.survivesExplosion())));
    }

    private static void growingBranchLoot(RegistrateBlockLootTables prov, Block block) {
        prov.add(block, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.STICK)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))
                        .when(ExplosionCondition.survivesExplosion())));
    }

    private static void saplingLoot(RegistrateBlockLootTables prov, Block block) {
        IntegerProperty SAPLINGS = IntegerProperty.create("saplings", 1, 4);
        var entry = LootItem.lootTableItem(block.asItem());
        for (int i = 1; i <= 4; i++) {
            entry = entry.apply(SetItemCountFunction.setCount(ConstantValue.exactly(i))
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(SAPLINGS, i))));
        }
        prov.add(block, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(entry.apply(ApplyExplosionDecay.explosionDecay()))
                        .when(ExplosionCondition.survivesExplosion())));
    }

    private static void leavesLoot(RegistrateBlockLootTables prov, Block block, Supplier<? extends Item> product) {
        var shearsOrSilkTouch = AnyOfCondition.anyOf(
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(Tags.Items.SHEARS)),
                MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(
                        new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1)))));

        prov.add(block, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(product.get())
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(FruitTreeLeavesBlock.LIFECYCLE, Lifecycle.FRUITING))))
                        .when(ExplosionCondition.survivesExplosion()))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(block.asItem()).when(shearsOrSilkTouch))
                        .when(ExplosionCondition.survivesExplosion()))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(Items.STICK)
                                        .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TFC_ITEM_SHARP_TOOLS)))
                                        .when(LootItemRandomChanceCondition.randomChance(0.2F))
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))),
                                LootItem.lootTableItem(Items.STICK)
                                        .when(LootItemRandomChanceCondition.randomChance(0.05F))
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                                .when(InvertedLootItemCondition.invert(shearsOrSilkTouch)))
                        .when(ExplosionCondition.survivesExplosion())));
    }

    /**
     * Registers a climate range entry with TFC's {@link ClimateRange#MANAGER}.
     * This needs to be made in KubeJS.
     *
     * @param tree The fruit tree type for which the climate range is registered.
     * @return A supplier for the climate range.
     */
    private static Supplier<ClimateRange> climateSupplier(FruitTreeType tree) {
        return ClimateRange.MANAGER.register(TFGCore.id("fruit_tree/" + tree.getSerializedName()));
    }

}
