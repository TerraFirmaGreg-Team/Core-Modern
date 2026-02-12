package su.terrafirmagreg.core.common.environment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.level.BlockEvent;

/**
 * Machine that is sensitive to block changes.
 * It wants to be notified when blocks change in the areas it cares about, and can do async revalidation runs.
 * Example: Oxygen distributor flood filling a sealed room.
 */
public interface IBlockSensitiveMachine {

    boolean isDirty();

    void setDirty(boolean dirty);

    /**
     * Call this async to revalidate the room.
     * Usually means run a new flood fill.
     * @param reader Thread-safe block reader, created on the main thread before dispatch
     */
    void validateAsync(AsyncBlockReader reader);

    /**
     * Call this on the main thread to apply the revalidation results when they're ready
     */
    void processValidationResult();

    void onBlockChange(BlockEvent event);

    void onGridSpatialEvent(BlockPos min, BlockPos max);

    void onChunkLoad(ChunkPos chunkPos);

    BlockPos getPos();

    ServerLevel getServerLevel();
}
