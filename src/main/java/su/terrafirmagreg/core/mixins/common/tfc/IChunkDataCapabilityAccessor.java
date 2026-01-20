package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataCapability;

/**
 * Accessor for ChunkDataCapability to access `getData()` method.
 */
@Mixin(value = ChunkDataCapability.class, remap = false)
public interface IChunkDataCapabilityAccessor {

    @Accessor("data")
    ChunkData tfg$getData();
}
