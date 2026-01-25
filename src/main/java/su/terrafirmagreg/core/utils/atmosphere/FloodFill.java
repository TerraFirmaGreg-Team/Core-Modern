package su.terrafirmagreg.core.utils.atmosphere;

import static su.terrafirmagreg.core.utils.atmosphere.PassabilityChecker.PassableResult;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

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
    private static final Direction[] DIRECTIONS = Direction.values();

    /** All directions as a bitmask */
    protected static final byte ALL_DIRECTIONS = 0b111111;

    /**
     * Direction order for DFS traversal.
     * UP is last so it's pushed last and popped first (stack is LIFO).
     * This prioritizes upward expansion for fast escape detection.
     */
    protected static final Direction[] DFS_DIRECTIONS = {
            Direction.DOWN,
            Direction.WEST,
            Direction.SOUTH,
            Direction.EAST,
            Direction.NORTH,
            Direction.UP
    };

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
        return fillWithState(level, heightAccessor, start, config, new FloodFillState());
    }

    /**
     * Performs a DFS flood fill with a pre-initialized state.
     * Used by DiagnosticFloodFill to enable parent tracking.
     */
    static FloodFillResult fillWithState(Level level, LevelHeightAccessor heightAccessor,
            BlockPos start, FloodFillConfig config, FloodFillState state) {

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Init
        long startLong = start.asLong();
        addInteriorBlock(state, startLong, config);
        addEnvelopeBlock(state, startLong);
        queueNeighbors(level, state, start, startLong, ALL_DIRECTIONS);

        // Main DFS loop
        while (!state.frontier.isEmpty()) {
            long posLong = state.frontier.popLong();

            // Skip if already processed
            if (state.envelope.contains(posLong)) {
                continue;
            }

            pos.set(posLong);

            if (!updateAndCheckBounds(level, heightAccessor, state, pos, config)) {
                recordParent(state, pos, posLong);
                return buildResult(state);
            }

            BlockState blockState = level.getBlockState(pos);

            PassableResult result = PassabilityChecker.isPassable(level, pos, blockState, state.visitDirections.get(posLong));
            switch (result) {
                case PASSABLE:
                    if (!addInteriorBlock(state, posLong, config)) {
                        return buildResult(state);
                    }
                    recordParent(state, pos, posLong);
                    queueNeighbors(level, state, pos, posLong, ALL_DIRECTIONS);
                    break;

                case BLOCKED:
                    addEnvelopeBlock(state, posLong);
                    break;

                case NO_OPEN_FACES:
                case BLOCKED_WINDOW_PANE:
                    addPendingShellBlock(state, posLong);
                    removeQueuedDirections(state, posLong);
                    break;

                case OPEN_FACES_WITH_OPEN_CROSS_SECTIONS:
                case PASSABLE_WINDOW_PANE:
                    if (!addInteriorBlock(state, posLong, config)) {
                        return buildResult(state);
                    }
                    recordParent(state, pos, posLong);
                    removePendingShellBlock(state, posLong);
                    queueAccessibleNeighbors(level, state, pos, posLong, blockState);
                    break;

                case ALREADY_CHECKED:
                default:
            }
        }
        // Fill complete without escape
        return buildResult(state);
    }

    /**
     * Adds an interior block to the FloodFillState, adding it to both interior representing passable blocks
     *  as well as envelope representing all visited blocks.
     * @return whether the volume limit was exceeded
     */
    private static boolean addInteriorBlock(FloodFillState state, long posLong, FloodFillConfig config) {
        state.interior.add(posLong);
        state.envelope.add(posLong);
        return state.interior.size() <= config.maxBlocks();
    }

    private static void addEnvelopeBlock(FloodFillState state, long posLong) {
        state.envelope.add(posLong);
    }

    private static void addPendingShellBlock(FloodFillState state, long posLong) {
        state.pendingShell.add(posLong);
    }

    private static void removePendingShellBlock(FloodFillState state, long posLong) {
        state.pendingShell.remove(posLong);
    }

    private static void removeQueuedDirections(FloodFillState state, long posLong) {
        state.visitDirections.remove(posLong);
    }

    /**
     * Records the parent for path building (diagnostic mode only).
     * Called after passability confirms we can enter the block.
     */
    private static void recordParent(FloodFillState state, BlockPos pos, long posLong) {
        if (state.parentMap == null) {
            return;
        }
        byte visitedFrom = state.visitDirections.get(posLong);
        for (Direction dir : Direction.values()) {
            if ((visitedFrom & (1 << dir.ordinal())) != 0) {
                state.parentMap.put(posLong, pos.relative(dir).asLong());
                return;
            }
        }
    }

    private static void queueAccessibleNeighbors(Level level, FloodFillState state, BlockPos currentPos, long currentPosLong, BlockState blockState) {
        byte closedFacesIncoming = PassabilityChecker.getPassableInfo(blockState).faces();
        byte openFacesIncoming = (byte) (~closedFacesIncoming & 0b111111);
        byte openFacesOutgoing = flipMaskDirections(openFacesIncoming);
        queueNeighbors(level, state, currentPos, currentPosLong, openFacesOutgoing);
    }

    private static void queueNeighbors(Level level, FloodFillState state, BlockPos currentPos, long currentPosLong, byte neighbors) {
        byte visitDirections = state.visitDirections.get(currentPosLong);
        byte toQueue = (byte) (neighbors & ~flipMaskDirections(visitDirections));
        for (Direction dir : DFS_DIRECTIONS) {
            if ((toQueue & (1 << dir.ordinal())) != 0) {
                long neighborPosLong = currentPos.relative(dir).asLong();
                state.markVisitDirection(neighborPosLong, dir);
                state.frontier.push(neighborPosLong);
            }
        }
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

        // Build escape path if diagnostic mode and there's an escape point
        List<BlockPos> escapePath = null;
        if (state.parentMap != null && state.escapePoint != null) {
            escapePath = buildPath(state.parentMap, state.escapePoint.asLong());
        }

        return new FloodFillResult(
                state.interior,
                state.envelope,
                status,
                state.escapePoint,
                escapePath,
                state.getBounds());
    }

    /**
     * Builds the path from start to the given position using the parent map.
     */
    private static List<BlockPos> buildPath(Long2LongOpenHashMap parentMap, long endLong) {
        long noParent = parentMap.defaultReturnValue();
        LongArrayList pathLongs = new LongArrayList();
        long current = endLong;

        while (current != noParent) {
            pathLongs.add(current);
            current = parentMap.get(current);
        }

        // Reverse to get start-to-end order
        List<BlockPos> path = new ArrayList<>(pathLongs.size());
        for (int i = pathLongs.size() - 1; i >= 0; i--) {
            path.add(BlockPos.of(pathLongs.getLong(i)));
        }

        return path;
    }

    /**
     * Updates bounds to include the given position.
     * @return whether the position got processed without crossing any limits.
     */
    protected static boolean updateAndCheckBounds(Level level, LevelHeightAccessor heightAccessor, FloodFillState state, BlockPos pos, FloodFillConfig config) {

        if (pos.getX() < state.minX || pos.getX() > state.maxX
                || pos.getY() < state.minZ || pos.getY() > state.minZ) {
            // Horizontal bounds expanded
            state.minX = Math.min(state.minX, pos.getX());
            state.maxX = Math.max(state.maxX, pos.getX());
            state.minZ = Math.min(state.minZ, pos.getX());
            state.minZ = Math.max(state.minZ, pos.getX());

            if (state.maxX - state.minX > config.maxHorizontalDimension() || state.maxZ - state.minZ > config.maxHorizontalDimension()) {
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
            state.minY = Math.min(state.minY, pos.getX());
            state.maxY = Math.max(state.maxY, pos.getX());

            if (heightAccessor.isOutsideBuildHeight(pos.getY())) {
                // Build height exceeded
                state.hitBuildHeight = true;
                state.setEscapePoint(pos.immutable());
                return false;
            }
        }

        return true;
    }

    /** Swap adjacent bit pairs: DOWN<->UP, NORTH<->SOUTH, WEST<->EAST */
    private static byte flipMaskDirections(byte mask) {
        int even = mask & 0b010101;  // bits 0, 2, 4 (DOWN, NORTH, WEST)
        int odd = mask & 0b101010;   // bits 1, 3, 5 (UP, SOUTH, EAST)
        return (byte) ((even << 1) | (odd >> 1));
    }
}
