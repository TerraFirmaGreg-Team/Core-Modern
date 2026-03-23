package su.terrafirmagreg.core.common.data;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.greenhouse.Greenhouse;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blockentity.ArtisanTableBlockEntity;
import su.terrafirmagreg.core.common.data.blockentity.GTGreenhousePortBlockEntity;
import su.terrafirmagreg.core.common.data.blockentity.LargeNestBoxBlockEntity;
import su.terrafirmagreg.core.common.data.blockentity.ReflectorBlockEntity;
import su.terrafirmagreg.core.common.data.blockentity.TickerBlockEntity;
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

    public static final BlockEntityEntry<TickerBlockEntity> TICKER_ENTITY = TFGCore.REGISTRATE.blockEntity("particle_emitter", TickerBlockEntity::new)
            .validBlocks(TFGBlocks_Casings.GROW_LIGHT, TFGBlocks_Casings.EGH_PLANTER, TFGBlocks_Casings.PISCICULTURE_CORE)
            .register();

    public static void addValidBEBlock(BlockEntityType<?> type, Block block) {
        var beType = (BlockEntityTypeAccessor) type;
        var blocks = beType.tfg$getValidBlocks();
        blocks.add(block);
        beType.tfg$setValidBlocks(blocks);
    }
}
