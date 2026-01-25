package su.terrafirmagreg.core.utils.atmosphere;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * Status of the flood fill operation.
 */
enum FloodFillStatus {
    /** Fill completed, room fully enclosed */
    SEALED,
    /** Fill found escape via horizontal dimension limit */
    ESCAPED_DIMENSION,
    /** Fill found escape via world height limit */
    ESCAPED_BUILD_HEIGHT,
    /** Fill stopped at unloaded chunk (escape assumed) */
    ESCAPED_UNLOADED,
    /** Fill stopped at block limit (seal status unknown) */
    BLOCK_LIMIT;

    public boolean isSealed() {
        return this == SEALED;
    }

    public boolean isComplete() {
        return this != BLOCK_LIMIT;
    }

    public boolean hasEscape() {
        return this != SEALED && this != BLOCK_LIMIT;
    }
}

/**
 * Result of a flood fill operation.
 *
 * @param interior Set of block positions (as longs) that are part of the room interior (passable blocks)
 * @param envelope Set of block positions (as longs) for interior + shell - used for oxygen checks
 * @param status The outcome of the flood fill
 * @param escapePoint Position where the room is breached (null if sealed) - used for vortex spawning
 * @param escapePath Path from start to escape point (null unless diagnostic fill, or no escape)
 * @param bounds Axis-aligned bounding box containing the entire room
 */
public record FloodFillResult(
        LongOpenHashSet interior,
        LongOpenHashSet envelope,
        FloodFillStatus status,
        @Nullable BlockPos escapePoint,
        @Nullable List<BlockPos> escapePath,
        AABB bounds) {

    /**
     * @return Whether the room is fully sealed
     */
    public boolean isSealed() {
        return status.isSealed();
    }

    /**
     * @return Whether the fill completed (didn't hit block limit)
     */
    public boolean isComplete() {
        return status.isComplete();
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
     * @return Whether this result represents a valid sealed room
     */
    public boolean isValidSealedRoom() {
        return status == FloodFillStatus.SEALED;
    }

    /**
     * @return Whether there is an escape point (room is breached)
     */
    public boolean hasEscapePoint() {
        return escapePoint != null;
    }

    /**
     * @return Whether there is an escape path (only from diagnostic fills)
     */
    public boolean hasEscapePath() {
        return escapePath != null && !escapePath.isEmpty();
    }

    /**
     * Creates an empty/failed result.
     */
    public static FloodFillResult empty() {
        return new FloodFillResult(
                new LongOpenHashSet(),
                new LongOpenHashSet(),
                FloodFillStatus.BLOCK_LIMIT,
                null,
                null,
                new AABB(0, 0, 0, 0, 0, 0));
    }
}
