package su.terrafirmagreg.core.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;

import su.terrafirmagreg.core.common.data.blocks.ActiveCardinalBlock;
import su.terrafirmagreg.core.common.data.blocks.ActiveParticleBlock;

public class TFGModelUtils {

    public static ModelFile cube2Layer(RegistrateBlockstateProvider prov, String name, ResourceLocation texture) {
        return prov.models().withExistingParent(name, GTCEu.id("block/cube_2_layer/all"))
                .texture("bot_all", texture)
                .texture("top_all", texture.withSuffix("_emissive"));
    }

    public static void activeBlock(VariantBlockStateBuilder builder, ModelFile inactive, ModelFile active) {
        builder.partialState().with(GTBlockStateProperties.ACTIVE, false)
                .modelForState().modelFile(inactive).addModel()
                .partialState().with(GTBlockStateProperties.ACTIVE, true)
                .modelForState().modelFile(active).addModel();
    }

    public static void activeCardinalBlock(VariantBlockStateBuilder builder, ModelFile inactive, ModelFile active) {
        builder.partialState().with(GTBlockStateProperties.ACTIVE, false).with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .modelForState().modelFile(inactive).addModel()
                .partialState().with(GTBlockStateProperties.ACTIVE, false).with(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
                .modelForState().modelFile(inactive).rotationY(180).addModel()
                .partialState().with(GTBlockStateProperties.ACTIVE, false).with(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
                .modelForState().modelFile(inactive).rotationY(270).addModel()
                .partialState().with(GTBlockStateProperties.ACTIVE, false).with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                .modelForState().modelFile(inactive).rotationY(90).addModel()
                .partialState().with(GTBlockStateProperties.ACTIVE, true).with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .modelForState().modelFile(active).addModel()
                .partialState().with(GTBlockStateProperties.ACTIVE, true).with(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
                .modelForState().modelFile(active).rotationY(180).addModel()
                .partialState().with(GTBlockStateProperties.ACTIVE, true).with(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
                .modelForState().modelFile(active).rotationY(270).addModel()
                .partialState().with(GTBlockStateProperties.ACTIVE, true).with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)
                .modelForState().modelFile(active).rotationY(90).addModel();
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
        return (ctx, prov) -> {
            activeBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().cubeAll(ctx.getName(), textureName), prov.models().cubeAll(ctx.getName() + "_active", textureName.withSuffix("_active")));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockstateProvider> existingActiveModel(ResourceLocation modelPath) {
        return (ctx, prov) -> {
            activeBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().getExistingFile(modelPath), prov.models().getExistingFile(modelPath.withSuffix("_active")));
        };
    }

    // has to be duplicated because of the different block type

    public static NonNullBiConsumer<DataGenContext<Block, ActiveCardinalBlock>, RegistrateBlockstateProvider> existingActiveCardinalModel(ResourceLocation modelPath) {
        return (ctx, prov) -> activeCardinalBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().getExistingFile(modelPath), prov.models().getExistingFile(modelPath.withSuffix("_active")));
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveParticleBlock>, RegistrateBlockstateProvider> existingActiveParticleModel(ResourceLocation modelPath) {
        return (ctx, prov) -> {
            activeBlock(prov.getVariantBuilder(ctx.getEntry()), prov.models().getExistingFile(modelPath), prov.models().getExistingFile(modelPath.withSuffix("_active")));
        };
    }

}
