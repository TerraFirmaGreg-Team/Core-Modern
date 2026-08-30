package su.terrafirmagreg.core.common.environment;

import static su.terrafirmagreg.core.common.environment.PassabilityChecker.getCachedPassInfo;

import java.util.*;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.BlockEvent;

import earth.terrarium.adastra.common.blocks.SlidingDoorBlock;
import earth.terrarium.adastra.common.blocks.properties.SlidingDoorPartProperty;
import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.environment.PassabilityChecker.PassInfo;
import su.terrafirmagreg.core.common.environment.PassabilityChecker.PassInfo.PassType;

/**
 * Manages environment providers for a single dimension.
 * Extends SavedData to persist oxygen provider data across world saves.
 */
public class DimEnvManager extends SavedData {

    /** The level this manager is for */
    @Getter
    private final ServerLevel level;

    /** Natural environmental properties of this dimension */
    @Getter
    private final DimensionEnvironment environment;

    /** Map of Chunks to Machines that want to be notified of block change events in those chunks */
    public final ChunkRegistry<IBlockSensitiveMachine> blockChangeListeners = new ChunkRegistry<>();

    /** Map of Chunks to Machines that want to be notified of chunk loads */
    public final ChunkRegistry<IBlockSensitiveMachine> chunkLoadListeners = new ChunkRegistry<>();

    /** Oxygen providers keyed by machine position. Persisted to NBT. */
    private final Map<BlockPos, OxygenProvider> oxygenProviders = new HashMap<>();
    /** Map of Chunks to OxygenProviders that affect oxygen in those Chunks */
    public final ChunkRegistry<OxygenProvider> oxygenIndex = new ChunkRegistry<>();

    /** Pressure providers keyed by machine position. Persisted to NBT. */
    private final Map<BlockPos, PressureProvider> pressureProviders = new HashMap<>();
    /** Map of Chunks to PressureProviders that affect pressure in those Chunks */
    public final ChunkRegistry<PressureProvider> pressureIndex = new ChunkRegistry<>();

    /** Temperature providers keyed by machine position. Persisted to NBT. */
    private final Map<BlockPos, TemperatureProvider> temperatureProviders = new HashMap<>();
    /** Map of Chunks to TemperatureProviders that affect temperature in those Chunks */
    public final ChunkRegistry<TemperatureProvider> temperatureIndex = new ChunkRegistry<>();

    /** Gravity providers keyed by machine position. Persisted to NBT. */
    private final Map<BlockPos, GravityProvider> gravityProviders = new HashMap<>();
    /** Map of Chunks to GravityProviders that affect gravity in those Chunks */
    public final ChunkRegistry<GravityProvider> gravityIndex = new ChunkRegistry<>();

    /** Active decompression events in this dimension */
    private final List<DecompressionEvent> activeDecompressions = new ArrayList<>();

    // ==================== Construction & SavedData ====================

    private static final String DATA_NAME = "tfg_atmosphere_system";

    public DimEnvManager(ServerLevel level) {
        this.level = level;
        this.environment = DimensionEnvironment.get(level.dimension());
    }

    /** Gets or creates the manager for a level via Minecraft's data storage. */
    public static DimEnvManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                tag -> load(level, tag),
                () -> new DimEnvManager(level),
                DATA_NAME);
    }

    /**
     * Load stored providers from SavedData.
     */
    private static DimEnvManager load(ServerLevel level, CompoundTag tag) {
        DimEnvManager manager = new DimEnvManager(level);

        ListTag list = tag.getList("oxygenProviders", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag providerTag = list.getCompound(i);
            try {
                OxygenProvider provider = OxygenProvider.load(providerTag);
                manager.oxygenProviders.put(provider.getMachinePos(), provider);

                // Register sealed providers into the chunk registry
                if (provider.getRoomScan().isSealed()) {
                    manager.oxygenIndex.add(provider, provider.getTouchedChunks());
                }
            } catch (Exception e) {
                TFGCore.LOGGER.error("Failed to load oxygen provider from NBT", e);
            }
        }

        TFGCore.LOGGER.debug("Loaded {} oxygen providers from saved data", manager.oxygenProviders.size());

        // Load pressure providers
        ListTag pressureList = tag.getList("pressureProviders", Tag.TAG_COMPOUND);
        for (int i = 0; i < pressureList.size(); i++) {
            CompoundTag providerTag = pressureList.getCompound(i);
            try {
                PressureProvider provider = PressureProvider.load(providerTag);
                manager.pressureProviders.put(provider.getMachinePos(), provider);
                if (provider.getRoomScan().isSealed()) {
                    manager.pressureIndex.add(provider, provider.getTouchedChunks());
                }
            } catch (Exception e) {
                TFGCore.LOGGER.error("Failed to load pressure provider from NBT", e);
            }
        }
        TFGCore.LOGGER.debug("Loaded {} pressure providers from saved data", manager.pressureProviders.size());

        // Load temperature providers
        ListTag tempList = tag.getList("temperatureProviders", Tag.TAG_COMPOUND);
        for (int i = 0; i < tempList.size(); i++) {
            CompoundTag providerTag = tempList.getCompound(i);
            try {
                TemperatureProvider provider = TemperatureProvider.load(providerTag);
                manager.temperatureProviders.put(provider.getMachinePos(), provider);
                manager.temperatureIndex.add(provider, provider.getAffectedChunks());
            } catch (Exception e) {
                TFGCore.LOGGER.error("Failed to load temperature provider from NBT", e);
            }
        }
        TFGCore.LOGGER.debug("Loaded {} temperature providers from saved data", manager.temperatureProviders.size());

        // Load gravity providers
        ListTag gravityList = tag.getList("gravityProviders", Tag.TAG_COMPOUND);
        for (int i = 0; i < gravityList.size(); i++) {
            CompoundTag providerTag = gravityList.getCompound(i);
            try {
                GravityProvider provider = GravityProvider.load(providerTag);
                manager.gravityProviders.put(provider.getMachinePos(), provider);
                manager.gravityIndex.add(provider, provider.getAffectedChunks());
            } catch (Exception e) {
                TFGCore.LOGGER.error("Failed to load gravity provider from NBT", e);
            }
        }
        TFGCore.LOGGER.debug("Loaded {} gravity providers from saved data", manager.gravityProviders.size());

        return manager;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();

        for (OxygenProvider provider : oxygenProviders.values()) {
            // Only persist sealed providers, the rest are irrelevant and will register themselves on chunkload
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

        tag.put("oxygenProviders", list);
        TFGCore.LOGGER.info("Saved {} sealed oxygen providers", list.size());

        // Save pressure providers
        ListTag pressureList = new ListTag();
        for (PressureProvider provider : pressureProviders.values()) {
            if (!provider.getRoomScan().isSealed())
                continue;
            try {
                CompoundTag providerTag = new CompoundTag();
                provider.save(providerTag);
                pressureList.add(providerTag);
            } catch (Exception e) {
                TFGCore.LOGGER.error("Failed to save pressure provider at {}", provider.getMachinePos(), e);
            }
        }
        tag.put("pressureProviders", pressureList);
        TFGCore.LOGGER.info("Saved {} sealed pressure providers", pressureList.size());

        // Save temperature providers
        ListTag tempList = new ListTag();
        for (TemperatureProvider provider : temperatureProviders.values()) {
            try {
                CompoundTag providerTag = new CompoundTag();
                provider.save(providerTag);
                tempList.add(providerTag);
            } catch (Exception e) {
                TFGCore.LOGGER.error("Failed to save temperature provider at {}", provider.getMachinePos(), e);
            }
        }
        tag.put("temperatureProviders", tempList);
        TFGCore.LOGGER.info("Saved {} temperature providers", tempList.size());

        // Save gravity providers
        ListTag gravityList = new ListTag();
        for (GravityProvider provider : gravityProviders.values()) {
            try {
                CompoundTag providerTag = new CompoundTag();
                provider.save(providerTag);
                gravityList.add(providerTag);
            } catch (Exception e) {
                TFGCore.LOGGER.error("Failed to save gravity provider at {}", provider.getMachinePos(), e);
            }
        }
        tag.put("gravityProviders", gravityList);
        TFGCore.LOGGER.info("Saved {} gravity providers", gravityList.size());

        return tag;
    }

    private void setSavedDataDirty() {
        setDirty();
    }

    /**
     * @return All oxygen providers in this dimension
     */
    public Map<BlockPos, OxygenProvider> getProviders() {
        return Collections.unmodifiableMap(oxygenProviders);
    }

    /**
     * @return All temperature providers in this dimension
     */
    public Map<BlockPos, TemperatureProvider> getTempProviders() {
        return Collections.unmodifiableMap(temperatureProviders);
    }

    /**
     * @return All gravity providers in this dimension
     */
    public Map<BlockPos, GravityProvider> getGravityProviders() {
        return Collections.unmodifiableMap(gravityProviders);
    }

    // ==================== Oxygen Provider Management ====================

    /**
     * Gets an existing provider or creates a new one at the given position.
     * Called when a machine loads and needs to attach.
     */
    public OxygenProvider getOrCreateProvider(BlockPos machinePos) {
        return oxygenProviders.computeIfAbsent(machinePos, pos -> {
            setSavedDataDirty();
            return new OxygenProvider(pos);
        });
    }

    /**
     * Removes an oxygen provider entirely. Called when a machine is broken.
     */
    public void removeProvider(BlockPos machinePos) {
        OxygenProvider provider = oxygenProviders.remove(machinePos);
        if (provider != null) {
            oxygenIndex.remove(provider, provider.getTouchedChunks());
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

            oxygenIndex.update(provider, toRemove, toAdd);
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
        if (environment.hasOxygen())
            return true;

        if (oxygenIndex.isEmpty())
            return false;

        ProfilerFiller profiler = level.getProfiler();
        profiler.push("tfg.environment.hasOxygen");
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<OxygenProvider> providerSet = oxygenIndex.get(chunkPos);
        if (providerSet != null) {
            for (OxygenProvider provider : providerSet) {
                if (provider.hasOxygen(pos)) {
                    profiler.pop();
                    return true;
                }
            }
        }
        profiler.pop();
        return false;
    }

    /**
     * Gets the ambient pressure at a position, in atmospheres.
     * Currently returns the dimension default, but may depend on Y-level in the future
     * (e.g. Europa ocean depth).
     */
    public float getPressure(BlockPos pos) {
        return environment.pressure();
    }

    // ==================== Decompression Events ====================

    public DecompressionEvent startDecompression(BlockPos breachPoint, RoomScan oldRoomScan) {
        for (DecompressionEvent existing : activeDecompressions) {
            if (existing.getBreachPoint().equals(breachPoint))
                return null;
        }
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
     * Tick all active decompression events. Called from EnvironmentSystem.onServerTick.
     */
    public void tickDecompressions() {
        activeDecompressions.removeIf(event -> !event.tick(level));
    }

    // ==================== Pressure Provider Management ====================

    public PressureProvider getOrCreatePressureProvider(BlockPos machinePos) {
        return pressureProviders.computeIfAbsent(machinePos, pos -> {
            setSavedDataDirty();
            return new PressureProvider(pos);
        });
    }

    public void removePressureProvider(BlockPos machinePos) {
        PressureProvider provider = pressureProviders.remove(machinePos);
        if (provider != null) {
            pressureIndex.remove(provider, provider.getTouchedChunks());
            setSavedDataDirty();
        }
    }

    public void updatePressureProvider(PressureProvider provider, RoomScan oldScan, RoomScan newScan) {
        Set<ChunkPos> oldChunks = oldScan.isSealed() ? oldScan.touchedChunks() : Set.of();
        Set<ChunkPos> newChunks = newScan.isSealed() ? newScan.touchedChunks() : Set.of();

        if (!oldChunks.isEmpty() || !newChunks.isEmpty()) {
            Set<ChunkPos> toRemove = new HashSet<>(oldChunks);
            toRemove.removeAll(newChunks);

            Set<ChunkPos> toAdd = new HashSet<>(newChunks);
            toAdd.removeAll(oldChunks);

            pressureIndex.update(provider, toRemove, toAdd);
        }

        provider.setRoomScan(newScan);
        setSavedDataDirty();
    }

    // ==================== Pressure Queries ====================

    public boolean hasSafePressure(BlockPos pos) {
        if (pressureIndex.isEmpty())
            return false;

        ChunkPos chunkPos = new ChunkPos(pos);
        Set<PressureProvider> providerSet = pressureIndex.get(chunkPos);
        if (providerSet != null) {
            for (PressureProvider provider : providerSet) {
                if (provider.hasSafePressure(pos))
                    return true;
            }
        }
        return false;
    }

    // ==================== Temperature Provider Management ====================

    /**
     * Gets an existing temperature provider or creates a new one.
     * Called when a temperature machine loads.
     */
    public TemperatureProvider getOrCreateTempProvider(BlockPos machinePos, int radius) {
        return temperatureProviders.computeIfAbsent(machinePos, pos -> {
            TemperatureProvider provider = new TemperatureProvider(pos, radius);
            temperatureIndex.add(provider, provider.getAffectedChunks());
            setSavedDataDirty();
            return provider;
        });
    }

    /**
     * Replaces a temperature provider with a new radius. Called when the machine's radius changes.
     * Returns the new provider (already registered in chunk registry).
     */
    public TemperatureProvider updateTempProvider(BlockPos machinePos, int newRadius) {
        TemperatureProvider old = temperatureProviders.remove(machinePos);
        if (old != null) {
            temperatureIndex.remove(old, old.getAffectedChunks());
        }
        TemperatureProvider newProvider = new TemperatureProvider(machinePos, newRadius);
        temperatureProviders.put(machinePos, newProvider);
        temperatureIndex.add(newProvider, newProvider.getAffectedChunks());
        setSavedDataDirty();
        return newProvider;
    }

    /**
     * Removes a temperature provider. Called when a temperature machine is broken.
     */
    public void removeTempProvider(BlockPos machinePos) {
        TemperatureProvider provider = temperatureProviders.remove(machinePos);
        if (provider != null) {
            temperatureIndex.remove(provider, provider.getAffectedChunks());
            setSavedDataDirty();
        }
    }

    // ==================== Temperature Queries ====================

    /**
     * Checks if a position has safe temperature (from natural environment or a machine bubble).
     */
    public boolean hasTemperature(BlockPos pos) {
        if (environment.hasNormalTemperature())
            return true;

        if (temperatureIndex.isEmpty())
            return false;

        ProfilerFiller profiler = level.getProfiler();
        profiler.push("tfg.environment.hasTemperature");
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<TemperatureProvider> providerSet = temperatureIndex.get(chunkPos);
        if (providerSet != null) {
            for (TemperatureProvider provider : providerSet) {
                if (provider.hasTemperature(pos)) {
                    profiler.pop();
                    return true;
                }
            }
        }
        profiler.pop();
        return false;
    }

    /**
     * Get a position's target temperature from a machine bubble.
     */
    public Optional<Float> getTargetTemperature(BlockPos pos) {
        if (temperatureIndex.isEmpty())
            return Optional.empty();

        ProfilerFiller profiler = level.getProfiler();
        profiler.push("tfg.environment.getTemperature");
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<TemperatureProvider> providerSet = temperatureIndex.get(chunkPos);
        if (providerSet != null) {
            for (TemperatureProvider provider : providerSet) {
                Optional<Float> targetTemperature = provider.getTargetTemperature(pos);
                if (targetTemperature.isPresent()) {
                    profiler.pop();
                    return targetTemperature;
                }
            }
        }
        profiler.pop();
        return Optional.empty();
    }

    // ==================== Gravity Provider Management ====================

    /**
     * Gets an existing gravity provider or creates a new one.
     * Called when a gravity machine loads.
     */
    public GravityProvider getOrCreateGravityProvider(BlockPos machinePos, int radius) {
        return gravityProviders.computeIfAbsent(machinePos, pos -> {
            GravityProvider provider = new GravityProvider(pos, radius);
            gravityIndex.add(provider, provider.getAffectedChunks());
            setSavedDataDirty();
            return provider;
        });
    }

    /**
     * Removes a gravity provider. Called when a gravity machine is broken.
     */
    public void removeGravityProvider(BlockPos machinePos) {
        GravityProvider provider = gravityProviders.remove(machinePos);
        if (provider != null) {
            gravityIndex.remove(provider, provider.getAffectedChunks());
            setSavedDataDirty();
        }
    }

    // ==================== Gravity Queries ====================

    /**
     * Checks if a position has normal (Earth-like) gravity (from natural environment or a machine bubble).
     */
    public boolean hasNormalGravity(BlockPos pos) {
        if (environment.hasNormalGravity())
            return true;

        if (gravityIndex.isEmpty())
            return false;

        ProfilerFiller profiler = level.getProfiler();
        profiler.push("tfg.environment.hasNormalGravity");
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<GravityProvider> providerSet = gravityIndex.get(chunkPos);
        if (providerSet != null) {
            for (GravityProvider provider : providerSet) {
                if (provider.hasNormalGravity(pos)) {
                    profiler.pop();
                    return true;
                }
            }
        }
        profiler.pop();
        return false;
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
        if (blockChangeListeners.get(new ChunkPos(pos)) == null)
            return;

        if (event instanceof BlockEvent.BreakEvent breakEvent) {
            TFGCore.LOGGER.info("breakEvent {}", breakEvent.getState());
            PassInfo before = getCachedPassInfo(breakEvent.getState());
            if (before.type() == PassType.EMPTY) {
                TFGCore.LOGGER.info("Ignored - empty block");
                return;
            }

            // ad astra airlock break
            if (breakEvent.getState().getBlock() instanceof SlidingDoorBlock) {
                dispatchSlidingDoorPositions(breakEvent.getState(), pos);
                return;
            }

        } else if (event instanceof BlockEvent.EntityPlaceEvent placeEvent) {
            TFGCore.LOGGER.info("placeEvent {} {}", placeEvent.getBlockSnapshot().getReplacedBlock(), placeEvent.getPlacedBlock());
            PassInfo before = getCachedPassInfo(placeEvent.getBlockSnapshot().getReplacedBlock());
            PassInfo after = getCachedPassInfo(placeEvent.getPlacedBlock());
            if (before.equals(after) && before.type() != PassType.NO_CACHE) {
                TFGCore.LOGGER.info("Ignored - passability unchanged");
                return;
            }

            // ad astra airlock place
            if (placeEvent.getPlacedBlock().getBlock() instanceof SlidingDoorBlock) {
                dispatchSlidingDoorPositions(placeEvent.getPlacedBlock(), pos);
                return;
            }

        } else if (event instanceof BlockEvent.NeighborNotifyEvent nighEvent) {
            TFGCore.LOGGER.info("neighborNotifyEvent {}", nighEvent.getState());
            PassInfo passInfo = getCachedPassInfo(nighEvent.getState());
            // NO_CACHE blocks (airlocks, pistons, etc.) have dynamic passability, always dispatch
            if (passInfo.type() == PassType.NO_CACHE) {
                TFGCore.LOGGER.info("Dynamic block (NO_CACHE), always dispatching");
            } else {
                if (passInfo.type() == PassType.EMPTY || passInfo.type() == PassType.FULL) {
                    TFGCore.LOGGER.info("Ignored - stable block type");
                    return;
                }
            }
        }

        dispatchToMachines(pos);
    }

    /**
     * Expands a SlidingDoorBlock event to all 9 block positions of the structure.
     * This is necessary for breaking and placing because the other 8 block updates don't fire events.
     */
    private void dispatchSlidingDoorPositions(BlockState doorState, BlockPos eventPos) {
        SlidingDoorPartProperty part = doorState.getValue(SlidingDoorBlock.PART);
        Direction facing = doorState.getValue(SlidingDoorBlock.FACING);
        Direction sideways = facing.getClockWise();

        // Resolve controller (BOTTOM) position from any part.
        BlockPos controller = eventPos
                .relative(sideways, -part.xOffset())
                .below(part.yOffset());

        TFGCore.LOGGER.info("SlidingDoor expansion: controller={}, from part={}", controller, part);

        for (SlidingDoorPartProperty p : SlidingDoorPartProperty.values()) {
            BlockPos partPos = controller.relative(sideways, p.xOffset()).above(p.yOffset());
            dispatchToMachines(partPos);
        }
    }

    /** Dispatches a block change at the given position to all machines listening in that chunk. */
    private void dispatchToMachines(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<IBlockSensitiveMachine> machines = blockChangeListeners.get(chunkPos);
        if (machines == null)
            return;

        TFGCore.LOGGER.info("Dispatching block change at {} to {} machines", pos, machines.size());
        for (IBlockSensitiveMachine machine : machines) {
            machine.onBlockChangeAt(pos);
        }
    }

    /**
     * Called when an AE2 spatial IO event happens.
     */
    public void onGridSpatialEvent(BlockPos min, BlockPos max) {
        ChunkPos minChunk = new ChunkPos(min);
        ChunkPos maxChunk = new ChunkPos(max);

        Set<IBlockSensitiveMachine> machinesAffected = new HashSet<>();
        for (int cx = minChunk.x; cx <= maxChunk.x; cx++) {
            for (int cz = minChunk.z; cz <= maxChunk.z; cz++) {
                var machinesInChunk = blockChangeListeners.get(new ChunkPos(cx, cz));
                if (machinesInChunk != null) {
                    machinesAffected.addAll(machinesInChunk);
                }
            }
        }
        for (IBlockSensitiveMachine machine : machinesAffected) {
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
        Set<IBlockSensitiveMachine> listeners = chunkLoadListeners.get(chunkPos);
        if (listeners != null) {
            for (IBlockSensitiveMachine machine : listeners) {
                machine.onChunkLoad(chunkPos);
            }
        }

        checkOrphanedProviders(chunkPos);
    }

    /**
     * Check if any providers in this chunk have lost their machine. This might happen with worldedit.
     * <p>
     * The orphan check is deferred to the next tick. By then, machine onLoad() has already fired
     * and the machine has attached to its provider. Any provider still without a machine is orphaned.
     */
    private void checkOrphanedProviders(ChunkPos chunkPos) {
        List<BlockPos> oxygenInChunk = oxygenProviders.keySet().stream()
                .filter(pos -> new ChunkPos(pos).equals(chunkPos))
                .toList();

        List<BlockPos> pressureInChunk = pressureProviders.keySet().stream()
                .filter(pos -> new ChunkPos(pos).equals(chunkPos))
                .toList();

        List<BlockPos> tempInChunk = temperatureProviders.keySet().stream()
                .filter(pos -> new ChunkPos(pos).equals(chunkPos))
                .toList();

        List<BlockPos> gravityInChunk = gravityProviders.keySet().stream()
                .filter(pos -> new ChunkPos(pos).equals(chunkPos))
                .toList();

        if (oxygenInChunk.isEmpty() && pressureInChunk.isEmpty() && tempInChunk.isEmpty() && gravityInChunk.isEmpty())
            return;

        level.getServer().tell(new net.minecraft.server.TickTask(
                level.getServer().getTickCount() + 1,
                () -> {
                    for (BlockPos providerPos : oxygenInChunk) {
                        OxygenProvider provider = oxygenProviders.get(providerPos);
                        if (provider != null && !provider.isMachineLoaded()) {
                            removeProvider(providerPos);
                            TFGCore.LOGGER.debug("Removing orphaned oxygen provider at {}", providerPos);
                        }
                    }
                    for (BlockPos providerPos : pressureInChunk) {
                        PressureProvider provider = pressureProviders.get(providerPos);
                        if (provider != null && !provider.isMachineLoaded()) {
                            removePressureProvider(providerPos);
                            TFGCore.LOGGER.debug("Removing orphaned pressure provider at {}", providerPos);
                        }
                    }
                    for (BlockPos providerPos : tempInChunk) {
                        TemperatureProvider provider = temperatureProviders.get(providerPos);
                        if (provider != null && !provider.isMachineLoaded()) {
                            removeTempProvider(providerPos);
                            TFGCore.LOGGER.debug("Removing orphaned temperature provider at {}", providerPos);
                        }
                    }
                    for (BlockPos providerPos : gravityInChunk) {
                        GravityProvider provider = gravityProviders.get(providerPos);
                        if (provider != null && !provider.isMachineLoaded()) {
                            removeGravityProvider(providerPos);
                            TFGCore.LOGGER.debug("Removing orphaned gravity provider at {}", providerPos);
                        }
                    }
                }));
    }

    // ==================== ChunkRegistry ====================

    /**
     * Wrapper around a Map<ChunkPos, Set<T>> that handles adding and removing.
     * Meant for quick lookup of providers and machines based on the ChunkPos.
     */
    public static class ChunkRegistry<T> {
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

        public boolean isEmpty() {
            return map.isEmpty();
        }
    }
}
