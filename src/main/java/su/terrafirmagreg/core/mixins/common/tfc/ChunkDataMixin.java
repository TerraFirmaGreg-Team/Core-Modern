package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataCapability;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.LazyOptional;

import su.terrafirmagreg.core.TFGCore;

/**
 * ChunkData.get(LevelChunk) is called from ChunkDataProvider.get(ChunkAccess), which is used during world gen only.
 * The comment there says that this call to ChunkData.get(LevelChunk) "should generally not happen".
 * Add some logging so that crashes caused by corrupted chunks using this path are easier to debug.
 */
@Mixin(value = ChunkData.class, remap = false)
@SuppressWarnings("deprecation")
public class ChunkDataMixin {

    /**
     * Add logging in case of chunks with initialized caps where the cap data is empty.
     * This causes NPE, but we don't try to silently pass the error since that could cause players to play on
     * corrupted chunks rather than fix the issue.
     *
     * @author Redeix, Mqrius
     * @reason Log chunk info when capability data is missing
     */
    @Overwrite(remap = false)
    public static ChunkData get(LevelChunk chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return ChunkData.EMPTY;
        }

        ChunkPos pos = chunk.getPos();
        LazyOptional<ChunkDataCapability> cap = chunk.getCapability(ChunkDataCapability.CAPABILITY);

        if (!cap.isPresent()) {
            tfg$corruptChunkLogger(pos, "TFC capability missing");
            return ChunkData.EMPTY;
        }

        try {
            ChunkData data = cap.map(c -> ((IChunkDataCapabilityAccessor) c).tfg$getData()).orElse(ChunkData.EMPTY);

            if (data == ChunkData.EMPTY) {
                tfg$corruptChunkLogger(pos, "ChunkData is EMPTY for populated chunk");
            }

            return data;
        } catch (NullPointerException e) {
            tfg$corruptChunkLogger(pos, "ChunkDataCapability exists but ChunkDataCapability.getData() is null");
            throw new IllegalStateException("Corrupt TFC chunk at " + pos, e);
        }
    }

    @Unique
    private static void tfg$corruptChunkLogger(ChunkPos pos, String reason) {
        TFGCore.LOGGER.error("Possibly corrupt chunk detected");
        TFGCore.LOGGER.error("Chunk: ({}, {})", pos.x, pos.z);
        TFGCore.LOGGER.error("Region: r.{}.{}.mca", pos.getRegionX(), pos.getRegionZ());
        TFGCore.LOGGER.error("Coords: {},{} to {},{}", pos.getMinBlockX(), pos.getMinBlockZ(), pos.getMaxBlockX(), pos.getMaxBlockZ());
        TFGCore.LOGGER.error("Reason: {}", reason);
    }
}
