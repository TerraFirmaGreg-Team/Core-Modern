package su.terrafirmagreg.core.common.atmosphere;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.events.GridSpatialEvent;
import appeng.api.networking.spatial.ISpatialService;

import su.terrafirmagreg.core.TFGCore;

/**
 * Global singleton managing atmosphere systems across all dimensions.
 * Handles event registration and provides the main API for atmosphere queries.
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class AtmosphereSystem {

    /** Per-dimension managers */
    private static final Map<ResourceKey<Level>, DimensionAtmosphereManager> managers = new ConcurrentHashMap<>();

    //    /** Whether the system is initialized */
    //    private static boolean initialized = false;

    private AtmosphereSystem() {
    }

    /**
     * Initializes the atmosphere system and registers event handlers.
     * Should be called during mod initialization.
     */
    public static void init() {
        //        if (initialized) {
        //            return;
        //        }

        GridHelper.addEventHandler(GridSpatialEvent.class, AtmosphereSystem::onGridSpatialEvent);
        //        initialized = true;
        TFGCore.LOGGER.info("Atmosphere system initialized");
    }

    /**
     * Gets the manager for a specific dimension, creating it if necessary.
     *
     * @param level The server level
     * @return The dimension manager
     */
    public static DimensionAtmosphereManager getManager(ServerLevel level) {
        return managers.computeIfAbsent(level.dimension(), k -> DimensionAtmosphereManager.get(level));
    }

    /**
     * Gets the manager for a dimension without creating it.
     *
     * @param dimension The dimension key
     * @return The manager, or null if none exists
     */
    @Nullable
    public static DimensionAtmosphereManager getManager(ResourceKey<Level> dimension) {
        return managers.get(dimension);
    }

    /**
     * Checks if a position has oxygen (server-side only).
     * For client-side queries (tooltips), use {@link su.terrafirmagreg.core.client.AtmosphereClientCache#get(BlockPos)}.
     *
     * @param level The level to check in
     * @param pos The position to check
     * @return true if the position has oxygen
     */
    public static boolean hasOxygen(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) {
            return false; // Use su.terrafirmagreg.core.client.AtmosphereClientCache instead
        }

        DimensionAtmosphereManager manager = managers.get(level.dimension());
        if (manager == null) {
            return false;
        }

        return manager.hasOxygen(pos);
    }

    // ==================== Interfaces ========================
    // TODO: Should this live here??
    public interface IOxygenProvider extends IAtmosphereMachine {
        boolean hasOxygen(BlockPos pos);
    }

    /**
     * Marker interface for gravity-normalizing bubble providers.
     */
    public interface IGravityProvider extends IAtmosphereMachine {
    }

    /**
     * Marker interface for temperature-regulating bubble providers.
     */
    public interface ITemperatureProvider extends IAtmosphereMachine {
    }

    // ==================== Async Handling ====================

    /** Executor for async jobs. */
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "FloodFill");
        t.setDaemon(true);
        return t;
    });

    public record ValidationJob(
            IFloodFillMachine machine,
            long earliestTick) {
    }

    static PriorityQueue<ValidationJob> validationQueue = new PriorityQueue<>(Comparator.comparingLong(ValidationJob::earliestTick));
    static Set<IFloodFillMachine> validationRequested = new HashSet<>();
    static Queue<IFloodFillMachine> doneValidating = new ConcurrentLinkedQueue<>();

    public static void requestValidation(IFloodFillMachine machine, long earliestTick) {
        if (!validationRequested.add(machine))
            return;

        validationQueue.add(new ValidationJob(machine, earliestTick));
    }

    private static void dispatchValidation(IFloodFillMachine machine) {
        EXECUTOR.submit(() -> {
            try {
                machine.validateAsync();
                doneValidating.add(machine); // Memory visibility guarantee
            } catch (Exception e) {
                TFGCore.LOGGER.error("Flood fill failed for room at {}", machine.getPos(), e);
            }
        });

        // Mark the room as clean to catch the case where the room is modified during the floodfill.
        // The floodfill result might be stale before it finishes, in which case we want to run a new floodfill.
        machine.setDirty(false);
    }

    private static void finalizeValidation(IFloodFillMachine machine) {
        validationRequested.remove(machine);
        machine.processValidationResult();
    }

    // ==================== Event Handling ====================

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        // Enqueue validation jobs
        long currentTick = server.getTickCount();
        ValidationJob job = validationQueue.peek();
        if (job != null && job.earliestTick() <= currentTick) {
            validationQueue.poll();
            dispatchValidation(job.machine());

        }

        // Process finished validation jobs
        IFloodFillMachine machine;
        while ((machine = doneValidating.poll()) != null) {
            finalizeValidation(machine);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent e) {
        onBlockChange(e);
    }

    @SubscribeEvent
    public static void EntityPlaceEvent(BlockEvent.EntityPlaceEvent e) {
        onBlockChange(e);
    }

    @SubscribeEvent
    public static void NeighborNotifyEvent(BlockEvent.NeighborNotifyEvent e) {
        onBlockChange(e);
    }

    @SubscribeEvent
    public static void FluidPlaceBlockEvent(BlockEvent.FluidPlaceBlockEvent e) {
        onBlockChange(e);
    }

    /**
     * Called when a block has potentially changed state
     * Dispatches to the applicable dimension manager
     * @param event The event that changes the block
     */
    public static void onBlockChange(BlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        DimensionAtmosphereManager manager = managers.get(serverLevel.dimension());
        if (manager != null) {
            manager.onBlockChange(event);
        }
    }

    /**
     * Called when an AE2 Spatial IO event happens
     * Dispatches to the applicable dimension manager
     * @param grid The AE2 grid
     * @param event The Spatial Event
     */
    public static void onGridSpatialEvent(IGrid grid, GridSpatialEvent event) {
        ISpatialService spatialService = grid.getSpatialService();

        if (!spatialService.hasRegion() || !spatialService.isValidRegion()) {
            return;
        }

        Level level = spatialService.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        DimensionAtmosphereManager manager = managers.get(serverLevel.dimension());
        if (manager != null) {
            manager.onGridSpatialEvent(spatialService.getMin(), spatialService.getMax());
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        DimensionAtmosphereManager manager = managers.get(serverLevel.dimension());
        if (manager != null) {
            ChunkPos chunkPos = event.getChunk().getPos();
            manager.onChunkLoaded(chunkPos);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Remove the manager for this dimension
        // TODO: Do we want to do this at all?
        managers.remove(serverLevel.dimension());
    }

    /**
     * Clears all managers. Called on server shutdown.
     */
    // TODO: Call on server shutdown?
    public void clear() {
        managers.clear();
    }
}
