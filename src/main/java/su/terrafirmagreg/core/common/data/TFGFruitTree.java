package su.terrafirmagreg.core.common.data;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import com.tterrag.registrate.providers.ProviderType;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
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
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.fruittreeblocks.TFGFruitTreeSaplingBlock;
import su.terrafirmagreg.core.common.data.blocks.fruittreeblocks.TFGGrowingFruitTreeBranchBlock;

/**
 * Registration of custom TFG fruit tree blocks and items.
 */
public final class TFGFruitTree {

    private static final TagKey<Item> TFC_AXES = TagKey.create(ForgeRegistries.Keys.ITEMS,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tfc", "axes"));
    private static final TagKey<Item> TFC_SHARP_TOOLS = TagKey.create(ForgeRegistries.Keys.ITEMS,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tfc", "sharp_tools"));

    public static final Map<TFGFruitTrees, BlockEntry<Block>> FRUIT_TREE_SAPLINGS = new EnumMap<>(TFGFruitTrees.class);
    public static final Map<TFGFruitTrees, BlockEntry<Block>> FRUIT_TREE_POTTED_SAPLINGS = new EnumMap<>(TFGFruitTrees.class);
    public static final Map<TFGFruitTrees, BlockEntry<Block>> FRUIT_TREE_LEAVES = new EnumMap<>(TFGFruitTrees.class);
    public static final Map<TFGFruitTrees, BlockEntry<Block>> FRUIT_TREE_BRANCHES = new EnumMap<>(TFGFruitTrees.class);
    public static final Map<TFGFruitTrees, BlockEntry<Block>> FRUIT_TREE_GROWING_BRANCHES = new EnumMap<>(TFGFruitTrees.class);
    public static final Map<TFGFruitTrees, ItemEntry<Item>> FRUIT_TREE_PRODUCTS = new EnumMap<>(TFGFruitTrees.class);

    static {
        for (TFGFruitTrees tree : TFGFruitTrees.values()) {
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
    private static void register(TFGFruitTrees tree) {
        String name = tree.getSerializedName();
        Supplier<ClimateRange> climate = climateSupplier(tree);

        // Product item (edible fruit).
        ItemEntry<Item> productItem = TFGCore.REGISTRATE.item("food/" + name,
                p -> new Item(p.food(TFGFruitTrees.FRUIT_FOOD)))
                .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
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
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
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
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
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
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .loot((prov, block) -> leavesLoot(prov, block, productItem))
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
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .loot(TFGFruitTree::saplingLoot)
                .item(BlockItem::new).setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop()).build()
                .tag(TFCTags.Blocks.FRUIT_TREE_SAPLING)
                .register();

        // Potted Sapling.
        BlockEntry<Block> potted = TFGCore.REGISTRATE.<Block>block("fruit_trees/potted_" + name + "_sapling",
                p -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, sapling,
                        Block.Properties.copy(Blocks.POTTED_ACACIA_SAPLING)))
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .loot((prov, block) -> prov.add(block, LootTable.lootTable()))
                .register();

        FRUIT_TREE_SAPLINGS.put(tree, sapling);
        FRUIT_TREE_POTTED_SAPLINGS.put(tree, potted);
        FRUIT_TREE_LEAVES.put(tree, leaves);
        FRUIT_TREE_BRANCHES.put(tree, branch);
        FRUIT_TREE_GROWING_BRANCHES.put(tree, growingBranch);
        FRUIT_TREE_PRODUCTS.put(tree, productItem);
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
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(TFC_AXES)));

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
                                        .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(TFC_SHARP_TOOLS)))
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
    private static Supplier<ClimateRange> climateSupplier(TFGFruitTrees tree) {
        return ClimateRange.MANAGER.register(TFGCore.id("fruit_tree/" + tree.getSerializedName()));
    }
}
