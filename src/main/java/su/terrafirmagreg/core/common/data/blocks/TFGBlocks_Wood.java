package su.terrafirmagreg.core.common.data.blocks;

import static com.eerussianguy.firmalife.common.blocks.FLBlocks.*;

import java.util.Map;
import java.util.function.Supplier;

import com.eerussianguy.firmalife.common.blockentities.BarrelPressBlockEntity;
import com.eerussianguy.firmalife.common.blockentities.FLBlockEntities;
import com.eerussianguy.firmalife.common.blocks.*;
import com.google.gson.JsonObject;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.devices.BarrelBlock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.BarrelBlockItem;
import net.dries007.tfc.common.items.ChestBlockItem;
import net.dries007.tfc.util.registry.RegistryWood;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.utils.ModelUtils;

public class TFGBlocks_Wood {

    public enum WoodType {
        GLACIAN("glacian", ResourceLocation.fromNamespaceAndPath("ad_astra", "block/glacian_planks"),
                ResourceLocation.fromNamespaceAndPath("ad_astra", "block/glacian_log"),
                ResourceLocation.fromNamespaceAndPath("ad_astra", "block/stripped_glacian_log"),
                MapColor.NONE),
        STROPHAR("strophar", ResourceLocation.fromNamespaceAndPath("ad_astra", "block/strophar_planks"),
                ResourceLocation.fromNamespaceAndPath("ad_astra", "block/strophar_stem"),
                ResourceLocation.fromNamespaceAndPath("ad_astra", "block/strophar_stem"),
                MapColor.NONE),
        AERONOS("aeronos", ResourceLocation.fromNamespaceAndPath("ad_astra", "block/aeronos_planks"),
                ResourceLocation.fromNamespaceAndPath("ad_astra", "block/aeronos_stem"),
                ResourceLocation.fromNamespaceAndPath("ad_astra", "block/aeronos_stem"),
                MapColor.NONE),
        GINKGO("ginkgo", ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts", "block/ginkgo_planks"),
                ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts", "block/ginkgo_log"),
                ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts", "block/stripped_ginkgo_log"),
                MapColor.NONE);

        public final String name;
        public final ResourceLocation plankTexture;
        public final ResourceLocation logTexture;
        public final ResourceLocation strippedLogTexture;
        public final RegistryWood registryWood;

        WoodType(String name, ResourceLocation plankTexture, ResourceLocation logBlock, ResourceLocation strippedLogTexture, MapColor col) {
            this.name = name;
            this.plankTexture = plankTexture;
            this.logTexture = logBlock;
            this.strippedLogTexture = strippedLogTexture;
            // This is just needed for the TFC wood block ctors, only the colour method is used.
            this.registryWood = new RegistryWood() {
                @Override
                public MapColor woodColor() {
                    return col;
                }

                @Override
                public MapColor barkColor() {
                    return col;
                }

                @Override
                public TFCTreeGrower tree() {
                    //noinspection DataFlowIssue
                    return null;
                }

                @Override
                public int daysToGrow() {
                    return 0;
                }

                @Override
                public int autumnIndex() {
                    return 0;
                }

                @Override
                public Supplier<Block> getBlock(Wood.BlockType blockType) {
                    //noinspection DataFlowIssue
                    return null;
                }

                @Override
                public BlockSetType getBlockSet() {
                    //noinspection DataFlowIssue
                    return null;
                }

                @Override
                public net.minecraft.world.level.block.state.properties.WoodType getVanillaWoodType() {
                    //noinspection DataFlowIssue
                    return null;
                }

                @Override
                public String getSerializedName() {
                    return name;
                }
            };
        }
    }

    public static final Map<WoodType, Map<Wood.BlockType, BlockEntry<? extends Block>>> WOOD_BLOCKS = new Object2ObjectOpenHashMap<>();

    public static final Map<WoodType, BlockEntry<? extends Block>> FOOD_SHELVES = new Object2ObjectOpenHashMap<>();
    public static final Map<WoodType, BlockEntry<? extends Block>> HANGERS = new Object2ObjectOpenHashMap<>();
    public static final Map<WoodType, BlockEntry<? extends Block>> JARBNETS = new Object2ObjectOpenHashMap<>();
    public static final Map<WoodType, BlockEntry<? extends Block>> BIG_BARRELS = new Object2ObjectOpenHashMap<>();
    public static final Map<WoodType, BlockEntry<? extends Block>> WINE_SHELVES = new Object2ObjectOpenHashMap<>();
    public static final Map<WoodType, BlockEntry<? extends Block>> STOMPING_BARRELS = new Object2ObjectOpenHashMap<>();
    public static final Map<WoodType, BlockEntry<? extends Block>> BARREL_PRESSES = new Object2ObjectOpenHashMap<>();

    public static void init() {
        for (WoodType value : WoodType.values()) {
            registerBlocks(value);

            FOOD_SHELVES.put(value, foodShelf(value));
            HANGERS.put(value, hanger(value));
            JARBNETS.put(value, jarbnet(value));
            BIG_BARRELS.put(value, bigBarrel(value));
            WINE_SHELVES.put(value, wineShelf(value));
            STOMPING_BARRELS.put(value, stompingBarrel(value));
            BARREL_PRESSES.put(value, barrelPress(value));
        }
    }

    private static void registerBlocks(WoodType woodType) {
        Map<Wood.BlockType, BlockEntry<? extends Block>> blocks = new Object2ObjectOpenHashMap<>();

        blocks.put(Wood.BlockType.TOOL_RACK, toolRack(woodType));
        blocks.put(Wood.BlockType.WORKBENCH, workbench(woodType));
        blocks.put(Wood.BlockType.CHEST, chest(woodType));
        blocks.put(Wood.BlockType.TRAPPED_CHEST, trappedChest(woodType));
        blocks.put(Wood.BlockType.LOOM, loom(woodType));
        blocks.put(Wood.BlockType.SLUICE, sluice(woodType));
        blocks.put(Wood.BlockType.BARREL, barrel(woodType));
        blocks.put(Wood.BlockType.LECTERN, lectern(woodType));
        blocks.put(Wood.BlockType.SCRIBING_TABLE, scribingTable(woodType));
        blocks.put(Wood.BlockType.SEWING_TABLE, sewingTable(woodType));
        blocks.put(Wood.BlockType.JAR_SHELF, jarShelf(woodType));
        blocks.put(Wood.BlockType.BOOKSHELF, bookshelf(woodType));

        WOOD_BLOCKS.put(woodType, blocks);
    }

    private static BlockEntry<Block> toolRack(WoodType woodType) {
        var toolRackBlock = Wood.BlockType.TOOL_RACK.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/tool_rack/" + woodType.name, p -> toolRackBlock)
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/tool_rack"))
                            .texture("texture", woodType.plankTexture)
                            .texture("particle", woodType.plankTexture);

                    ModelUtils.cardinalBlockInverted(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "tool_racks")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFCBlockEntities.TOOL_RACK, block);
                })
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "tool_racks"))).build()
                .register();
    }

    private static BlockEntry<Block> workbench(WoodType woodType) {
        var workbenchBlock = Wood.BlockType.WORKBENCH.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/workbench/" + woodType.name, p -> workbenchBlock)
                .blockstate((ctx, prov) -> {
                    ResourceLocation path = TFGCore.id("block/wood/workbench/" + woodType.name);
                    prov.simpleBlock(ctx.getEntry(), prov.models().cube(ctx.getName(), woodType.plankTexture, path.withSuffix("_top"), path.withSuffix("_front"),
                            path.withSuffix("_side"), path.withSuffix("_side"), path.withSuffix("_front")));
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "workbenches")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "workbenches"))).build()
                .register();
    }

    private static BlockEntry<Block> chest(WoodType woodType) {
        var chestBlock = Wood.BlockType.CHEST.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/chest/" + woodType.name, p -> chestBlock)
                .blockstate((ctx, prov) -> {
                    prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(ctx.getName()).texture("particle", woodType.plankTexture));
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "chests")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFCBlockEntities.CHEST, block);
                })
                .item((b, i) -> new ChestBlockItem(b, i, woodType.registryWood))
                .model((ctx, prov) -> {
                    prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/chest"))
                            .texture("particle", woodType.plankTexture);
                })
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", "chests"))).build()
                .register();
    }

    private static BlockEntry<Block> trappedChest(WoodType woodType) {
        var trappedChestBlock = Wood.BlockType.TRAPPED_CHEST.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/trapped_chest/" + woodType.name, p -> trappedChestBlock)
                .blockstate((ctx, prov) -> {
                    prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(ctx.getName()).texture("particle", woodType.plankTexture));
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "chests")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFCBlockEntities.TRAPPED_CHEST, block);
                })
                .item((b, i) -> new ChestBlockItem(b, i, woodType.registryWood))
                .model((ctx, prov) -> {
                    prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/chest"))
                            .texture("particle", woodType.plankTexture);
                })
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", "chests"))).build()
                .register();
    }

    private static BlockEntry<Block> loom(WoodType woodType) {
        var loomBlock = Wood.BlockType.LOOM.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/loom/" + woodType.name, p -> loomBlock)
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/loom"))
                            .texture("texture", woodType.plankTexture)
                            .texture("particle", woodType.plankTexture);

                    ModelUtils.cardinalBlockInverted(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "looms")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFCBlockEntities.LOOM, block);
                })
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "looms"))).build()
                .register();

    }

    private static BlockEntry<Block> sluice(WoodType woodType) {
        var sluiceBlock = Wood.BlockType.SLUICE.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/sluice/" + woodType.name, p -> sluiceBlock)
                .blockstate((ctx, prov) -> {

                    ModelFile sluiceUpper = prov.models().withExistingParent("wood/sluice/" + woodType.name + "_upper", ResourceLocation.fromNamespaceAndPath("tfc", "block/sluice_upper"))
                            .texture("texture", TFGCore.id("block/wood/sheet/" + woodType.name));
                    ModelFile sluiceLower = prov.models().withExistingParent("wood/sluice/" + woodType.name + "_lower", ResourceLocation.fromNamespaceAndPath("tfc", "block/sluice_lower"))
                            .texture("texture", TFGCore.id("block/wood/sheet/" + woodType.name));

                    var builder = prov.getVariantBuilder(ctx.getEntry());

                    ModelUtils.forEachCardinalDirection(builder, sluiceLower, b -> b.with(TFCBlockStateProperties.UPPER, false));
                    ModelUtils.forEachCardinalDirection(builder, sluiceUpper, b -> b.with(TFCBlockStateProperties.UPPER, true));
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "sluices")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFCBlockEntities.SLUICE, block);
                })
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/sluice/" + woodType.name + "_lower")))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "sluices"))).build()
                .register();
    }

    private static BlockEntry<BarrelBlock> barrel(WoodType woodType) {
        var barrelBlock = Wood.BlockType.BARREL.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/barrel/" + woodType.name, p -> (BarrelBlock) barrelBlock)
                .blockstate((ctx, prov) -> {

                    ModelFile barrel = prov.models().withExistingParent("wood/barrel/" + woodType.name, ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel"))
                            .texture("particle", woodType.plankTexture)
                            .texture("planks", woodType.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));

                    ModelFile barrelSide = prov.models().withExistingParent("wood/barrel/" + woodType.name + "_side", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side"))
                            .texture("particle", woodType.plankTexture)
                            .texture("planks", woodType.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));

                    ModelFile barrelSideRack = prov.models().withExistingParent("wood/barrel/" + woodType.name + "_side_rack", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side_rack"))
                            .texture("particle", woodType.plankTexture)
                            .texture("planks", woodType.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));

                    ModelFile sealedBarrel = prov.models().withExistingParent("wood/barrel_sealed/" + woodType.name, ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_sealed"))
                            .texture("particle", woodType.plankTexture)
                            .texture("planks", woodType.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));

                    ModelFile sealedBarrelSide = prov.models()
                            .withExistingParent("wood/barrel_sealed/" + woodType.name + "_side", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side_sealed"))
                            .texture("particle", woodType.plankTexture)
                            .texture("planks", woodType.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));

                    ModelFile sealedBarrelSideRack = prov.models()
                            .withExistingParent("wood/barrel_sealed/" + woodType.name + "_side_rack", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side_sealed_rack"))
                            .texture("particle", woodType.plankTexture)
                            .texture("planks", woodType.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));

                    var builder = prov.getVariantBuilder(ctx.getEntry());

                    buildBarrelBlockStateEntry(builder, Direction.UP, 0, barrel, barrel, sealedBarrel, sealedBarrel);
                    buildBarrelBlockStateEntry(builder, Direction.EAST, 0, barrelSide, barrelSideRack, sealedBarrelSide, sealedBarrelSideRack);
                    buildBarrelBlockStateEntry(builder, Direction.WEST, 180, barrelSide, barrelSideRack, sealedBarrelSide, sealedBarrelSideRack);
                    buildBarrelBlockStateEntry(builder, Direction.SOUTH, 90, barrelSide, barrelSideRack, sealedBarrelSide, sealedBarrelSideRack);
                    buildBarrelBlockStateEntry(builder, Direction.NORTH, 270, barrelSide, barrelSideRack, sealedBarrelSide, sealedBarrelSideRack);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "barrels")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFCBlockEntities.BARREL, block);
                })
                .item(BarrelBlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "barrels"))).build()
                .register();
    }

    private static void buildBarrelBlockStateEntry(VariantBlockStateBuilder builder, Direction facing, int y, ModelFile barrel, ModelFile rack, ModelFile sealed, ModelFile sealedRack) {
        builder.partialState().with(TFCBlockStateProperties.FACING_NOT_DOWN, facing).with(TFCBlockStateProperties.SEALED, false).with(TFCBlockStateProperties.RACK, false).modelForState().rotationY(y)
                .modelFile(barrel).addModel()
                .partialState().with(TFCBlockStateProperties.FACING_NOT_DOWN, facing).with(TFCBlockStateProperties.SEALED, true).with(TFCBlockStateProperties.RACK, false).modelForState().rotationY(y)
                .modelFile(sealed).addModel()
                .partialState().with(TFCBlockStateProperties.FACING_NOT_DOWN, facing).with(TFCBlockStateProperties.SEALED, false).with(TFCBlockStateProperties.RACK, true).modelForState().rotationY(y)
                .modelFile(rack).addModel()
                .partialState().with(TFCBlockStateProperties.FACING_NOT_DOWN, facing).with(TFCBlockStateProperties.SEALED, true).with(TFCBlockStateProperties.RACK, true).modelForState().rotationY(y)
                .modelFile(sealedRack).addModel();
    }

    private static BlockEntry<Block> lectern(WoodType woodType) {
        var lecternBlock = Wood.BlockType.LECTERN.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/lectern/" + woodType.name, p -> lecternBlock)
                .blockstate((ctx, prov) -> {
                    var path = "block/wood/lectern/" + woodType.name + "/";
                    ModelFile model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("block/lectern"))
                            .texture("bottom", woodType.plankTexture)
                            .texture("base", TFGCore.id(path + "base"))
                            .texture("front", TFGCore.id(path + "front"))
                            .texture("sides", TFGCore.id(path + "sides"))
                            .texture("top", TFGCore.id(path + "top"))
                            .texture("particle", TFGCore.id(path + "sides"));

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "lecterns")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFCBlockEntities.LECTERN, block);
                })
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "lecterns"))).build()
                .register();

    }

    private static BlockEntry<Block> scribingTable(WoodType woodType) {
        var scribingTableBlock = Wood.BlockType.SCRIBING_TABLE.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/scribing_table/" + woodType.name, p -> scribingTableBlock)
                .blockstate((ctx, prov) -> {
                    var model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/scribing_table"))
                            .texture("top", TFGCore.id("block/wood/scribing_table/" + woodType.name))
                            .texture("leg", woodType.logTexture)
                            .texture("side", woodType.plankTexture)
                            .texture("misc", ResourceLocation.fromNamespaceAndPath("tfc", "block/wood/scribing_table/scribing_paraphernalia"))
                            .texture("particle", woodType.plankTexture);

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "scribing_tables")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "scribing_tables"))).build()
                .register();

    }

    private static BlockEntry<Block> sewingTable(WoodType woodType) {
        var sewingTableBlock = Wood.BlockType.SEWING_TABLE.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/sewing_table/" + woodType.name, p -> sewingTableBlock)
                .blockstate((ctx, prov) -> {
                    var model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/sewing_table"))
                            .texture("0", woodType.logTexture)
                            .texture("1", woodType.plankTexture);

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "sewing_tables")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "sewing_tables"))).build()
                .register();
    }

    private static BlockEntry<Block> jarShelf(WoodType woodType) {
        var jarShelfBlock = Wood.BlockType.JAR_SHELF.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/jar_shelf/" + woodType.name, p -> jarShelfBlock)
                .blockstate((ctx, prov) -> {
                    var model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/jar_shelf"))
                            .texture("0", woodType.plankTexture);

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "jar_shelves")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "jar_shelves"))).build()
                .register();

    }

    private static BlockEntry<Block> bookshelf(WoodType woodType) {
        var bookshelfBlock = Wood.BlockType.BOOKSHELF.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/bookshelf/" + woodType.name, p -> bookshelfBlock)
                .blockstate((ctx, prov) -> {
                    prov.models()
                            .withExistingParent("wood/bookshelf/" + woodType.name + "_inventory", ResourceLocation.withDefaultNamespace("block/chiseled_bookshelf_inventory"))
                            .texture("top", TFGCore.id("block/wood/bookshelf/" + woodType.name + "_top"))
                            .texture("side", TFGCore.id("block/wood/bookshelf/" + woodType.name + "_side"))
                            .texture("front", TFGCore.id("block/wood/bookshelf/" + woodType.name + "_empty"));

                    ModelFile base = prov.models()
                            .withExistingParent("wood/bookshelf/" + woodType.name, ResourceLocation.withDefaultNamespace("block/chiseled_bookshelf"))
                            .texture("top", TFGCore.id("block/wood/bookshelf/" + woodType.name + "_top"))
                            .texture("side", TFGCore.id("block/wood/bookshelf/" + woodType.name + "_side"));

                    var builder = prov.getMultipartBuilder(ctx.getEntry());

                    BooleanProperty[] slots = new BooleanProperty[] {
                            BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED,
                            BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED,
                            BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED,
                            BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED,
                            BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED,
                            BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED
                    };
                    String[] vertical = { "top", "top", "top", "bottom", "bottom", "bottom" };
                    String[] horizontal = { "left", "mid", "right", "left", "mid", "right" };

                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        int rot = switch (dir) {
                            case EAST -> 90;
                            case SOUTH -> 180;
                            case WEST -> 270;
                            default -> 0;
                        };

                        builder.part()
                                .modelFile(base)
                                .rotationY(rot)
                                .uvLock(true)
                                .addModel()
                                .condition(BlockStateProperties.HORIZONTAL_FACING, dir);

                        for (int i = 0; i < slots.length; i++)
                            for (boolean occupied : new boolean[] { false, true }) {
                                String state = occupied ? "occupied" : "empty";

                                ModelFile model = prov.models()
                                        .withExistingParent(
                                                ctx.getName() + "_" + state + "_" + vertical[i] + "_" + horizontal[i],
                                                "block/chiseled_bookshelf_" + state + "_slot_" + vertical[i] + "_" + horizontal[i])
                                        .texture("texture", TFGCore.id("block/wood/bookshelf/" + woodType.name + "_" + state));

                                builder.part()
                                        .modelFile(model)
                                        .rotationY(rot)
                                        .addModel()
                                        .condition(BlockStateProperties.HORIZONTAL_FACING, dir)
                                        .condition(slots[i], occupied);
                            }
                    }
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "bookshelves")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/bookshelf/" + woodType.name + "_inventory")))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "bookshelves"))).build()
                .register();
    }

    private static class FirmaCustomLoader extends CustomLoaderBuilder<BlockModelBuilder> {

        private final ResourceLocation parentBlock;

        public static FirmaCustomLoader get(ResourceLocation loaderId, ResourceLocation parentBlock, BlockModelBuilder parent, ExistingFileHelper existingFileHelper) {
            return new FirmaCustomLoader(loaderId, parentBlock, parent, existingFileHelper);
        }

        protected FirmaCustomLoader(ResourceLocation loaderId, ResourceLocation parentBlock, BlockModelBuilder parent, ExistingFileHelper existingFileHelper) {
            super(loaderId, parent, existingFileHelper);
            this.parentBlock = parentBlock;
        }

        @Override
        public JsonObject toJson(JsonObject json) {
            super.toJson(json);
            var obj = new JsonObject();
            obj.addProperty("parent", parentBlock.toString());
            json.add("base", obj);
            return json;
        }
    }

    private static BlockEntry<FoodShelfBlock> foodShelf(WoodType type) {
        return TFGCore.REGISTRATE.block("wood/food_shelf/" + type.name, p -> new FoodShelfBlock(shelfProperties().mapColor(type.registryWood.woodColor())))
                .blockstate((ctx, prov) -> {
                    prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/food_shelf_base"))
                            .texture("wood", type.plankTexture);

                    var dynamicModel = prov.models().getBuilder("wood/food_shelf/" + type.name + "_dynamic")
                            .customLoader((t, existing) -> FirmaCustomLoader.get(ResourceLocation.fromNamespaceAndPath("firmalife", "food_shelf"), TFGCore.id("block/wood/food_shelf/" + type.name), t,
                                    existing))
                            .end();

                    ModelUtils.cardinalBlockInverted(prov.getVariantBuilder(ctx.getEntry()), dynamicModel);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "food_shelves")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/food_shelf/" + type.name)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmalife", "food_shelves"))).build()
                .register();
    }

    private static BlockEntry<HangerBlock> hanger(WoodType type) {
        return TFGCore.REGISTRATE.block("wood/hanger/" + type.name, p -> new HangerBlock(hangerProperties().mapColor(type.registryWood.woodColor())))
                .blockstate((ctx, prov) -> {
                    prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/hanger_base"))
                            .texture("wood", type.plankTexture)
                            .texture("string", ResourceLocation.withDefaultNamespace("block/white_wool"));

                    var dynamicModel = prov.models().getBuilder("wood/hanger/" + type.name + "_dynamic")
                            .customLoader(
                                    (t, existing) -> FirmaCustomLoader.get(ResourceLocation.fromNamespaceAndPath("firmalife", "hanger"), TFGCore.id("block/wood/hanger/" + type.name), t, existing))
                            .end();

                    prov.simpleBlock(ctx.getEntry(), dynamicModel);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "hangers")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/hanger/" + type.name)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmalife", "hangers"))).build()
                .register();

    }

    private static BlockEntry<JarbnetBlock> jarbnet(WoodType type) {
        return TFGCore.REGISTRATE.block("wood/jarbnet/" + type.name, p -> new JarbnetBlock(jarbnetProperties().mapColor(type.registryWood.woodColor())))
                .blockstate((ctx, prov) -> {
                    prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/jarbnet"))
                            .texture("planks", type.plankTexture)
                            .texture("log", type.logTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + type.name));

                    prov.models().withExistingParent(ctx.getName() + "_shut", ResourceLocation.fromNamespaceAndPath("firmalife", "block/jarbnet_shut"))
                            .texture("planks", type.plankTexture)
                            .texture("log", type.logTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + type.name));

                    var dynamicModel = prov.models().getBuilder("wood/jarbnet/" + type.name + "_dynamic")
                            .customLoader(
                                    (t, existing) -> FirmaCustomLoader.get(ResourceLocation.fromNamespaceAndPath("firmalife", "jarbnet"), TFGCore.id("block/wood/jarbnet/" + type.name), t, existing))
                            .end();

                    var dynamicModelShut = prov.models().getBuilder("wood/jarbnet/" + type.name + "_shut_dynamic")
                            .customLoader((t, existing) -> FirmaCustomLoader.get(ResourceLocation.fromNamespaceAndPath("firmalife", "jarbnet"), TFGCore.id("block/wood/jarbnet/" + type.name + "_shut"),
                                    t, existing))
                            .end();

                    var builder = prov.getVariantBuilder(ctx.getEntry());

                    ModelUtils.forEachCardinalDirection(builder, dynamicModel, b -> b.with(BlockStateProperties.OPEN, true));
                    ModelUtils.forEachCardinalDirection(builder, dynamicModelShut, b -> b.with(BlockStateProperties.OPEN, false));
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "jarbnets")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/jarbnet/" + type.name)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmalife", "jarbnets"))).build()
                .register();

    }

    private static BlockModelBuilder bigBarrelTextures(BlockModelBuilder builder, WoodType type) {
        return builder.texture("0", TFGCore.id("block/wood/big_barrel/" + type.name + "_3_side"))
                .texture("1", TFGCore.id("block/wood/big_barrel/" + type.name + "_0"))
                .texture("2", TFGCore.id("block/wood/big_barrel/" + type.name + "_0_side"))
                .texture("3", TFGCore.id("block/wood/big_barrel/" + type.name + "_1"))
                .texture("4", TFGCore.id("block/wood/big_barrel/" + type.name + "_1_side"))
                .texture("5", TFGCore.id("block/wood/big_barrel/" + type.name + "_2"))
                .texture("6", TFGCore.id("block/wood/big_barrel/" + type.name + "_2_side"))
                .texture("7", TFGCore.id("block/wood/big_barrel/" + type.name + "_3"))
                .texture("8", TFGCore.id("block/wood/big_barrel/" + type.name + "_3_top"))
                .texture("9", TFGCore.id("block/wood/big_barrel/" + type.name + "_0_top"))
                .texture("10", TFGCore.id("block/wood/big_barrel/" + type.name + "_1_top"))
                .texture("11", TFGCore.id("block/wood/big_barrel/" + type.name + "_2_top"))
                .texture("12", type.logTexture);
    }

    private static BlockEntry<BigBarrelBlock> bigBarrel(WoodType type) {
        var properties = ExtendedProperties.of().mapColor(type.registryWood.woodColor()).sound(SoundType.WOOD)
                .noOcclusion().strength(10f).pushReaction(PushReaction.BLOCK).flammableLikeLogs().blockEntity(FLBlockEntities.BIG_BARREL);

        return TFGCore.REGISTRATE.block("wood/big_barrel/" + type.name, p -> new BigBarrelBlock(properties))
                .blockstate((ctx, prov) -> {
                    var builder = prov.getVariantBuilder(ctx.getEntry());

                    bigBarrelTextures(prov.models().withExistingParent(ctx.getName() + "_item", ResourceLocation.fromNamespaceAndPath("firmalife", "block/big_barrel_item")), type);
                    for (int i = 0; i < 8; i++) {
                        var model = bigBarrelTextures(prov.models().withExistingParent(ctx.getName() + "_" + i, ResourceLocation.fromNamespaceAndPath("firmalife", "block/big_barrel_" + i)), type);
                        int finalI = i;
                        ModelUtils.forEachCardinalDirection(builder, model, b -> b.with(FLStateProperties.BARREL_PART, finalI));
                    }

                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "big_barrels")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/big_barrel/" + type.name + "_item")))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmalife", "big_barrels"))).build()
                .loot((lt, block) -> lt.add(block, LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .name("loot_pool")
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(block)
                                        .when(LootItemBlockStatePropertyCondition
                                                .hasBlockStateProperties(block)
                                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                                        .hasProperty(FLStateProperties.BARREL_PART, 0))))
                                .when(ExplosionCondition.survivesExplosion()))))
                .register();
    }

    private static BlockEntry<WineShelfBlock> wineShelf(WoodType type) {
        var properties = ExtendedProperties.of().mapColor(type.registryWood.woodColor()).sound(SoundType.WOOD).noOcclusion()
                .strength(4f).pushReaction(PushReaction.BLOCK).flammableLikeLogs().blockEntity(FLBlockEntities.WINE_SHELF);

        return TFGCore.REGISTRATE.block("wood/wine_shelf/" + type.name, p -> new WineShelfBlock(properties))
                .blockstate((ctx, prov) -> {

                    prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/wine_shelf"))
                            .texture("0", type.plankTexture)
                            .texture("2", TFGCore.id("block/wood/sheet/" + type.name))
                            .texture("3", type.strippedLogTexture);

                    var dynamicModel = prov.models().getBuilder("wood/wine_shelf/" + type.name + "_dynamic")
                            .customLoader((t, existing) -> FirmaCustomLoader.get(ResourceLocation.fromNamespaceAndPath("firmalife", "wine_shelf"), TFGCore.id("block/wood/wine_shelf/" + type.name), t,
                                    existing))
                            .end();

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), dynamicModel);

                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "wine_shelves")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/wine_shelf/" + type.name)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmalife", "wine_shelves"))).build()
                .register();

    }

    private static BlockEntry<StompingBarrelBlock> stompingBarrel(WoodType type) {
        var properties = ExtendedProperties.of().mapColor(type.registryWood.woodColor()).sound(SoundType.WOOD).noOcclusion().strength(4f)
                .pushReaction(PushReaction.BLOCK).flammableLikeLogs().blockEntity(FLBlockEntities.STOMPING_BARREL);

        return TFGCore.REGISTRATE.block("wood/stomping_barrel/" + type.name, p -> new StompingBarrelBlock(properties))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/stomping_barrel"))
                                .texture("0", TFGCore.id("block/wood/sheet/" + type.name))))
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "stomping_barrels")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(FLBlockEntities.STOMPING_BARREL, block);
                })
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmalife", "stomping_barrels"))).build()
                .register();

    }

    private static BlockEntry<BarrelPressBlock> barrelPress(WoodType type) {
        var properties = ExtendedProperties.of().mapColor(type.registryWood.woodColor()).sound(SoundType.WOOD).noOcclusion()
                .strength(4f).pushReaction(PushReaction.BLOCK).flammableLikeLogs().blockEntity(FLBlockEntities.BARREL_PRESS).ticks(BarrelPressBlockEntity::tick);

        return TFGCore.REGISTRATE.block("wood/barrel_press/" + type.name, p -> new BarrelPressBlock(properties))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/barrel_press"))
                                .texture("0", TFGCore.id("block/wood/sheet/" + type.name))))
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "barrel_presses")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(FLBlockEntities.BARREL_PRESS, block);
                })
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmalife", "barrel_presses"))).build()
                .register();

    }

}
