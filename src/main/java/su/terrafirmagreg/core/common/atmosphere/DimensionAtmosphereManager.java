package su.terrafirmagreg.core.common.atmosphere;

import static su.terrafirmagreg.core.common.atmosphere.PassabilityChecker.getPassCache;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.level.BlockEvent;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.atmosphere.PassabilityChecker.PassCache;
import su.terrafirmagreg.core.common.atmosphere.PassabilityChecker.PassCache.PassType;

/**
 * Manages atmosphere machines for a single dimension.
 */
public class DimensionAtmosphereManager {

    // TODO: Normalize machine vs provider naming
    // TODO: Refactor "Atmosphere" to "Environment"
    // TODO: Rename DimensionAtmosphereManager to something shorter

    /** The level this manager is for */
    @Getter
    private final ServerLevel level;

    /** Map of Chunks to Machines that want to be notified of block change events in those chunks */
    public final MachineRegistry<IFloodFillMachine> blockChangeListeners = new MachineRegistry<>();
    /** Map of Chunks to Machines that want to be notified of chunk loads */
    public final MachineRegistry<IFloodFillMachine> chunkLoadListeners = new MachineRegistry<>();

    /** Map of Chunks to Machines that affect oxygen in those Chunks */
    public final MachineRegistry<AtmosphereSystem.IOxygenProvider> oxygenMachines = new MachineRegistry<>();
    /** Map of Chunks to Machines that affect temperature in those Chunks */
    public final MachineRegistry<AtmosphereSystem.IGravityProvider> gravityMachines = new MachineRegistry<>();
    /** Map of Chunks to Machines that affect gravity in those Chunks */
    public final MachineRegistry<AtmosphereSystem.ITemperatureProvider> temperatureMachines = new MachineRegistry<>();

    public DimensionAtmosphereManager(ServerLevel level) {
        this.level = level;
    }


    /**
     * Called when a block changes in this dimension.
     * Marks affected rooms as dirty for revalidation.
     *
     * @param event The event that changes the block
     */
    public void onBlockChange(BlockEvent event) {
        BlockPos pos = event.getPos();
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<IFloodFillMachine> machinesInChunk = blockChangeListeners.get(chunkPos);
        if (machinesInChunk == null)
            return;

        if (event instanceof BlockEvent.BreakEvent breakEvent) {
            TFGCore.LOGGER.warn("breakEvent {}", breakEvent.getState());
            PassCache before = getPassCache(level, pos, breakEvent.getState());
            if (before.type() == PassType.EMPTY) {
                TFGCore.LOGGER.warn("Ignored");
                return;
            }

        } else if (event instanceof BlockEvent.EntityPlaceEvent placeEvent) {
            TFGCore.LOGGER.warn("placeEvent {} {}", placeEvent.getBlockSnapshot().getReplacedBlock(), placeEvent.getPlacedBlock());
            PassCache before = getPassCache(level, pos, placeEvent.getBlockSnapshot().getReplacedBlock());
            PassCache after = getPassCache(level, pos, placeEvent.getPlacedBlock());
            if (before.equals(after) && before.type() != PassType.NO_CACHE) {
                TFGCore.LOGGER.warn("Ignored");
                return;
            }

        } else if (event instanceof BlockEvent.NeighborNotifyEvent nighEvent) {
            TFGCore.LOGGER.warn("nighEvent {}", nighEvent.getState());
            PassCache current = getPassCache(level, pos, nighEvent.getState());
            if (current.type() == PassType.EMPTY || current.type() == PassType.FULL) {
                TFGCore.LOGGER.warn("Ignored");
                // Assuming that EMPTY and FULL blocks never change from EMPTY or FULL.
                return;
            }
        }

        TFGCore.LOGGER.warn("Dispatch to machines");
        for (IFloodFillMachine machine : machinesInChunk) {
            machine.onBlockChange(event);
        }
    }

    /**
     * Called when an AE2 spatial IO event happens.
     */
    public void onGridSpatialEvent(BlockPos min, BlockPos max) {
        ChunkPos minChunk = new ChunkPos(min);
        ChunkPos maxChunk = new ChunkPos(max);

        Set<IFloodFillMachine> machinesAffected = new HashSet<>();
        for (int cx = minChunk.x; cx <= maxChunk.x; cx++) {
            for (int cz = minChunk.z; cz <= maxChunk.z; cz++) {
                var machinesInChunk = blockChangeListeners.get(new ChunkPos(cx, cz));
                if (machinesInChunk != null) {
                    machinesAffected.addAll(machinesInChunk);
                }
            }
        }
        for (IFloodFillMachine machine : machinesAffected) {
            machine.onGridSpatialEvent(min, max);
        }
    }

    /**
     * Called when a chunk is loaded.
     * Notifies rooms that were waiting for this chunk.
     *
     * @param chunkPos Position of the loaded chunk
     */
    //TODO: Make sure we de-register here if we revalidate for any reason, even non-chunkloading events
    public void onChunkLoaded(ChunkPos chunkPos) {
        // Check all rooms for pending chunks
        for (IFloodFillMachine machine : chunkLoadListeners.get(chunkPos)) {
            machine.onChunkLoaded(chunkPos);
        }
    }

    /**
     * Checks if a position has oxygen (from any source - flood fill or bubble).
     *
     * @param pos Position to check
     * @return true if the position has oxygen
     */
    public boolean hasOxygen(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<AtmosphereSystem.IOxygenProvider> machines = oxygenMachines.get(chunkPos);
        if (machines != null) {
            for (AtmosphereSystem.IOxygenProvider machine : machines) {
                if (machine.hasOxygen(pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Wrapper around a Map<ChunkPos, Set<MachineType>> that handles adding and removing
     * @param <T>
     */
    public static class MachineRegistry<T> {
        private final Map<ChunkPos, Set<T>> map = new HashMap<>();

        public void update(T machine, Set<ChunkPos> toRemove, Set<ChunkPos> toAdd) {
            remove(machine, toRemove);
            add(machine, toAdd);
        }

        public void add(T machine, Set<ChunkPos> chunks) {
            for (ChunkPos chunk : chunks) {
                addSingle(machine, chunk);
            }
        }

        public void remove(T machine, Set<ChunkPos> chunks) {
            for (ChunkPos chunk : chunks) {
                removeSingle(machine, chunk);
            }
        }

        public void addSingle(T machine, ChunkPos chunk) {
            map.computeIfAbsent(chunk, k -> new HashSet<>())
                    .add(machine);
        }

        public void removeSingle(T machine, ChunkPos chunk) {
            Set<T> set = map.get(chunk);
            if (set != null) {
                set.remove(machine);
                if (set.isEmpty()) map.remove(chunk);
            }
        }

        public Set<T> get(ChunkPos chunk) {
            return map.get(chunk);
        }
    }
}
