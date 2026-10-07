package su.terrafirmagreg.core.client;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
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

// If I want to change a boolean to a float
//  1. Change the record fields - just change boolean to float
//  2. Update the packet encoding - replace the byte packing with actual float writes:
//
//// In EnvironmentResponsePacket
//public static void encode(EnvironmentResponsePacket pkt, FriendlyByteBuf buf) {
//    buf.writeBlockPos(pkt.pos);
//    buf.writeFloat(pkt.state.oxygen());
//    buf.writeFloat(pkt.state.gravity());
//    buf.writeFloat(pkt.state.temperature());
//    buf.writeFloat(pkt.state.pressure());
//}
//
//public static EnvironmentResponsePacket decode(FriendlyByteBuf buf) {
//    return new EnvironmentResponsePacket(
//            buf.readBlockPos(),
//            new EnvironmentState(
//                    buf.readFloat(),
//                    buf.readFloat(),
//                    buf.readFloat(),
//                    buf.readFloat()));
//}
//
//  3. Update the server-side query in EnvironmentQueryPacket to compute floats instead of booleans
//  4. Remove toByte()/fromByte() - they don't make sense for floats

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
            boolean hasNormalGravity,
            boolean hasNormalTemperature,
            boolean hasNormalPressure) {

        /** Encodes to a single byte for network transmission. */
        public byte toByte() {
            byte flags = 0;
            if (hasOxygen)
                flags |= 0x01;
            if (hasNormalGravity)
                flags |= 0x02;
            if (hasNormalTemperature)
                flags |= 0x04;
            if (hasNormalPressure)
                flags |= 0x08;
            return flags;
        }

        /** Decodes from a byte. */
        public static EnvironmentState fromByte(byte flags) {
            return new EnvironmentState(
                    (flags & 0x01) != 0,
                    (flags & 0x02) != 0,
                    (flags & 0x04) != 0,
                    (flags & 0x08) != 0);
        }
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
     * Checks if a position has normal gravity from the client cache.
     * Returns true by default if query is pending.
     */
    public static boolean hasNormalGravity(BlockPos pos) {
        return hasNormalGravity(pos, true);
    }

    public static boolean hasNormalGravity(BlockPos pos, boolean fallback) {
        EnvironmentState state = get(pos);
        return state != null ? state.hasNormalGravity() : fallback;
    }

    /**
     * Checks if a position has normal temperature from the client cache.
     * Returns true by default if query is pending.
     */
    public static boolean hasNormalTemperature(BlockPos pos) {
        return hasNormalTemperature(pos, true);
    }

    public static boolean hasNormalTemperature(BlockPos pos, boolean fallback) {
        EnvironmentState state = get(pos);
        return state != null ? state.hasNormalTemperature() : fallback;
    }

    /**
     * Checks if a position has normal pressure from the client cache.
     * Returns true by default if query is pending.
     */
    public static boolean hasNormalPressure(BlockPos pos) {
        return hasNormalPressure(pos, true);
    }

    public static boolean hasNormalPressure(BlockPos pos, boolean fallback) {
        EnvironmentState state = get(pos);
        return state != null ? state.hasNormalPressure() : fallback;
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
