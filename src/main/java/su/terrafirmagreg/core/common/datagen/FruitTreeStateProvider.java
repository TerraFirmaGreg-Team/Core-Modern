package su.terrafirmagreg.core.common.datagen;

import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeLeavesBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGFruitTree;
import su.terrafirmagreg.core.common.data.TFGFruitTrees;

/**
 * Generates blockstates, block models, and item models for all {@link TFGFruitTrees} entries.
 */
public final class FruitTreeStateProvider {

    private final BlockStateProvider provider;
    private final BlockModelProvider models;
    private final ItemModelProvider itemModels;

    public FruitTreeStateProvider(BlockStateProvider provider) {
        this.provider = provider;
        this.models = provider.models();
        this.itemModels = provider.itemModels();
    }

    public void generate() {
        for (TFGFruitTrees tree : TFGFruitTrees.values()) {
            fruitTree(tree);
        }
    }

    /**
     * Generates block states, block models, and item models for fruit trees.
     *
     * @param tree The {@link TFGFruitTrees} entry representing the fruit tree.
     */
    private void fruitTree(TFGFruitTrees tree) {
        String name = tree.getSerializedName();

        // Block texture locations.
        ResourceLocation branchTex = TFGCore.id("block/fruit_tree/" + name + "_branch");
        ResourceLocation leavesTex = TFGCore.id("block/fruit_tree/" + name + "_leaves");
        ResourceLocation dryTex = TFGCore.id("block/fruit_tree/" + name + "_dry_leaves");
        ResourceLocation flowerTex = TFGCore.id("block/fruit_tree/" + name + "_flowering_leaves");
        ResourceLocation fruitTex = TFGCore.id("block/fruit_tree/" + name + "_fruiting_leaves");
        ResourceLocation saplingTex = TFGCore.id("block/fruit_tree/" + name + "_sapling");

        // Branch block models.
        String prefix = "block/fruit_trees/" + name;
        BlockModelBuilder branchCore = models.withExistingParent(prefix + "_branch_core",
                ResourceLocation.fromNamespaceAndPath("tfc", "block/plant/branch_core")).texture("bark", branchTex);
        BlockModelBuilder branchDown = models.withExistingParent(prefix + "_branch_down",
                ResourceLocation.fromNamespaceAndPath("tfc", "block/plant/branch_down")).texture("bark", branchTex);
        BlockModelBuilder branchUp = models.withExistingParent(prefix + "_branch_up",
                ResourceLocation.fromNamespaceAndPath("tfc", "block/plant/branch_up")).texture("bark", branchTex);
        BlockModelBuilder branchSide = models.withExistingParent(prefix + "_branch_side",
                ResourceLocation.fromNamespaceAndPath("tfc", "block/plant/branch_side")).texture("bark", branchTex);

        // Branch, growing branch multipart.
        buildBranchMultipart(TFGFruitTree.FRUIT_TREE_BRANCHES.get(tree).get(), branchCore, branchDown, branchUp, branchSide);
        buildBranchMultipart(TFGFruitTree.FRUIT_TREE_GROWING_BRANCHES.get(tree).get(), branchCore, branchDown, branchUp, branchSide);

        // Leaf block models.
        BlockModelBuilder leavesModel = models.withExistingParent(prefix + "_leaves", "block/leaves").texture("all", leavesTex);
        BlockModelBuilder dryModel = models.withExistingParent(prefix + "_dry_leaves", "block/leaves").texture("all", dryTex);
        BlockModelBuilder floweringModel = models.withExistingParent(prefix + "_flowering_leaves", "block/leaves").texture("all", flowerTex);
        BlockModelBuilder fruitingModel = models.withExistingParent(prefix + "_fruiting_leaves", "block/leaves").texture("all", fruitTex);

        // Leaf blockstate.
        provider.getVariantBuilder(TFGFruitTree.FRUIT_TREE_LEAVES.get(tree).get())
                .partialState().with(FruitTreeLeavesBlock.LIFECYCLE, Lifecycle.HEALTHY).addModels(new ConfiguredModel(leavesModel))
                .partialState().with(FruitTreeLeavesBlock.LIFECYCLE, Lifecycle.DORMANT).addModels(new ConfiguredModel(dryModel))
                .partialState().with(FruitTreeLeavesBlock.LIFECYCLE, Lifecycle.FLOWERING).addModels(new ConfiguredModel(floweringModel))
                .partialState().with(FruitTreeLeavesBlock.LIFECYCLE, Lifecycle.FRUITING).addModels(new ConfiguredModel(fruitingModel));

        // Sapling block models (4 stages).
        String[] saplingParents = {
                "block/cross",
                "tfc:block/plant/cross_2",
                "tfc:block/plant/cross_3",
                "tfc:block/plant/cross_4"
        };
        BlockModelBuilder[] saplingModels = new BlockModelBuilder[4];
        for (int i = 0; i < 4; i++) {
            saplingModels[i] = models.withExistingParent(prefix + "_sapling_" + (i + 1), saplingParents[i]).texture("cross", saplingTex);
        }

        // Sapling blockstate.
        IntegerProperty SAPLINGS = IntegerProperty.create("saplings", 1, 4);
        var saplingBuilder = provider.getVariantBuilder(TFGFruitTree.FRUIT_TREE_SAPLINGS.get(tree).get());
        for (int i = 0; i < 4; i++) {
            saplingBuilder.partialState().with(SAPLINGS, i + 1).addModels(new ConfiguredModel(saplingModels[i]));
        }

        // Potted sapling.
        BlockModelBuilder pottedModel = models.withExistingParent(prefix + "/potted_" + name + "_sapling",
                "minecraft:block/flower_pot_cross")
                .texture("plant", saplingTex)
                .texture("dirt", ResourceLocation.fromNamespaceAndPath("tfc", "block/dirt/loam"));
        provider.simpleBlock(TFGFruitTree.FRUIT_TREE_POTTED_SAPLINGS.get(tree).get(), pottedModel);

        // Item models.
        itemModels.withExistingParent("fruit_trees/" + name + "_sapling", "item/generated").texture("layer0", saplingTex);
        itemModels.withExistingParent("fruit_trees/" + name + "_leaves", TFGCore.id(prefix + "_leaves"));
        itemModels.withExistingParent("food/" + name, "item/generated").texture("layer0", TFGCore.id("item/food/" + name));
    }

    /**
     * Builds a multipart block state for a branch block.
     *
     * @param block The block to build the multipart state for.
     * @param core Core model file.
     * @param down Down model file.
     * @param up Up model file.
     * @param side Side model file.
     */
    private void buildBranchMultipart(Block block, ModelFile core, ModelFile down, ModelFile up, ModelFile side) {
        MultiPartBlockStateBuilder builder = provider.getMultipartBuilder(block);
        builder.part().modelFile(core).addModel().end();
        builder.part().modelFile(down).addModel().condition(PipeBlock.DOWN, true).end();
        builder.part().modelFile(up).addModel().condition(PipeBlock.UP, true).end();
        builder.part().modelFile(side).rotationY(90).addModel().condition(PipeBlock.NORTH, true).end();
        builder.part().modelFile(side).rotationY(270).addModel().condition(PipeBlock.SOUTH, true).end();
        builder.part().modelFile(side).addModel().condition(PipeBlock.WEST, true).end();
        builder.part().modelFile(side).rotationY(180).addModel().condition(PipeBlock.EAST, true).end();
    }
}
