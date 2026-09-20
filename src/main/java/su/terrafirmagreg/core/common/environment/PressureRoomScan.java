package su.terrafirmagreg.core.common.environment;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * Result of a pressure flood fill. Wraps a {@link RoomScan} and adds the positions of
 * GT machine blocks found in the wall (hatches, controller), to be resolved on the main thread.
 */
public record PressureRoomScan(RoomScan roomScan, LongOpenHashSet partPositions) {

    public boolean isSealed() {
        return roomScan.isSealed();
    }

    public RoomScan.Status status() {
        return roomScan.status();
    }

    public static PressureRoomScan empty() {
        return new PressureRoomScan(RoomScan.empty(), new LongOpenHashSet());
    }
}
