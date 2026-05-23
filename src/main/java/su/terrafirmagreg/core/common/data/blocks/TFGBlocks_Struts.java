package su.terrafirmagreg.core.common.data.blocks;

import com.cake.struts.content.StrutModelType;
import com.cake.struts.content.block.StrutBlockItem;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.TFGStrutBlock;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.TFGTags;

public class TFGBlocks_Struts {
    public static final ObjectOpenHashSet<BlockEntry<? extends Block>> STRUTS = new ObjectOpenHashSet<>();

    public static void init() {
        // Beam Struts
        STRUTS.add(strut("steel", "beam", TFGCore.id("block/girder/beam/steel")));
        STRUTS.add(strut("brass", "beam", TFGCore.id("block/girder/beam/brass")));
        STRUTS.add(strut("copper", "beam", TFGCore.id("block/girder/beam/copper")));
        STRUTS.add(strut("tin_alloy", "beam", TFGCore.id("block/girder/beam/tin_alloy")));
        STRUTS.add(strut("zinc", "beam", TFGCore.id("block/girder/beam/zinc")));
        STRUTS.add(strut("wrought_iron", "beam", TFGCore.id("block/girder/beam/wrought_iron")));

        // Truss Struts
        STRUTS.add(strut("steel", "truss", TFGCore.id("block/girder/truss/steel")));
        STRUTS.add(strut("brass", "truss", TFGCore.id("block/girder/truss/brass")));
        STRUTS.add(strut("copper", "truss", TFGCore.id("block/girder/truss/copper")));
        STRUTS.add(strut("tin_alloy", "truss", TFGCore.id("block/girder/truss/tin_alloy")));
        STRUTS.add(strut("zinc", "truss", TFGCore.id("block/girder/truss/zinc")));
        STRUTS.add(strut("wrought_iron", "truss", TFGCore.id("block/girder/truss/wrought_iron")));
    }

    public static BlockEntry<TFGStrutBlock> strut(String id, String type, ResourceLocation texLoc) {
        String fullId = "strut/" + type + "/" + id;

        return TFGCore.REGISTRATE.block(fullId, p -> new TFGStrutBlock(p, new StrutModelType(
                TFGCore.id("block/" + fullId),
                texLoc)))
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .strength(3f, 6f)
                        .noOcclusion()
                        .sound(SoundType.NETHERITE_BLOCK))
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent(fullId + "_attachment", TFGCore.id("block/strut/" + type + "_attachment"))
                            .texture("0", texLoc)
                            .texture("particle", texLoc);

                    ModelFile extraModel = prov.models().withExistingParent(fullId, TFGCore.id("block/strut/" + type))
                            .texture("0", texLoc)
                            .texture("particle", texLoc);

                    var builder = prov.getVariantBuilder(ctx.getEntry());

                    buildGirderStrutBlockStateEntry(builder, model, false);
                    buildGirderStrutBlockStateEntry(builder, model, true);
                })
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFGBlockEntities.STRUT, block);
                })
                .tag(TFGTags.Blocks.STRUT)
                .item(StrutBlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/strut/" + type + "_item"))
                        .texture("0", texLoc)
                        .texture("1_0", texLoc)
                        .texture("particle", texLoc))
                .tag(TFGTags.Items.STRUT)
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
