package su.terrafirmagreg.core.common.data.blocks;

import java.util.*;
import java.util.stream.IntStream;

import com.alekiponi.alekiroofs.SquaredAngleBlock;
import com.alekiponi.alekiships.AlekiShips;
import com.alekiponi.alekiships.common.block.AngledWoodenBoatFrameBlock;
import com.alekiponi.alekiships.common.block.FlatWoodenBoatFrameBlock;
import com.alekiponi.alekiships.util.AlekiShipsTags;
import com.alekiponi.firmaciv.Firmaciv;
import com.alekiponi.firmaciv.common.block.CanoeComponentBlock;
import com.alekiponi.firmaciv.common.block.FirmacivAngledWoodenBoatFrameBlock;
import com.alekiponi.firmaciv.common.block.FirmacivBlocks;
import com.alekiponi.firmaciv.common.block.FirmacivFlatWoodenBoatFrameBlock;
import com.alekiponi.firmaciv.common.item.FirmacivItems;
import com.alekiponi.firmaciv.util.FirmacivTags;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGWatercraftMaterial;

//Taken and Modified from FirmaCivPlus, rewritten to use Registrate
//All of FirmaCiv's OG datagen is done with a Python based framework, so a lot of this code was rewriting that datagen to work with Java datagen. Some things might be a bit silly because of that
//-Nebby
public class TFGBlocks_FirmaCiv {
    public static final Map<TFGWatercraftMaterial, BlockEntry<CanoeComponentBlock>> CANOE_COMPONENT_BLOCKS = new HashMap<>();

    public static final Map<TFGWatercraftMaterial, BlockEntry<FirmacivAngledWoodenBoatFrameBlock>> WOODEN_BOAT_FRAME_ANGLED = new HashMap<>();

    public static final Map<TFGWatercraftMaterial, BlockEntry<FirmacivFlatWoodenBoatFrameBlock>> WOODEN_BOAT_FRAME_FLAT = new HashMap<>();

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
            var stairs = watercraftMaterial.getWood().getBlock(Wood.BlockType.STAIRS).get();
            var blockState = stairs.defaultBlockState();
            properties = BlockBehaviour.Properties.copy(stairs)
                    .mapColor(watercraftMaterial.getWood().woodColor()).noOcclusion();
            return new SquaredAngleBlock(blockState, properties);
        })
                .blockstate((dataGenContext, blockStateProvider) -> {
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
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("alekiroofs", "roofing")))
                .loot(RegistrateBlockLootTables::dropSelf)
                .item(BlockItem::new)
                .tag()
                .build()
                .register();

        WOOD_ROOFING.put(watercraftMaterial, entry);
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
                .loot((registrateBlockLootTables, canoeComponentBlock) -> registrateBlockLootTables.add(canoeComponentBlock,
                        registrateBlockLootTables.createSingleItemTable(watercraftMaterial.getRailing())))
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
                        var strippedLogTopTexture = strippedLogSideTexture.withSuffix("_top");

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
        String name = "wood/watercraft_frame/angled/" + watercraftMaterial.getSerializedName();
        var entry = TFGCore.REGISTRATE.block(name, (properties) -> {
            //We can't copy directly from FirmaCiv's, so we'll just copy from planks, its what the og mod does anyways.
            var blockProps = BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion();
            return new FirmacivAngledWoodenBoatFrameBlock(watercraftMaterial, blockProps);
        })
                .blockstate(angledBoatFrameBlockState(watercraftMaterial))
                .tag(AlekiShipsTags.Blocks.WOODEN_WATERCRAFT_FRAMES)
                .loot((registrateBlockLootTables, firmacivAngledWoodenBoatFrameBlock) -> registrateBlockLootTables.add(firmacivAngledWoodenBoatFrameBlock,
                        createBoatFrameAngledTable(registrateBlockLootTables, firmacivAngledWoodenBoatFrameBlock, watercraftMaterial)))
                .register();

        WOODEN_BOAT_FRAME_ANGLED.put(watercraftMaterial, entry);
    }

    private static LootTable.Builder createBoatFrameAngledTable(RegistrateBlockLootTables registrateBlockLootTables, FirmacivAngledWoodenBoatFrameBlock firmacivAngledWoodenBoatFrameBlock,
            TFGWatercraftMaterial watercraftMaterial) {
        var lootTable = LootTable.lootTable();

        //base frame drop
        lootTable
                .withPool(registrateBlockLootTables.applyExplosionCondition(firmacivAngledWoodenBoatFrameBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1F)))
                        .add(LootItem.lootTableItem(FirmacivBlocks.BOAT_FRAME_ANGLED.get())));

        //handle plank drop
        ArrayList<LootPoolEntryContainer.Builder<?>> alternatives = new ArrayList<>();
        for (int frameProcessed : FirmacivAngledWoodenBoatFrameBlock.FRAME_PROCESSED.getPossibleValues()) {
            int count = Math.min(frameProcessed + 1, 4);
            alternatives.add(LootItem.lootTableItem(watercraftMaterial.getDeckItem())
                    .when(LootItemBlockStatePropertyCondition
                            .hasBlockStateProperties(firmacivAngledWoodenBoatFrameBlock)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(FirmacivAngledWoodenBoatFrameBlock.FRAME_PROCESSED, frameProcessed)))
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count))));
        }
        lootTable.withPool(registrateBlockLootTables.applyExplosionCondition(firmacivAngledWoodenBoatFrameBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1f))
                .add(AlternativesEntry.alternatives(alternatives, func -> func))));

        alternatives.clear();
        for (int frameProcessed : FirmacivAngledWoodenBoatFrameBlock.FRAME_PROCESSED.getPossibleValues()) {
            if (frameProcessed + 1 <= 4) {
                continue;
            }

            int count = frameProcessed % 4;
            alternatives.add(LootItem.lootTableItem(FirmacivItems.COPPER_BOLT.get())
                    .when(LootItemBlockStatePropertyCondition
                            .hasBlockStateProperties(firmacivAngledWoodenBoatFrameBlock)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(FirmacivAngledWoodenBoatFrameBlock.FRAME_PROCESSED, frameProcessed)))
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count + 1))));
        }
        lootTable.withPool(registrateBlockLootTables.applyExplosionCondition(firmacivAngledWoodenBoatFrameBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1f))
                .add(AlternativesEntry.alternatives(alternatives, func -> func))));

        return lootTable;
    }

    private static final String[] BOAT_FRAME_PROGRESS_STRINGS = { "first", "second", "third", "fourth" };

    static int angledBoatFrameYRot(final StairsShape shape, final Direction facing) {
        return switch (shape) {
            case INNER_RIGHT, STRAIGHT -> ((int) facing.toYRot());
            case OUTER_LEFT -> (int) facing.toYRot() + 90;
            case OUTER_RIGHT -> (int) facing.toYRot() + 180;
            case INNER_LEFT -> (int) facing.toYRot() + 270;
        } % 360;
    }

    private static NonNullBiConsumer<DataGenContext<Block, FirmacivAngledWoodenBoatFrameBlock>, RegistrateBlockstateProvider> angledBoatFrameBlockState(TFGWatercraftMaterial watercraftMaterial) {
        return (dataGenContext, blockStateProvider) -> {
            if (watercraftMaterial.woodKind != TFGWatercraftMaterial.WoodKind.HARD)
                return;

            ModelFile.ExistingModelFile straight = blockStateProvider.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(Firmaciv.MOD_ID, "block/watercraft_frame/angled/straight"));
            ModelFile.ExistingModelFile inner = blockStateProvider.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(Firmaciv.MOD_ID, "block/watercraft_frame/angled/inner"));
            ModelFile.ExistingModelFile outer = blockStateProvider.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(Firmaciv.MOD_ID, "block/watercraft_frame/angled/outer"));

            var plankTexture = blockStateProvider.blockTexture(watercraftMaterial.getDeckBlock().getBlock());

            AngledWoodenBoatFrameBlock.FACING.getPossibleValues().forEach(facing -> {
                AngledWoodenBoatFrameBlock.SHAPE.getPossibleValues().forEach(shape -> {
                    final var multipartBuilder = blockStateProvider.getMultipartBuilder(dataGenContext.getEntry());

                    final int yRot = angledBoatFrameYRot(shape, facing);

                    multipartBuilder.part().modelFile(
                            shape == StairsShape.STRAIGHT ? straight : shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT ? inner : outer)
                            .rotationY(yRot).addModel()
                            .condition(AngledWoodenBoatFrameBlock.FACING, facing)
                            .condition(AngledWoodenBoatFrameBlock.SHAPE, shape);

                    IntStream.range(0, 8).forEach(progress -> {
                        final String modelShape = shape == StairsShape.STRAIGHT ? "straight" : shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT ? "inner" : "outer";
                        if (progress < 4) {
                            final var plankModel = blockStateProvider.models().withExistingParent(
                                    String.format(Locale.ROOT, "block/wood/watercraft_frame/angled/%s/%s/%s",
                                            watercraftMaterial.getSerializedName(), modelShape, BOAT_FRAME_PROGRESS_STRINGS[progress]),
                                    ResourceLocation.fromNamespaceAndPath(AlekiShips.MOD_ID,
                                            String.format(Locale.ROOT, "block/watercraft_frame/angled/template/%s/%s",
                                                    modelShape, BOAT_FRAME_PROGRESS_STRINGS[progress])))
                                    .texture("plank", plankTexture);

                            multipartBuilder.part().modelFile(plankModel).rotationY(yRot).uvLock(yRot != 0).addModel()
                                    .condition(AngledWoodenBoatFrameBlock.FACING, facing)
                                    .condition(AngledWoodenBoatFrameBlock.SHAPE, shape)
                                    .condition(AngledWoodenBoatFrameBlock.FRAME_PROCESSED,
                                            IntStream.range(progress, 8).boxed().toArray(Integer[]::new));
                        } else {
                            final var plankModel = blockStateProvider.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(Firmaciv.MOD_ID,
                                    String.format(Locale.ROOT, "block/watercraft_frame/angled/bolt/%s/%s", modelShape, BOAT_FRAME_PROGRESS_STRINGS[progress % 4])));

                            multipartBuilder.part().modelFile(plankModel).rotationY(yRot)/*.uvLock(yRot != 0)*/.addModel()
                                    .condition(AngledWoodenBoatFrameBlock.FACING, facing)
                                    .condition(AngledWoodenBoatFrameBlock.SHAPE, shape)
                                    .condition(AngledWoodenBoatFrameBlock.FRAME_PROCESSED,
                                            IntStream.range(progress, 8).boxed().toArray(Integer[]::new));
                        }
                    });
                });
            });
        };
    }

    private static void putFlatBoatFrameBlock(TFGWatercraftMaterial watercraftMaterial) {
        String name = "wood/watercraft_frame/flat/" + watercraftMaterial.getSerializedName();
        var entry = TFGCore.REGISTRATE.block(name, (properties) -> {
            var blockProps = BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion();
            return new FirmacivFlatWoodenBoatFrameBlock(watercraftMaterial, blockProps);
        })
                .blockstate(flatBoatFrameBlockState(watercraftMaterial))
                .tag(AlekiShipsTags.Blocks.WOODEN_WATERCRAFT_FRAMES)
                .loot(((registrateBlockLootTables, firmacivFlatWoodenBoatFrameBlock) -> registrateBlockLootTables.add(firmacivFlatWoodenBoatFrameBlock,
                        createBoatFrameFlatTable(registrateBlockLootTables, firmacivFlatWoodenBoatFrameBlock, watercraftMaterial))))
                .register();
        WOODEN_BOAT_FRAME_FLAT.put(watercraftMaterial, entry);
    }

    private static LootTable.Builder createBoatFrameFlatTable(RegistrateBlockLootTables registrateBlockLootTables, FirmacivFlatWoodenBoatFrameBlock firmacivFlatWoodenBoatFrameBlock,
            TFGWatercraftMaterial watercraftMaterial) {
        var lootTable = LootTable.lootTable();

        //base frame drop
        lootTable
                .withPool(registrateBlockLootTables.applyExplosionCondition(firmacivFlatWoodenBoatFrameBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1F)))
                        .add(LootItem.lootTableItem(FirmacivBlocks.BOAT_FRAME_FLAT.get())));

        //handle plank drop
        ArrayList<LootPoolEntryContainer.Builder<?>> alternatives = new ArrayList<>();
        for (int frameProcessed : FirmacivFlatWoodenBoatFrameBlock.FRAME_PROCESSED.getPossibleValues()) {
            int count = Math.min(frameProcessed + 1, 4);
            alternatives.add(LootItem.lootTableItem(watercraftMaterial.getDeckItem())
                    .when(LootItemBlockStatePropertyCondition
                            .hasBlockStateProperties(firmacivFlatWoodenBoatFrameBlock)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(FirmacivFlatWoodenBoatFrameBlock.FRAME_PROCESSED, frameProcessed)))
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count))));
        }
        lootTable.withPool(registrateBlockLootTables.applyExplosionCondition(firmacivFlatWoodenBoatFrameBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1f))
                .add(AlternativesEntry.alternatives(alternatives, func -> func))));

        alternatives.clear();
        for (int frameProcessed : FirmacivFlatWoodenBoatFrameBlock.FRAME_PROCESSED.getPossibleValues()) {
            if (frameProcessed + 1 <= 4) {
                continue;
            }

            int count = frameProcessed % 4;
            alternatives.add(LootItem.lootTableItem(FirmacivItems.COPPER_BOLT.get())
                    .when(LootItemBlockStatePropertyCondition
                            .hasBlockStateProperties(firmacivFlatWoodenBoatFrameBlock)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(FirmacivFlatWoodenBoatFrameBlock.FRAME_PROCESSED, frameProcessed)))
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count + 1))));
        }
        lootTable.withPool(registrateBlockLootTables.applyExplosionCondition(firmacivFlatWoodenBoatFrameBlock, LootPool.lootPool().setRolls(ConstantValue.exactly(1f))
                .add(AlternativesEntry.alternatives(alternatives, func -> func))));

        return lootTable;
    }

    private static NonNullBiConsumer<DataGenContext<Block, FirmacivFlatWoodenBoatFrameBlock>, RegistrateBlockstateProvider> flatBoatFrameBlockState(TFGWatercraftMaterial watercraftMaterial) {
        return (datagenContext, blockStateProvider) -> {
            if (watercraftMaterial.woodKind != TFGWatercraftMaterial.WoodKind.HARD) {
                return;
            }

            var frameFlat = blockStateProvider.models().getExistingFile(ResourceLocation.fromNamespaceAndPath(Firmaciv.MOD_ID, "block/watercraft_frame/flat/frame"));

            final var plankTexture = blockStateProvider.blockTexture(watercraftMaterial.getDeckBlock().getBlock());
            final var multipartBuilder = blockStateProvider.getMultipartBuilder(datagenContext.get()).part()
                    .modelFile(frameFlat).addModel().end();

            IntStream.range(0, 8).forEach(progress -> {
                if (progress < 4) {
                    final var plankModel = blockStateProvider.models().withExistingParent(
                            String.format(Locale.ROOT, "block/wood/watercraft_frame/flat/%s/%s", watercraftMaterial.getSerializedName(),
                                    BOAT_FRAME_PROGRESS_STRINGS[progress]),
                            ResourceLocation.fromNamespaceAndPath(AlekiShips.MOD_ID,
                                    String.format(Locale.ROOT, "block/watercraft_frame/flat/template/%s",
                                            BOAT_FRAME_PROGRESS_STRINGS[progress])))
                            .texture("plank", plankTexture);

                    multipartBuilder.part().modelFile(plankModel).addModel()
                            .condition(FlatWoodenBoatFrameBlock.FRAME_PROCESSED,
                                    IntStream.range(progress, 8).boxed().toArray(Integer[]::new));
                } else {
                    final var plankModel = blockStateProvider.models().getExistingFile(
                            ResourceLocation.fromNamespaceAndPath(Firmaciv.MOD_ID, String.format(Locale.ROOT, "block/watercraft_frame/flat/bolt/%s", BOAT_FRAME_PROGRESS_STRINGS[progress % 4])));

                    multipartBuilder.part().modelFile(plankModel).addModel()
                            .condition(FlatWoodenBoatFrameBlock.FRAME_PROCESSED, IntStream.range(progress, 8).boxed().toArray(Integer[]::new));
                }
            });
        };
    }

    public static void registerFrames() {
        for (var material : TFGWatercraftMaterial.values()) {
            if (material.woodKind == TFGWatercraftMaterial.WoodKind.SOFT) {
                CanoeComponentBlock.registerCanoeComponent((RotatedPillarBlock) material.getWood().getBlock(Wood.BlockType.STRIPPED_LOG).get(), CANOE_COMPONENT_BLOCKS.get(material).get());
            } else if (material.woodKind == TFGWatercraftMaterial.WoodKind.HARD) {
                FirmacivBlocks.BOAT_FRAME_ANGLED.get()
                        .registerFrame(material.getWood().getBlock(Wood.BlockType.PLANKS).get().asItem(),
                                TFGBlocks_FirmaCiv.WOODEN_BOAT_FRAME_ANGLED.get(material).get());
                FirmacivBlocks.BOAT_FRAME_FLAT.get()
                        .registerFrame(material.getWood().getBlock(Wood.BlockType.PLANKS).get().asItem(),
                                TFGBlocks_FirmaCiv.WOODEN_BOAT_FRAME_FLAT.get(material).get());
            }
        }
    }
}
