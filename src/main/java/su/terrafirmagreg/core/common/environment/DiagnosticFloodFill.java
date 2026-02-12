package su.terrafirmagreg.core.common.environment;

import static su.terrafirmagreg.core.common.environment.FloodFillHelpers.*;
import static su.terrafirmagreg.core.common.environment.PassabilityChecker.*;
import static su.terrafirmagreg.core.common.environment.PassabilityChecker.PassableResult.*;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import it.unimi.dsi.fastutil.longs.*;

import su.terrafirmagreg.core.TFGCore;

/**
 * Diagnostic version of flood fill that finds the shortest path to the escape point.
 * Uses regular DFS flood fill first, then BFS through the interior with parent tracking to find shortest path.
 *
 * <p>Returns a {@link RoomScan} with the {@code escapePath} field populated.
 * <p>Handles particle visualization of leak paths via {@link #spawnTrace}.
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class DiagnosticFloodFill {

    private static final byte NO_PARENT = 0;
    private static final byte ORIGIN = ALL_DIRECTIONS;
    private static final List<ActiveTrace> ACTIVE_TRACES = new CopyOnWriteArrayList<>();

    /** Queue entry containing position and approach direction. */
    private record QueueEntry(long pos, byte dir) {
    }

    private DiagnosticFloodFill() {
    }

    // ==================== Diagnostic Flood Fill ====================

    /**
     * Perform a diagnostic flood fill with shortest path tracking.
     *
     * <p>First runs regular DFS to find the room and escape point, then uses BFS
     * through the interior blocks with parent tracking to find the shortest path to the escape.
     *
     * @param level                  Level object for BlockStates and build height
     * @param start                  Starting position
     * @param maxBlocks              Maximum number of interior blocks we should find
     * @param maxHorizontalDimension Maximum horizontal distance this room can span, including walls (make sure it's in render distance)
     * @return RoomScan with escapePath populated if there's an escape
     */
    public static RoomScan fill(Level level, BlockPos start, int maxBlocks, int maxHorizontalDimension) {
        // Run regular flood fill to find the room and escape point
        RoomScan result = FloodFill.fill(level, start, maxBlocks, maxHorizontalDimension);

        // If no escape point, return as-is (sealed room or block limit)
        if (result.escapePoint() == null) {
            return result;
        }

        // Find shortest path through interior using BFS
        LongArrayList shortestPath = findShortestPath(
                level,
                result.interior(),
                start.asLong(),
                result.escapePoint().asLong());

        // Return new result with the path
        return new RoomScan(
                result.interior(),
                result.envelope(),
                result.status(),
                result.escapePoint(),
                shortestPath,
                result.bounds(),
                result.touchedChunks());
    }

    /**
     * Finds the shortest path from start to escape through interior blocks using BFS.
     *
     * <p>The parentDir map tracks visited blocks and stores the direction we successfully entered from.
     */
    // TODO: This should be A* from both sides instead of BFS
    private static LongArrayList findShortestPath(Level level, LongOpenHashSet interior, long startLong, long escapeLong) {
        Long2ByteOpenHashMap parentDir = new Long2ByteOpenHashMap();
        parentDir.defaultReturnValue(NO_PARENT);

        ArrayDeque<QueueEntry> queue = new ArrayDeque<>();
        MutableBlockPos pos = new MutableBlockPos();
        Random random = new Random();

        parentDir.put(startLong, ORIGIN);
        queueNeighborsRandomly(startLong, parentDir, ALL_DIRECTIONS, queue, random);

        // The actual algorithm for this is very similar to the DFS flood fill, but it's different enough that it's
        //  hard to prevent code duplication.
        // Since we want the shortest path, we can't check a block for passability from multiple directions at once
        //  because then we don't know if it succeeded. This makes the algo slower.
        while (!queue.isEmpty()) {
            QueueEntry entry = queue.poll();
            long posLong = entry.pos();
            byte approachDir = entry.dir();

            if (parentDir.containsKey(posLong)) {
                continue;
            }

            // Is this the escape point?
            if (posLong == escapeLong) {
                parentDir.put(posLong, approachDir);
                return reconstructPath(parentDir, escapeLong);
            }

            // Only traverse interior blocks
            if (!interior.contains(posLong))
                continue;

            pos.set(posLong);
            BlockState blockState = level.getBlockState(pos);
            PassInfo passInfo = getPassInfo(level, pos, blockState);

            PassableResult result = switch (passInfo.type()) {
                case EMPTY -> PassableResult.EMPTY;
                case FULL -> PassableResult.FULL;
                case COLLISION -> isPassableFromDirections(level, pos, passInfo, approachDir);
                default -> ALREADY_CHECKED;
            };

            byte neighbors;
            switch (result) {
                case FULL:
                case NO_OPEN_FACES:
                case BLOCKED_WINDOW_PANE:
                case ALREADY_CHECKED:
                    continue; // Can't enter from this direction

                case EMPTY:
                    neighbors = ALL_DIRECTIONS;
                    break;
                case OPEN_SILHOUETTE:
                case PASSABLE_WINDOW_PANE:
                    neighbors = mirrorDirs(passInfo.openFaces());
                    break;

                default:
                    continue;
            }

            // Track parent
            parentDir.put(posLong, approachDir);

            queueNeighborsRandomly(posLong, parentDir, neighbors, queue, random);
        }

        // No path found. Maybe the world changed in the meantime?
        return LongArrayList.of();
    }

    /**
     * Queue in random order to make the path more interesting.
     *  Will still get a shortest path but there's multiple options in Manhattan.
     */
    static private void queueNeighborsRandomly(long current, Long2ByteOpenHashMap parentDir, byte neighbors,
            ArrayDeque<QueueEntry> queue, Random random) {
        forEachDirRandomly(neighbors, random, neighborDirInt -> {
            long neighborLong = relativeLong(current, neighborDirInt);
            if (!parentDir.containsKey(neighborLong)) {
                queue.add(new QueueEntry(neighborLong, int2byte(neighborDirInt)));
            }
        });
    }

    /**
     * Reconstructs the path from end to start using the parent direction map, then reverses it.
     */
    private static LongArrayList reconstructPath(Long2ByteOpenHashMap parentDir, long endLong) {
        LongArrayList path = new LongArrayList();
        long cur = endLong;
        path.add(cur);

        byte dir = parentDir.get(cur);
        // Walk backwards from the end
        while (dir != ORIGIN) {
            cur = relativeLong(cur, mirrorDirs(dir));
            path.add(cur);
            dir = parentDir.get(cur);
        }

        // Reverse in place
        long[] a = path.elements();
        for (int lo = 0, hi = path.size() - 1; lo < hi; lo++, hi--) {
            long tmp = a[lo];
            a[lo] = a[hi];
            a[hi] = tmp;
        }

        return path;
    }

    // ==================== Leak Trace Visualization ====================

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        ACTIVE_TRACES.removeIf(ActiveTrace::tick);
    }

    /**
     * Spawns a particle trace along the given path.
     */
    public static void spawnTrace(ServerLevel level, LongArrayList path) {
        ACTIVE_TRACES.add(new ActiveTrace(level, path));
    }

    private static class ActiveTrace {
        private final ServerLevel level;
        private final LongArrayList path;
        private int index = 0;
        private int tick = 0;
        private static final int SEGMENTS_PER_TICK = 1;

        ActiveTrace(ServerLevel level, LongArrayList path) {
            this.level = level;
            this.path = path;
        }

        boolean tick() {
            int spawned = 0;
            while (spawned < SEGMENTS_PER_TICK && index < path.size() - 1) {
                spawnSegment(index);
                spawned++;
            }
            if (tick++ % 3 == 0)
                index++;
            return index >= path.size() - 3;
        }

        private void spawnSegment(int i) {
            for (int j = i; j < i + 3 && j < path.size() - 1; j++) {
                BlockPos a = BlockPos.of(path.getLong(j));
                BlockPos b = BlockPos.of(path.getLong(j + 1));

                Vec3 from = Vec3.atCenterOf(a);
                Vec3 to = Vec3.atCenterOf(b);
                Vec3 dir = to.subtract(from).normalize();

                level.sendParticles(
                        ParticleTypes.CLOUD,
                        from.x, from.y, from.z,
                        3,
                        dir.x * 0.15,
                        dir.y * 0.15,
                        dir.z * 0.15,
                        0);
            }
        }
    }
}
