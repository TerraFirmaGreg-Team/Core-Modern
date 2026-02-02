package su.terrafirmagreg.core.common.atmosphere;

import static su.terrafirmagreg.core.common.atmosphere.AtmosphereHelpers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
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

    private static final long NO_PARENT = Long.MIN_VALUE;
    private static final List<ActiveTrace> ACTIVE_TRACES = new ArrayList<>();

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
        private static final int SEGMENTS_PER_TICK = 1;

        ActiveTrace(ServerLevel level, List<BlockPos> path) {
            this.level = level;
            this.path = path;
        }

        boolean tick() {
            int spawned = 0;
            while (spawned < SEGMENTS_PER_TICK && index < path.size() - 1) {
                spawnSegment(index++);
                spawned++;
            }
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

        // Find shortest path through interior using BFS
        List<BlockPos> shortestPath = findShortestPath(
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
     */
    private static List<BlockPos> findShortestPath(LongOpenHashSet interior, long startLong, long escapeLong) {
        Long2LongOpenHashMap parent = new Long2LongOpenHashMap();
        parent.defaultReturnValue(NO_PARENT);

        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();

        queue.enqueue(startLong);
        parent.put(startLong, NO_PARENT);

        Random random = new Random();

        while (!queue.isEmpty()) {
            long current = queue.dequeueLong();

            // Check all 6 neighbors
            forEachRandomDirection(random, dir -> {
                long neighbor = relativeLong(current, dir);

                // Already visited?
                if (parent.containsKey(neighbor)) {
                    return true;
                }

                // Is this the escape point? (might be just outside interior)
                if (neighbor == escapeLong) {
                    parent.put(neighbor, current);
                    return false;
                }

                // Only traverse through interior blocks
                if (interior.contains(neighbor)) {
                    parent.put(neighbor, current);
                    queue.enqueue(neighbor);
                }

                return true;
            });
        }
        if (parent.size() > 1) {
            return reconstructPath(parent, escapeLong);
        }

        // No path found (shouldn't happen if escape point is adjacent to interior)
        return List.of();
    }

    /**
     * Reconstructs the path from start to end using the parent map.
     */
    private static List<BlockPos> reconstructPath(Long2LongOpenHashMap parent, long endLong) {
        LongArrayList pathLongs = new LongArrayList();
        long current = endLong;

        while (current != NO_PARENT) {
            pathLongs.add(current);
            current = parent.get(current);
        }

        // Reverse to get start-to-end order
        List<BlockPos> path = new ArrayList<>(pathLongs.size());
        for (int i = pathLongs.size() - 1; i >= 0; i--) {
            path.add(BlockPos.of(pathLongs.getLong(i)));
        }

        return path;
    }

    private static final Direction[] SHUFFLED = Direction.values().clone();

    public static void forEachRandomDirection(Random random, Function<Direction, Boolean> action) {
        for (int i = SHUFFLED.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction tmp = SHUFFLED[i];
            SHUFFLED[i] = SHUFFLED[j];
            SHUFFLED[j] = tmp;
        }

        boolean cont;
        for (Direction d : SHUFFLED) {
            cont = action.apply(d);
            if (!cont) {
                return;
            }
        }
    }
}
