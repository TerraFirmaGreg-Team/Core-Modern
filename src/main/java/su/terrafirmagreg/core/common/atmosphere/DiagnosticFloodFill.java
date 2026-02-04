package su.terrafirmagreg.core.common.atmosphere;

import static su.terrafirmagreg.core.common.atmosphere.AtmosphereHelpers.*;
import static su.terrafirmagreg.core.common.atmosphere.PassabilityChecker.*;
import static su.terrafirmagreg.core.common.atmosphere.PassabilityChecker.PassableResult.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import it.unimi.dsi.fastutil.longs.*;

import su.terrafirmagreg.core.TFGCore;

/**
 * Diagnostic version of flood fill that finds the shortest path to the escape point.
 * Uses regular DFS flood fill first, then BFS through the interior to find shortest path.
 *
 * <p>Returns a {@link RoomScan} with the {@code escapePath} field populated.
 * <p>Also handles particle visualization of leak paths via {@link #spawnTrace}.
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class DiagnosticFloodFill {

    private static final byte NO_PARENT = 0;
    private static final byte ORIGIN = ALL_DIRECTIONS;
    private static final List<ActiveTrace> ACTIVE_TRACES = new ArrayList<>();

    /** Queue entry containing position and approach direction. */
    private record QueueEntry(long pos, byte dir) {
    }

    private DiagnosticFloodFill() {
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
    public static void spawnTrace(ServerLevel level, List<BlockPos> path) {
        ACTIVE_TRACES.add(new ActiveTrace(level, path));
    }

    private static class ActiveTrace {
        private final ServerLevel level;
        private final List<BlockPos> path;
        private int index = 0;
        private int tick = 0;
        private static final int SEGMENTS_PER_TICK = 1;

        ActiveTrace(ServerLevel level, List<BlockPos> path) {
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
                BlockPos a = path.get(j);
                BlockPos b = path.get(j + 1);

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

    // ==================== Diagnostic Flood Fill ====================

    /**
     * Performs a diagnostic flood fill with shortest path tracking.
     *
     * <p>First runs regular DFS to find the room and escape point, then uses BFS
     * through the interior blocks to find the shortest path to the escape.
     *
     * @param level Block getter
     * @param heightAccessor Height accessor for build height limits
     * @param start Starting position
     * @param config Configuration for limits
     * @return RoomScan with escapePath populated if there's an escape
     */
    public static RoomScan fill(Level level, LevelHeightAccessor heightAccessor,
            BlockPos start, FloodFillConfig config) {
        // Run regular flood fill to find the room and escape point
        RoomScan result = FloodFill.fill(level, heightAccessor, start, config);

        // If no escape point, return as-is (sealed room or block limit)
        if (result.escapePoint() == null) {
            return result;
        }

        // Find shortest path through interior using BFS with direction-aware passability
        List<BlockPos> shortestPath = findShortestPath(
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
     * Uses direction-aware passability checks to ensure the path is actually traversable.
     *
     * <p>The parentDir map tracks visited blocks and stores the direction we successfully entered from.
     */
    private static List<BlockPos> findShortestPath(Level level, LongOpenHashSet interior, long startLong, long escapeLong) {
        Long2ByteOpenHashMap parentDir = new Long2ByteOpenHashMap();
        parentDir.defaultReturnValue(NO_PARENT);

        ArrayDeque<QueueEntry> queue = new ArrayDeque<>();
        MutableBlockPos pos = new MutableBlockPos();
        Random random = new Random();

        queue.add(new QueueEntry(startLong, ORIGIN));

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
            PassCache passCache = getPassCache(level, pos, blockState);

            PassableResult result = switch (passCache.type()) {
                case EMPTY -> PassableResult.EMPTY;
                case FULL -> PassableResult.FULL;
                case COLLISION -> isPassableFromDirections(level, pos, passCache, approachDir);
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
                    neighbors = mirrorDirs(passCache.openFaces());
                    break;

                default:
                    continue;
            }

            // Successfully entered - mark visited with the direction we came from
            parentDir.put(posLong, approachDir);

            // Queue neighbors with their approach directions
            forEachDirRandomly(neighbors, random, neighborDirInt -> {
                long neighborLong = relativeLong(posLong, neighborDirInt);
                if (!parentDir.containsKey(neighborLong)) {
                    queue.add(new QueueEntry(neighborLong, int2byte(neighborDirInt)));
                }
            });
        }

        // No path found (shouldn't happen if escape point is reachable)
        return List.of();
    }

    /**
     * Reconstructs the path from end to start using the parent direction map, then reverses it.
     */
    private static List<BlockPos> reconstructPath(Long2ByteOpenHashMap parentDir, long endLong) {
        LongArrayList pathLongs = new LongArrayList();
        long currentLong = endLong;
        pathLongs.add(currentLong);

        byte dir = parentDir.get(currentLong);
        while (dir != ORIGIN) {
            // Direction stored is how we entered; mirror to get direction back to parent
            currentLong = relativeLong(currentLong, mirrorDirs(dir));
            pathLongs.add(currentLong);
            dir = parentDir.get(currentLong);
        }

        // Reverse to get start-to-end order
        List<BlockPos> path = new ArrayList<>(pathLongs.size());
        for (int i = pathLongs.size() - 1; i >= 0; i--) {
            path.add(BlockPos.of(pathLongs.getLong(i)));
        }

        return path;
    }
}
