package su.terrafirmagreg.core.common.data.blocks;

import com.therighthon.rnr.common.block.PathStairBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadHelper;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadHotBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadMarkingMask;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadPouringBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadSlabBlock;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadStairsBlock;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.utils.ModelUtils;

@SuppressWarnings("unused")
public final class TFGBlocksAsphalt {

    public static void init() {
    }

    public static final BlockEntry<AsphaltRoadPouringBlock> ASPHALT_ROAD_POURING = TFGCore.REGISTRATE.block("asphalt_road_pouring",
            AsphaltRoadPouringBlock::new)
            .initialProperties(() -> Blocks.BLACK_CONCRETE)
            .properties(p -> p.strength(-1.0F, 3600000.0F).sound(SoundType.MUD).mapColor(MapColor.COLOR_BLACK).noLootTable())
            .blockstate(TFGBlocksAsphalt::asphaltRoadPouringBlockstate)
            .loot((prov, block) -> prov.add(block, LootTable.lootTable()))
            .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/asphalt_road/pouring_" + AsphaltRoadPouringBlock.MAX_VISUAL_LEVEL))).build()
            .register();

    public static final BlockEntry<AsphaltRoadHotBlock> ASPHALT_ROAD_HOT = TFGCore.REGISTRATE.block("asphalt_road_hot", AsphaltRoadHotBlock::new)
            .initialProperties(() -> Blocks.BLACK_CONCRETE)
            .properties(p -> p.strength(1.5F, 6).sound(SoundType.TUFF).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops())
            .blockstate(TFGBlocksAsphalt::asphaltRoadHotBlockstate)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, TFCTags.Blocks.SUPPORTS_LANDSLIDE, TFCTags.Blocks.TOUGHNESS_2)
            .onRegister(block -> TFGBlockEntities.addValidBEBlock(TFCBlockEntities.TICK_COUNTER, block))
            .loot(RegistrateBlockLootTables::dropSelf)
            .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/asphalt_road/hot"))).build()
            .register();

    public static final BlockEntry<AsphaltRoadBlock> ASPHALT_ROAD = TFGCore.REGISTRATE.block("asphalt_road", AsphaltRoadBlock::new)
            .initialProperties(() -> Blocks.BLACK_CONCRETE)
            .properties(p -> p.strength(5F, 64).sound(SoundType.STONE).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops())
            .addLayer(() -> RenderType::cutout)
            .blockstate(TFGBlocksAsphalt::asphaltRoadBlockstate)
            .loot(RegistrateBlockLootTables::dropSelf)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, TFCTags.Blocks.SUPPORTS_LANDSLIDE, TFCTags.Blocks.TOUGHNESS_2)
            .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/asphalt_road/block_base"))).build()
            .register();

    public static final BlockEntry<AsphaltRoadStairsBlock> ASPHALT_ROAD_STAIRS = TFGCore.REGISTRATE.block("asphalt_road_stairs",
            p -> new AsphaltRoadStairsBlock(() -> ASPHALT_ROAD.get().defaultBlockState(), p))
            .initialProperties(ASPHALT_ROAD)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .blockstate(TFGBlocksAsphalt::asphaltRoadStairsBlockstate)
            .loot(RegistrateBlockLootTables::dropSelf)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.STAIRS, TFCTags.Blocks.SUPPORTS_LANDSLIDE, TFCTags.Blocks.TOUGHNESS_2)
            .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/asphalt_road/stairs"))).build()
            .register();

    public static final BlockEntry<AsphaltRoadSlabBlock> ASPHALT_ROAD_SLAB = TFGCore.REGISTRATE.block("asphalt_road_slab", AsphaltRoadSlabBlock::new)
            .initialProperties(ASPHALT_ROAD)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .addLayer(() -> RenderType::cutout)
            .blockstate(TFGBlocksAsphalt::asphaltRoadSlabBlockstate)
            .loot(RegistrateBlockLootTables::dropSelf)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.SLABS, TFCTags.Blocks.SUPPORTS_LANDSLIDE, TFCTags.Blocks.TOUGHNESS_2)
            .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/asphalt_road/slab_base"))).build()
            .register();

    private static void asphaltRoadPouringBlockstate(DataGenContext<Block, AsphaltRoadPouringBlock> ctx, RegistrateBlockstateProvider prov) {
        var builder = prov.getVariantBuilder(ctx.getEntry());
        for (int level = 0; level <= AsphaltRoadPouringBlock.MAX_VISUAL_LEVEL; level++) {
            builder.partialState()
                    .with(AsphaltRoadPouringBlock.ASPHALT_LEVEL, level)
                    .modelForState()
                    .modelFile(asphaltPouringLevelModel(prov, level))
                    .addModel();
        }
    }

    private static void asphaltRoadHotBlockstate(DataGenContext<Block, AsphaltRoadHotBlock> ctx, RegistrateBlockstateProvider prov) {
        prov.simpleBlock(ctx.getEntry(), asphaltPathBlockModel(prov, "hot", TFGCore.id("block/asphalt_road/hot"), ResourceLocation.withDefaultNamespace("block/gravel")));
    }

    private static void asphaltRoadBlockstate(DataGenContext<Block, AsphaltRoadBlock> ctx, RegistrateBlockstateProvider prov) {
        MultiPartBlockStateBuilder builder = prov.getMultipartBuilder(ctx.getEntry());
        ModelFile base = asphaltPathBlockModel(prov, "block_base", TFGCore.id("block/asphalt_road/block"), ResourceLocation.withDefaultNamespace("block/gravel"));
        builder.part().modelFile(base).addModel().end();

        ModelFile overlay = overlayTemplate(prov, "block_overlay", 14.992F, 15.015F);
        for (AsphaltRoadMarkingMask mask : AsphaltRoadMarkingMask.values()) {
            if (mask.isNone()) {
                continue;
            }
            ModelFile modelFile = overlayModel(prov, "block_overlay_" + mask.getSerializedName(), overlay, "mask_" + mask.getSerializedName());
            if (mask.getDirs() == 0) {
                addFlatOverlay(builder, mask, modelFile);
            } else if (mask.getDirs() == 2) {
                addTwoWayOverlays(builder, mask, modelFile);
            } else {
                addDirectionalOverlays(builder, mask, modelFile);
            }
        }
    }

    private static void asphaltRoadStairsBlockstate(DataGenContext<Block, AsphaltRoadStairsBlock> ctx, RegistrateBlockstateProvider prov) {
        ModelFile straight = asphaltStairsModel(prov, "stairs", ResourceLocation.fromNamespaceAndPath("rnr", "block/path_stairs"));
        ModelFile inner = asphaltStairsModel(prov, "stairs_inner", ResourceLocation.fromNamespaceAndPath("rnr", "block/path_inner_stairs"));
        ModelFile outer = asphaltStairsModel(prov, "stairs_outer", ResourceLocation.fromNamespaceAndPath("rnr", "block/path_outer_stairs"));
        MultiPartBlockStateBuilder builder = prov.getMultipartBuilder(ctx.getEntry());

        addStairModel(builder, StairsShape.STRAIGHT, straight);
        addStairModel(builder, StairsShape.OUTER_RIGHT, outer);
        addStairModel(builder, StairsShape.OUTER_LEFT, outer);
        addStairModel(builder, StairsShape.INNER_RIGHT, inner);
        addStairModel(builder, StairsShape.INNER_LEFT, inner);
    }

    private static void asphaltRoadSlabBlockstate(DataGenContext<Block, AsphaltRoadSlabBlock> ctx, RegistrateBlockstateProvider prov) {
        MultiPartBlockStateBuilder builder = prov.getMultipartBuilder(ctx.getEntry());
        ModelFile base = prov.models()
                .withExistingParent("asphalt_road/slab_base", ResourceLocation.fromNamespaceAndPath("rnr", "block/path_slab"))
                .texture("gravel", ResourceLocation.withDefaultNamespace("block/gravel"))
                .texture("top", TFGCore.id("block/asphalt_road/block"));
        builder.part().modelFile(base).addModel().end();

        ModelFile overlay = overlayTemplate(prov, "slab_overlay", 6.992F, 7.015F);
        for (AsphaltRoadMarkingMask mask : AsphaltRoadMarkingMask.values()) {
            if (mask.isNone()) {
                continue;
            }
            ModelFile modelFile = overlayModel(prov, "slab_overlay_" + mask.getSerializedName(), overlay, "mask_" + mask.getSerializedName());
            if (mask.getDirs() == 0) {
                addFlatOverlay(builder, mask, modelFile);
            } else if (mask.getDirs() == 2) {
                addTwoWayOverlays(builder, mask, modelFile);
            } else {
                addDirectionalOverlays(builder, mask, modelFile);
            }
        }
    }

    private static ModelFile asphaltPathBlockModel(RegistrateBlockstateProvider prov, String name, ResourceLocation top, ResourceLocation gravel) {
        return prov.models()
                .withExistingParent("asphalt_road/" + name, ResourceLocation.fromNamespaceAndPath("rnr", "block/path_block"))
                .texture("gravel", gravel)
                .texture("top", top);
    }

    private static ModelFile asphaltPouringLevelModel(RegistrateBlockstateProvider prov, int level) {
        return prov.models()
                .withExistingParent("asphalt_road/pouring_" + level, ResourceLocation.withDefaultNamespace("block/block"))
                .texture("particle", TFGCore.id("block/asphalt_road/hot"))
                .texture("top", TFGCore.id("block/asphalt_road/hot"))
                .ao(false)
                .element()
                .from(0.0F, 0.0F, 0.0F)
                .to(16.0F, 1.0F + 14.0F * level / AsphaltRoadPouringBlock.MAX_VISUAL_LEVEL, 16.0F)
                .face(Direction.EAST).texture("#top").end()
                .face(Direction.SOUTH).texture("#top").end()
                .face(Direction.NORTH).texture("#top").end()
                .face(Direction.WEST).texture("#top").end()
                .face(Direction.UP).texture("#top").end()
                .face(Direction.DOWN).cullface(Direction.DOWN).texture("#top").end()
                .end();
    }

    private static ModelFile asphaltStairsModel(RegistrateBlockstateProvider prov, String name, ResourceLocation parent) {
        return prov.models()
                .withExistingParent("asphalt_road/" + name, parent)
                .texture("side", TFGCore.id("block/asphalt_road/block"))
                .texture("top", TFGCore.id("block/asphalt_road/block"))
                .texture("bottom", TFGCore.id("block/asphalt_road/block"));
    }

    private static ModelFile overlayTemplate(RegistrateBlockstateProvider prov, String name, float yMin, float yMax) {
        return prov.models()
                .withExistingParent("asphalt_road/" + name, ResourceLocation.withDefaultNamespace("block/block"))
                .element()
                .from(-0.01F, yMin, -0.01F)
                .to(16.01F, yMax, 16.01F)
                .face(Direction.UP).uvs(0.0F, 0.0F, 16.0F, 16.0F).texture("#decal").tintindex(1).end()
                .face(Direction.DOWN).uvs(0.0F, 0.0F, 16.0F, 16.0F).texture("#decal").tintindex(1).end()
                .end();
    }

    private static ModelFile overlayModel(RegistrateBlockstateProvider prov, String name, ModelFile parent, String texture) {
        return prov.models()
                .withExistingParent("asphalt_road/" + name, parent.getLocation())
                .texture("decal", TFGCore.id("block/asphalt_road/" + texture));
    }

    private static void addStairModel(MultiPartBlockStateBuilder builder, StairsShape shape, ModelFile model) {
        if (shape == StairsShape.OUTER_LEFT || shape == StairsShape.INNER_LEFT) {
            addStairPart(builder, shape, Direction.EAST, model, 270);
            addStairPart(builder, shape, Direction.WEST, model, 90);
            addStairPart(builder, shape, Direction.SOUTH, model, 0);
            addStairPart(builder, shape, Direction.NORTH, model, 180);
            return;
        }
        addStairPart(builder, shape, Direction.EAST, model, 0);
        addStairPart(builder, shape, Direction.WEST, model, 180);
        addStairPart(builder, shape, Direction.SOUTH, model, 90);
        addStairPart(builder, shape, Direction.NORTH, model, 270);
    }

    private static void addStairPart(MultiPartBlockStateBuilder builder, StairsShape shape, Direction facing, ModelFile model, int rotationY) {
        if (rotationY == 0) {
            builder.part()
                    .modelFile(model).addModel()
                    .condition(PathStairBlock.SHAPE, shape)
                    .condition(PathStairBlock.FACING, facing)
                    .end();
            return;
        }
        builder.part()
                .modelFile(model).rotationY(rotationY).uvLock(true).addModel()
                .condition(PathStairBlock.SHAPE, shape)
                .condition(PathStairBlock.FACING, facing)
                .end();
    }

    private static void addTwoWayOverlays(MultiPartBlockStateBuilder builder,
            AsphaltRoadMarkingMask mask, ModelFile model) {
        builder.part()
                .modelFile(model).addModel()
                .condition(AsphaltRoadHelper.MASK, mask)
                .condition(AsphaltRoadHelper.FACING, Direction.NORTH, Direction.SOUTH)
                .end();
        builder.part()
                .modelFile(model).rotationY(90).addModel()
                .condition(AsphaltRoadHelper.MASK, mask)
                .condition(AsphaltRoadHelper.FACING, Direction.EAST, Direction.WEST)
                .end();
    }

    private static void addDirectionalOverlays(MultiPartBlockStateBuilder builder,
            AsphaltRoadMarkingMask mask, ModelFile model) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            builder.part()
                    .modelFile(model).rotationY(rotationY(direction)).addModel()
                    .condition(AsphaltRoadHelper.MASK, mask)
                    .condition(AsphaltRoadHelper.FACING, direction)
                    .end();
        }
    }

    private static void addFlatOverlay(MultiPartBlockStateBuilder builder,
            AsphaltRoadMarkingMask mask, ModelFile model) {
        builder.part()
                .modelFile(model).addModel()
                .condition(AsphaltRoadHelper.MASK, mask)
                .end();
    }

    private static int rotationY(Direction direction) {
        return switch (direction) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

}
