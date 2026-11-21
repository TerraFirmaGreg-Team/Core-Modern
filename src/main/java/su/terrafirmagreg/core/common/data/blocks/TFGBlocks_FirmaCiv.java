package su.terrafirmagreg.core.common.data.blocks;

import java.util.*;

import com.alekiponi.alekiroofs.SquaredAngleBlock;
import com.alekiponi.firmaciv.common.block.CanoeComponentBlock;
import com.alekiponi.firmaciv.common.block.FirmacivAngledWoodenBoatFrameBlock;
import com.alekiponi.firmaciv.common.block.FirmacivFlatWoodenBoatFrameBlock;
import com.alekiponi.firmaciv.util.FirmacivTags;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGWatercraftMaterial;

//Taken and Modified from FirmaCivPlus, rewritten to use Registrate -Nebby
public class TFGBlocks_FirmaCiv {

    public static final Map<TFGWatercraftMaterial, BlockEntry<CanoeComponentBlock>> CANOE_COMPONENT_BLOCKS = new HashMap<>();

    private static final Map<TFGWatercraftMaterial, BlockEntry<FirmacivAngledWoodenBoatFrameBlock>> _WOODEN_BOAT_FRAME_ANGLED = new HashMap<>();

    public static Map<TFGWatercraftMaterial, BlockEntry<FirmacivAngledWoodenBoatFrameBlock>> getWoodenBoatFrameAngledBlocks() {
        return Collections.unmodifiableMap(_WOODEN_BOAT_FRAME_ANGLED);
    }

    private static final Map<TFGWatercraftMaterial, BlockEntry<FirmacivFlatWoodenBoatFrameBlock>> _WOODEN_BOAT_FRAME_FLAT = new HashMap<>();

    public static Map<TFGWatercraftMaterial, BlockEntry<FirmacivFlatWoodenBoatFrameBlock>> getWoodenBoatFrameFlatBlocks() {
        return Collections.unmodifiableMap(_WOODEN_BOAT_FRAME_FLAT);
    }

    public static final Map<TFGWatercraftMaterial, BlockEntry<SquaredAngleBlock>> WOOD_ROOFING = new HashMap<>();

    static void init() {
        for (var woodEntry : TFGWatercraftMaterial.values()) {
            putWoodRoofing(woodEntry);

            if (woodEntry.woodKind == TFGWatercraftMaterial.WoodKind.SOFT) {
                putCanoeComponentBlock(woodEntry);
            } else if (woodEntry.woodKind == TFGWatercraftMaterial.WoodKind.HARD) {
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
            properties = BlockBehaviour.Properties.copy(stairs)
                    .mapColor(watercraftMaterial.getWood().woodColor()).noOcclusion();
            return new SquaredAngleBlock(blockState, properties);
        })
                .blockstate(woodRoofingBlockState(watercraftMaterial))
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("alekiroofs", "roofing")))
                .item(BlockItem::new)
                .tag()
                .build()
                .register();

        WOOD_ROOFING.put(watercraftMaterial, entry);
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
        String name = "wood/canoe_component_block/" + watercraftMaterial.getSerializedName();
        var entry = TFGCore.REGISTRATE.block(name, (properties) -> {
            var wood = watercraftMaterial.getWood();
            var strippedLog = wood.getBlock(Wood.BlockType.STRIPPED_LOG).get();

            properties = BlockBehaviour.Properties.copy(strippedLog).mapColor(wood.woodColor()).noOcclusion();
            return new CanoeComponentBlock(properties, watercraftMaterial);
        })
                .blockstate(canoeComponentBlockState(watercraftMaterial))
                .tag(FirmacivTags.Blocks.CANOE_COMPONENT_BLOCKS)
                .register();

        CANOE_COMPONENT_BLOCKS.put(watercraftMaterial, entry);
    }

    private static final Map<String, List<Integer>> CANOE_SECTION_TO_STEP_RANGE = Map.of(
            "all", List.of(0, 1, 2, 3, 4, 5, 6, 7),
            "end", List.of(8, 9, 10, 11, 12),
            "middle", List.of(8, 9, 10, 11, 12));
    private static final Direction[] VALID_CANOE_DIRECTIONS = new Direction[] {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST
    };

    static int directionToYRot(final Direction direction) {
        return switch (direction) {
            case NORTH -> 0;
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default ->
                throw new IllegalArgumentException("Invalid direction provided, Direction was \"" + direction + "\" when it should conform to NORTH, EAST, SOUTH or WEST");
        };
    }

    private static NonNullBiConsumer<DataGenContext<Block, CanoeComponentBlock>, RegistrateBlockstateProvider> canoeComponentBlockState(TFGWatercraftMaterial watercraftMaterial) {
        return (dataGenContext, blockStateProvider) -> {
            if (watercraftMaterial.woodKind != TFGWatercraftMaterial.WoodKind.SOFT) {
                return;
            }

            RegistryWood wood = watercraftMaterial.getWood();
            var variantBlockStateBuilder = blockStateProvider.getVariantBuilder(dataGenContext.getEntry());

            CANOE_SECTION_TO_STEP_RANGE.forEach((section, canoeCarvedSteps) -> {
                for (Direction validDirection : VALID_CANOE_DIRECTIONS) {
                    for (int step : canoeCarvedSteps) {
                        //Canoe State Selectors
                        var whenFacingAndStep = variantBlockStateBuilder.partialState()
                                .with(CanoeComponentBlock.FACING, validDirection)
                                .with(CanoeComponentBlock.CANOE_CARVED, step + 1);

                        VariantBlockStateBuilder.PartialBlockstate postSectionSelection = null;
                        switch (section) {
                            case "end" -> postSectionSelection = whenFacingAndStep.with(CanoeComponentBlock.END, true);
                            case "middle" -> postSectionSelection = whenFacingAndStep.with(CanoeComponentBlock.END, false);
                            case "all" -> postSectionSelection = whenFacingAndStep;
                        }

                        var strippedLogSideTexture = blockStateProvider.blockTexture(Block.byItem(watercraftMaterial.getStrippedLog()));
                        var strippedLogTopTexture = strippedLogSideTexture;

                        String modelName = String.format(Locale.ROOT, "block/wood/canoe_component_block/%s/%s/%s", wood.getSerializedName(), section, step);
                        String parentName = String.format(Locale.ROOT, "block/canoe_component_block/template/%s/%s",
                                section, step);
                        var modelFile = blockStateProvider.models()
                                .withExistingParent(modelName, ResourceLocation.fromNamespaceAndPath("firmaciv", parentName))
                                .texture("0", strippedLogSideTexture)
                                .texture("1", strippedLogTopTexture)
                                .texture("particle", strippedLogSideTexture);

                        //set model for state above, rotate on Y if needed
                        var configuredModel = postSectionSelection.modelForState()
                                .modelFile(modelFile);

                        if (validDirection != Direction.NORTH)
                            configuredModel.rotationY(directionToYRot(validDirection));

                        configuredModel.addModel(); //may be wrong?
                    }
                }
            });
        };
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

    public static void registerFrames() {
        for (var material : TFGWatercraftMaterial.values()) {
            if (material.woodKind == TFGWatercraftMaterial.WoodKind.SOFT) {
                CanoeComponentBlock.registerCanoeComponent((RotatedPillarBlock) material.getWood().getBlock(Wood.BlockType.STRIPPED_LOG).get(), CANOE_COMPONENT_BLOCKS.get(material).get());
            }
        }
    }
}
