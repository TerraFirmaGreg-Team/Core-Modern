package su.terrafirmagreg.core.utils.atmosphere;

import static su.terrafirmagreg.core.utils.atmosphere.AtmosphereHelpers.*;
import static su.terrafirmagreg.core.utils.atmosphere.PassabilityChecker.PassableResult;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Flood fill algorithm for atmosphere room detection.
 * Uses DFS with UP-first direction order for fast escape detection.
 *
 * <p>Uses lazy evaluation: neighbors are added to the frontier with minimal checks,
 * and expensive checks (block state lookup, passability) are deferred until pop time.
 * This reduces wasted work for unsealed rooms that escape quickly.
 *
 * <p>Subclasses (like {@link DiagnosticFloodFill}) can hide the static {@code fill()}
 * method to provide additional functionality like path tracking.
 */
public class FloodFill {

    protected FloodFill() {
    }

    /**
     * Performs a DFS flood fill starting from the given position.
     *
     * <p>Uses lazy evaluation: expensive checks (block state lookup, passability) are
     * deferred until we pop from the frontier. This reduces work for unsealed rooms.
     *
     * @param level Block getter (should be a Level or ChunkAccess)
     * @param heightAccessor Height accessor for build height limits
     * @param start Starting position (typically one block from machine)
     * @param config Configuration for limits
     * @return FloodFillResult containing the room data
     */
    public static FloodFillResult fill(Level level, LevelHeightAccessor heightAccessor,
            BlockPos start, FloodFillConfig config) {
        FloodFillState state = new FloodFillState();

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

            if (!updateAndCheckBounds(level, heightAccessor, state, pos, config)) {
                return buildResult(state);
            }

            BlockState blockState = level.getBlockState(pos);

            PassableResult result = PassabilityChecker.isPassable(level, pos, posLong, blockState, state);
            switch (result) {
                case EMPTY:
                    state.addInteriorBlock(posLong);
                    if (state.interior.size() > config.maxBlocks()) {
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
                    if (state.interior.size() > config.maxBlocks()) {
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

    private static void queueAccessibleNeighbors(Level level, FloodFillState state, BlockPos currentPos, long currentPosLong, BlockState blockState) {
        byte openFacesInward = PassabilityChecker.getPassCache(level, currentPos, blockState).openFaces();
        byte openFacesOutward = mirrorDirs(openFacesInward);
        queueNeighbors(level, state, currentPos, currentPosLong, openFacesOutward);
    }

    /**
     * Add neighbors to the current block to the stack
     * @param level level
     * @param state FloodFill state
     * @param currentPos Current block pos
     * @param currentPosLong Current block pos as long
     * @param neighbors Bitmask representing which directions to add
     */
    private static void queueNeighbors(Level level, FloodFillState state, BlockPos currentPos, long currentPosLong, byte neighbors) {

        // Filter out the directions we came from to get to the current block, leaving just toQueue
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
     * Updates bounds to include the given position.
     * @return whether the position got processed without crossing any limits.
     */
    protected static boolean updateAndCheckBounds(Level level, LevelHeightAccessor heightAccessor,
            FloodFillState state, BlockPos pos, FloodFillConfig config) {

        if (pos.getX() < state.minX || pos.getX() > state.maxX || pos.getZ() < state.minZ || pos.getZ() > state.maxZ) {
            // Horizontal bounds expanded
            state.minX = Math.min(state.minX, pos.getX());
            state.maxX = Math.max(state.maxX, pos.getX());
            state.minZ = Math.min(state.minZ, pos.getZ());
            state.maxZ = Math.max(state.maxZ, pos.getZ());

            if (state.maxX - state.minX > config.maxHorizontalDimension()
                    || state.maxZ - state.minZ > config.maxHorizontalDimension()) {
                // Horizontal dimension limit exceeded
                state.hitDimensionLimit = true;
                state.setEscapePoint(pos.immutable());
                return false;
            }

            if (!level.hasChunkAt(pos)) {
                // Unloaded chunk encountered
                state.hitUnloadedChunk = true;
                state.setEscapePoint(pos.immutable());
                return false;
            }

        } else if (pos.getY() < state.minY || pos.getY() > state.maxY) {
            // Vertical bounds expanded
            state.minY = Math.min(state.minY, pos.getY());
            state.maxY = Math.max(state.maxY, pos.getY());

            if (heightAccessor.isOutsideBuildHeight(pos.getY())) {
                // Build height exceeded
                state.hitBuildHeight = true;
                state.setEscapePoint(pos.immutable());
                return false;
            }
        }

        return true;
    }

    /**
     * Builds the final result from the current state.
     */
    protected static FloodFillResult buildResult(FloodFillState state) {
        state.envelope.addAll(state.pendingShell);
        state.pendingShell.clear();

        FloodFillStatus status;
        if (state.hitBlockLimit) {
            status = FloodFillStatus.BLOCK_LIMIT;
        } else if (state.hitBuildHeight) {
            status = FloodFillStatus.ESCAPED_BUILD_HEIGHT;
        } else if (state.hitDimensionLimit) {
            status = FloodFillStatus.ESCAPED_DIMENSION;
        } else if (state.hitUnloadedChunk) {
            status = FloodFillStatus.ESCAPED_UNLOADED;
        } else {
            status = FloodFillStatus.SEALED;
        }

        return new FloodFillResult(
                state.interior,
                state.envelope,
                status,
                state.escapePoint,
                null,
                state.getBounds());
    }
}
