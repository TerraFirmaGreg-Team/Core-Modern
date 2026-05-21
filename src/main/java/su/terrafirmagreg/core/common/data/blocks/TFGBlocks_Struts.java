package su.terrafirmagreg.core.common.data.blocks;

import com.cake.struts.content.StrutModelType;
import com.cake.struts.content.block.StrutBlockItem;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.TFGStrutBlock;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

@SuppressWarnings({ "unused" })
public class TFGBlocks_Struts {
    public static final ObjectOpenHashSet<BlockEntry<? extends Block>> STRUTS_AND_CABLES = new ObjectOpenHashSet<>();
    public static final ObjectOpenHashSet<BlockEntry<? extends Block>> GIRDER = new ObjectOpenHashSet<>();
    public static final ObjectOpenHashSet<BlockEntry<? extends Block>> GIRDER_ENCASED_SHAFT = new ObjectOpenHashSet<>();

    public static void init() {
        // beam Struts
        // STRUTS_AND_CABLES.add(beamStrut("steel", TFGCore.id("block/girder/beam/steel"), TFGCore.id("block/girder/beam/steel"), TFGCore.id("block/girder/beam/steel")));
        // STRUTS_AND_CABLES.add(beamStrut("brass", TFGCore.id("block/girder/beam/brass"), TFGCore.id("block/girder/beam/brass"), TFGCore.id("block/girder/beam/brass")));
        // STRUTS_AND_CABLES.add(beamStrut("copper", TFGCore.id("block/girder/beam/copper"), TFGCore.id("block/girder/beam/copper"), TFGCore.id("block/girder/beam/copper")));
        STRUTS_AND_CABLES.add(beamStrut("tin_alloy", TFGCore.id("block/girder/beam/tin_alloy"), TFGCore.id("block/girder/beam/tin_alloy"), TFGCore.id("block/girder/beam/tin_alloy")));
        // STRUTS_AND_CABLES.add(beamStrut("zinc", TFGCore.id("block/girder/beam/zinc"), TFGCore.id("block/girder/beam/zinc"), TFGCore.id("block/girder/beam/zinc")));
        // STRUTS_AND_CABLES.add(beamStrut("iron", TFGCore.id("block/girder/beam/iron"), TFGCore.id("block/girder/beam/iron"), TFGCore.id("block/girder/beam/iron")));

        // Truss Struts
        // STRUTS_AND_CABLES.add(trussStrut("steel", TFGCore.id("block/girder/truss/steel"), TFGCore.id("block/girder/truss/steel"), TFGCore.id("block/girder/truss/steel")));
        // STRUTS_AND_CABLES.add(trussStrut("brass", TFGCore.id("block/girder/truss/brass"), TFGCore.id("block/girder/truss/brass"), TFGCore.id("block/girder/truss/brass")));
        // STRUTS_AND_CABLES.add(trussStrut("copper", TFGCore.id("block/girder/truss/copper"), TFGCore.id("block/girder/truss/copper"), TFGCore.id("block/girder/truss/copper")));
        // STRUTS_AND_CABLES.add(trussStrut("tin_alloy", TFGCore.id("block/girder/truss/tin_alloy"), TFGCore.id("block/girder/truss/tin_alloy"), TFGCore.id("block/girder/truss/tin_alloy")));
        // STRUTS_AND_CABLES.add(trussStrut("zinc", TFGCore.id("block/girder/truss/zinc"), TFGCore.id("block/girder/truss/zinc"), TFGCore.id("block/girder/truss/zinc")));
        // STRUTS_AND_CABLES.add(trussStrut("iron", TFGCore.id("block/girder/truss/iron"), TFGCore.id("block/girder/truss/iron"), TFGCore.id("block/girder/truss/iron")));
    }

    public static boolean isAnyGirder(BlockState state) {
        return GIRDER.contains(state.getBlock());
    }

    public static boolean isAnyGirderEncasedShaft(BlockState state) {
        return GIRDER_ENCASED_SHAFT.contains(state.getBlock());
    }

    public static BlockEntry<TFGStrutBlock> beamStrut(String id, ResourceLocation texLoc, ResourceLocation texLocAttachment, ResourceLocation texLocParticle) {
        return TFGCore.REGISTRATE.block("strut/beam/" + id, p -> new TFGStrutBlock(p, new StrutModelType(
                TFGCore.id("block/strut/beam/" + id),
                texLocParticle)))
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .strength(3f, 6f)
                        .noOcclusion()
                        .sound(SoundType.NETHERITE_BLOCK))
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent("strut/beam/" + id + "_attachment", TFGCore.id("block/strut/beam_attachment"))
                            .texture("0", texLocAttachment)
                            .texture("particle", texLocParticle);

                    ModelFile extraModel = prov.models().withExistingParent("strut/beam/" + id, TFGCore.id("block/strut/beam"))
                            .texture("0", texLoc)
                            .texture("particle", texLocParticle);

                    var builder = prov.getVariantBuilder(ctx.getEntry());

                    buildGirderStrutBlockStateEntry(builder, model, false);
                    buildGirderStrutBlockStateEntry(builder, model, true);
                })
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFGBlockEntities.STRUT, block);
                })
                .item(StrutBlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/strut/item"))
                        .texture("0", texLoc)
                        .texture("1_0", texLocAttachment)
                        .texture("particle", texLocParticle))
                .build()
                .register();
    }

    public static BlockEntry<TFGStrutBlock> trussStrut(String id, ResourceLocation texLoc, ResourceLocation texLocAttachment, ResourceLocation texLocParticle) {
        return TFGCore.REGISTRATE.block("strut/truss/" + id, p -> new TFGStrutBlock(p, new StrutModelType(
                TFGCore.id("block/strut/truss/" + id),
                texLocParticle)))
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .strength(3f, 6f)
                        .noOcclusion()
                        .sound(SoundType.NETHERITE_BLOCK))
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent("strut/truss/" + id + "_attachment", TFGCore.id("block/strut/truss_attachment"))
                            .texture("0", texLocAttachment)
                            .texture("particle", texLocParticle);

                    ModelFile extraModel = prov.models().withExistingParent("strut/truss/" + id, TFGCore.id("block/strut/truss"))
                            .texture("0", texLoc)
                            .texture("particle", texLocParticle);

                    var builder = prov.getVariantBuilder(ctx.getEntry());

                    buildGirderStrutBlockStateEntry(builder, model, false);
                    buildGirderStrutBlockStateEntry(builder, model, true);
                })
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFGBlockEntities.STRUT, block);
                })
                .item(StrutBlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/strut/item"))
                        .texture("0", texLoc)
                        .texture("1_0", texLocAttachment)
                        .texture("particle", texLocParticle))
                .build()
                .register();
    }

    private static void buildGirderStrutBlockStateEntry(VariantBlockStateBuilder builder, ModelFile model, boolean waterlogged) {
        builder.partialState().with(BlockStateProperties.FACING, Direction.DOWN).with(BlockStateProperties.WATERLOGGED, waterlogged).modelForState().rotationX(180).rotationY(0)
                .modelFile(model).addModel()
                .partialState().with(BlockStateProperties.FACING, Direction.EAST).with(BlockStateProperties.WATERLOGGED, waterlogged).modelForState().rotationX(90).rotationY(90)
                .modelFile(model).addModel()
                .partialState().with(BlockStateProperties.FACING, Direction.NORTH).with(BlockStateProperties.WATERLOGGED, waterlogged).modelForState().rotationX(90).rotationY(0)
                .modelFile(model).addModel()
                .partialState().with(BlockStateProperties.FACING, Direction.SOUTH).with(BlockStateProperties.WATERLOGGED, waterlogged).modelForState().rotationX(90).rotationY(180)
                .modelFile(model).addModel()
                .partialState().with(BlockStateProperties.FACING, Direction.UP).with(BlockStateProperties.WATERLOGGED, waterlogged).modelForState().rotationX(0).rotationY(0)
                .modelFile(model).addModel()
                .partialState().with(BlockStateProperties.FACING, Direction.WEST).with(BlockStateProperties.WATERLOGGED, waterlogged).modelForState().rotationX(90).rotationY(270)
                .modelFile(model).addModel();
    }
}
