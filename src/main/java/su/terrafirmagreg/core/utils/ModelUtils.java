package su.terrafirmagreg.core.utils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.ActiveCardinalBlock;
import su.terrafirmagreg.core.common.block.ActiveParticleBlock;
import su.terrafirmagreg.core.common.block.SandLayerBlock;

public class ModelUtils {

    public static <T extends Item> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> layeredItemModel(ResourceLocation... layers) {
        return (ctx, prov) -> {
            ItemModelBuilder ret = prov.getBuilder(ctx.getName()).parent(new ModelFile.UncheckedModelFile("item/generated"));
            for (int i = 0; i < layers.length; i++) {
                ret = ret.texture("layer" + i, layers[i]);
            }
        };
    }

    public static <T extends BlockItem> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> blockItemModel(ResourceLocation blockModel) {
        return (ctx, prov) -> prov.withExistingParent(ctx.getName(), blockModel);
    }

    public static <T extends BlockItem> NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> barrelItemModel(ResourceLocation openBarrelModel, ResourceLocation sealedBarrelModel) {
        return (ctx, prov) -> prov.withExistingParent(ctx.getName(), openBarrelModel)
                .override().predicate(ResourceLocation.fromNamespaceAndPath(TerraFirmaCraft.MOD_ID, "sealed"), 1.0f)
                .model(prov.getExistingFile(sealedBarrelModel));
    }

    public static ModelFile cube2Layer(RegistrateBlockstateProvider prov, String name, ResourceLocation texture) {
        return prov.models().withExistingParent(name, GTCEu.id("block/cube_2_layer/all"))
                .texture("bot_all", texture)
                .texture("top_all", texture.withSuffix("_emissive"));
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> blockVariantsRotated(ResourceLocation base) {
        return (ctx, prov) -> {
            var builder = prov.getVariantBuilder(ctx.get()).partialState();
            var model = prov.models().getExistingFile(base);
            builder.addModels(new ConfiguredModel(model),
                    new ConfiguredModel(model, 0, 90, false),
                    new ConfiguredModel(model, 0, 180, false),
                    new ConfiguredModel(model, 0, 270, false));
        };
    }

    public static void blockVariantsRotated(VariantBlockStateBuilder builder, ModelFile model) {
        builder.partialState().addModels(new ConfiguredModel(model),
                new ConfiguredModel(model, 0, 90, false),
                new ConfiguredModel(model, 0, 180, false),
                new ConfiguredModel(model, 0, 270, false));
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> blockVariants(ResourceLocation... variants) {
        return (ctx, prov) -> {
            List<ModelFile.ExistingModelFile> models = Arrays.stream(variants).map(v -> prov.models().getExistingFile(v)).toList();

            var builder = prov.getVariantBuilder(ctx.get()).partialState();
            models.forEach(m -> builder.addModels(new ConfiguredModel(m)));
        };
    }

    public static void activeBlock(VariantBlockStateBuilder builder, ModelFile inactive, ModelFile active) {
        builder.partialState().with(GTBlockStateProperties.ACTIVE, false)
                .modelForState().modelFile(inactive).addModel()
                .partialState().with(GTBlockStateProperties.ACTIVE, true)
                .modelForState().modelFile(active).addModel();
    }

    public static void cardinalBlock(VariantBlockStateBuilder builder, ModelFile model) {
        builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .modelForState().modelFile(model).addModel()
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
                .modelForState().modelFile(model).rotationY(180).addModel()
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
                .modelForState().modelFile(model).rotationY(270).addModel()
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                .modelForState().modelFile(model).rotationY(90).addModel();
    }

    public static void forEachCardinalDirection(VariantBlockStateBuilder builder, ModelFile model,
            Function<VariantBlockStateBuilder.PartialBlockstate, VariantBlockStateBuilder.PartialBlockstate> func) {
        var north = builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        func.apply(north).modelForState().modelFile(model).addModel();
        var south = builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
        func.apply(south).modelForState().modelFile(model).rotationY(180).addModel();
        var east = builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
        func.apply(east).modelForState().modelFile(model).rotationY(90).addModel();
        var west = builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST);
        func.apply(west).modelForState().modelFile(model).rotationY(270).addModel();
    }

    // Thanks TFC
    public static void cardinalBlockInverted(VariantBlockStateBuilder builder, ModelFile model) {
        builder.partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .modelForState().modelFile(model).rotationY(180).addModel()
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
                .modelForState().modelFile(model).addModel()
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
                .modelForState().modelFile(model).rotationY(90).addModel()
                .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                .modelForState().modelFile(model).rotationY(270).addModel();
    }

    public static void activeCardinalBlock(VariantBlockStateBuilder builder, ModelFile inactive, ModelFile active) {
        forEachCardinalDirection(builder, inactive, b -> b.with(GTBlockStateProperties.ACTIVE, false));
        forEachCardinalDirection(builder, active, b -> b.with(GTBlockStateProperties.ACTIVE, true));
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockstateProvider> createActiveCasingModel(ResourceLocation texture) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            activeBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().cubeAll(name, texture), cube2Layer(prov, name + "_active", texture.withSuffix("_active")));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveCardinalBlock>, RegistrateBlockstateProvider> createActiveCardinalCasingModel(ResourceLocation texture) {
        return (ctx, prov) -> {
            String name = ctx.getName();
            activeCardinalBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().cubeAll(name, texture), cube2Layer(prov, name + "_active", texture.withSuffix("_active")));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockstateProvider> createActiveModel(ResourceLocation textureName) {
        return (ctx, prov) -> activeBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().cubeAll(ctx.getName(), textureName),
                prov.models().cubeAll(ctx.getName() + "_active", textureName.withSuffix("_active")));
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockstateProvider> existingActiveModel(ResourceLocation modelPath) {
        return (ctx, prov) -> activeBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().getExistingFile(modelPath), prov.models().getExistingFile(modelPath.withSuffix("_active")));
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveCardinalBlock>, RegistrateBlockstateProvider> existingActiveCardinalModel(ResourceLocation modelPath) {
        return (ctx, prov) -> activeCardinalBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().getExistingFile(modelPath), prov.models().getExistingFile(modelPath.withSuffix("_active")));
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveParticleBlock>, RegistrateBlockstateProvider> existingActiveParticleModel(ResourceLocation modelPath) {
        return (ctx, prov) -> activeBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().getExistingFile(modelPath), prov.models().getExistingFile(modelPath.withSuffix("_active")));
    }

    public static <T extends SandLayerBlock> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> generateSandLayersFromBlock(ResourceLocation blockToUse) {
        return (ctx, prov) -> {

            var fullModel = prov.models().getExistingFile(blockToUse);
            ModelFile layer1model = prov.models().withExistingParent("block/" + ctx.getName() + "_height2", TFGCore.id("block/ash_pile/ash_height2"))
                    .texture("particle", blockToUse).texture("texture", blockToUse);
            ModelFile layer2model = prov.models().withExistingParent("block/" + ctx.getName() + "_height4", TFGCore.id("block/ash_pile/ash_height4"))
                    .texture("particle", blockToUse).texture("texture", blockToUse);
            ModelFile layer3model = prov.models().withExistingParent("block/" + ctx.getName() + "_height6", TFGCore.id("block/ash_pile/ash_height6"))
                    .texture("particle", blockToUse).texture("texture", blockToUse);
            ModelFile layer4model = prov.models().withExistingParent("block/" + ctx.getName() + "_height8", TFGCore.id("block/ash_pile/ash_height8"))
                    .texture("particle", blockToUse).texture("texture", blockToUse);
            ModelFile layer5model = prov.models().withExistingParent("block/" + ctx.getName() + "_height10", TFGCore.id("block/ash_pile/ash_height10"))
                    .texture("particle", blockToUse).texture("texture", blockToUse);
            ModelFile layer6model = prov.models().withExistingParent("block/" + ctx.getName() + "_height12", TFGCore.id("block/ash_pile/ash_height12"))
                    .texture("particle", blockToUse).texture("texture", blockToUse);
            ModelFile layer7model = prov.models().withExistingParent("block/" + ctx.getName() + "_height14", TFGCore.id("block/ash_pile/ash_height14"))
                    .texture("particle", blockToUse).texture("texture", blockToUse);

            prov.getVariantBuilder(ctx.getEntry())
                    .partialState().with(SandLayerBlock.LAYERS, 1).modelForState().modelFile(layer1model).addModel()
                    .partialState().with(SandLayerBlock.LAYERS, 2).modelForState().modelFile(layer2model).addModel()
                    .partialState().with(SandLayerBlock.LAYERS, 3).modelForState().modelFile(layer3model).addModel()
                    .partialState().with(SandLayerBlock.LAYERS, 4).modelForState().modelFile(layer4model).addModel()
                    .partialState().with(SandLayerBlock.LAYERS, 5).modelForState().modelFile(layer5model).addModel()
                    .partialState().with(SandLayerBlock.LAYERS, 6).modelForState().modelFile(layer6model).addModel()
                    .partialState().with(SandLayerBlock.LAYERS, 7).modelForState().modelFile(layer7model).addModel()
                    .partialState().with(SandLayerBlock.LAYERS, 8).modelForState().modelFile(fullModel).addModel();
        };
    }

}
