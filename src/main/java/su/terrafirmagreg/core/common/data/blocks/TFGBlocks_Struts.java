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

@SuppressWarnings({ "unused" })
public class TFGBlocks_Struts {
    public static final ObjectOpenHashSet<BlockEntry<? extends Block>> STRUTS_AND_CABLES = new ObjectOpenHashSet<>();

    public static void init() {
        STRUTS_AND_CABLES.add(basicStrut("steel", TFGCore.id("block/strut/basic/steel"), TFGCore.id("block/strut/basic/steel_attachment"), TFGCore.id("block/strut/basic/steel_particle")));
        STRUTS_AND_CABLES.add(basicStrut("brass", TFGCore.id("block/strut/basic/brass"), TFGCore.id("block/strut/basic/brass"), TFGCore.id("block/strut/basic/brass")));
    }

    public static BlockEntry<TFGStrutBlock> basicStrut(String id, ResourceLocation texLoc, ResourceLocation texLocAttachment, ResourceLocation texLocParticle) {
        return TFGCore.REGISTRATE.block("strut/basic/" + id, p -> new TFGStrutBlock(p, new StrutModelType(
                TFGCore.id("block/strut/basic/" + id),
                texLocParticle)))
                .properties(p -> p
                        .mapColor(MapColor.METAL)
                        .strength(3f, 6f)
                        .noOcclusion()
                        .sound(SoundType.NETHERITE_BLOCK))
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent("strut/basic/" + id + "_attachment", TFGCore.id("block/strut/basic_attachment"))
                            .texture("0", texLocAttachment)
                            .texture("particle", texLocParticle);

                    ModelFile extraModel = prov.models().withExistingParent("strut/basic/" + id, TFGCore.id("block/strut/basic"))
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
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), TFGCore.id("block/strut/basic_item"))
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
