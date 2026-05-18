package su.terrafirmagreg.core.common.data;

import static java.util.Arrays.stream;

import java.util.*;
import java.util.function.Supplier;

import com.cake.struts.content.block.StrutBlockEntity;
import com.cake.struts.content.block.StrutBlockEntityRenderer;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.greenhouse.Greenhouse;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.dries007.tfc.common.blockentities.BerryBushBlockEntity;
import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.blockentity.AsphaltPouringSpreadBlockEntity;
import su.terrafirmagreg.core.common.blockentity.*;
import su.terrafirmagreg.core.common.data.blocks.*;
import su.terrafirmagreg.core.mixins.common.minecraft.BlockEntityTypeAccessor;

public class TFGBlockEntities {
    public static void init() {
    }

    public static final BlockEntityEntry<GTGreenhousePortBlockEntity> GT_GREENHOUSE_PORT = TFGCore.REGISTRATE.blockEntity("gt_greenhouse_port", GTGreenhousePortBlockEntity::new)
            .validBlocks(FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.STAINLESS_STEEL).get(Greenhouse.BlockType.PORT)::get,
                    FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.COPPER).get(Greenhouse.BlockType.PORT)::get,
                    FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.IRON).get(Greenhouse.BlockType.PORT)::get,
                    FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.RUSTED_IRON).get(Greenhouse.BlockType.PORT)::get,
                    FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.OXIDIZED_COPPER).get(Greenhouse.BlockType.PORT)::get,
                    FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.WEATHERED_COPPER).get(Greenhouse.BlockType.PORT)::get,
                    FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.EXPOSED_COPPER).get(Greenhouse.BlockType.PORT)::get,
                    FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.WEATHERED_TREATED_WOOD).get(Greenhouse.BlockType.PORT)::get,
                    FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.TREATED_WOOD).get(Greenhouse.BlockType.PORT)::get)
            .register();

    // private static final Block[] LARGE_NEST_TYPES = {TFGBlocks.LARGE_NEST_BOX.get(),
    // TFGBlocks.LARGE_NEST_BOX_WARPED.get()};

    public static final BlockEntityEntry<LargeNestBoxBlockEntity> LARGE_NEST_BOX = TFGCore.REGISTRATE.blockEntity("large_nest_box", LargeNestBoxBlockEntity::new)
            .validBlocks(TFGBlocks_Mars.LARGE_NEST_BOX, TFGBlocks_Mars.LARGE_NEST_BOX_WARPED)
            .register();

    public static final BlockEntityEntry<ArtisanTableBlockEntity> ARTISAN_TABLE = TFGCore.REGISTRATE.blockEntity("artisan_table", ArtisanTableBlockEntity::new)
            .validBlock(TFGBlocks.ARTISAN_TABLE)
            .register();

    public static final BlockEntityEntry<ReflectorBlockEntity> REFLECTOR_BLOCK_ENTITY = TFGCore.REGISTRATE.blockEntity("reflector", ReflectorBlockEntity::new)
            .validBlock(TFGBlocks_Casings.REFLECTOR_BLOCK)
            .register();

    public static final BlockEntityEntry<AsphaltPouringSpreadBlockEntity> ASPHALT_POURING_SPREAD = TFGCore.REGISTRATE
            .blockEntity("asphalt_pouring_spread", AsphaltPouringSpreadBlockEntity::new)
            .validBlock(TFGBlocksAsphalt.ASPHALT_ROAD_POURING)
            .register();

    public static final BlockEntityEntry<TickerBlockEntity> TICKER_ENTITY = TFGCore.REGISTRATE.blockEntity("particle_emitter", TickerBlockEntity::new)
            .validBlocks(TFGBlocks_Casings.GROW_LIGHT, TFGBlocks_Casings.EGH_PLANTER, TFGBlocks_Casings.PISCICULTURE_CORE)
            .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<BerryBushBlockEntity> FRUIT_TREE_BERRY_BUSH = TFGCore.REGISTRATE
            .<BerryBushBlockEntity>blockEntity("fruit_tree_berry_bush", (type, pos, state) -> new TFGBerryBushBlockEntity(pos, state))
            .validBlocks(stream(TFGFruitTree.FruitTreeType.values())
                    .map(TFGFruitTree.FRUIT_TREE_LEAVES::get)
                    .toArray(NonNullSupplier[]::new))
            .register();

    @SuppressWarnings("unchecked")
    public static final BlockEntityEntry<TickCounterBlockEntity> FRUIT_TREE_TICK_COUNTER = TFGCore.REGISTRATE
            .<TickCounterBlockEntity>blockEntity("fruit_tree_tick_counter", (type, pos, state) -> new TFGTickCounterBlockEntity(pos, state))
            .validBlocks(stream(TFGFruitTree.FruitTreeType.values())
                    .flatMap(tree -> java.util.stream.Stream.of(
                            TFGFruitTree.FRUIT_TREE_SAPLINGS.get(tree),
                            TFGFruitTree.FRUIT_TREE_GROWING_BRANCHES.get(tree)))
                    .toArray(NonNullSupplier[]::new))
            .register();

    public static final BlockEntityEntry<StrutBlockEntity> STRUT = TFGCore.REGISTRATE
            .blockEntity("strut", StrutBlockEntity::new)
            .renderer(() -> StrutBlockEntityRenderer::new)
            .register();

    private static final Map<Supplier<?>, Set<Block>> beModification = new Object2ObjectOpenHashMap<>();

    public static void addValidBEBlock(Supplier<?> type, Block block) {
        beModification.computeIfAbsent(type, t -> new HashSet<>());
        beModification.get(type).add(block);
    }

    public static void finaliseBEModification() {
        for (var key : beModification.keySet()) {
            var beType = (BlockEntityTypeAccessor) key.get();
            Set<Block> blocks = new HashSet<>();
            blocks.addAll(beType.tfg$getValidBlocks());
            blocks.addAll(beModification.get(key));
            beType.tfg$setValidBlocks(blocks);
        }
    }
}
