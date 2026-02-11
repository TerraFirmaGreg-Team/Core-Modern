package su.terrafirmagreg.core.common.atmosphere;

import static su.terrafirmagreg.core.common.atmosphere.PassabilityChecker.getPassCache;

import java.util.*;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.BlockEvent;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.atmosphere.PassabilityChecker.PassCache;
import su.terrafirmagreg.core.common.atmosphere.PassabilityChecker.PassCache.PassType;

/**
 * Manages atmosphere providers for a single dimension.
 * Extends SavedData to persist oxygen provider data across world saves.
 */
public class DimensionAtmosphereManager extends SavedData {

    // TODO: Normalize machine vs provider naming
    // TODO: Refactor "Atmosphere" to "Environment"
    // TODO: Rename DimensionAtmosphereManager to something shorter

    /** The level this manager is for */
    @Getter
    private final ServerLevel level;

    /** Oxygen providers keyed by machine position. Persisted to NBT. */
    private final Map<BlockPos, OxygenProvider> providers = new HashMap<>();

    /** Map of Chunks to Machines that want to be notified of block change events in those chunks */
    public final MachineRegistry<IFloodFillMachine> blockChangeListeners = new MachineRegistry<>();

    /** Map of Chunks to Machines that want to be notified of chunk loads */
    public final MachineRegistry<IFloodFillMachine> chunkLoadListeners = new MachineRegistry<>();

    /** Map of Chunks to OxygenProviders that affect oxygen in those Chunks */
    public final MachineRegistry<OxygenProvider> oxygenProviders = new MachineRegistry<>();

    /** Map of Chunks to Machines that affect gravity in those Chunks */
    public final MachineRegistry<AtmosphereSystem.IGravityProvider> gravityMachines = new MachineRegistry<>();

    /** Map of Chunks to Machines that affect temperature in those Chunks */
    public final MachineRegistry<AtmosphereSystem.ITemperatureProvider> temperatureMachines = new MachineRegistry<>();

    /** Active decompression events in this dimension */
    private final List<DecompressionEvent> activeDecompressions = new ArrayList<>();

    // ==================== Construction & SavedData ====================
    private static final String DATA_NAME = "tfg_atmosphere_system";

    public DimensionAtmosphereManager(ServerLevel level) {
        this.level = level;
    }

    /**
     * Gets or creates the manager for a level via Minecraft's data storage.
     */
    public static DimensionAtmosphereManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                tag -> load(level, tag),
                () -> new DimensionAtmosphereManager(level),
                DATA_NAME);
    }

    private static DimensionAtmosphereManager load(ServerLevel level, CompoundTag tag) {
        DimensionAtmosphereManager manager = new DimensionAtmosphereManager(level);

        ListTag list = tag.getList("providers", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag providerTag = list.getCompound(i);
            try {
                OxygenProvider provider = OxygenProvider.load(providerTag);
                manager.providers.put(provider.getMachinePos(), provider);

                // Register sealed providers into the chunk registry
                if (provider.getRoomScan().isSealed()) {
                    manager.oxygenProviders.add(provider, provider.getTouchedChunks());
                }
            } catch (Exception e) {
                TFGCore.LOGGER.error("Failed to load oxygen provider from NBT", e);
            }
        }

        TFGCore.LOGGER.debug("Loaded {} oxygen providers from saved data", manager.providers.size());
        return manager;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();

        for (OxygenProvider provider : providers.values()) {
            // Only persist sealed providers
            if (!provider.getRoomScan().isSealed())
                continue;

            try {
                CompoundTag providerTag = new CompoundTag();
                provider.save(providerTag);
                list.add(providerTag);
            } catch (Exception e) {
                TFGCore.LOGGER.error("Failed to save oxygen provider at {}", provider.getMachinePos(), e);
            }
        }

        tag.put("providers", list);
        TFGCore.LOGGER.debug("Saved {} sealed oxygen providers", list.size());
        return tag;
    }

    private void setSavedDataDirty() {
        setDirty();
    }

    /**
     * @return All oxygen providers in this dimension (for debug/commands)
     */
    public Map<BlockPos, OxygenProvider> getProviders() {
        return Collections.unmodifiableMap(providers);
    }

    // ==================== Oxygen Provider Management ====================

    /**
     * Gets an existing provider or creates a new one at the given position.
     * Called when a machine loads and needs to attach.
     */
    public OxygenProvider getOrCreateProvider(BlockPos machinePos) {
        return providers.computeIfAbsent(machinePos, pos -> {
            setSavedDataDirty();
            return new OxygenProvider(pos);
        });
    }

    /**
     * Removes an oxygen provider entirely. Called when a machine is broken.
     */
    public void removeProvider(BlockPos machinePos) {
        OxygenProvider provider = providers.remove(machinePos);
        if (provider != null) {
            oxygenProviders.remove(provider, provider.getTouchedChunks());
            setSavedDataDirty();
        }
    }

    /**
     * Updates a provider's room scan and chunk registrations.
     * Called by the machine after validation completes.
     */
    public void updateProvider(OxygenProvider provider, RoomScan oldScan, RoomScan newScan) {
        Set<ChunkPos> oldChunks = oldScan.isSealed() ? oldScan.touchedChunks() : Set.of();
        Set<ChunkPos> newChunks = newScan.isSealed() ? newScan.touchedChunks() : Set.of();

        if (!oldChunks.isEmpty() || !newChunks.isEmpty()) {
            Set<ChunkPos> toRemove = new HashSet<>(oldChunks);
            toRemove.removeAll(newChunks);

            Set<ChunkPos> toAdd = new HashSet<>(newChunks);
            toAdd.removeAll(oldChunks);

            oxygenProviders.update(provider, toRemove, toAdd);
        }

        provider.setRoomScan(newScan);
        setSavedDataDirty();
    }

    // ==================== Oxygen Queries ====================

    /**
     * Checks if a position has oxygen (from any source - flood fill or bubble).
     *
     * @param pos Position to check
     * @return true if the position has oxygen
     */
    public boolean hasOxygen(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<OxygenProvider> providerSet = oxygenProviders.get(chunkPos);
        if (providerSet != null) {
            for (OxygenProvider provider : providerSet) {
                if (provider.hasOxygen(pos)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ==================== Decompression Events ====================

    public DecompressionEvent startDecompression(BlockPos breachPoint, RoomScan oldRoomScan) {
        DecompressionEvent event = new DecompressionEvent(level, breachPoint, oldRoomScan);
        activeDecompressions.add(event);
        return event;
    }

    /**
     * Cancel all active decompression events (e.g. when dimension unloads).
     */
    public void cancelAllDecompressions() {
        for (DecompressionEvent event : activeDecompressions) {
            event.cancel(level);
        }
        activeDecompressions.clear();
    }

    /**
     * Tick all active decompression events. Called from AtmosphereSystem.onServerTick.
     */
    public void tickDecompressions() {
        activeDecompressions.removeIf(event -> !event.tick(level));
    }

    // ==================== Event Handling ====================

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
            TFGCore.LOGGER.info("breakEvent {}", breakEvent.getState());
            PassCache before = getPassCache(level, pos, breakEvent.getState());
            if (before.type() == PassType.EMPTY) {
                TFGCore.LOGGER.info("Ignored - empty block");
                return;
            }

        } else if (event instanceof BlockEvent.EntityPlaceEvent placeEvent) {
            TFGCore.LOGGER.info("placeEvent {} {}", placeEvent.getBlockSnapshot().getReplacedBlock(), placeEvent.getPlacedBlock());
            PassCache before = getPassCache(level, pos, placeEvent.getBlockSnapshot().getReplacedBlock());
            PassCache after = getPassCache(level, pos, placeEvent.getPlacedBlock());
            if (before.equals(after) && before.type() != PassType.NO_CACHE) {
                TFGCore.LOGGER.info("Ignored - passability unchanged");
                return;
            }

        } else if (event instanceof BlockEvent.NeighborNotifyEvent nighEvent) {
            TFGCore.LOGGER.info("neighborNotifyEvent {}", nighEvent.getState());
            // NO_CACHE blocks (airlocks, pistons, etc.) have dynamic passability — always dispatch
            if (PassabilityChecker.getCachedPassType(nighEvent.getState()) == PassType.NO_CACHE) {
                TFGCore.LOGGER.info("Dynamic block (NO_CACHE) - always dispatching");
            } else {
                PassCache current = getPassCache(level, pos, nighEvent.getState());
                if (current.type() == PassType.EMPTY || current.type() == PassType.FULL) {
                    TFGCore.LOGGER.info("Ignored - stable block type");
                    return;
                }
            }
        }

        TFGCore.LOGGER.info("Dispatching block change to {} machines", machinesInChunk.size());
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
     * - Notifies machines waiting for this chunk
     * - Checks for orphaned providers (machine was removed while chunk was unloaded)
     *
     * @param chunkPos Position of the loaded chunk
     */
    public void onChunkLoad(ChunkPos chunkPos) {
        // Notify machines waiting for this chunk
        Set<IFloodFillMachine> listeners = chunkLoadListeners.get(chunkPos);
        if (listeners != null) {
            for (IFloodFillMachine machine : listeners) {
                machine.onChunkLoad(chunkPos);
            }
        }

        checkOrphanedProviders(chunkPos);
    }

    /**
     * Defers orphan check to the next tick. By then, machine onLoad() has already fired
     * and attached to its provider. Any provider still without a machine is orphaned.
     */
    private void checkOrphanedProviders(ChunkPos chunkPos) {
        // Check if any providers have machines in this chunk
        boolean hasProvidersInChunk = false;
        for (BlockPos machinePos : providers.keySet()) {
            if (new ChunkPos(machinePos).equals(chunkPos)) {
                hasProvidersInChunk = true;
                break;
            }
        }
        if (!hasProvidersInChunk)
            return;

        level.getServer().tell(new net.minecraft.server.TickTask(
                level.getServer().getTickCount() + 1,
                () -> {
                    List<BlockPos> orphaned = new ArrayList<>();
                    for (var entry : providers.entrySet()) {
                        BlockPos machinePos = entry.getKey();
                        if (new ChunkPos(machinePos).equals(chunkPos) && !entry.getValue().isMachineLoaded()) {
                            orphaned.add(machinePos);
                            TFGCore.LOGGER.debug("Removing orphaned oxygen provider at {}", machinePos);
                        }
                    }

                    for (BlockPos pos : orphaned) {
                        removeProvider(pos);
                    }
                }));
    }

    // ==================== MachineRegistry ====================

    /**
     * Wrapper around a Map<ChunkPos, Set<T>> that handles adding and removing
     */
    public static class MachineRegistry<T> {
        private final Map<ChunkPos, Set<T>> map = new HashMap<>();

        public void update(T item, Set<ChunkPos> toRemove, Set<ChunkPos> toAdd) {
            remove(item, toRemove);
            add(item, toAdd);
        }

        public void add(T item, Set<ChunkPos> chunks) {
            for (ChunkPos chunk : chunks) {
                addSingle(item, chunk);
            }
        }

        public void remove(T item, Set<ChunkPos> chunks) {
            for (ChunkPos chunk : chunks) {
                removeSingle(item, chunk);
            }
        }

        public void addSingle(T item, ChunkPos chunk) {
            map.computeIfAbsent(chunk, k -> new HashSet<>())
                    .add(item);
        }

        public void removeSingle(T item, ChunkPos chunk) {
            Set<T> set = map.get(chunk);
            if (set != null) {
                set.remove(item);
                if (set.isEmpty())
                    map.remove(chunk);
            }
        }

        public Set<T> get(ChunkPos chunk) {
            return map.get(chunk);
        }
    }
}
