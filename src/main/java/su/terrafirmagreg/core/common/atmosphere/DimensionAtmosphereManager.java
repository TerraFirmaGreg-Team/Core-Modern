package su.terrafirmagreg.core.common.atmosphere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nullable;

import appeng.api.networking.events.GridSpatialEvent;
import appeng.api.networking.spatial.ISpatialService;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.utils.atmosphere.FloodFill;
import su.terrafirmagreg.core.utils.atmosphere.FloodFillConfig;
import su.terrafirmagreg.core.utils.atmosphere.RoomScan;
import su.terrafirmagreg.core.utils.atmosphere.PassabilityChecker.PassCache;
import su.terrafirmagreg.core.utils.atmosphere.PassabilityChecker.PassCache.PassType;

import static su.terrafirmagreg.core.utils.atmosphere.PassabilityChecker.getPassCache;

/**
 * Manages atmosphere providers for a single dimension.
 * Handles flood fill machines, bubble providers, and coordinate checks.
 */
public class DimensionAtmosphereManager {

    /** The level this manager is for */
    @Getter
    private final ServerLevel level;

    /**
     * All flood-fill atmosphere providers (Oxygen Distributors, etc.)
     * Key: machine position
     */
    private final Map<BlockPos, AtmosphereRoom> rooms = new HashMap<>();

    /**
     * All bubble providers (Gravity Normalizers, Temperature Regulators, etc.)
     */
    private final Set<IBubbleProvider> bubbleProviders = new HashSet<>();

    /**
     * Index of chunks to rooms that touch them, for fast lookup on block change.
     */
    private final Map<ChunkPos, Set<AtmosphereRoom>> chunkIndex = new HashMap<>();

    /**
     * Queue of completed async flood fill results waiting to be processed on main thread.
     */
    private final ConcurrentLinkedQueue<PendingResult> pendingResults = new ConcurrentLinkedQueue<>();

    /**
     * Rooms currently being filled asynchronously.
     */
    private final Set<BlockPos> asyncFillsInProgress = new HashSet<>();

    public DimensionAtmosphereManager(ServerLevel level) {
        this.level = level;
    }

    /**
     * Registers a flood-fill atmosphere provider (e.g., Oxygen Distributor).
     *
     * @param provider The provider to register
     */
    public void addProvider(IAtmosphereProvider provider) {
        BlockPos pos = provider.getPosition();
        if (rooms.containsKey(pos)) {
            return; // Already registered
        }

        AtmosphereRoom room = new AtmosphereRoom(provider);
        rooms.put(pos, room);

        // Mark for immediate validation
        room.markDirty();
    }

    /**
     * Unregisters a flood-fill atmosphere provider.
     *
     * @param provider The provider to remove
     */
    public void removeProvider(IAtmosphereProvider provider) {
        BlockPos pos = provider.getPosition();
        AtmosphereRoom room = rooms.remove(pos);
        if (room != null) {
            // Remove from chunk index
            for (ChunkPos chunkPos : room.getScan().touchedChunks()) {
                Set<AtmosphereRoom> roomsInChunk = chunkIndex.get(chunkPos);
                if (roomsInChunk != null) {
                    roomsInChunk.remove(room);
                    if (roomsInChunk.isEmpty()) {
                        chunkIndex.remove(chunkPos);
                    }
                }
            }

            // Notify the room it's being removed
            room.setInactive();
        }

        asyncFillsInProgress.remove(pos);
    }

    /**
     * Registers a bubble provider (e.g., Gravity Normalizer).
     *
     * @param provider The provider to register
     */
    public void addBubbleProvider(IBubbleProvider provider) {
        bubbleProviders.add(provider);
    }

    /**
     * Unregisters a bubble provider.
     *
     * @param provider The provider to remove
     */
    public void removeBubbleProvider(IBubbleProvider provider) {
        bubbleProviders.remove(provider);
    }

    /**
     * Called when a block changes in this dimension.
     * Marks affected rooms as dirty for revalidation.
     *
     * @param event The event that changes the block
     */
    public void onBlockChanged(BlockEvent event) {
        BlockPos pos = event.getPos();
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<AtmosphereRoom> roomsInChunk = chunkIndex.get(chunkPos);
        if (roomsInChunk == null) {
            return;
        }
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
            if (before.equals(after)) {
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

        TFGCore.LOGGER.warn("Mark Dirty");
        for (AtmosphereRoom room : roomsInChunk) {
            if (room.isPositionRelevant(pos)) {
                room.markDirty();
            }
        }
    }

    /**
     * Called when an AE2 spatial IO event happens.
     * Marks affected rooms as dirty for revalidation.
     *
     * @param spatialService Object that provides information on the spatial bounds
     * @param event The spatial IO event itself
     */
    public void onGridSpatialEvent(ISpatialService spatialService, GridSpatialEvent event) {
        BlockPos min = spatialService.getMin();
        BlockPos max = spatialService.getMax();
        ChunkPos minChunk = new ChunkPos(min);
        ChunkPos maxChunk = new ChunkPos(max);

        for (int cx = minChunk.x; cx <= maxChunk.x; cx++) {
            for (int cz = minChunk.z; cz <= maxChunk.z; cz++) {
                Set<AtmosphereRoom> rooms = chunkIndex.get(new ChunkPos(cx, cz));
                if (rooms == null) continue;

                AABB spatialAabb = new AABB(min, max);
                for (AtmosphereRoom room : rooms) {
                    if (spatialAabb.intersects(room.getScan().bounds())) {
                        // Technically the AABB can overlap without any of the room being in the spatial event.
                        //  However this is way faster and spatial events are rare anyway.
                        room.markDirty();
                    }
                }
            }
        }
    }

    /**
     * Called when a chunk is loaded.
     * Notifies rooms that were waiting for this chunk.
     *
     * @param chunkPos Position of the loaded chunk
     */
    public void onChunkLoaded(ChunkPos chunkPos) {
        // Check all rooms for pending chunks
        for (AtmosphereRoom room : rooms.values()) {
            room.onChunkLoaded(chunkPos);
        }
    }

    /**
     * Tick handler - processes dirty rooms and pending results.
     * Should be called once per server tick.
     *
     * @param currentTick Current game tick
     */
    public void tick(long currentTick) {
        // Process pending async results first
        processPendingResults();

        // Process dirty rooms
        List<AtmosphereRoom> roomsToValidate = new ArrayList<>();
        for (AtmosphereRoom room : rooms.values()) {
            if (room.canRevalidate(currentTick) && !asyncFillsInProgress.contains(room.getProvider().getPosition())) {
                roomsToValidate.add(room);
            }
        }

        // Stagger validation to avoid lag spikes
        // Use position hash to distribute work across ticks
        for (AtmosphereRoom room : roomsToValidate) {
            BlockPos pos = room.getProvider().getPosition();
            int hash = pos.hashCode();
            if ((hash & 0xF) == (currentTick & 0xF)) {
                validateRoom(room, currentTick);
            }
        }
    }

    /**
     * Validates a room by performing a flood fill.
     */
    private void validateRoom(AtmosphereRoom room, long currentTick) {
        IAtmosphereProvider provider = room.getProvider();

        // Check if provider is still active
        if (!provider.isActive()) {
            room.setInactive();
            return;
        }

        room.onRevalidationStarted(currentTick);

        // Determine start position (one block from machine)
        BlockPos machinePos = provider.getPosition();
        BlockPos startPos = machinePos.above(); // Start above the machine

        // Create config for this provider
        FloodFillConfig config = new FloodFillConfig(
                provider.getMaxRoomSize(),
                provider.getMaxHorizontalDimension());

        // Perform flood fill synchronously for now
        // TODO: Make async for large rooms
        RoomScan result = FloodFill.fill(level, level, startPos, config);

        // Update room from result
        room.updateFromResult(result);

        // Update chunk index
        updateChunkIndex(room);
    }

    /**
     * Updates the chunk index for a room after its interior changes.
     */
    private void updateChunkIndex(AtmosphereRoom room) {
        // First, remove from all chunks
        for (Map.Entry<ChunkPos, Set<AtmosphereRoom>> entry : chunkIndex.entrySet()) {
            entry.getValue().remove(room);
        }

        // Then add to new chunks
        for (ChunkPos chunkPos : room.getScan().touchedChunks()) {
            chunkIndex.computeIfAbsent(chunkPos, k -> new HashSet<>()).add(room);
        }

        // Clean up empty entries
        chunkIndex.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    /**
     * Processes pending async flood fill results.
     */
    private void processPendingResults() {
        PendingResult pending;
        while ((pending = pendingResults.poll()) != null) {
            BlockPos pos = pending.machinePos;
            asyncFillsInProgress.remove(pos);

            AtmosphereRoom room = rooms.get(pos);
            if (room != null) {
                room.updateFromResult(pending.result);
                updateChunkIndex(room);
            }
        }
    }

    /**
     * Checks if a position has oxygen (from any source - flood fill or bubble).
     *
     * @param pos Position to check
     * @return true if the position has oxygen
     */
    public boolean hasOxygen(BlockPos pos) {
        // Check flood-fill rooms first (more common)
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<AtmosphereRoom> roomsInChunk = chunkIndex.get(chunkPos);
        if (roomsInChunk != null) {
            for (AtmosphereRoom room : roomsInChunk) {
                if (room.hasAtmosphere(pos)) {
                    return true;
                }
            }
        }

        // Check bubble providers
        for (IBubbleProvider provider : bubbleProviders) {
            if (provider.containsPosition(pos)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a position has normal gravity (from gravity normalizers).
     *
     * @param pos Position to check
     * @return true if gravity is normalized at this position
     */
    public boolean hasNormalGravity(BlockPos pos) {
        // Only bubble providers handle gravity
        for (IBubbleProvider provider : bubbleProviders) {
            if (provider instanceof IGravityProvider && provider.containsPosition(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a position has normal temperature.
     *
     * @param pos Position to check
     * @return true if temperature is normalized at this position
     */
    public boolean hasNormalTemperature(BlockPos pos) {
        for (IBubbleProvider provider : bubbleProviders) {
            if (provider instanceof ITemperatureProvider && provider.containsPosition(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the room for a specific machine position.
     *
     * @param machinePos Position of the machine
     * @return The room, or null if not found
     */
    @Nullable
    public AtmosphereRoom getRoom(BlockPos machinePos) {
        return rooms.get(machinePos);
    }

    /**
     * @return Number of registered flood-fill rooms
     */
    public int getRoomCount() {
        return rooms.size();
    }

    /**
     * @return Number of registered bubble providers
     */
    public int getBubbleProviderCount() {
        return bubbleProviders.size();
    }

    /**
     * Pending async result holder.
     */
    private record PendingResult(BlockPos machinePos, RoomScan result) {
    }

    /**
     * Marker interface for gravity-normalizing bubble providers.
     */
    public interface IGravityProvider {
    }

    /**
     * Marker interface for temperature-regulating bubble providers.
     */
    public interface ITemperatureProvider {
    }
}
