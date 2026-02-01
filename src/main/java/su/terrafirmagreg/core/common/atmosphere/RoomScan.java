package su.terrafirmagreg.core.common.atmosphere;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * Represents a scanned room's spatial data and seal status.
 * Created by flood fill analysis, used for atmosphere containment checks.
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
        FloodFillStatus status,
        @Nullable BlockPos escapePoint,
        @Nullable List<BlockPos> escapePath,
        AABB bounds,
        Set<ChunkPos> touchedChunks) {

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
     * Checks if a position is in the interior (passable blocks only).
     */
    public boolean containsInterior(BlockPos pos) {
        return interior.contains(pos.asLong());
    }

    /**
     * Checks if a position is in the envelope (interior + shell).
     * This is the primary method for atmosphere/oxygen checks.
     */
    public boolean containsEnvelope(BlockPos pos) {
        return envelope.contains(pos.asLong());
    }

    /**
     * Checks if a position is in the shell (envelope but not interior).
     */
    public boolean containsShell(BlockPos pos) {
        long posLong = pos.asLong();
        return envelope.contains(posLong) && !interior.contains(posLong);
    }

    /**
     * @return Number of shell blocks (envelope - interior)
     */
    public int shellSize() {
        return envelope.size() - interior.size();
    }

    /**
     * Checks if a specific chunk intersects with this room.
     *
     * @param chunkPos Chunk position to check
     * @return true if the chunk is touched by this room
     */
    public boolean touchesChunk(ChunkPos chunkPos) {
        return touchedChunks.contains(chunkPos);
    }

    /**
     * Checks if any of the given positions intersect with the envelope.
     */
    public boolean containsAnyEnvelope(LongOpenHashSet positions) {
        LongIterator iter = positions.iterator();
        while (iter.hasNext()) {
            if (envelope.contains(iter.nextLong())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates an empty/failed result.
     */
    public static RoomScan empty() {
        return new RoomScan(
                new LongOpenHashSet(),
                new LongOpenHashSet(),
                FloodFillStatus.BLOCK_LIMIT,
                null,
                null,
                new AABB(0, 0, 0, 0, 0, 0),
                new HashSet<>());
    }
}
