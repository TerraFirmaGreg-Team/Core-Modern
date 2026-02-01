package su.terrafirmagreg.core.common.atmosphere;

import static su.terrafirmagreg.core.common.atmosphere.AtmosphereHelpers.*;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks state during a flood fill operation.
 * Handles interior blocks, shell blocks, escape point, and bounds tracking.
 */
public class FloodFillState {
    /**
     * Interior blocks (passable blocks that are part of the room)
     */
    final LongOpenHashSet interior = new LongOpenHashSet();

    /**
     * Union of walls and interior blocks, represents the entire room
     */
    final LongOpenHashSet envelope = new LongOpenHashSet();

    /**
     * DFS frontier stack
     */
    final LongArrayList frontier = new LongArrayList();

    /**
     * Direction-aware visited tracking for partial blocks.
     * Maps position (packed long) to a bitmask of checked directions.
     */
    final Long2ByteOpenHashMap visitDirections = new Long2ByteOpenHashMap();

    /**
     * Blocks that are partially passable, we have visited them from a blocked direction, but it might still be
     *  visited from an open direction as well. Any blocks still in here at the end of the flood fill become part
     *  of the shell.
     */
    final LongOpenHashSet pendingShell = new LongOpenHashSet();

    /**
     * Pos where the DFS reaches a termination condition (build height, dimension, volume, unloaded chunk, etc)
     */
    @Nullable
    BlockPos escapePoint = null;

    // Bounds tracking
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int minZ = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    int maxZ = Integer.MIN_VALUE;

    Set<ChunkPos> touchedChunks = new HashSet<>();

    // Termination flags
    boolean hitBlockLimit = false;
    boolean hitDimensionLimit = false;
    boolean hitBuildHeight = false;
    boolean hitUnloadedChunk = false;

    public FloodFillState() {
        visitDirections.defaultReturnValue((byte) 0);
    }

    /**
     * Marks a position as visited in a specific direction.
     */
    void markVisitDirection(long posLong, int dirInt) {
        visitDirections.merge(posLong, int2byte(dirInt), AtmosphereHelpers::unionDirs);
    }

    /**
     * Checks if a position is already in the envelope (interior or shell).
     */
    boolean isInEnvelope(long posLong) {
        return envelope.contains(posLong);
    }

    /**
     * Checks if a position is in the interior.
     */
    boolean isInInterior(long posLong) {
        return interior.contains(posLong);
    }

    /**
     * Checks if a position is in the shell.
     */
    boolean isInShell(long posLong) {
        return envelope.contains(posLong) && !interior.contains(posLong);
    }

    /**
     * Gets the current bounding box of the room (envelope = interior + shell).
     */
    AABB getBounds() {
        if (envelope.isEmpty()) {
            return new AABB(0, 0, 0, 0, 0, 0);
        }
        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    /**
     * Updates bounds to include the given position.
     * @return whether the position got processed without crossing any limits.
     */
    protected boolean updateAndCheckBounds(Level level, LevelHeightAccessor heightAccessor, BlockPos pos, FloodFillConfig config) {

        if (pos.getX() < minX || pos.getX() > maxX || pos.getZ() < minZ || pos.getZ() > maxZ) {
            // Horizontal bounds expanded
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());

            touchedChunks.add(new ChunkPos(pos));

            if (maxX - minX > config.maxHorizontalDimension()
                    || maxZ - minZ > config.maxHorizontalDimension()) {
                // Horizontal dimension limit exceeded
                hitDimensionLimit = true;
                setEscapePoint(pos.immutable());
                return false;
            }

            if (!level.hasChunkAt(pos)) {
                // Unloaded chunk encountered
                hitUnloadedChunk = true;
                setEscapePoint(pos.immutable());
                return false;
            }

        } else if (pos.getY() < minY || pos.getY() > maxY) {
            // Vertical bounds expanded
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());

            if (heightAccessor.isOutsideBuildHeight(pos.getY())) {
                // Build height exceeded
                hitBuildHeight = true;
                setEscapePoint(pos.immutable());
                return false;
            }
        }

        return true;
    }

    /**
     * Sets the escape point.
     */
    void setEscapePoint(BlockPos pos) {
        escapePoint = pos.immutable();
    }

    /**
     * @return Number of interior blocks
     */
    int interiorSize() {
        return interior.size();
    }

    /**
     * @return Whether an escape point was found
     */
    boolean hasEscapePoint() {
        return escapePoint != null;
    }

    /**
     * Adds an interior block to the FloodFillState, adding it to both interior representing passable blocks
     *  as well as envelope representing all visited blocks.
     */
    public void addInteriorBlock(long posLong) {
        interior.add(posLong);
        envelope.add(posLong);
    }

    public void addEnvelopeBlock(long posLong) {
        envelope.add(posLong);
    }

    public void addPendingShellBlock(long posLong) {
        pendingShell.add(posLong);
    }

    public void removePendingShellBlock(long posLong) {
        pendingShell.remove(posLong);
    }

    public void removeQueuedDirections(long posLong) {
        visitDirections.remove(posLong);
    }
}
