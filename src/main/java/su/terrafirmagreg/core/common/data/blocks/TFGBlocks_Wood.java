package su.terrafirmagreg.core.common.data.blocks;

import static com.eerussianguy.firmalife.common.blocks.FLBlocks.*;

import java.util.Map;

import com.eerussianguy.beneath.common.blocks.Stem;
import com.eerussianguy.firmalife.common.blockentities.BarrelPressBlockEntity;
import com.eerussianguy.firmalife.common.blockentities.FLBlockEntities;
import com.eerussianguy.firmalife.common.blocks.*;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.common.block.GTCeilingHangingSignBlock;
import com.gregtechceu.gtceu.common.block.GTStandingSignBlock;
import com.gregtechceu.gtceu.common.block.GTWallHangingSignBlock;
import com.gregtechceu.gtceu.common.block.GTWallSignBlock;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.therighthon.afc.common.blocks.AFCWood;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.devices.BarrelBlock;
import net.dries007.tfc.common.blocks.wood.LogBlock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.BarrelBlockItem;
import net.dries007.tfc.common.items.ChestBlockItem;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
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
import tfar.craftingstation.CraftingStationBlock;
import tfar.craftingstation.CraftingStationSlabBlock;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.TFGWood;
import su.terrafirmagreg.core.utils.ModelUtils;

public class TFGBlocks_Wood {
    public static final Map<TFGWood, Map<Wood.BlockType, BlockEntry<? extends Block>>> WOODS = new Object2ObjectOpenHashMap<>();

    public static final Map<TFGWood, BlockEntry<? extends Block>> SIGN = new Object2ObjectOpenHashMap<>();
    public static final Map<TFGWood, BlockEntry<? extends Block>> WALL_SIGN = new Object2ObjectOpenHashMap<>();
    public static final Map<TFGWood, BlockEntry<? extends Block>> HANGING_SIGN = new Object2ObjectOpenHashMap<>();
    public static final Map<TFGWood, BlockEntry<? extends Block>> HANGING_WALL_SIGN = new Object2ObjectOpenHashMap<>();
    public static final Map<TFGWood, BlockEntry<? extends Block>> FOOD_SHELVES = new Object2ObjectOpenHashMap<>();
    public static final Map<TFGWood, BlockEntry<? extends Block>> HANGERS = new Object2ObjectOpenHashMap<>();
    public static final Map<TFGWood, BlockEntry<? extends Block>> JARBNETS = new Object2ObjectOpenHashMap<>();
    public static final Map<TFGWood, BlockEntry<? extends Block>> BIG_BARRELS = new Object2ObjectOpenHashMap<>();
    public static final Map<TFGWood, BlockEntry<? extends Block>> WINE_SHELVES = new Object2ObjectOpenHashMap<>();
    public static final Map<TFGWood, BlockEntry<? extends Block>> STOMPING_BARRELS = new Object2ObjectOpenHashMap<>();
    public static final Map<TFGWood, BlockEntry<? extends Block>> BARREL_PRESSES = new Object2ObjectOpenHashMap<>();
    public static final Map<String, BlockEntry<? extends Block>> CRAFTING_STATIONS = new Object2ObjectOpenHashMap<>();
    public static final Map<String, BlockEntry<? extends Block>> SLAB_CRAFTING_STATIONS = new Object2ObjectOpenHashMap<>();

    public static void init() {
        TFGWood.registerBlockSetTypes();
        for (TFGWood value : TFGWood.VALUES) {
            registerWood(value);
            if (!value.serializedName.equals("ginkgo")) {
                // Do not touch!
                BlockEntry<? extends Block> wallSign = wallSign(value);
                SIGN.put(value, sign(value, wallSign));
                WALL_SIGN.put(value, wallSign);
                BlockEntry<? extends Block> wallHangingSign = wallHangingSign(value);
                HANGING_SIGN.put(value, hangingSign(value, wallHangingSign));
                HANGING_WALL_SIGN.put(value, wallHangingSign);
            }
            FOOD_SHELVES.put(value, foodShelf(value));
            HANGERS.put(value, hanger(value));
            JARBNETS.put(value, jarbnet(value));
            BIG_BARRELS.put(value, bigBarrel(value));
            WINE_SHELVES.put(value, wineShelf(value));
            STOMPING_BARRELS.put(value, stompingBarrel(value));
            BARREL_PRESSES.put(value, barrelPress(value));
            CRAFTING_STATIONS.put(value.serializedName, craftingStation(value.serializedName, value.plankTexture, value.woodColor()));
            SLAB_CRAFTING_STATIONS.put(value.serializedName, craftingStationSlab(value.serializedName, value.plankTexture, value.woodColor()));
        }

        for (AFCWood value : AFCWood.VALUES) {
            CRAFTING_STATIONS.put(value.getSerializedName(),
                    craftingStation(value.getSerializedName(), ResourceLocation.fromNamespaceAndPath("afc", "block/wood/planks/" + value.getSerializedName()), value.woodColor()));
            SLAB_CRAFTING_STATIONS.put(value.getSerializedName(),
                    craftingStationSlab(value.getSerializedName(), ResourceLocation.fromNamespaceAndPath("afc", "block/wood/planks/" + value.getSerializedName()), value.woodColor()));
        }

        for (Wood value : Wood.VALUES) {
            CRAFTING_STATIONS.put(value.getSerializedName(),
                    craftingStation(value.getSerializedName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/wood/planks/" + value.getSerializedName()), value.woodColor()));
            SLAB_CRAFTING_STATIONS.put(value.getSerializedName(),
                    craftingStationSlab(value.getSerializedName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/wood/planks/" + value.getSerializedName()), value.woodColor()));
        }

        for (Stem value : Stem.VALUES) {
            CRAFTING_STATIONS.put(value.getSerializedName(),
                    craftingStation(value.getSerializedName(), ResourceLocation.fromNamespaceAndPath("beneath", "block/wood/planks/" + value.getSerializedName()), value.woodColor()));
            SLAB_CRAFTING_STATIONS.put(value.getSerializedName(),
                    craftingStationSlab(value.getSerializedName(), ResourceLocation.fromNamespaceAndPath("beneath", "block/wood/planks/" + value.getSerializedName()), value.woodColor()));
        }
    }

    private static void registerWood(TFGWood wood) {
        Map<Wood.BlockType, BlockEntry<? extends Block>> blocks = new Object2ObjectOpenHashMap<>();

        if (wood.generateWood) {
            blocks.put(Wood.BlockType.LOG, log(wood, blocks));
            blocks.put(Wood.BlockType.STRIPPED_LOG, stripped_log(wood));
            blocks.put(Wood.BlockType.WOOD, wood(wood, blocks));
            blocks.put(Wood.BlockType.STRIPPED_WOOD, stripped_wood(wood));
            blocks.put(Wood.BlockType.PLANKS, plank(wood));
            blocks.put(Wood.BlockType.DOOR, door(wood));
            blocks.put(Wood.BlockType.TRAPDOOR, trapdoor(wood));
            blocks.put(Wood.BlockType.FENCE, fence(wood));
            blocks.put(Wood.BlockType.FENCE_GATE, fenceGate(wood));
            blocks.put(Wood.BlockType.BUTTON, button(wood));
            blocks.put(Wood.BlockType.PRESSURE_PLATE, pressurePlate(wood));
            blocks.put(Wood.BlockType.SLAB, slab(wood));
            blocks.put(Wood.BlockType.STAIRS, stairs(wood));
            blocks.put(Wood.BlockType.TWIG, twig(wood));
        }

        blocks.put(Wood.BlockType.LOG_FENCE, logFence(wood));
        blocks.put(Wood.BlockType.BOOKSHELF, bookshelf(wood));
        blocks.put(Wood.BlockType.TOOL_RACK, toolRack(wood));
        blocks.put(Wood.BlockType.WORKBENCH, workbench(wood));
        blocks.put(Wood.BlockType.TRAPPED_CHEST, trappedChest(wood));
        blocks.put(Wood.BlockType.CHEST, chest(wood));
        blocks.put(Wood.BlockType.LOOM, loom(wood));
        blocks.put(Wood.BlockType.SLUICE, sluice(wood));
        blocks.put(Wood.BlockType.BARREL, barrel(wood));
        blocks.put(Wood.BlockType.LECTERN, lectern(wood));
        blocks.put(Wood.BlockType.SCRIBING_TABLE, scribingTable(wood));
        blocks.put(Wood.BlockType.SEWING_TABLE, sewingTable(wood));
        blocks.put(Wood.BlockType.JAR_SHELF, jarShelf(wood));

        WOODS.put(wood, blocks);
    }

    private static BlockEntry<LogBlock> log(TFGWood wood, Map<Wood.BlockType, BlockEntry<? extends Block>> blocks) {
        var properties = ExtendedProperties.of(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? wood.woodColor() : wood.barkColor()).strength(8f).sound(SoundType.WOOD)
                .instrument(NoteBlockInstrument.BASS).requiresCorrectToolForDrops().flammableLikeLogs();

        return TFGCore.REGISTRATE.block("wood/log/" + wood.serializedName, p -> new LogBlock(properties, blocks.get(Wood.BlockType.STRIPPED_LOG)))
                .blockstate((ctx, prov) -> {
                    prov.axisBlock(ctx.getEntry(), TFGCore.id("block/wood/log/" + wood.serializedName), TFGCore.id("block/wood/log_top/" + wood.serializedName));
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("logs")))
                .tag(TagKey.create(Registries.BLOCK, TFGCore.id(wood.serializedName + "_logs")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                        TFGCore.id("item/wood/log/" + wood.serializedName)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("logs")))
                .tag(TagKey.create(Registries.ITEM, TFGCore.id(wood.serializedName + "_logs"))).build()
                .register();
    }

    private static BlockEntry<LogBlock> stripped_log(TFGWood wood) {
        var properties = ExtendedProperties.of(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? wood.woodColor() : wood.barkColor()).strength(7.5f).sound(SoundType.WOOD)
                .instrument(NoteBlockInstrument.BASS).requiresCorrectToolForDrops().flammableLikeLogs();
        return TFGCore.REGISTRATE.block("wood/stripped_log/" + wood.serializedName, p -> new LogBlock(properties, null))
                .blockstate((ctx, prov) -> {
                    prov.axisBlock(ctx.getEntry(), TFGCore.id("block/wood/stripped_log/" + wood.serializedName), TFGCore.id("block/wood/stripped_log_top/" + wood.serializedName));
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("logs")))
                .tag(TagKey.create(Registries.BLOCK, TFGCore.id(wood.serializedName + "_logs")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                        TFGCore.id("item/wood/stripped_log/" + wood.serializedName)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("logs")))
                .tag(TagKey.create(Registries.ITEM, TFGCore.id(wood.serializedName + "_logs"))).build()
                .register();
    }

    private static BlockEntry<LogBlock> wood(TFGWood wood, Map<Wood.BlockType, BlockEntry<? extends Block>> blocks) {
        var properties = ExtendedProperties.of(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? wood.woodColor() : wood.barkColor()).strength(8f).sound(SoundType.WOOD)
                .instrument(NoteBlockInstrument.BASS).requiresCorrectToolForDrops().flammableLikeLogs();

        return TFGCore.REGISTRATE.block("wood/wood/" + wood.serializedName, p -> new LogBlock(properties, blocks.get(Wood.BlockType.STRIPPED_LOG)))
                .blockstate((ctx, prov) -> {
                    prov.axisBlock(ctx.getEntry(), TFGCore.id("block/wood/log/" + wood.serializedName), TFGCore.id("block/wood/log/" + wood.serializedName));
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("logs")))
                .tag(TagKey.create(Registries.BLOCK, TFGCore.id(wood.serializedName + "_logs")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                        TFGCore.id("item/wood/wood/" + wood.serializedName)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("logs")))
                .tag(TagKey.create(Registries.ITEM, TFGCore.id(wood.serializedName + "_logs"))).build()
                .register();
    }

    private static BlockEntry<LogBlock> stripped_wood(TFGWood wood) {
        var properties = ExtendedProperties.of().mapColor(wood.woodColor).strength(7.5f).sound(SoundType.WOOD).requiresCorrectToolForDrops().flammableLikeLogs();
        return TFGCore.REGISTRATE.block("wood/stripped_wood/" + wood.serializedName, p -> new LogBlock(properties, null))
                .blockstate((ctx, prov) -> {
                    prov.axisBlock(ctx.getEntry(), TFGCore.id("block/wood/stripped_log/" + wood.serializedName), TFGCore.id("block/wood/stripped_log/" + wood.serializedName));
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("logs")))
                .tag(TagKey.create(Registries.BLOCK, TFGCore.id(wood.serializedName + "_logs")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                        TFGCore.id("item/wood/stripped_wood/" + wood.serializedName)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("logs")))
                .tag(TagKey.create(Registries.ITEM, TFGCore.id(wood.serializedName + "_logs"))).build()
                .register();
    }

    private static BlockEntry<Block> plank(TFGWood wood) {
        var plankBlock = Wood.BlockType.PLANKS.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/planks/" + wood.serializedName, p -> plankBlock)
                .blockstate((ctx, prov) -> {
                    prov.simpleBlock(ctx.getEntry());
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("planks")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("planks"))).build()
                .register();
    }

    private static BlockEntry<Block> door(TFGWood wood) {
        var doorBlock = Wood.BlockType.DOOR.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/door/" + wood.serializedName, p -> doorBlock)
                .blockstate((ctx, prov) -> {
                    prov.doorBlock((DoorBlock) ctx.getEntry(), TFGCore.id("block/wood/door/" + wood.serializedName + "_bottom"), TFGCore.id("block/wood/door/" + wood.serializedName + "_top"));
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("doors")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                        TFGCore.id("item/wood/door/" + wood.serializedName)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("doors"))).build()
                .register();
    }

    private static BlockEntry<Block> trapdoor(TFGWood wood) {
        var trapdoorBlock = Wood.BlockType.TRAPDOOR.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/trapdoor/" + wood.serializedName, p -> trapdoorBlock)
                .blockstate((ctx, prov) -> {
                    prov.trapdoorBlock((TrapDoorBlock) ctx.getEntry(), TFGCore.id("block/wood/trapdoor/" + wood.serializedName), true);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("trapdoors")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/trapdoor/" + wood.serializedName + "_bottom")))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("trapdoors"))).build()
                .register();
    }

    private static BlockEntry<Block> fence(TFGWood wood) {
        var fenceBlock = Wood.BlockType.FENCE.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/fence/" + wood.serializedName, p -> fenceBlock)
                .blockstate((ctx, prov) -> {
                    prov.models().withExistingParent(ctx.getName() + "_inventory", ResourceLocation.withDefaultNamespace("block/fence_inventory"))
                            .texture("texture", TFGCore.id("block/wood/planks/" + wood.serializedName));

                    prov.fenceBlock((FenceBlock) ctx.getEntry(), TFGCore.id("block/wood/planks/" + wood.serializedName));
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("fences")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/fence/" + wood.serializedName + "_inventory")))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("fences"))).build()
                .register();
    }

    private static BlockEntry<Block> logFence(TFGWood wood) {
        var logFenceBlock = Wood.BlockType.LOG_FENCE.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/log_fence/" + wood.serializedName, p -> logFenceBlock)
                .blockstate((ctx, prov) -> {
                    prov.models().withExistingParent(ctx.getName() + "_inventory", ResourceLocation.fromNamespaceAndPath("tfc", "block/log_fence_inventory"))
                            .texture("log", wood.logTexture)
                            .texture("planks", wood.plankTexture);

                    ModelFile modelSide = prov.models().withExistingParent(ctx.getName() + "_side", ResourceLocation.withDefaultNamespace("block/fence_side"))
                            .texture("texture", wood.plankTexture);

                    ModelFile modelPost = prov.models().withExistingParent(ctx.getName() + "_post", ResourceLocation.withDefaultNamespace("block/fence_post"))
                            .texture("texture", wood.logTexture);

                    prov.getMultipartBuilder(ctx.getEntry()).part().modelFile(modelPost).addModel().end()
                            .part().modelFile(modelSide).uvLock(true).addModel().condition(BlockStateProperties.NORTH, true).end()
                            .part().modelFile(modelSide).rotationY(90).uvLock(true).addModel().condition(BlockStateProperties.EAST, true).end()
                            .part().modelFile(modelSide).rotationY(180).uvLock(true).addModel().condition(BlockStateProperties.SOUTH, true).end()
                            .part().modelFile(modelSide).rotationY(270).uvLock(true).addModel().condition(BlockStateProperties.WEST, true).end();
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("fences")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/log_fence/" + wood.serializedName + "_inventory")))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("fences"))).build()
                .register();
    }

    private static BlockEntry<Block> fenceGate(TFGWood wood) {
        var fenceGateBlock = Wood.BlockType.FENCE_GATE.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/fence_gate/" + wood.serializedName, p -> fenceGateBlock)
                .blockstate((ctx, prov) -> {
                    prov.fenceGateBlock((FenceGateBlock) ctx.getEntry(), TFGCore.id("block/wood/planks/" + wood.serializedName));
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("fence_gates")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("fence_gates"))).build()
                .register();
    }

    private static BlockEntry<Block> button(TFGWood wood) {
        var buttonBlock = Wood.BlockType.BUTTON.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/button/" + wood.serializedName, p -> buttonBlock)
                .blockstate((ctx, prov) -> {
                    prov.models().withExistingParent(ctx.getName() + "_inventory", ResourceLocation.withDefaultNamespace("block/button_inventory"))
                            .texture("texture", TFGCore.id("block/wood/planks/" + wood.serializedName));

                    prov.buttonBlock((ButtonBlock) ctx.getEntry(), TFGCore.id("block/wood/planks/" + wood.serializedName));
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("buttons")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/button/" + wood.serializedName + "_inventory")))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("buttons"))).build()
                .register();
    }

    private static BlockEntry<Block> pressurePlate(TFGWood wood) {
        var pressurePlateBlock = Wood.BlockType.PRESSURE_PLATE.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/pressure_plate/" + wood.serializedName, p -> pressurePlateBlock)
                .blockstate((ctx, prov) -> {
                    prov.pressurePlateBlock((PressurePlateBlock) ctx.getEntry(), TFGCore.id("block/wood/planks/" + wood.serializedName));
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("pressure_plates")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("pressure_plates"))).build()
                .register();
    }

    private static BlockEntry<Block> slab(TFGWood wood) {
        var slabBlock = Wood.BlockType.SLAB.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/slab/" + wood.serializedName, p -> slabBlock)
                .blockstate((ctx, prov) -> {
                    ModelFile modelTop = prov.models().withExistingParent(ctx.getName() + "_top", ResourceLocation.withDefaultNamespace("block/slab_top"))
                            .texture("top", wood.plankTexture)
                            .texture("bottom", wood.plankTexture)
                            .texture("side", wood.plankTexture);

                    ModelFile modelBottom = prov.models().withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("block/slab"))
                            .texture("top", wood.plankTexture)
                            .texture("bottom", wood.plankTexture)
                            .texture("side", wood.plankTexture);

                    ModelFile modelDouble = prov.models().getExistingFile(TFGCore.id("block/wood/planks/" + wood.serializedName));

                    prov.slabBlock((SlabBlock) ctx.getEntry(), modelBottom, modelTop, modelDouble);
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("slabs")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("slabs"))).build()
                .register();
    }

    private static BlockEntry<Block> stairs(TFGWood wood) {
        var stairsBlock = Wood.BlockType.STAIRS.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/stairs/" + wood.serializedName, p -> stairsBlock)
                .blockstate((ctx, prov) -> {
                    prov.stairsBlock((StairBlock) ctx.getEntry(), wood.plankTexture);
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("stairs")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("stairs"))).build()
                .register();
    }

    private static BlockEntry<Block> twig(TFGWood wood) {
        var twigBlock = Wood.BlockType.TWIG.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/twig/" + wood.serializedName, p -> twigBlock)
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/groundcover/twig"))
                            .texture("side", wood.logTexture)
                            .texture("top", TFGCore.id("block/wood/log_top/" + wood.serializedName));

                    ModelUtils.blockVariantsRotated(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "rods/wooden")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item()
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/generated")).texture("layer0",
                        TFGCore.id("item/wood/twig/" + wood.serializedName)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", "rods/wooden"))).build()
                .register();
    }

    private static BlockEntry<Block> toolRack(TFGWood wood) {
        var toolRackBlock = Wood.BlockType.TOOL_RACK.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/tool_rack/" + wood.serializedName, p -> toolRackBlock)
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/tool_rack"))
                            .texture("texture", wood.plankTexture)
                            .texture("particle", wood.plankTexture);

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

    private static BlockEntry<Block> workbench(TFGWood wood) {
        var workbenchBlock = Wood.BlockType.WORKBENCH.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/workbench/" + wood.serializedName, p -> workbenchBlock)
                .blockstate((ctx, prov) -> {
                    ResourceLocation path = TFGCore.id("block/wood/workbench/" + wood.serializedName);
                    prov.simpleBlock(ctx.getEntry(), prov.models().cube(ctx.getName(), wood.plankTexture, path.withSuffix("_top"), path.withSuffix("_front"),
                            path.withSuffix("_side"), path.withSuffix("_side"), path.withSuffix("_front"))
                            .texture("particle", path.withSuffix("_front")));
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "workbenches")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "workbenches"))).build()
                .register();
    }

    private static BlockEntry<Block> chest(TFGWood wood) {
        var chestBlock = Wood.BlockType.CHEST.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/chest/" + wood.serializedName, p -> chestBlock)
                .blockstate((ctx, prov) -> {
                    prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(ctx.getName()).texture("particle", wood.plankTexture));
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "chests")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFCBlockEntities.CHEST, block);
                })
                .item((b, i) -> new ChestBlockItem(b, i, wood))
                .model((ctx, prov) -> {
                    prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/chest"))
                            .texture("particle", wood.plankTexture);
                })
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", "chests"))).build()
                .register();
    }

    private static BlockEntry<Block> trappedChest(TFGWood wood) {
        var trappedChestBlock = Wood.BlockType.TRAPPED_CHEST.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/trapped_chest/" + wood.serializedName, p -> trappedChestBlock)
                .blockstate((ctx, prov) -> {
                    prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(ctx.getName()).texture("particle", wood.plankTexture));
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "chests")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(TFCBlockEntities.TRAPPED_CHEST, block);
                })
                .item((b, i) -> new ChestBlockItem(b, i, wood))
                .model((ctx, prov) -> {
                    prov.withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("item/chest"))
                            .texture("particle", wood.plankTexture);
                })
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", "chests"))).build()
                .register();
    }

    private static BlockEntry<Block> loom(TFGWood wood) {
        var loomBlock = Wood.BlockType.LOOM.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/loom/" + wood.serializedName, p -> loomBlock)
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/loom"))
                            .texture("texture", wood.plankTexture)
                            .texture("particle", wood.plankTexture);

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

    private static BlockEntry<Block> sluice(TFGWood wood) {
        var sluiceBlock = Wood.BlockType.SLUICE.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/sluice/" + wood.serializedName, p -> sluiceBlock)
                .blockstate((ctx, prov) -> {

                    ModelFile sluiceUpper = prov.models().withExistingParent("wood/sluice/" + wood.serializedName + "_upper", ResourceLocation.fromNamespaceAndPath("tfc", "block/sluice_upper"))
                            .texture("texture", TFGCore.id("block/wood/sheet/" + wood.serializedName));
                    ModelFile sluiceLower = prov.models().withExistingParent("wood/sluice/" + wood.serializedName + "_lower", ResourceLocation.fromNamespaceAndPath("tfc", "block/sluice_lower"))
                            .texture("texture", TFGCore.id("block/wood/sheet/" + wood.serializedName));

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
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/sluice/" + wood.serializedName + "_lower")))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "sluices"))).build()
                .register();
    }

    // Signs use GT's way of doing it because TFC's just break.
    // Unless you know what you are doing, please do not touch the code!
    private static BlockEntry<GTStandingSignBlock> sign(TFGWood wood, BlockEntry<? extends Block> wallSign) {
        return TFGCore.REGISTRATE.block("wood/sign/" + wood.serializedName, (p) -> new GTStandingSignBlock(p, wood.getVanillaWoodType()))
                .initialProperties(() -> Blocks.SPRUCE_SIGN)
                .blockstate((ctx, prov) -> {
                    prov.signBlock(ctx.getEntry(), (WallSignBlock) wallSign.get(), wood.plankTexture);
                })
                .tag(BlockTags.STANDING_SIGNS, BlockTags.MINEABLE_WITH_AXE)
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(GTBlockEntities.GT_SIGN, block);
                })
                .item((ctx, prov) -> new SignItem(prov, ctx, wallSign.get()))
                .defaultModel()
                .tag(ItemTags.SIGNS)
                .build()
                .register();
    }

    private static BlockEntry<GTWallSignBlock> wallSign(TFGWood wood) {
        return TFGCore.REGISTRATE.block("wood/wall_sign/" + wood.serializedName, (p) -> new GTWallSignBlock(p, wood.getVanillaWoodType()))
                .initialProperties(() -> Blocks.SPRUCE_WALL_SIGN)
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .tag(BlockTags.WALL_SIGNS, BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(GTBlockEntities.GT_SIGN, block);
                })
                .register();
    }

    private static BlockEntry<GTCeilingHangingSignBlock> hangingSign(TFGWood wood, BlockEntry<? extends Block> wallHangingSign) {
        return TFGCore.REGISTRATE.block("wood/hanging_sign/" + wood.serializedName, (p) -> new GTCeilingHangingSignBlock(p, wood.getVanillaWoodType()))
                .initialProperties(() -> Blocks.SPRUCE_HANGING_SIGN)
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().sign(ctx.getName(), wood.plankTexture);

                    prov.simpleBlock(ctx.getEntry(), model);
                })
                .tag(BlockTags.CEILING_HANGING_SIGNS, BlockTags.MINEABLE_WITH_AXE)
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(GTBlockEntities.GT_HANGING_SIGN, block);
                })
                .item((ctx, prov) -> new HangingSignItem(ctx, wallHangingSign.get(), prov))
                .defaultModel()
                .tag(ItemTags.HANGING_SIGNS)
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .build()
                .register();
    }

    private static BlockEntry<GTWallHangingSignBlock> wallHangingSign(TFGWood wood) {
        return TFGCore.REGISTRATE.block("wood/wall_hanging_sign/" + wood.serializedName, (p) -> new GTWallHangingSignBlock(p, wood.getVanillaWoodType()))
                .initialProperties(() -> Blocks.SPRUCE_WALL_HANGING_SIGN)
                .blockstate((ctx, prov) -> {
                    ModelFile model = prov.models().sign(ctx.getName(), wood.plankTexture);

                    prov.simpleBlock(ctx.getEntry(), model);
                })
                .tag(BlockTags.WALL_HANGING_SIGNS, BlockTags.MINEABLE_WITH_AXE)
                .onRegister(block -> {
                    TFGBlockEntities.addValidBEBlock(GTBlockEntities.GT_HANGING_SIGN, block);
                })
                .register();
    }

    private static BlockEntry<BarrelBlock> barrel(TFGWood wood) {
        var barrelBlock = Wood.BlockType.BARREL.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/barrel/" + wood.serializedName, p -> (BarrelBlock) barrelBlock)
                .blockstate((ctx, prov) -> {

                    ModelFile barrel = prov.models().withExistingParent("wood/barrel/" + wood.serializedName, ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel"))
                            .texture("particle", wood.plankTexture)
                            .texture("planks", wood.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + wood.serializedName));

                    ModelFile barrelSide = prov.models().withExistingParent("wood/barrel/" + wood.serializedName + "_side", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side"))
                            .texture("particle", wood.plankTexture)
                            .texture("planks", wood.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + wood.serializedName));

                    ModelFile barrelSideRack = prov.models()
                            .withExistingParent("wood/barrel/" + wood.serializedName + "_side_rack", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side_rack"))
                            .texture("particle", wood.plankTexture)
                            .texture("planks", wood.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + wood.serializedName));

                    ModelFile sealedBarrel = prov.models().withExistingParent("wood/barrel_sealed/" + wood.serializedName, ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_sealed"))
                            .texture("particle", wood.plankTexture)
                            .texture("planks", wood.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + wood.serializedName));

                    ModelFile sealedBarrelSide = prov.models()
                            .withExistingParent("wood/barrel_sealed/" + wood.serializedName + "_side", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side_sealed"))
                            .texture("particle", wood.plankTexture)
                            .texture("planks", wood.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + wood.serializedName));

                    ModelFile sealedBarrelSideRack = prov.models()
                            .withExistingParent("wood/barrel_sealed/" + wood.serializedName + "_side_rack", ResourceLocation.fromNamespaceAndPath("tfc", "block/barrel_side_sealed_rack"))
                            .texture("particle", wood.plankTexture)
                            .texture("planks", wood.plankTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + wood.serializedName));

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

    private static BlockEntry<Block> lectern(TFGWood wood) {
        var lecternBlock = Wood.BlockType.LECTERN.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/lectern/" + wood.serializedName, p -> lecternBlock)
                .blockstate((ctx, prov) -> {

                    var path = "block/wood/lectern/" + wood.serializedName + "/";
                    ModelFile model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.withDefaultNamespace("block/lectern"))
                            .texture("bottom", wood.plankTexture)
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

    private static BlockEntry<Block> scribingTable(TFGWood wood) {
        var scribingTableBlock = Wood.BlockType.SCRIBING_TABLE.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/scribing_table/" + wood.serializedName, p -> scribingTableBlock)
                .blockstate((ctx, prov) -> {
                    var model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/scribing_table"))
                            .texture("top", TFGCore.id("block/wood/scribing_table/" + wood.serializedName))
                            .texture("leg", wood.logTexture)
                            .texture("side", wood.plankTexture)
                            .texture("misc", ResourceLocation.fromNamespaceAndPath("tfc", "block/wood/scribing_table/scribing_paraphernalia"))
                            .texture("particle", wood.plankTexture);

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "scribing_tables")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "scribing_tables"))).build()
                .register();

    }

    private static BlockEntry<Block> sewingTable(TFGWood wood) {
        var sewingTableBlock = Wood.BlockType.SEWING_TABLE.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/sewing_table/" + wood.serializedName, p -> sewingTableBlock)
                .blockstate((ctx, prov) -> {
                    var model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/sewing_table"))
                            .texture("0", wood.logTexture)
                            .texture("1", wood.plankTexture);

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "sewing_tables")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "sewing_tables"))).build()
                .register();
    }

    private static BlockEntry<Block> jarShelf(TFGWood wood) {
        var jarShelfBlock = Wood.BlockType.JAR_SHELF.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/jar_shelf/" + wood.serializedName, p -> jarShelfBlock)
                .blockstate((ctx, prov) -> {
                    var model = prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("tfc", "block/jar_shelf"))
                            .texture("0", wood.plankTexture);

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), model);
                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("tfc", "jar_shelves")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", "jar_shelves"))).build()
                .register();

    }

    private static BlockEntry<Block> bookshelf(TFGWood wood) {
        var bookshelfBlock = Wood.BlockType.BOOKSHELF.create(wood).get();
        return TFGCore.REGISTRATE.block("wood/bookshelf/" + wood.serializedName, p -> bookshelfBlock)
                .blockstate((ctx, prov) -> {
                    prov.models()
                            .withExistingParent("wood/bookshelf/" + wood.serializedName + "_inventory", ResourceLocation.withDefaultNamespace("block/chiseled_bookshelf_inventory"))
                            .texture("top", TFGCore.id("block/wood/bookshelf/" + wood.serializedName + "_top"))
                            .texture("side", TFGCore.id("block/wood/bookshelf/" + wood.serializedName + "_side"))
                            .texture("front", TFGCore.id("block/wood/bookshelf/" + wood.serializedName + "_empty"));

                    ModelFile base = prov.models()
                            .withExistingParent("wood/bookshelf/" + wood.serializedName, ResourceLocation.withDefaultNamespace("block/chiseled_bookshelf"))
                            .texture("top", TFGCore.id("block/wood/bookshelf/" + wood.serializedName + "_top"))
                            .texture("side", TFGCore.id("block/wood/bookshelf/" + wood.serializedName + "_side"));

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
                                        .texture("texture", TFGCore.id("block/wood/bookshelf/" + wood.serializedName + "_" + state));

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
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/bookshelf/" + wood.serializedName + "_inventory")))
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

    private static BlockEntry<FoodShelfBlock> foodShelf(TFGWood wood) {
        return TFGCore.REGISTRATE.block("wood/food_shelf/" + wood.serializedName, p -> new FoodShelfBlock(shelfProperties().mapColor(wood.woodColor())))
                .blockstate((ctx, prov) -> {
                    prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/food_shelf_base"))
                            .texture("wood", wood.plankTexture);

                    var dynamicModel = prov.models().getBuilder("wood/food_shelf/" + wood.serializedName + "_dynamic")
                            .customLoader((t, existing) -> FirmaCustomLoader.get(ResourceLocation.fromNamespaceAndPath("firmalife", "food_shelf"),
                                    TFGCore.id("block/wood/food_shelf/" + wood.serializedName), t,
                                    existing))
                            .end();

                    ModelUtils.cardinalBlockInverted(prov.getVariantBuilder(ctx.getEntry()), dynamicModel);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "food_shelves")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/food_shelf/" + wood.serializedName)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmalife", "food_shelves"))).build()
                .register();
    }

    private static BlockEntry<HangerBlock> hanger(TFGWood wood) {
        return TFGCore.REGISTRATE.block("wood/hanger/" + wood.serializedName, p -> new HangerBlock(hangerProperties().mapColor(wood.woodColor())))
                .blockstate((ctx, prov) -> {
                    prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/hanger_base"))
                            .texture("wood", wood.plankTexture)
                            .texture("string", ResourceLocation.withDefaultNamespace("block/white_wool"));

                    var dynamicModel = prov.models().getBuilder("wood/hanger/" + wood.serializedName + "_dynamic")
                            .customLoader(
                                    (t, existing) -> FirmaCustomLoader.get(ResourceLocation.fromNamespaceAndPath("firmalife", "hanger"), TFGCore.id("block/wood/hanger/" + wood.serializedName), t,
                                            existing))
                            .end();

                    prov.simpleBlock(ctx.getEntry(), dynamicModel);
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "hangers")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/hanger/" + wood.serializedName)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmalife", "hangers"))).build()
                .register();

    }

    private static BlockEntry<JarbnetBlock> jarbnet(TFGWood wood) {
        return TFGCore.REGISTRATE.block("wood/jarbnet/" + wood.serializedName, p -> new JarbnetBlock(jarbnetProperties().mapColor(wood.woodColor())))
                .blockstate((ctx, prov) -> {
                    prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/jarbnet"))
                            .texture("planks", wood.plankTexture)
                            .texture("log", wood.logTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + wood.serializedName));

                    prov.models().withExistingParent(ctx.getName() + "_shut", ResourceLocation.fromNamespaceAndPath("firmalife", "block/jarbnet_shut"))
                            .texture("planks", wood.plankTexture)
                            .texture("log", wood.logTexture)
                            .texture("sheet", TFGCore.id("block/wood/sheet/" + wood.serializedName));

                    var dynamicModel = prov.models().getBuilder("wood/jarbnet/" + wood.serializedName + "_dynamic")
                            .customLoader(
                                    (t, existing) -> FirmaCustomLoader.get(ResourceLocation.fromNamespaceAndPath("firmalife", "jarbnet"), TFGCore.id("block/wood/jarbnet/" + wood.serializedName), t,
                                            existing))
                            .end();

                    var dynamicModelShut = prov.models().getBuilder("wood/jarbnet/" + wood.serializedName + "_shut_dynamic")
                            .customLoader((t, existing) -> FirmaCustomLoader.get(ResourceLocation.fromNamespaceAndPath("firmalife", "jarbnet"),
                                    TFGCore.id("block/wood/jarbnet/" + wood.serializedName + "_shut"),
                                    t, existing))
                            .end();

                    var builder = prov.getVariantBuilder(ctx.getEntry());

                    ModelUtils.forEachCardinalDirection(builder, dynamicModel, b -> b.with(BlockStateProperties.OPEN, true));
                    ModelUtils.forEachCardinalDirection(builder, dynamicModelShut, b -> b.with(BlockStateProperties.OPEN, false));
                })
                .addLayer(() -> RenderType::cutout)
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "jarbnets")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/jarbnet/" + wood.serializedName)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmalife", "jarbnets"))).build()
                .register();

    }

    private static BlockModelBuilder bigBarrelTextures(BlockModelBuilder builder, TFGWood wood) {
        return builder.texture("0", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_3_side"))
                .texture("1", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_0"))
                .texture("2", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_0_side"))
                .texture("3", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_1"))
                .texture("4", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_1_side"))
                .texture("5", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_2"))
                .texture("6", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_2_side"))
                .texture("7", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_3"))
                .texture("8", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_3_top"))
                .texture("9", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_0_top"))
                .texture("10", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_1_top"))
                .texture("11", TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_2_top"))
                .texture("12", wood.logTexture);
    }

    private static BlockEntry<BigBarrelBlock> bigBarrel(TFGWood wood) {
        var properties = ExtendedProperties.of().mapColor(wood.woodColor()).sound(SoundType.WOOD)
                .noOcclusion().strength(10f).pushReaction(PushReaction.BLOCK).flammableLikeLogs().blockEntity(FLBlockEntities.BIG_BARREL);

        return TFGCore.REGISTRATE.block("wood/big_barrel/" + wood.serializedName, p -> new BigBarrelBlock(properties))
                .blockstate((ctx, prov) -> {
                    var builder = prov.getVariantBuilder(ctx.getEntry());

                    bigBarrelTextures(prov.models().withExistingParent(ctx.getName() + "_item", ResourceLocation.fromNamespaceAndPath("firmalife", "block/big_barrel_item")), wood);
                    for (int i = 0; i < 8; i++) {
                        var model = bigBarrelTextures(prov.models().withExistingParent(ctx.getName() + "_" + i, ResourceLocation.fromNamespaceAndPath("firmalife", "block/big_barrel_" + i)), wood);
                        int finalI = i;
                        ModelUtils.forEachCardinalDirection(builder, model, b -> b.with(FLStateProperties.BARREL_PART, finalI));
                    }

                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "big_barrels")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/big_barrel/" + wood.serializedName + "_item")))
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

    private static BlockEntry<WineShelfBlock> wineShelf(TFGWood wood) {
        var properties = ExtendedProperties.of().mapColor(wood.woodColor()).sound(SoundType.WOOD).noOcclusion()
                .strength(4f).pushReaction(PushReaction.BLOCK).flammableLikeLogs().blockEntity(FLBlockEntities.WINE_SHELF);

        return TFGCore.REGISTRATE.block("wood/wine_shelf/" + wood.serializedName, p -> new WineShelfBlock(properties))
                .blockstate((ctx, prov) -> {

                    prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/wine_shelf"))
                            .texture("0", wood.plankTexture)
                            .texture("2", TFGCore.id("block/wood/sheet/" + wood.serializedName))
                            .texture("3", wood.strippedLogTexture);

                    var dynamicModel = prov.models().getBuilder("wood/wine_shelf/" + wood.serializedName + "_dynamic")
                            .customLoader((t, existing) -> FirmaCustomLoader.get(ResourceLocation.fromNamespaceAndPath("firmalife", "wine_shelf"),
                                    TFGCore.id("block/wood/wine_shelf/" + wood.serializedName), t,
                                    existing))
                            .end();

                    ModelUtils.cardinalBlock(prov.getVariantBuilder(ctx.getEntry()), dynamicModel);

                })
                .tag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("firmalife", "wine_shelves")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new).model(ModelUtils.blockItemModel(TFGCore.id("block/wood/wine_shelf/" + wood.serializedName)))
                .tag(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("firmalife", "wine_shelves"))).build()
                .register();

    }

    private static BlockEntry<StompingBarrelBlock> stompingBarrel(TFGWood wood) {
        var properties = ExtendedProperties.of().mapColor(wood.woodColor()).sound(SoundType.WOOD).noOcclusion().strength(4f)
                .pushReaction(PushReaction.BLOCK).flammableLikeLogs().blockEntity(FLBlockEntities.STOMPING_BARREL);

        return TFGCore.REGISTRATE.block("wood/stomping_barrel/" + wood.serializedName, p -> new StompingBarrelBlock(properties))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/stomping_barrel"))
                                .texture("0", TFGCore.id("block/wood/sheet/" + wood.serializedName))))
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

    private static BlockEntry<BarrelPressBlock> barrelPress(TFGWood wood) {
        var properties = ExtendedProperties.of().mapColor(wood.woodColor()).sound(SoundType.WOOD).noOcclusion()
                .strength(4f).pushReaction(PushReaction.BLOCK).flammableLikeLogs().blockEntity(FLBlockEntities.BARREL_PRESS).ticks(BarrelPressBlockEntity::tick);

        return TFGCore.REGISTRATE.block("wood/barrel_press/" + wood.serializedName, p -> new BarrelPressBlock(properties))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("firmalife", "block/barrel_press"))
                                .texture("0", TFGCore.id("block/wood/sheet/" + wood.serializedName))))
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

    private static BlockEntry<CraftingStationBlock> craftingStation(String name, ResourceLocation plank, MapColor color) {
        return TFGCore.REGISTRATE.block("wood/crafting_station/" + name, CraftingStationBlock::new)
                .properties(p -> ExtendedProperties.of()
                        .mapColor(color)
                        .sound(SoundType.WOOD)
                        .strength(2.5F)
                        .flammableLikeLogs()
                        .properties())
                .blockstate((ctx, prov) -> {
                    prov.simpleBlock(ctx.getEntry(), prov.models().withExistingParent(ctx.getName(), ResourceLocation.fromNamespaceAndPath("craftingstation", "block/crafting_station"))
                            .texture("2", TFGCore.id("block/wood/crafting_station/" + name + "_side"))
                            .texture("3", TFGCore.id("block/wood/crafting_station/" + name + "_top"))
                            .texture("4", plank)
                            .texture("particle", TFGCore.id("block/wood/crafting_station/" + name + "_top")));
                })
                .tag(TagKey.create(Registries.BLOCK, TFGCore.id("crafting_stations")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, TFGCore.id("crafting_stations"))).build()
                .register();
    }

    private static BlockEntry<CraftingStationSlabBlock> craftingStationSlab(String name, ResourceLocation plank, MapColor color) {
        return TFGCore.REGISTRATE.block("wood/crafting_station/" + name + "_slab", CraftingStationSlabBlock::new)
                .properties(p -> ExtendedProperties.of()
                        .mapColor(color)
                        .sound(SoundType.WOOD)
                        .strength(2.5F)
                        .flammableLikeLogs()
                        .properties())
                .blockstate((ctx, prov) -> {
                    var topTex = TFGCore.id("block/wood/crafting_station/" + name + "_top");
                    var sideTex = TFGCore.id("block/wood/crafting_station/" + name + "_slab_side");

                    ModelFile base = prov.models().slab("wood/crafting_station/" + name + "_slab", sideTex, plank, topTex)
                            .texture("particle", plank);

                    ModelFile full = prov.models().cube("wood/crafting_station/" + name + "_slab_double", plank, topTex, sideTex, sideTex, sideTex, sideTex)
                            .texture("particle", plank);

                    ModelFile top = prov.models().slabTop("wood/crafting_station/" + name + "_slab_top", sideTex, plank, topTex)
                            .texture("particle", plank);

                    prov.slabBlock(ctx.getEntry(), base, top, full);
                })
                .tag(TagKey.create(Registries.BLOCK, TFGCore.id("crafting_stations")))
                .tag(BlockTags.MINEABLE_WITH_AXE)
                .item(BlockItem::new)
                .tag(TagKey.create(Registries.ITEM, TFGCore.id("crafting_stations"))).build()
                .register();
    }

}
