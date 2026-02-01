package su.terrafirmagreg.core.common.atmosphere;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.events.GridSpatialEvent;
import appeng.api.networking.spatial.ISpatialService;
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

import su.terrafirmagreg.core.TFGCore;

/**
 * Global singleton managing atmosphere systems across all dimensions.
 * Handles event registration and provides the main API for atmosphere queries.
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class AtmosphereSystem {

    /** Per-dimension managers */
    private static final Map<ResourceKey<Level>, DimensionAtmosphereManager> managers = new ConcurrentHashMap<>();

    /** Whether the system is initialized */
    private static boolean initialized = false;

    private AtmosphereSystem() {
    }

    /**
     * Initializes the atmosphere system and registers event handlers.
     * Should be called during mod initialization.
     */
    public static void init() {
        if (initialized) {
            return;
        }

        GridHelper.addEventHandler(GridSpatialEvent.class, AtmosphereSystem::onGridSpatialEvent);
        initialized = true;
        TFGCore.LOGGER.info("Atmosphere system initialized");
    }

    /**
     * Gets the manager for a specific dimension, creating it if necessary.
     *
     * @param level The server level
     * @return The dimension manager
     */
    public static DimensionAtmosphereManager getManager(ServerLevel level) {
        return managers.computeIfAbsent(level.dimension(), k -> new DimensionAtmosphereManager(level));
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

    // ==================== Public API Methods ====================

    /**
     * Registers a flood-fill atmosphere provider (eg Oxygen Distributor).
     *
     * @param provider The provider to register
     */
    public static void registerProvider(IAtmosphereProvider provider) {
        Level level = provider.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            getManager(serverLevel).addProvider(provider);
        }
    }

    /**
     * Unregisters a flood-fill atmosphere provider.
     *
     * @param provider The provider to remove
     */
    public static void unregisterProvider(IAtmosphereProvider provider) {
        Level level = provider.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            DimensionAtmosphereManager manager = managers.get(serverLevel.dimension());
            if (manager != null) {
                manager.removeProvider(provider);
            }
        }
    }

    /**
     * Registers a bubble provider (eg Gravity Normalizer).
     *
     * @param provider The provider to register
     */
    public static void registerBubbleProvider(IBubbleProvider provider) {
        Level level = provider.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            getManager(serverLevel).addBubbleProvider(provider);
        }
    }

    /**
     * Unregisters a bubble provider.
     *
     * @param provider The provider to remove
     */
    public static void unregisterBubbleProvider(IBubbleProvider provider) {
        Level level = provider.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            DimensionAtmosphereManager manager = managers.get(serverLevel.dimension());
            if (manager != null) {
                manager.removeBubbleProvider(provider);
            }
        }
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

    /**
     * Checks if a position has normal gravity.
     *
     * @param level The level to check in
     * @param pos The position to check
     * @return true if gravity is normalized
     */
    public static boolean hasNormalGravity(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) {
            return false;
        }

        DimensionAtmosphereManager manager = managers.get(level.dimension());
        if (manager == null) {
            return false;
        }

        return manager.hasNormalGravity(pos);
    }

    /**
     * Checks if a position has normal temperature.
     *
     * @param level The level to check in
     * @param pos The position to check
     * @return true if temperature is normalized
     */
    public static boolean hasNormalTemperature(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) {
            return false;
        }

        DimensionAtmosphereManager manager = managers.get(level.dimension());
        if (manager == null) {
            return false;
        }

        return manager.hasNormalTemperature(pos);
    }

    // ==================== Event Handlers ====================

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        long currentTick = server.getTickCount();

        // Tick all dimension managers
        for (DimensionAtmosphereManager manager : managers.values()) {
            manager.tick(currentTick);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent e) {onBlockChange(e);}
    @SubscribeEvent
    public static void EntityPlaceEvent(BlockEvent.EntityPlaceEvent e) {onBlockChange(e);}
    @SubscribeEvent
    public static void NeighborNotifyEvent(BlockEvent.NeighborNotifyEvent e) {onBlockChange(e);}
    @SubscribeEvent
    public static void FluidPlaceBlockEvent(BlockEvent.FluidPlaceBlockEvent e) {onBlockChange(e);}

    /**
     * Called when a block has potentially changed state
     * @param event The event that changes the block
     */
    public static void onBlockChange(BlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        DimensionAtmosphereManager manager = managers.get(serverLevel.dimension());
        if (manager != null) {
            manager.onBlockChanged(event);
        }
    }

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
            manager.onGridSpatialEvent(spatialService, event);
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
        managers.remove(serverLevel.dimension());
    }

    /**
     * Clears all managers. Called on server shutdown.
     */
    public void clear() {
        managers.clear();
    }
}
