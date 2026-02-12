package su.terrafirmagreg.core.common.data;

import java.util.ArrayList;
import java.util.List;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.greenhouse.Greenhouse;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blockentity.ArtisanTableBlockEntity;
import su.terrafirmagreg.core.common.data.blockentity.GTGreenhousePortBlockEntity;
import su.terrafirmagreg.core.common.data.blockentity.LargeNestBoxBlockEntity;
import su.terrafirmagreg.core.common.data.blockentity.ReflectorBlockEntity;
import su.terrafirmagreg.core.common.data.blockentity.TickerBlockEntity;
import su.terrafirmagreg.core.compat.kjs.GTActiveParticleBuilder;
import su.terrafirmagreg.core.compat.kjs.ParticleEmitterBlockBuilder;
import su.terrafirmagreg.core.compat.kjs.ParticleEmitterDecorationBlockBuilder;

public class TFGBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, TFGCore.MOD_ID);

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
            .validBlocks(TFGBlocks.LARGE_NEST_BOX::get, TFGBlocks.LARGE_NEST_BOX_WARPED::get)
            .register();

    public static final BlockEntityEntry<ArtisanTableBlockEntity> ARTISAN_TABLE = TFGCore.REGISTRATE.blockEntity("artisan_table", ArtisanTableBlockEntity::new)
            .validBlock(TFGBlocks.ARTISAN_TABLE::get)
            .register();

    public static final BlockEntityEntry<ReflectorBlockEntity> REFLECTOR_BLOCK_ENTITY = TFGCore.REGISTRATE.blockEntity("reflector", ReflectorBlockEntity::new)
            .validBlock(TFGBlocks.REFLECTOR_BLOCK::get)
            .register();

    public static final RegistryObject<BlockEntityType<TickerBlockEntity>> TICKER_ENTITY = BLOCK_ENTITIES
            .register("particle_emitter", () -> {
                List<Block> blocks = new ArrayList<>();
                blocks.addAll(ParticleEmitterBlockBuilder.REGISTERED_BLOCKS);
                blocks.addAll(ParticleEmitterDecorationBlockBuilder.REGISTERED_BLOCKS);
                blocks.addAll(GTActiveParticleBuilder.REGISTERED_BLOCKS);
                return BlockEntityType.Builder.of(TickerBlockEntity::new, blocks.toArray(Block[]::new)).build(null);
            });
}
