package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataCapability;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Fixes a crash where `ChunkData.get(LevelChunk)` is called before the data is fully initialized.
 * Fix mostly targets crashes related to "TFC Improved Badlands".
 */
@Mixin(value = ChunkData.class, remap = false)
@SuppressWarnings("deprecation")
public class ChunkDataMixin {

    /**
     * Replaces `get(LevelChunk)` to handle null data.
     * The original uses `Optional.of()` which throws NullPointerException on null, we use `orElse()` instead.
     *
     * @author Redeix
     * @reason Prevent NullPointerException when capability data is null
     */
    @Overwrite
    public static ChunkData get(LevelChunk chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return ChunkData.EMPTY;
        }
        return chunk.getCapability(ChunkDataCapability.CAPABILITY)
                .map(cap -> {
                    ChunkData data = ((IChunkDataCapabilityAccessor) cap).tfg$getData();
                    return data != null ? data : ChunkData.EMPTY;
                })
                .orElse(ChunkData.EMPTY);
    }
}
