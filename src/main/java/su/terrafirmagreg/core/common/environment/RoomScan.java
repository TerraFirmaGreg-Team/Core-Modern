package su.terrafirmagreg.core.common.environment;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * Represents a scanned room's spatial data and seal status.
 * Created by flood fill analysis, used for environment containment checks.
 *
 * @param interior Set of block positions (as longs) that are passable (air, torches, etc.)
 * @param envelope Set of block positions (as longs) for interior + shell - used for oxygen checks
 * @param status The seal status (SEALED, ESCAPED_*, BLOCK_LIMIT)
 * @param escapePoint Position where the room is breached (null if sealed)
 * @param escapePath Path from start to escape point (diagnostic mode only)
 * @param bounds Axis-aligned bounding box containing the entire room
 */
public record RoomScan(
        LongOpenHashSet interior,
        LongOpenHashSet envelope,
        Status status,
        @Nullable BlockPos escapePoint,
        @Nullable LongArrayList escapePath,
        AABB bounds,
        Set<ChunkPos> touchedChunks) {

    /**
     * Status of the flood fill / room scan.
     */
    public enum Status {
        /** Fill completed, room fully enclosed */
        SEALED,
        /** Fill found escape via horizontal dimension limit */
        ESCAPED_DIMENSION,
        /** Fill found escape via world height limit */
        ESCAPED_BUILD_HEIGHT,
        /** Fill stopped at unloaded chunk (escape assumed, may recover on chunk load) */
        ESCAPED_UNLOADED,
        /** Fill stopped at block limit (seal status unknown) */
        BLOCK_LIMIT,
        /** Restored from saved data, not from a real flood fill. Treated as sealed for oxygen queries. */
        SAVED_DATA,
        /** Fill doesn't exist, used for init values */
        NULL;

        public boolean isSealed() {
            return this == SEALED || this == SAVED_DATA;
        }
    }

    /**
     * @return Whether the room is fully sealed
     */
    public boolean isSealed() {
        return status.isSealed();
    }

    /**
     * @return Total number of interior blocks
     */
    public int interiorSize() {
        return interior.size();
    }

    /**
     * @return Total number of envelope blocks (interior + shell)
     */
    public int envelopeSize() {
        return envelope.size();
    }

    /**
     * @return Whether there is an escape point (room is breached)
     */
    public boolean hasEscapePoint() {
        return escapePoint != null;
    }

    /**
     * Checks if a position is in the interior (passable blocks only).
     */
    public boolean containsInterior(BlockPos pos) {
        return interior.contains(pos.asLong());
    }

    /**
     * Checks if a position is in the envelope (interior + shell).
     * This is the primary method for environment/oxygen checks.
     */
    public boolean containsEnvelope(BlockPos pos) {
        return envelope.contains(pos.asLong());
    }

    /**
     * Creates an empty/failed result.
     */
    public static RoomScan empty() {
        return new RoomScan(
                new LongOpenHashSet(),
                new LongOpenHashSet(),
                Status.NULL,
                null,
                null,
                new AABB(0, 0, 0, 0, 0, 0),
                new HashSet<>());
    }
}
