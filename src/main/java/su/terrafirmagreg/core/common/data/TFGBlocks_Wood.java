package su.terrafirmagreg.core.common.data;

import com.tterrag.registrate.util.entry.BlockEntry;
import dev.architectury.platform.Mod;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.BarrelBlockItem;
import net.dries007.tfc.common.items.ChestBlockItem;
import net.dries007.tfc.util.registry.RegistryWood;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.utils.ModelUtils;

import java.util.Map;
import java.util.function.Supplier;

public class TFGBlocks_Wood {

    public enum WoodType {
        GLACIAN("glacian", ResourceLocation.fromNamespaceAndPath("ad_astra","glacian_planks"), ResourceLocation.fromNamespaceAndPath("ad_astra", "glacian_log"), MapColor.NONE),
        STROPHAR("strophar", ResourceLocation.fromNamespaceAndPath("ad_astra","strophar_planks"), ResourceLocation.fromNamespaceAndPath("ad_astra", "glacian_log"), MapColor.NONE),
        AERONOS("aeronos", ResourceLocation.fromNamespaceAndPath("ad_astra","aeronos_planks"), ResourceLocation.fromNamespaceAndPath("ad_astra", "glacian_log"), MapColor.NONE),
        GINKGO("ginkgo", ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts","ginkgo_planks"), ResourceLocation.fromNamespaceAndPath("wan_ancient_beasts","ginkgo_log"), MapColor.NONE);
        public final String name;
        public final ResourceLocation plankBlock;
        public final ResourceLocation logBlock;
        public final RegistryWood registryWood;
        WoodType(String name, ResourceLocation plankBlock, ResourceLocation logBlock, MapColor col) {
            this.name = name;
            this.plankBlock = plankBlock;
            this.logBlock = logBlock;
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
                    return "";
                }
            };
        }
    }

    public static final Map<WoodType, Map<Wood.BlockType, BlockEntry<? extends Block>>> WOOD_BLOCKS = new Object2ObjectOpenHashMap<>();

    public static void init() {
        for (WoodType value : WoodType.values()) {
            registerBlocks(value);
        }
    }

    private static void registerBlocks(WoodType woodType) {
        var blocks = WOOD_BLOCKS.computeIfAbsent(woodType, new Object2ObjectOpenHashMap<>());
        
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

    }

    private static BlockEntry<Block> toolRack(WoodType woodType) {
        var toolRackBlock = Wood.BlockType.TOOL_RACK.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/tool_rack/" + woodType.name(), p -> toolRackBlock)
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/tool_rack"))
                            .texture("texture", woodType.plankBlock)
                            .texture("particle", woodType.plankBlock);

                    ModelUtils.cardinalBlockInverted(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .simpleItem()
                .register();
    }

    private static BlockEntry<Block> workbench(WoodType woodType) {
        var workbenchBlock = Wood.BlockType.WORKBENCH.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/workbench/" + woodType.name(), p -> workbenchBlock)
                .blockstate((ctx, prov) -> {
                    ResourceLocation path = TFGCore.id("wood/workbench/" + woodType.name());
                    prov.simpleBlock(ctx.getEntry(), prov.models().cube(ctx.getName(), woodType.plankBlock, path.withSuffix("_up"), path.withSuffix("_front"),
                            path.withSuffix("_side"), path.withSuffix("_side"), path.withSuffix("_front")));
                })
                .simpleItem()
                .register();
    }

    private static BlockEntry<Block> chest(WoodType woodType) {
        var chestBlock = Wood.BlockType.CHEST.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/chest/" + woodType.name(), p -> chestBlock)
                .item((b, i) -> new ChestBlockItem(b, i, woodType.registryWood)).build()
                .register();
    }

    private static BlockEntry<Block> trappedChest(WoodType woodType) {
        var trappedChestBlock = Wood.BlockType.TRAPPED_CHEST.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/trapped_chest/" + woodType.name(), p -> trappedChestBlock)
                .item((b, i) -> new ChestBlockItem(b, i, woodType.registryWood)).build()
                .register();
    }

    private static BlockEntry<Block> loom(WoodType woodType) {
        var loomBlock = Wood.BlockType.LOOM.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/loom/" + woodType.name(), p -> loomBlock)
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/loom"))
                            .texture("texture", woodType.plankBlock)
                            .texture("particle", woodType.plankBlock);

                    ModelUtils.cardinalBlockInverted(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .simpleItem()
                .register();

    }

    private static BlockEntry<Block> sluice(WoodType woodType) {
        var sluiceBlock = Wood.BlockType.SLUICE.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/sluice/" + woodType.name(), p -> sluiceBlock)
                .blockstate((ctx, prov) -> {

                    ModelFile sluiceUpper = prov.models().withExistingParent("wood/sluice/" + woodType.name + "_upper", ResourceLocation.fromNamespaceAndPath("tfc", "block/sluice_upper"))
                            .texture("texture", TFGCore.id("block/wood/sheet/" + woodType.name));
                    ModelFile sluiceLower = prov.models().withExistingParent("wood/sluice/" + woodType.name + "_lower", ResourceLocation.fromNamespaceAndPath("tfc", "block/sluice_lower"))
                            .texture("texture", TFGCore.id("block/wood/sheet/" + woodType.name));

                    prov.getVariantBuilder(ctx.getEntry())
                            .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH).with(TFCBlockStateProperties.UPPER, true)
                            .modelForState().modelFile(sluiceUpper).addModel()
                            .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH).with(TFCBlockStateProperties.UPPER, true)
                            .modelForState().modelFile(sluiceUpper).rotationY(180).addModel()
                            .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST).with(TFCBlockStateProperties.UPPER, true)
                            .modelForState().modelFile(sluiceUpper).rotationY(270).addModel()
                            .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST).with(TFCBlockStateProperties.UPPER, true)
                            .modelForState().modelFile(sluiceUpper).rotationY(90).addModel()
                            .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH).with(TFCBlockStateProperties.UPPER, false)
                            .modelForState().modelFile(sluiceLower).addModel()
                            .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH).with(TFCBlockStateProperties.UPPER, false)
                            .modelForState().modelFile(sluiceLower).rotationY(180).addModel()
                            .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST).with(TFCBlockStateProperties.UPPER, false)
                            .modelForState().modelFile(sluiceLower).rotationY(270).addModel()
                            .partialState().with(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST).with(TFCBlockStateProperties.UPPER, false)
                            .modelForState().modelFile(sluiceLower).rotationY(90).addModel();
                })
                .simpleItem()
                .register();

    }

    private static BlockEntry<Block> barrel(WoodType woodType) {
        var barrelBlock = Wood.BlockType.BARREL.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/barrel/" + woodType.name(), p -> barrelBlock)
                .blockstate((ctx, prov) -> {

                    ModelFile barrel = prov.models().withExistingParent("wood/barrel/" + woodType.name, ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel"))
                            .texture("particle", woodType.plankBlock)
                            .texture("planks", woodType.plankBlock)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));

                    ModelFile barrelSide = prov.models().withExistingParent("wood/barrel/" + woodType.name + "_side", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side"))
                            .texture("particle", woodType.plankBlock)
                            .texture("planks", woodType.plankBlock)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));

                    ModelFile barrelSideRack = prov.models().withExistingParent("wood/barrel/" + woodType.name + "_side_rack", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side_rack"))
                            .texture("particle", woodType.plankBlock)
                            .texture("planks", woodType.plankBlock)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));


                    ModelFile sealedBarrel = prov.models().withExistingParent("wood/barrel_sealed/" + woodType.name, ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_sealed"))
                            .texture("particle", woodType.plankBlock)
                            .texture("planks", woodType.plankBlock)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));

                    ModelFile sealedBarrelSide = prov.models().withExistingParent("wood/barrel_sealed/" + woodType.name + "_side", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side_sealed"))
                            .texture("particle", woodType.plankBlock)
                            .texture("planks", woodType.plankBlock)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));

                    ModelFile sealedBarrelSideRack = prov.models().withExistingParent("wood/barrel_sealed/" + woodType.name + "_side_rack", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side_sealed_rack"))
                            .texture("particle", woodType.plankBlock)
                            .texture("planks", woodType.plankBlock)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + woodType.name));

                    var builder = prov.getVariantBuilder(ctx.getEntry());
                    buildBarrelBlockStateEntry(builder, Direction.UP, 0, barrel, barrel, sealedBarrel, sealedBarrel);
                    buildBarrelBlockStateEntry(builder, Direction.EAST, 0, barrelSide, barrelSideRack, sealedBarrelSide, sealedBarrelSideRack);
                    buildBarrelBlockStateEntry(builder, Direction.WEST, 180, barrelSide, barrelSideRack, sealedBarrelSide, sealedBarrelSideRack);
                    buildBarrelBlockStateEntry(builder, Direction.SOUTH, 90, barrelSide, barrelSideRack, sealedBarrelSide, sealedBarrelSideRack);
                    buildBarrelBlockStateEntry(builder, Direction.NORTH, 270, barrelSide, barrelSideRack, sealedBarrelSide, sealedBarrelSideRack);
                })
                .item(BarrelBlockItem::new).build()
                .register();
    }

    private static void buildBarrelBlockStateEntry(VariantBlockStateBuilder builder, Direction facing, int y, ModelFile barrel, ModelFile rack, ModelFile sealed, ModelFile sealedRack) {
        builder.partialState().with(BlockStateProperties.FACING, facing).with(TFCBlockStateProperties.SEALED, false).with(TFCBlockStateProperties.RACK, false).modelForState().rotationY(y).modelFile(barrel).addModel()
                .partialState().with(BlockStateProperties.FACING, facing).with(TFCBlockStateProperties.SEALED, true).with(TFCBlockStateProperties.RACK, false).modelForState().rotationY(y).modelFile(sealed).addModel()
                .partialState().with(BlockStateProperties.FACING, facing).with(TFCBlockStateProperties.SEALED, false).with(TFCBlockStateProperties.RACK, true).modelForState().rotationY(y).modelFile(rack).addModel()
                .partialState().with(BlockStateProperties.FACING, facing).with(TFCBlockStateProperties.SEALED, true).with(TFCBlockStateProperties.RACK, true).modelForState().rotationY(y).modelFile(sealedRack).addModel();
    }

    private static BlockEntry<Block> lectern(WoodType woodType) {
        var lecternBlock = Wood.BlockType.LECTERN.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/lectern/" + woodType.name(), p -> lecternBlock)
                .blockstate((ctx, prov) -> {

                    var path = "block/wood/lectern/" + woodType.name + "/";
                    ModelFile model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("block/lectern"))
                            .texture("bottom", woodType.plankBlock)
                            .texture("base", TFGCore.id(path + "base"))
                            .texture("front", TFGCore.id(path + "front"))
                            .texture("sides", TFGCore.id(path + "sides"))
                            .texture("top", TFGCore.id(path + "top"))
                            .texture("particle", TFGCore.id(path + "sides"));

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .simpleItem()
                .register();

    }

    private static BlockEntry<Block> scribingTable(WoodType woodType) {
        var scribingTableBlock = Wood.BlockType.SCRIBING_TABLE.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/scribing_table/" + woodType.name(), p -> scribingTableBlock)
                .blockstate((ctx, prov) -> {
                    var model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/scribing_table"))
                            .texture("top", TFGCore.id("wood/scribing_table/" + woodType.name()))
                            .texture("leg", woodType.logBlock)
                            .texture("side", woodType.plankBlock)
                            .texture("misc", ResourceLocation.fromNamespaceAndPath("tfc", "block/wood/scribing_table/scribing_paraphernalia"))
                            .texture("particle", woodType.plankBlock);

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .simpleItem()
                .register();

    }

    private static BlockEntry<Block> sewingTable(WoodType woodType) {
        var sewingTableBlock = Wood.BlockType.SEWING_TABLE.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/sewing_table/" + woodType.name(), p -> sewingTableBlock)
                .blockstate((ctx, prov) -> {
                    var model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/sewing_table"))
                            .texture("0", woodType.logBlock)
                            .texture("1", woodType.plankBlock);

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .simpleItem()
                .register();
    }

    private static BlockEntry<Block> jarShelf(WoodType woodType) {
        var jarShelfBlock = Wood.BlockType.JAR_SHELF.create(woodType.registryWood).get();
        return TFGCore.REGISTRATE.block("wood/jar_shelf/" + woodType.name(), p -> jarShelfBlock)
                .blockstate((ctx, prov) -> {
                    var model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/jar_shelf"))
                            .texture("0", woodType.plankBlock);

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .simpleItem().register();

    }

}
