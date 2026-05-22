package su.terrafirmagreg.core.common.data.blocks;

import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.girder.TFGGirderBlock;
import su.terrafirmagreg.core.common.block.girder.TFGGirderConnectedBeamModel;
import su.terrafirmagreg.core.common.block.girder.TFGGirderConnectedTrussModel;
import su.terrafirmagreg.core.common.block.girder.TFGGirderPlacementHelper;
import su.terrafirmagreg.core.common.data.TFGTags;

public class TFGBlocks_Girders {
    public static final ObjectOpenHashSet<BlockEntry<? extends Block>> GIRDERS = new ObjectOpenHashSet<>();

    public static void init() {
        GIRDERS.add(TIN_ALLOY_BEAM);
        GIRDERS.add(BRASS_BEAM);
        GIRDERS.add(WROUGHT_IRON_BEAM);
        GIRDERS.add(COPPER_BEAM);
        GIRDERS.add(ZINC_BEAM);

        GIRDERS.add(TIN_ALLOY_TRUSS);
        GIRDERS.add(BRASS_TRUSS);
        GIRDERS.add(WROUGHT_IRON_TRUSS);
        GIRDERS.add(COPPER_TRUSS);
        GIRDERS.add(STEEL_TRUSS);
        GIRDERS.add(ZINC_TRUSS);
    }

    public static final BlockEntry<TFGGirderBlock> TIN_ALLOY_BEAM = TFGCore.REGISTRATE
            .block("girder/beam/tin_alloy", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.TIN_ALLOY_BEAM)), false))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .blockstate((ctx, prov) -> {
                buildGirderBeamBlockStateEntry(ctx, prov, TFGCore.id("block/girder/beam/tin_alloy"), TFGCore.id("block/girder/pole/tin_alloy"), TFGCore.id("block/girder/pole_side/tin_alloy"));
            })
            .tag(TFGTags.Blocks.GIRDER, TFGTags.Blocks.PAVING_GIRDER)
            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedBeamModel::new))
            .item().model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/beam/item")).texture("0", TFGCore.id("block/girder/beam/tin_alloy"));
            })
            .tag(TFGTags.Items.GIRDER).build()
            .register();

    public static final BlockEntry<TFGGirderBlock> BRASS_BEAM = TFGCore.REGISTRATE
            .block("girder/beam/brass", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.BRASS_BEAM)), false))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .blockstate((ctx, prov) -> {
                buildGirderBeamBlockStateEntry(ctx, prov, TFGCore.id("block/girder/beam/brass"), TFGCore.id("block/girder/pole/brass"), TFGCore.id("block/girder/pole_side/brass"));
            })
            .tag(TFGTags.Blocks.GIRDER, TFGTags.Blocks.PAVING_GIRDER)
            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedBeamModel::new))
            .item().model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/beam/item")).texture("0", TFGCore.id("block/girder/beam/brass"));
            })
            .tag(TFGTags.Items.GIRDER).build()
            .register();

    public static final BlockEntry<TFGGirderBlock> WROUGHT_IRON_BEAM = TFGCore.REGISTRATE
            .block("girder/beam/wrought_iron", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.WROUGHT_IRON_BEAM)), false))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .blockstate((ctx, prov) -> {
                buildGirderBeamBlockStateEntry(ctx, prov, TFGCore.id("block/girder/beam/wrought_iron"), TFGCore.id("block/girder/pole/wrought_iron"),
                        TFGCore.id("block/girder/pole_side/wrought_iron"));
            })
            .tag(TFGTags.Blocks.GIRDER, TFGTags.Blocks.PAVING_GIRDER)
            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedBeamModel::new))
            .item().model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/beam/item")).texture("0", TFGCore.id("block/girder/beam/wrought_iron"));
            })
            .tag(TFGTags.Items.GIRDER).build()
            .register();

    public static final BlockEntry<TFGGirderBlock> COPPER_BEAM = TFGCore.REGISTRATE
            .block("girder/beam/copper", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.COPPER_BEAM)), false))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .blockstate((ctx, prov) -> {
                buildGirderBeamBlockStateEntry(ctx, prov, TFGCore.id("block/girder/beam/copper"), TFGCore.id("block/girder/pole/copper"), TFGCore.id("block/girder/pole_side/copper"));
            })
            .tag(TFGTags.Blocks.GIRDER, TFGTags.Blocks.PAVING_GIRDER)
            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedBeamModel::new))
            .item().model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/beam/item")).texture("0", TFGCore.id("block/girder/beam/copper"));
            })
            .tag(TFGTags.Items.GIRDER).build()
            .register();

    public static final BlockEntry<TFGGirderBlock> ZINC_BEAM = TFGCore.REGISTRATE
            .block("girder/beam/zinc", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.ZINC_BEAM)), false))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .blockstate((ctx, prov) -> {
                buildGirderBeamBlockStateEntry(ctx, prov, TFGCore.id("block/girder/beam/zinc"), TFGCore.id("block/girder/pole/zinc"), TFGCore.id("block/girder/pole_side/zinc"));
            })
            .tag(TFGTags.Blocks.GIRDER, TFGTags.Blocks.PAVING_GIRDER)
            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedBeamModel::new))
            .item().model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/beam/item")).texture("0", TFGCore.id("block/girder/beam/zinc"));
            })
            .tag(TFGTags.Items.GIRDER).build()
            .register();

    public static final BlockEntry<TFGGirderBlock> TIN_ALLOY_TRUSS = TFGCore.REGISTRATE
            .block("girder/truss/tin_alloy", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.TIN_ALLOY_TRUSS)), true))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .blockstate((ctx, prov) -> {
                buildGirderTrussBlockStateEntry(ctx, prov, TFGCore.id("block/girder/truss/tin_alloy"));
            })
            .tag(TFGTags.Blocks.GIRDER, TFGTags.Blocks.PAVING_GIRDER, TFGTags.Blocks.TRUSS)
            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedTrussModel::new))
            .item().model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/truss/item")).texture("0", TFGCore.id("block/girder/truss/tin_alloy"));
            })
            .tag(TFGTags.Items.GIRDER).build()
            .register();

    public static final BlockEntry<TFGGirderBlock> BRASS_TRUSS = TFGCore.REGISTRATE
            .block("girder/truss/brass", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.BRASS_TRUSS)), true))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .blockstate((ctx, prov) -> {
                buildGirderTrussBlockStateEntry(ctx, prov, TFGCore.id("block/girder/truss/brass"));
            })
            .tag(TFGTags.Blocks.GIRDER, TFGTags.Blocks.PAVING_GIRDER, TFGTags.Blocks.TRUSS)
            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedTrussModel::new))
            .item().model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/truss/item")).texture("0", TFGCore.id("block/girder/truss/brass"));
            })
            .tag(TFGTags.Items.GIRDER).build()
            .register();

    public static final BlockEntry<TFGGirderBlock> WROUGHT_IRON_TRUSS = TFGCore.REGISTRATE
            .block("girder/truss/wrought_iron", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.WROUGHT_IRON_TRUSS)), true))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .blockstate((ctx, prov) -> {
                buildGirderTrussBlockStateEntry(ctx, prov, TFGCore.id("block/girder/truss/wrought_iron"));
            })
            .tag(TFGTags.Blocks.GIRDER, TFGTags.Blocks.PAVING_GIRDER, TFGTags.Blocks.TRUSS)
            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedTrussModel::new))
            .item().model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/truss/item")).texture("0", TFGCore.id("block/girder/truss/wrought_iron"));
            })
            .tag(TFGTags.Items.GIRDER).build()
            .register();

    public static final BlockEntry<TFGGirderBlock> COPPER_TRUSS = TFGCore.REGISTRATE
            .block("girder/truss/copper", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.COPPER_TRUSS)), true))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .blockstate((ctx, prov) -> {
                buildGirderTrussBlockStateEntry(ctx, prov, TFGCore.id("block/girder/truss/copper"));
            })
            .tag(TFGTags.Blocks.GIRDER, TFGTags.Blocks.PAVING_GIRDER, TFGTags.Blocks.TRUSS)
            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedTrussModel::new))
            .item().model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/truss/item")).texture("0", TFGCore.id("block/girder/truss/copper"));
            })
            .tag(TFGTags.Items.GIRDER).build()
            .register();

    public static final BlockEntry<TFGGirderBlock> STEEL_TRUSS = TFGCore.REGISTRATE
            .block("girder/truss/steel", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.STEEL_TRUSS)), true))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .blockstate((ctx, prov) -> {
                buildGirderTrussBlockStateEntry(ctx, prov, TFGCore.id("block/girder/truss/steel"));
            })
            .tag(TFGTags.Blocks.GIRDER, TFGTags.Blocks.PAVING_GIRDER, TFGTags.Blocks.TRUSS)
            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedTrussModel::new))
            .item().model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/truss/item")).texture("0", TFGCore.id("block/girder/truss/steel"));
            })
            .tag(TFGTags.Items.GIRDER).build()
            .register();

    public static final BlockEntry<TFGGirderBlock> ZINC_TRUSS = TFGCore.REGISTRATE
            .block("girder/truss/zinc", p -> new TFGGirderBlock(p, PlacementHelpers.register(new TFGGirderPlacementHelper(TFGBlocks_Girders.ZINC_TRUSS)), true))
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .blockstate((ctx, prov) -> {
                buildGirderTrussBlockStateEntry(ctx, prov, TFGCore.id("block/girder/truss/zinc"));
            })
            .tag(TFGTags.Blocks.GIRDER, TFGTags.Blocks.PAVING_GIRDER, TFGTags.Blocks.TRUSS)
            .onRegister(CreateRegistrate.blockModel(() -> TFGGirderConnectedTrussModel::new))
            .item().model((ctx, prov) -> {
                prov.withExistingParent(ctx.getName(), TFGCore.id("block/girder/truss/item")).texture("0", TFGCore.id("block/girder/truss/zinc"));
            })
            .tag(TFGTags.Items.GIRDER).build()
            .register();

    private static void buildGirderBeamBlockStateEntry(DataGenContext<Block, ? extends Block> context, RegistrateBlockstateProvider provider, ResourceLocation texLoc, ResourceLocation poleTexLoc,
            ResourceLocation poleCtTexLoc) {
        var builder = provider.getMultipartBuilder(context.getEntry());

        builder.part()
                .modelFile(provider.models().withExistingParent(context.getName() + "/block_pole", TFGCore.id("block/girder/beam/block_pole")).texture("2", poleTexLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.X, false).condition(GirderBlock.Z, false).end()

                .part().modelFile(provider.models().withExistingParent(context.getName() + "/block_x", TFGCore.id("block/girder/beam/block_x")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.X, true).end()

                .part().modelFile(provider.models().withExistingParent(context.getName() + "/block_z", TFGCore.id("block/girder/beam/block_z")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.Z, true).end()

                .part().modelFile(provider.models().withExistingParent(context.getName() + "/block_top", TFGCore.id("block/girder/beam/block_top")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.TOP, true).condition(GirderBlock.X, true).condition(GirderBlock.Z, false).end()

                .part()
                .modelFile(provider.models().withExistingParent(context.getName() + "/block_bottom", TFGCore.id("block/girder/beam/block_bottom")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.BOTTOM, true).condition(GirderBlock.X, true).condition(GirderBlock.Z, false).end()

                .part().modelFile(provider.models().withExistingParent(context.getName() + "/block_top", TFGCore.id("block/girder/beam/block_top")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.TOP, true).condition(GirderBlock.X, false).condition(GirderBlock.Z, true).end()

                .part()
                .modelFile(provider.models().withExistingParent(context.getName() + "/block_bottom", TFGCore.id("block/girder/beam/block_bottom")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.BOTTOM, true).condition(GirderBlock.X, false).condition(GirderBlock.Z, true).end()

                .part()
                .modelFile(provider.models().withExistingParent(context.getName() + "/block_cross", TFGCore.id("block/girder/beam/block_cross")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.X, true).condition(GirderBlock.Z, true).end();

        provider.models().withExistingParent(context.getName() + "/block_pole_top", TFGCore.id("block/girder/beam/block_pole_top")).texture("2", poleTexLoc).texture("3", poleCtTexLoc)
                .texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/block_pole_middle", TFGCore.id("block/girder/beam/block_pole_middle")).texture("2", poleTexLoc).texture("3", poleCtTexLoc)
                .texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/block_pole_bottom", TFGCore.id("block/girder/beam/block_pole_bottom")).texture("2", poleTexLoc).texture("3", poleCtTexLoc)
                .texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/bracket_east", TFGCore.id("block/girder/beam/bracket_east")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/bracket_west", TFGCore.id("block/girder/beam/bracket_west")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/bracked_north", TFGCore.id("block/girder/beam/bracket_north")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/bracket_south", TFGCore.id("block/girder/beam/bracket_south")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/segment_middle", TFGCore.id("block/girder/beam/segment_middle")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/segment_top", TFGCore.id("block/girder/beam/segment_top")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/segment_bottom", TFGCore.id("block/girder/beam/segment_bottom")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/segment_middle_alt", TFGCore.id("block/girder/beam/segment_middle_alt")).texture("0", texLoc).texture("particle", texLoc);
    }

    private static void buildGirderTrussBlockStateEntry(DataGenContext<Block, ? extends Block> context, RegistrateBlockstateProvider provider, ResourceLocation texLoc) {
        var builder = provider.getMultipartBuilder(context.getEntry());

        builder.part()
                .modelFile(provider.models().withExistingParent(context.getName() + "/block_pole", TFGCore.id("block/girder/truss/block_pole")).texture("3", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.X, false).condition(GirderBlock.Z, false).end()

                .part().modelFile(provider.models().withExistingParent(context.getName() + "/block_x", TFGCore.id("block/girder/truss/block_x")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.X, true).end()

                .part().modelFile(provider.models().withExistingParent(context.getName() + "/block_z", TFGCore.id("block/girder/truss/block_z")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.Z, true).end()

                .part().modelFile(provider.models().withExistingParent(context.getName() + "/block_top", TFGCore.id("block/girder/truss/block_top")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.TOP, true).condition(GirderBlock.X, true).condition(GirderBlock.Z, false).end()

                .part()
                .modelFile(provider.models().withExistingParent(context.getName() + "/block_bottom", TFGCore.id("block/girder/truss/block_bottom")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.BOTTOM, true).condition(GirderBlock.X, true).condition(GirderBlock.Z, false).end()

                .part().modelFile(provider.models().withExistingParent(context.getName() + "/block_top", TFGCore.id("block/girder/truss/block_top")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.TOP, true).condition(GirderBlock.X, false).condition(GirderBlock.Z, true).end()

                .part()
                .modelFile(provider.models().withExistingParent(context.getName() + "/block_bottom", TFGCore.id("block/girder/truss/block_bottom")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.BOTTOM, true).condition(GirderBlock.X, false).condition(GirderBlock.Z, true).end()

                .part()
                .modelFile(provider.models().withExistingParent(context.getName() + "/block_cross", TFGCore.id("block/girder/truss/block_cross")).texture("0", texLoc).texture("particle", texLoc))
                .addModel()
                .condition(GirderBlock.X, true).condition(GirderBlock.Z, true).end();

        provider.models().withExistingParent(context.getName() + "/bracket_east", TFGCore.id("block/girder/truss/bracket_east")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/bracket_west", TFGCore.id("block/girder/truss/bracket_west")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/bracked_north", TFGCore.id("block/girder/truss/bracket_north")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/bracket_south", TFGCore.id("block/girder/truss/bracket_south")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/segment_middle", TFGCore.id("block/girder/truss/segment_middle")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/segment_top", TFGCore.id("block/girder/truss/segment_top")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/segment_bottom", TFGCore.id("block/girder/truss/segment_bottom")).texture("0", texLoc).texture("particle", texLoc);
        provider.models().withExistingParent(context.getName() + "/segment_middle_alt", TFGCore.id("block/girder/truss/segment_middle_alt")).texture("0", texLoc).texture("particle", texLoc);
    }

    public static boolean isAnyGirder(BlockState state) {
        return GIRDERS.contains(state.getBlock());
    }
}
