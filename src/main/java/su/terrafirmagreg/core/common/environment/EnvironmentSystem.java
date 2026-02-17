package su.terrafirmagreg.core.common.environment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import su.terrafirmagreg.core.client.EnvironmentClientCache;

/**
 * Global class with static state managing environment systems across all dimensions.
 * Handles event registration and provides the main API for environment queries.
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class EnvironmentSystem {

    /** Per-dimension managers */
    private static final Map<ResourceKey<Level>, DimEnvManager> managers = new ConcurrentHashMap<>();

    private EnvironmentSystem() {
    }

    /**
     * Initializes the environment system and registers spatial event handler.
     * Should be called during mod initialization.
     */
    public static void init() {
        GridHelper.addEventHandler(GridSpatialEvent.class, EnvironmentSystem::onGridSpatialEvent);
        TFGCore.LOGGER.info("Atmosphere system initialized");
    }

    /**
     * Gets the manager for a specific dimension, creating it if necessary.
     *
     * @param level The server level
     * @return The dimension manager
     */
    public static DimEnvManager getManager(ServerLevel level) {
        return managers.computeIfAbsent(level.dimension(), k -> DimEnvManager.get(level));
    }

    /**
     * Checks if a position has oxygen (server-side only).
     * For client-side queries (tooltips), use {@link EnvironmentClientCache#get(BlockPos)}.
     *
     * @param level The level to check in
     * @param pos The position to check
     * @return true if the position has oxygen
     */
    public static boolean hasOxygen(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) {
            return false; // Use su.terrafirmagreg.core.client.EnvironmentClientCache instead
        }

        DimEnvManager manager = managers.get(level.dimension());
        if (manager == null) {
            return false;
        }

        return manager.hasOxygen(pos);
    }

    /**
     * Checks if a position has safe temperature (server-side only).
     *
     * @param level The level to check in
     * @param pos The position to check
     * @return true if the position has safe temperature
     */
    public static boolean hasTemperature(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) {
            return false;
        }

        DimEnvManager manager = managers.get(level.dimension());
        if (manager == null) {
            return false;
        }

        return manager.hasTemperature(pos);
    }

    /**
     * Checks if a dimension has normal gravity (planet-level, no per-position providers yet).
     *
     * @param level The level to check
     * @return true if the dimension has Earth-like gravity
     */
    public static boolean hasNormalGravity(Level level) {
        DimEnvManager manager = managers.get(level.dimension());
        return manager != null && manager.getEnvironment().hasNormalGravity();
    }

    // ==================== Async Handling ====================

    /** Executor for async jobs (flood fill, diagnostic trace). */
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "FloodFill");
        t.setDaemon(true);
        return t;
    });

    public record ValidationJob(
            IBlockSensitiveMachine machine,
            long earliestTick) {
    }

    static PriorityQueue<ValidationJob> validationQueue = new PriorityQueue<>(Comparator.comparingLong(ValidationJob::earliestTick));
    static Set<IBlockSensitiveMachine> validationRequested = new HashSet<>();
    static Queue<IBlockSensitiveMachine> doneValidating = new ConcurrentLinkedQueue<>();

    public static void cancelValidation(IBlockSensitiveMachine machine) {
        if (validationRequested.remove(machine)) {
            validationQueue.removeIf(job -> job.machine() == machine);
            TFGCore.LOGGER.info("[validation] cancelValidation, pos={}, identity={}",
                    machine.getPos(), System.identityHashCode(machine));
        }
    }

    public static void requestValidation(IBlockSensitiveMachine machine, long earliestTick) {
        if (!validationRequested.add(machine)) {
            TFGCore.LOGGER.info("[validation] requestValidation SKIPPED (already requested), pos={}, identity={}, earliestTick={}",
                    machine.getPos(), System.identityHashCode(machine), earliestTick);
            return;
        }

        TFGCore.LOGGER.info("[validation] requestValidation QUEUED, pos={}, identity={}, earliestTick={}, queueSize={}",
                machine.getPos(), System.identityHashCode(machine), earliestTick, validationQueue.size() + 1);
        validationQueue.add(new ValidationJob(machine, earliestTick));
    }

    private static void dispatchValidation(IBlockSensitiveMachine machine) {
        TFGCore.LOGGER.info("[validation] dispatchValidation, pos={}, identity={}",
                machine.getPos(), System.identityHashCode(machine));

        // Create the AsyncBlockReader on the main thread — it captures ChunkMap safely
        AsyncBlockReader reader = new AsyncBlockReader(machine.getServerLevel());

        long startTime = System.nanoTime();
        EXECUTOR.submit(() -> {
            long threadStartTime = System.nanoTime();
            TFGCore.LOGGER.info("[validation] async thread started, pos={}, identity={}, waitedMs={}",
                    machine.getPos(), System.identityHashCode(machine),
                    (threadStartTime - startTime) / 1_000_000);
            try {
                machine.validateAsync(reader);
                long elapsed = (System.nanoTime() - threadStartTime) / 1_000_000;
                TFGCore.LOGGER.info("[validation] async thread finished, pos={}, identity={}, elapsedMs={}",
                        machine.getPos(), System.identityHashCode(machine), elapsed);
                doneValidating.add(machine); // Memory visibility guarantee
            } catch (Exception e) {
                TFGCore.LOGGER.error("Flood fill failed for room at {}", machine.getPos(), e);
            }
        });

        // Mark the room as clean to catch the case where the room is modified during the flood fill.
        machine.setDirty(false);
    }

    private static void finalizeValidation(IBlockSensitiveMachine machine) {
        TFGCore.LOGGER.info("[validation] finalizeValidation, pos={}, identity={}, isDirty={}",
                machine.getPos(), System.identityHashCode(machine), machine.isDirty());
        validationRequested.remove(machine);
        machine.processValidationResult();

        // If the machine got dirty during the async fill (block changed while scanning),
        // the validation result is stale. Re-request validation.
        if (machine.isDirty()) {
            TFGCore.LOGGER.info("[validation] re-requesting (dirty during async), pos={}, identity={}",
                    machine.getPos(), System.identityHashCode(machine));
            requestValidation(machine, 0);
        }
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
            TFGCore.LOGGER.info("[validation] tick {} dispatching job, earliestTick={}, pos={}, identity={}",
                    currentTick, job.earliestTick(), job.machine().getPos(), System.identityHashCode(job.machine()));
            dispatchValidation(job.machine());
        }

        // Process finished validation jobs
        IBlockSensitiveMachine machine;
        while ((machine = doneValidating.poll()) != null) {
            TFGCore.LOGGER.info("[validation] tick {} finalizing, pos={}, identity={}",
                    currentTick, machine.getPos(), System.identityHashCode(machine));
            finalizeValidation(machine);
        }

        // Tick decompression events in all dimensions
        managers.values().forEach(DimEnvManager::tickDecompressions);
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

        DimEnvManager manager = managers.get(serverLevel.dimension());
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

        DimEnvManager manager = managers.get(serverLevel.dimension());
        if (manager != null) {
            manager.onGridSpatialEvent(spatialService.getMin(), spatialService.getMax());
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        DimEnvManager manager = managers.get(serverLevel.dimension());
        if (manager != null) {
            ChunkPos chunkPos = event.getChunk().getPos();
            manager.onChunkLoad(chunkPos);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Remove the manager for this dimension
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
