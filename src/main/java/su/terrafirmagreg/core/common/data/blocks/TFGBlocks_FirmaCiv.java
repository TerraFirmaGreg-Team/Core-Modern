package su.terrafirmagreg.core.common.data.blocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alekiponi.alekiroofs.SquaredAngleBlock;
import com.alekiponi.firmaciv.common.block.CanoeComponentBlock;
import com.alekiponi.firmaciv.common.block.FirmacivAngledWoodenBoatFrameBlock;
import com.alekiponi.firmaciv.common.block.FirmacivFlatWoodenBoatFrameBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGWatercraftMaterial;

//Taken and Modified from FirmaCivPlus, rewritten to use Registrate -Nebby
public class TFGBlocks_FirmaCiv {

    private static final Map<TFGWatercraftMaterial, BlockEntry<CanoeComponentBlock>> _CANOE_COMPONENT_BLOCKS = new HashMap<>();

    public static Map<TFGWatercraftMaterial, BlockEntry<CanoeComponentBlock>> getCanoeComponentBlocks() {
        return Collections.unmodifiableMap(_CANOE_COMPONENT_BLOCKS);
    }

    private static final Map<TFGWatercraftMaterial, BlockEntry<FirmacivAngledWoodenBoatFrameBlock>> _WOODEN_BOAT_FRAME_ANGLED = new HashMap<>();

    public static Map<TFGWatercraftMaterial, BlockEntry<FirmacivAngledWoodenBoatFrameBlock>> getWoodenBoatFrameAngledBlocks() {
        return Collections.unmodifiableMap(_WOODEN_BOAT_FRAME_ANGLED);
    }

    private static final Map<TFGWatercraftMaterial, BlockEntry<FirmacivFlatWoodenBoatFrameBlock>> _WOODEN_BOAT_FRAME_FLAT = new HashMap<>();

    public static Map<TFGWatercraftMaterial, BlockEntry<FirmacivFlatWoodenBoatFrameBlock>> getWoodenBoatFrameFlatBlocks() {
        return Collections.unmodifiableMap(_WOODEN_BOAT_FRAME_FLAT);
    }

    private static final Map<TFGWatercraftMaterial, BlockEntry<SquaredAngleBlock>> _WOOD_ROOFING = new HashMap<>();

    public static Map<TFGWatercraftMaterial, BlockEntry<SquaredAngleBlock>> getWoodRoofings() {
        return Collections.unmodifiableMap(_WOOD_ROOFING);
    }

    static void init() {
        for (var woodEntry : TFGWatercraftMaterial.values()) {
            putWoodRoofing(woodEntry);
            if (woodEntry.isSoftWood) {
                putCanoeComponentBlock(woodEntry);
            } else {
                putAngledBoatFrameBlock(woodEntry);
                putFlatBoatFrameBlock(woodEntry);
            }
        }
    }

    private static void putWoodRoofing(TFGWatercraftMaterial watercraftMaterial) {
        String name = "wood/" + watercraftMaterial.getSerializedName() + "_roofing";
        var entry = TFGCore.REGISTRATE.block(name, (properties) -> {
            var stairs = watercraftMaterial.getWoodBlockType(Wood.BlockType.STAIRS).get();
            var blockState = stairs.defaultBlockState();
            var blockProperties = BlockBehaviour.Properties.copy(stairs)
                    .mapColor(watercraftMaterial.getWood().woodColor()).noOcclusion();
            return new SquaredAngleBlock(blockState, blockProperties);
        })
                .blockstate(woodRoofingBlockState(watercraftMaterial))
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("alekiroofs", "roofing")))
                .item(BlockItem::new)
                .tag()
                .build()
                .register();

        _WOOD_ROOFING.put(watercraftMaterial, entry);
    }

    private static NonNullBiConsumer<DataGenContext<Block, SquaredAngleBlock>, RegistrateBlockstateProvider> woodRoofingBlockState(TFGWatercraftMaterial watercraftMaterial) {
        return (dataGenContext, blockStateProvider) -> {
            var registryWood = watercraftMaterial.getWood();
            ResourceLocation planksBlockTexture = blockStateProvider.blockTexture(registryWood.getBlock(Wood.BlockType.PLANKS).get());
            var variantBlockStateBuilder = blockStateProvider.getVariantBuilder(dataGenContext.getEntry());

            SquaredAngleBlock.FACING.getPossibleValues().forEach(facing -> {
                SquaredAngleBlock.SHAPE.getPossibleValues().forEach(shape -> {
                    int yRot = (int) ((switch (shape) {
                        case INNER_LEFT, OUTER_LEFT -> facing.toYRot();
                        case INNER_RIGHT, OUTER_RIGHT, STRAIGHT -> facing.toYRot() + 90;
                    }) % 360);

                    var whenFacingAndShape = variantBlockStateBuilder.partialState()
                            .with(SquaredAngleBlock.FACING, facing)
                            .with(SquaredAngleBlock.SHAPE, shape);

                    String parentModelShape = null;
                    switch (shape) {
                        case INNER_RIGHT, INNER_LEFT -> parentModelShape = "_inner";
                        case OUTER_LEFT, OUTER_RIGHT -> parentModelShape = "_outer";
                        case STRAIGHT -> parentModelShape = "";
                    }

                    String modelName = "block/wood/" + registryWood.getSerializedName() + "_roofing" + parentModelShape;
                    String parentName = "block/roofing" + parentModelShape;

                    var modelFile = blockStateProvider.models()
                            .withExistingParent(modelName, ResourceLocation.fromNamespaceAndPath("alekiroofs", parentName))
                            .texture("bottom", planksBlockTexture)
                            .texture("top", planksBlockTexture)
                            .texture("side", planksBlockTexture);

                    var configuredModel = whenFacingAndShape.modelForState()
                            .modelFile(modelFile)
                            .rotationY(yRot)
                            .uvLock(yRot != 0);

                    configuredModel.addModel();
                });
            });
            blockStateProvider.itemModels().withExistingParent("item/wood/" + registryWood.getSerializedName() + "_roofing",
                    blockStateProvider.modLoc("block/wood/" + registryWood.getSerializedName() + "_roofing"));
        };
    }

    private static void putCanoeComponentBlock(TFGWatercraftMaterial watercraftMaterial) {
        //        String name = "wood/canoe_component_block/" + watercraftMaterial.getSerializedName();
        //        Supplier<CanoeComponentBlock> supplier = () ->
        //        {
        //            var wood = watercraftMaterial.getWood();
        //            var strippedLog = wood.getBlock(Wood.BlockType.STRIPPED_LOG).get();
        //
        //            BlockBehaviour.Properties properties = BlockBehaviour.Properties.copy(strippedLog).mapColor(wood.woodColor())
        //                    .noOcclusion();
        //            return new CanoeComponentBlock(properties, watercraftMaterial);
        //        };
        //
        //        var entry = BLOCKS.register(name, supplier);
        //        _CANOE_COMPONENT_BLOCKS.put(watercraftMaterial, entry);
    }

    private static void putAngledBoatFrameBlock(TFGWatercraftMaterial watercraftMaterial) {
        //        String name = "wood/watercraft_frame/angled/" + watercraftMaterial.getSerializedName();
        //        Supplier<FirmacivAngledWoodenBoatFrameBlock> supplier = () ->
        //                new FirmacivAngledWoodenBoatFrameBlock(watercraftMaterial, BlockBehaviour.Properties.copy(FirmacivBlocks.BOAT_FRAME_ANGLED.get()));
        //        var entry = BLOCKS.register(name, supplier);
        //        _WOODEN_BOAT_FRAME_ANGLED.put(watercraftMaterial, entry);
    }

    private static void putFlatBoatFrameBlock(TFGWatercraftMaterial watercraftMaterial) {
        //        String name = "wood/watercraft_frame/flat/" + watercraftMaterial.getSerializedName();
        //        Supplier<FirmacivFlatWoodenBoatFrameBlock> supplier = () ->
        //                new FirmacivFlatWoodenBoatFrameBlock(watercraftMaterial, BlockBehaviour.Properties.copy(FirmacivBlocks.BOAT_FRAME_FLAT.get()));
        //        var entry = BLOCKS.register(name, supplier);
        //        _WOODEN_BOAT_FRAME_FLAT.put(watercraftMaterial, entry);
    }
}
