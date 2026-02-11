package su.terrafirmagreg.core.common.atmosphere;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.level.BlockEvent;

// TODO: Rename this to something more descriptive, like "BlockChangeListenerMachine"? "MachineThatCanBeMarkedDirtyAndWantsRevalidation"?
public interface IFloodFillMachine extends IAtmosphereMachine {

    boolean isDirty();

    void setDirty(boolean dirty);

    /**
     * Call this async to revalidate the room.
     * Usually means run a new flood fill.
     */
    void validateAsync();

    /**
     * Call this on the main thread to apply the revalidation results when they're ready
     */
    void processValidationResult();

    void onBlockChange(BlockEvent event);

    void onGridSpatialEvent(BlockPos min, BlockPos max);

    void onChunkLoad(ChunkPos chunkPos);
}
