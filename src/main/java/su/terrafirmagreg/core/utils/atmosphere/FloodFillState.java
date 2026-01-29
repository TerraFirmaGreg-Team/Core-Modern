package su.terrafirmagreg.core.utils.atmosphere;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

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
     * Parent tracking for diagnostic path building. Null for normal fills.
     * Maps position -> parent position (the block we came from).
     */
    @Nullable
    Long2LongOpenHashMap parentMap = null;

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
    void markVisitDirection(long posLong, Direction dir) {
        byte checkedDirs = visitDirections.get(posLong);
        byte dirBit = (byte) (1 << dir.ordinal());
        visitDirections.put(posLong, (byte) (checkedDirs | dirBit));
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
     * @return whether the volume limit was exceeded
     */
    public boolean addInteriorBlock(long posLong) {
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
