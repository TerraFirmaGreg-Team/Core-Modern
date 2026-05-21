package su.terrafirmagreg.core.common.block.girder;

import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.content.decoration.girder.GirderEncasedShaftBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

public class TFGGirderGenerator {
    /**
     * Generic blockstate generator for any girder type
     * Creates blockstate that directly references the existing model files
     */
    public static void blockState(DataGenContext<Block, ? extends Block> c, RegistrateBlockstateProvider p) {
        String blockName = c.getName();
        String modId = c.getId().getNamespace();

        p.getMultipartBuilder(c.get())
                .part().modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block_pole"))).addModel()
                .condition(GirderBlock.X, false).condition(GirderBlock.Z, false).end()

                .part().modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block_x"))).addModel()
                .condition(GirderBlock.X, true).end()

                .part().modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block_z"))).addModel()
                .condition(GirderBlock.Z, true).end()

                .part().modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block_top"))).addModel()
                .condition(GirderBlock.TOP, true).condition(GirderBlock.X, true).condition(GirderBlock.Z, false).end()

                .part().modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block_bottom"))).addModel()
                .condition(GirderBlock.BOTTOM, true).condition(GirderBlock.X, true).condition(GirderBlock.Z, false).end()

                .part().modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block_top"))).addModel()
                .condition(GirderBlock.TOP, true).condition(GirderBlock.X, false).condition(GirderBlock.Z, true).end()

                .part().modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block_bottom"))).addModel()
                .condition(GirderBlock.BOTTOM, true).condition(GirderBlock.X, false).condition(GirderBlock.Z, true).end()

                .part().modelFile(p.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(modId, "block/" + blockName + "/block_cross"))).addModel()
                .condition(GirderBlock.X, true).condition(GirderBlock.Z, true).end();
    }

    public static void blockStateWithShaft(DataGenContext<Block, ? extends Block> c, RegistrateBlockstateProvider p) {
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

    /**
     * Generic item model generator for girder items
     * Creates simple item model referencing the existing item.json model
     */
    public static void itemModel(DataGenContext<Item, BlockItem> c, RegistrateItemModelProvider p) {
        String blockName = c.getName();
        String modId = c.getId().getNamespace();

        p.withExistingParent(blockName, modId + ":block/" + blockName + "/item");
    }
}
