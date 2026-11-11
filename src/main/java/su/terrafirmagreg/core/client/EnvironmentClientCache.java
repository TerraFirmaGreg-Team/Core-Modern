package su.terrafirmagreg.core.client;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.network.TFGNetworkHandler;

/**
 * Client-side cache for environment query results.
 * Used for tooltips and other client-side display.
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public final class EnvironmentClientCache {

    /**
     * Atmosphere state at a position.
     */
    public record EnvironmentState(
            boolean hasOxygen,
            float gravity,
            float temperature,
            float pressure) {
    }

    private record CacheEntry(EnvironmentState state, long queryTick) {
    }

    private static final Long2ObjectOpenHashMap<CacheEntry> cache = new Long2ObjectOpenHashMap<>();
    private static final Long2LongOpenHashMap pending = new Long2LongOpenHashMap();

    /** Cache entries are refreshed in background if older than 20 ticks when accessed */
    private static final int REFRESH_TICKS = 20;
    /** Cache entries are completely evicted if not accessed within 600 ticks */
    private static final int EVICT_TICKS = 600;
    /** Pending queries time out after 100 ticks */
    private static final int PENDING_TIMEOUT_TICKS = 100;
    private static long clientTicks = 0;
    private static ResourceKey<Level> lastDimension = null;

    private EnvironmentClientCache() {
    }

    /**
     * Queries the environment state at a position.
     * Returns cached value if available, otherwise sends a request to the server.
     *
     * @param pos The position to check
     * @return The environment state, or null if query is pending
     */
    @Nullable
    public static EnvironmentState get(BlockPos pos) {
        long posLong = pos.asLong();

        CacheEntry entry = cache.get(posLong);
        if (entry != null) {
            if (clientTicks - entry.queryTick() >= REFRESH_TICKS) {
                requestIfEligible(pos, posLong);
            }
            return entry.state();
        }

        requestIfEligible(pos, posLong);
        return null;
    }

    private static void requestIfEligible(BlockPos pos, long posLong) {
        if (!pending.containsKey(posLong) || clientTicks - pending.get(posLong) >= PENDING_TIMEOUT_TICKS) {
            pending.put(posLong, clientTicks);
            TFGNetworkHandler.sendAtmosphereQuery(pos);
        }
    }

    /**
     * Checks if a position has oxygen from the client cache.
     * Returns true by default if query is pending.
     */
    public static boolean hasOxygen(BlockPos pos) {
        return hasOxygen(pos, true);
    }

    public static boolean hasOxygen(BlockPos pos, boolean fallback) {
        EnvironmentState state = get(pos);
        return state != null ? state.hasOxygen() : fallback;
    }

    /**
     * Gets the gravity at a position from the client cache.
     * Returns null if the query is still pending.
     */
    @Nullable
    public static Float getGravity(BlockPos pos) {
        EnvironmentState state = get(pos);
        return state != null ? state.gravity() : null;
    }

    /**
     * Gets the temperature at a position from the client cache.
     * Returns null if the query is still pending.
     */
    @Nullable
    public static Float getTemperature(BlockPos pos) {
        EnvironmentState state = get(pos);
        return state != null ? state.temperature() : null;
    }

    /**
     * Gets the pressure at a position from the client cache.
     * Returns null if the query is still pending.
     */
    @Nullable
    public static Float getPressure(BlockPos pos) {
        EnvironmentState state = get(pos);
        return state != null ? state.pressure() : null;
    }

    /**
     * Called when we receive a response from the server.
     *
     * @param pos The position that was queried
     * @param state The environment state at that position
     */
    public static void receive(BlockPos pos, EnvironmentState state) {
        long posLong = pos.asLong();
        cache.put(posLong, new CacheEntry(state, clientTicks));
        pending.remove(posLong);
    }

    /**
     * Called each client tick to handle cache eviction and pending timeouts.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        clientTicks++;

        Level level = Minecraft.getInstance().level;
        if (level != null && !level.dimension().equals(lastDimension)) {
            lastDimension = level.dimension();
            clear();
        }

        if (clientTicks % 100 == 0) {
            if (!cache.isEmpty()) {
                cache.long2ObjectEntrySet().removeIf(entry -> clientTicks - entry.getValue().queryTick() > EVICT_TICKS);
            }
            if (!pending.isEmpty()) {
                pending.long2LongEntrySet().removeIf(entry -> clientTicks - entry.getLongValue() > PENDING_TIMEOUT_TICKS);
            }
        }
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    /**
     * Clears the cache. Called on dimension change or disconnect.
     */
    public static void clear() {
        cache.clear();
        pending.clear();
    }
}
