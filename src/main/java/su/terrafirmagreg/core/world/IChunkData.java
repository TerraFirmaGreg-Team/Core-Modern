package su.terrafirmagreg.core.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;

public interface IChunkData {
    long tfg$getLastRandomTick();

    void tfg$setLastRandomTick(ChunkAccess chunk, long lastRandomTick);

    BlockPos tfg$getNextSnowPos(ChunkPos chunkPos);

    void tfg$iterateSnowPos(ChunkAccess chunk);
}
