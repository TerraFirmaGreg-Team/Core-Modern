package su.terrafirmagreg.core.common.atmosphere;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import su.terrafirmagreg.core.TFGCore;

/**
 * Global singleton managing atmosphere systems across all dimensions.
 * Handles event registration and provides the main API for atmosphere queries.
 */
public final class AtmosphereSystem {

    private static final AtmosphereSystem INSTANCE = new AtmosphereSystem();

    /** Per-dimension managers */
    private final Map<ResourceKey<Level>, DimensionAtmosphereManager> managers = new ConcurrentHashMap<>();

    /** Whether the system is initialized */
    private boolean initialized = false;

    private AtmosphereSystem() {
    }

    /** @return The singleton instance */
    public static AtmosphereSystem get() {
        return INSTANCE;
    }

    /**
     * Initializes the atmosphere system and registers event handlers.
     * Should be called during mod initialization.
     */
    public static void init() {
        if (INSTANCE.initialized) {
            return;
        }

        MinecraftForge.EVENT_BUS.register(INSTANCE);
        INSTANCE.initialized = true;
        TFGCore.LOGGER.info("Atmosphere system initialized");
    }

    /**
     * Gets the manager for a specific dimension, creating it if necessary.
     *
     * @param level The server level
     * @return The dimension manager
     */
    public DimensionAtmosphereManager getManager(ServerLevel level) {
        return managers.computeIfAbsent(level.dimension(), k -> new DimensionAtmosphereManager(level));
    }

    /**
     * Gets the manager for a dimension without creating it.
     *
     * @param dimension The dimension key
     * @return The manager, or null if none exists
     */
    @Nullable
    public DimensionAtmosphereManager getManager(ResourceKey<Level> dimension) {
        return managers.get(dimension);
    }

    // ==================== Public API Methods ====================

    /**
     * Registers a flood-fill atmosphere provider (eg Oxygen Distributor).
     *
     * @param provider The provider to register
     */
    public void registerProvider(IAtmosphereProvider provider) {
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
    public void unregisterProvider(IAtmosphereProvider provider) {
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
    public void registerBubbleProvider(IBubbleProvider provider) {
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
    public void unregisterBubbleProvider(IBubbleProvider provider) {
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
    public boolean hasOxygen(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) {
            return false; // Can only check on server
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
    public boolean hasNormalGravity(Level level, BlockPos pos) {
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
    public boolean hasNormalTemperature(Level level, BlockPos pos) {
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
    public void onServerTick(TickEvent.ServerTickEvent event) {
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
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        onBlockChanged(event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        onBlockChanged(event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        // This catches block state changes that aren't place/break
        onBlockChanged(event.getLevel(), event.getPos());
    }

    private void onBlockChanged(net.minecraft.world.level.LevelAccessor levelAccessor, BlockPos pos) {
        if (!(levelAccessor instanceof ServerLevel serverLevel)) {
            return;
        }

        DimensionAtmosphereManager manager = managers.get(serverLevel.dimension());
        if (manager != null) {
            manager.onBlockChanged(pos);
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
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
    public void onLevelUnload(LevelEvent.Unload event) {
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
