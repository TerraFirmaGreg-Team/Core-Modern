package su.terrafirmagreg.core.common.data.blocks;

import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.content.decoration.girder.GirderEncasedShaftBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

import su.terrafirmagreg.core.TFGCore;

public class TFGBlocks_Girders {
    public static void init() {

    }

    //    public static final BlockEntry<TFGGirderBlock> TIN_ALLOY_GIRDER = TFGCore.REGISTRATE
    //            .block("girder/beam/tin_alloy", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.TIN_ALLOY_GIRDER))))
    //            .initialProperties(SharedProperties::softMetal)
    //            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
    //            .blockstate((ctx, prov) -> {
    //                buildGirderBlockStateEntry(ctx, prov, TFGCore.id("block/girder/beam/tin_alloy"), TFGCore.id("block/girder/pole/tin_alloy"), TFGCore.id("block/girder/pole_side/tin_alloy"));
    //            })
    //            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedBeamModel::new))
    //            .item().model((ctx, prov) -> {
    //                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/beam/item")).texture("0", TFGCore.id("block/girder/beam/tin_alloy"));
    //            }).build()
    //            .register();

    private static void buildGirderBlockStateEntry(DataGenContext<Block, ? extends Block> context, RegistrateBlockstateProvider provider, ResourceLocation texLoc, ResourceLocation poleTexLoc,
            ResourceLocation poleCtTexLoc) {
        var builder = provider.getMultipartBuilder(context.getEntry());

        builder.part().modelFile(provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/block_pole")).texture("2", poleTexLoc).texture("particle", texLoc)).addModel()
                .condition(GirderBlock.X, false).condition(GirderBlock.Z, false).end()

                .part().modelFile(provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/block_x")).texture("0", texLoc).texture("particle", texLoc)).addModel()
                .condition(GirderBlock.X, true).end()

                .part().modelFile(provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/block_z")).texture("0", texLoc).texture("particle", texLoc)).addModel()
                .condition(GirderBlock.Z, true).end()

                .part().modelFile(provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/block_top")).texture("0", texLoc).texture("particle", texLoc)).addModel()
                .condition(GirderBlock.TOP, true).condition(GirderBlock.X, true).condition(GirderBlock.Z, false).end()

                .part().modelFile(provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/block_bottom")).texture("0", texLoc).texture("particle", texLoc)).addModel()
                .condition(GirderBlock.BOTTOM, true).condition(GirderBlock.X, true).condition(GirderBlock.Z, false).end()

                .part().modelFile(provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/block_top")).texture("0", texLoc).texture("particle", texLoc)).addModel()
                .condition(GirderBlock.TOP, true).condition(GirderBlock.X, false).condition(GirderBlock.Z, true).end()

                .part().modelFile(provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/block_bottom")).texture("0", texLoc).texture("particle", texLoc)).addModel()
                .condition(GirderBlock.BOTTOM, true).condition(GirderBlock.X, false).condition(GirderBlock.Z, true).end()

                .part().modelFile(provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/block_cross")).texture("0", texLoc).texture("particle", texLoc)).addModel()
                .condition(GirderBlock.X, true).condition(GirderBlock.Z, true).end();

        provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/block_pole_top")).texture("2", poleTexLoc).texture("3", poleCtTexLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/block_pole_middle")).texture("2", poleTexLoc).texture("3", poleCtTexLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/block_pole_bottom")).texture("2", poleTexLoc).texture("3", poleCtTexLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/bracket_east")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/bracket_west")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/bracket_north")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/bracket_south")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/segment_middle")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/segment_top")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/segment_bottom")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName(), TFGCore.id("block/girder/beam/segment_middle_alt")).texture("0", texLoc).texture("particle", texLoc);
    }

    private static void buildGirderShaftBlockStateEntry(DataGenContext<Block, ? extends Block> c, RegistrateBlockstateProvider p) {
        MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());
        String blockName = c.getName();
        String modId = c.getId().getNamespace();

        builder.part()
                .modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block")))
                .rotationY(0)
                .addModel()
                .condition(GirderEncasedShaftBlock.HORIZONTAL_AXIS, Direction.Axis.Z)
                .end();

        builder.part()
                .modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block")))
                .rotationY(90)
                .addModel()
                .condition(GirderEncasedShaftBlock.HORIZONTAL_AXIS, Direction.Axis.X)
                .end();

        builder.part()
                .modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block_top")))
                .addModel()
                .condition(GirderEncasedShaftBlock.TOP, true)
                .end();

        builder.part()
                .modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block_bottom")))
                .addModel()
                .condition(GirderEncasedShaftBlock.BOTTOM, true)
                .end();
    }
}
