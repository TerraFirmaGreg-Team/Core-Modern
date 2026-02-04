package su.terrafirmagreg.core.client;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import su.terrafirmagreg.core.network.TFGNetworkHandler;

// If I want to change a boolean to a float
//  1. Change the record fields - just change boolean to float
//  2. Update the packet encoding - replace the byte packing with actual float writes:
//
//// In AtmosphereResponsePacket
//public static void encode(AtmosphereResponsePacket pkt, FriendlyByteBuf buf) {
//    buf.writeBlockPos(pkt.pos);
//    buf.writeFloat(pkt.state.oxygen());
//    buf.writeFloat(pkt.state.gravity());
//    buf.writeFloat(pkt.state.temperature());
//    buf.writeFloat(pkt.state.pressure());
//}
//
//public static AtmosphereResponsePacket decode(FriendlyByteBuf buf) {
//    return new AtmosphereResponsePacket(
//            buf.readBlockPos(),
//            new AtmosphereState(
//                    buf.readFloat(),
//                    buf.readFloat(),
//                    buf.readFloat(),
//                    buf.readFloat()));
//}
//
//  3. Update the server-side query in AtmosphereQueryPacket to compute floats instead of booleans
//  4. Remove toByte()/fromByte() - they don't make sense for floats

/**
 * Client-side cache for atmosphere query results.
 * Used for tooltips and other client-side display.
 */
public final class AtmosphereClientCache {

    /**
     * Atmosphere state at a position.
     */
    public record AtmosphereState(
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
        public static AtmosphereState fromByte(byte flags) {
            return new AtmosphereState(
                    (flags & 0x01) != 0,
                    (flags & 0x02) != 0,
                    (flags & 0x04) != 0,
                    (flags & 0x08) != 0);
        }
    }

    private static final Long2ObjectOpenHashMap<AtmosphereState> cache = new Long2ObjectOpenHashMap<>();
    private static final LongOpenHashSet pending = new LongOpenHashSet();

    /** Cache entries expire after this many ticks (5 seconds) */
    private static final int CACHE_EXPIRY_TICKS = 100;

    /** Tick when the cache was last cleared */
    private static long lastClearTick = 0;

    private AtmosphereClientCache() {
    }

    /**
     * Queries the atmosphere state at a position.
     * Returns cached value if available, otherwise sends a request to the server.
     *
     * @param pos The position to check
     * @return The atmosphere state, or null if query is pending
     */
    @Nullable
    public static AtmosphereState get(BlockPos pos) {
        long posLong = pos.asLong();

        // Check cache first
        AtmosphereState cached = cache.get(posLong);
        if (cached != null) {
            return cached;
        }

        // If not pending, send a request
        if (!pending.contains(posLong)) {
            pending.add(posLong);
            TFGNetworkHandler.sendAtmosphereQuery(pos);
        }

        // Query is pending, return null
        return null;
    }

    /**
     * Called when we receive a response from the server.
     *
     * @param pos The position that was queried
     * @param state The atmosphere state at that position
     */
    public static void receive(BlockPos pos, AtmosphereState state) {
        long posLong = pos.asLong();
        cache.put(posLong, state);
        pending.remove(posLong);
    }

    /**
     * Called each client tick to handle cache expiry.
     *
     * @param currentTick The current client tick
     */
    public static void tick(long currentTick) {
        if (currentTick - lastClearTick >= CACHE_EXPIRY_TICKS) {
            cache.clear();
            pending.clear();
            lastClearTick = currentTick;
        }
    }

    /**
     * Clears the cache. Called on dimension change or disconnect.
     */
    public static void clear() {
        cache.clear();
        pending.clear();
    }
}
