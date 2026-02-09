package su.terrafirmagreg.core.common.atmosphere;

import static su.terrafirmagreg.core.common.atmosphere.AtmosphereHelpers.*;
import static su.terrafirmagreg.core.common.atmosphere.PassabilityChecker.PassableResult;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * Flood fill algorithm for atmosphere room detection.
 * Uses DFS with UP-first direction order for fast escape detection.
 */
public class FloodFill {

    private FloodFill() {
    }

    /**
     * Performs a DFS flood fill starting from the given position.
     *
     * @param level Block getter (should be a Level or ChunkAccess)
     * @param heightAccessor Height accessor for build height limits
     * @param start Starting position (typically one block from machine)
     * @param maxBlocks Maximum number of interior blocks we should find
     * @param maxHorizontalDimension Maximum horizontal distance this room can span, including walls (make sure it's in render distance)
     * @return RoomScan containing the room data
     */
    public static RoomScan fill(Level level, LevelHeightAccessor heightAccessor,
            BlockPos start, int maxBlocks, int maxHorizontalDimension) {
        State state = new State();

        // Init
        long startLong = start.asLong();
        state.addInteriorBlock(startLong);
        state.addEnvelopeBlock(startLong);
        queueNeighbors(level, state, start, startLong, ALL_DIRECTIONS);

        BlockPos.MutableBlockPos pos = start.mutable();

        // Main DFS loop
        while (!state.frontier.isEmpty()) {
            long posLong = state.frontier.popLong();

            // Skip if already processed
            if (state.envelope.contains(posLong)) {
                continue;
            }

            pos.set(posLong);

            if (!state.updateAndCheckBounds(level, heightAccessor, pos, maxHorizontalDimension)) {
                return buildResult(state);
            }

            BlockState blockState = level.getBlockState(pos);

            PassableResult result = PassabilityChecker.isPassable(level, pos, posLong, blockState, state);
            switch (result) {
                case EMPTY:
                    state.addInteriorBlock(posLong);
                    if (state.interior.size() > maxBlocks) {
                        state.hitBlockLimit = true;
                        return buildResult(state);
                    }
                    queueNeighbors(level, state, pos, posLong, ALL_DIRECTIONS);
                    break;

                case FULL:
                    state.addEnvelopeBlock(posLong);
                    break;

                case NO_OPEN_FACES:
                case BLOCKED_WINDOW_PANE:
                    state.addPendingShellBlock(posLong);
                    state.removeQueuedDirections(posLong);
                    break;

                case OPEN_SILHOUETTE:
                case PASSABLE_WINDOW_PANE:
                    state.addInteriorBlock(posLong);
                    if (state.interior.size() > maxBlocks) {
                        state.hitBlockLimit = true;
                        return buildResult(state);
                    }
                    state.removePendingShellBlock(posLong);
                    queueAccessibleNeighbors(level, state, pos, posLong, blockState);
                    break;

                case ALREADY_CHECKED:
                default:
            }
        }
        // Fill complete without escape
        return buildResult(state);
    }

    private static void queueAccessibleNeighbors(Level level, State state, BlockPos currentPos, long currentPosLong, BlockState blockState) {
        byte openFacesInward = PassabilityChecker.getPassCache(level, currentPos, blockState).openFaces();
        byte openFacesOutward = mirrorDirs(openFacesInward);
        queueNeighbors(level, state, currentPos, currentPosLong, openFacesOutward);
    }

    /**
     * Add neighbors of the current block to the stack.
     */
    private static void queueNeighbors(Level level, State state, BlockPos currentPos, long currentPosLong, byte neighbors) {
        // Filter out the directions we came from to get to the current block
        byte visitDirections = state.visitDirections.get(currentPosLong);
        byte toQueue = subtractDirs(neighbors, mirrorDirs(visitDirections));

        for (int dirInt : mask2DFSDirections(toQueue)) {
            long neighborPosLong = relativeLong(currentPosLong, dirInt);
            if (state.envelope.contains(neighborPosLong))
                continue;
            state.markVisitDirection(neighborPosLong, dirInt);
            state.frontier.push(neighborPosLong);
        }
    }

    /**
     * Builds the final result from the current state.
     */
    private static RoomScan buildResult(State state) {
        state.envelope.addAll(state.pendingShell);
        state.pendingShell.clear();

        RoomScan.Status status;
        if (state.hitBlockLimit) {
            status = RoomScan.Status.BLOCK_LIMIT;
        } else if (state.hitBuildHeight) {
            status = RoomScan.Status.ESCAPED_BUILD_HEIGHT;
        } else if (state.hitDimensionLimit) {
            status = RoomScan.Status.ESCAPED_DIMENSION;
        } else if (state.hitUnloadedChunk) {
            status = RoomScan.Status.ESCAPED_UNLOADED;
        } else {
            status = RoomScan.Status.SEALED;
        }

        return new RoomScan(
                state.interior,
                state.envelope,
                status,
                state.escapePoint,
                null,
                state.getBounds(),
                state.touchedChunks);
    }

    /**
     * Mutable state during a flood fill operation.
     * Tracks interior blocks, shell blocks, escape point, and bounds.
     */
    static class State {
        /** Interior blocks (passable blocks that are part of the room) */
        final LongOpenHashSet interior = new LongOpenHashSet();

        /** Union of walls and interior blocks, represents the entire room */
        final LongOpenHashSet envelope = new LongOpenHashSet();

        /** DFS frontier stack */
        final LongArrayList frontier = new LongArrayList();

        /**
         * Direction-aware visited tracking for partial blocks.
         * Maps position (packed long) to a bitmask of checked directions.
         */
        final Long2ByteOpenHashMap visitDirections = new Long2ByteOpenHashMap();

        /**
         * Blocks that are partially passable - visited from a blocked direction,
         * but might still be visited from an open direction. Any blocks still here
         * at the end of the flood fill become part of the shell.
         */
        final LongOpenHashSet pendingShell = new LongOpenHashSet();

        /** Position where the DFS reaches a termination condition */
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

        State() {
            visitDirections.defaultReturnValue((byte) 0);
        }

        void markVisitDirection(long posLong, int dirInt) {
            visitDirections.merge(posLong, int2byte(dirInt), AtmosphereHelpers::unionDirs);
        }

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
        boolean updateAndCheckBounds(Level level, LevelHeightAccessor heightAccessor, BlockPos pos, int maxHorizontalDimension) {
            touchedChunks.add(new ChunkPos(pos));

            if (pos.getX() < minX || pos.getX() > maxX || pos.getZ() < minZ || pos.getZ() > maxZ) {
                // Horizontal bounds expanded
                minX = Math.min(minX, pos.getX());
                maxX = Math.max(maxX, pos.getX());
                minZ = Math.min(minZ, pos.getZ());
                maxZ = Math.max(maxZ, pos.getZ());

                if (maxX - minX > maxHorizontalDimension
                        || maxZ - minZ > maxHorizontalDimension) {
                    hitDimensionLimit = true;
                    escapePoint = pos.immutable();
                    return false;
                }

                if (!level.hasChunkAt(pos)) {
                    hitUnloadedChunk = true;
                    escapePoint = pos.immutable();
                    return false;
                }

            } else if (pos.getY() < minY || pos.getY() > maxY) {
                // Vertical bounds expanded
                minY = Math.min(minY, pos.getY());
                maxY = Math.max(maxY, pos.getY());

                if (heightAccessor.isOutsideBuildHeight(pos.getY())) {
                    hitBuildHeight = true;
                    escapePoint = pos.immutable();
                    return false;
                }
            }

            return true;
        }

        void addInteriorBlock(long posLong) {
            interior.add(posLong);
            envelope.add(posLong);
        }

        void addEnvelopeBlock(long posLong) {
            envelope.add(posLong);
        }

        void addPendingShellBlock(long posLong) {
            pendingShell.add(posLong);
        }

        void removePendingShellBlock(long posLong) {
            pendingShell.remove(posLong);
        }

        void removeQueuedDirections(long posLong) {
            visitDirections.remove(posLong);
        }
    }
}
