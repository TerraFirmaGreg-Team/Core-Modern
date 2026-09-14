package su.terrafirmagreg.core.common.environment;

import static su.terrafirmagreg.core.common.environment.FloodFillHelpers.ALL_DIRECTIONS;
import static su.terrafirmagreg.core.common.environment.FloodFillHelpers.mask2DFSDirections;
import static su.terrafirmagreg.core.common.environment.FloodFillHelpers.relativeLong;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * Flood fill for pressure rooms. Simpler than {@link FloodFill}: all blocks are passable
 * by default; a block is a wall only if it matches the given tag. Pressure resistant blocks are assumed
 * to be full blocks.
 */
public class PressureFloodFill {

    private PressureFloodFill() {
    }

    /** Convenience overload for main-thread callers (commands, debug). */
    public static PressureRoomScan fill(Level level, BlockPos start, int maxBlocks, int maxHorizontalDimension,
            TagKey<Block> wallTag) {
        return fill(new AsyncBlockReader((ServerLevel) level), start, maxBlocks, maxHorizontalDimension, wallTag);
    }

    /**
     * Performs a DFS flood fill starting from {@code start}.
     *
     * @param reader                  Thread-safe block reader for async use
     * @param start                   Starting position (one block inside the room from the controller)
     * @param maxBlocks               Maximum interior block count before returning BLOCK_LIMIT
     * @param maxHorizontalDimension  Maximum horizontal span of the room including walls
     * @param wallTag                 Blocks matching this tag are treated as walls
     */
    public static PressureRoomScan fill(AsyncBlockReader reader, BlockPos start, int maxBlocks,
            int maxHorizontalDimension, TagKey<Block> wallTag) {
        LongOpenHashSet interior = new LongOpenHashSet();
        LongOpenHashSet envelope = new LongOpenHashSet();
        LongOpenHashSet partPositions = new LongOpenHashSet();
        LongArrayList frontier = new LongArrayList();
        Set<ChunkPos> touchedChunks = new HashSet<>();

        // Bounds
        int minX = start.getX(), maxX = start.getX();
        int minY = start.getY(), maxY = start.getY();
        int minZ = start.getZ(), maxZ = start.getZ();

        RoomScan.Status status = RoomScan.Status.SEALED;
        @Nullable
        BlockPos escapePoint = null;

        // Start
        long startLong = start.asLong();
        interior.add(startLong);
        envelope.add(startLong);
        touchedChunks.add(new ChunkPos(start));
        for (int dirInt : mask2DFSDirections(ALL_DIRECTIONS)) {
            long neighbor = relativeLong(startLong, dirInt);
            frontier.push(neighbor);
        }

        BlockPos.MutableBlockPos pos = start.mutable();

        while (!frontier.isEmpty()) {
            long posLong = frontier.popLong();

            if (envelope.contains(posLong)) {
                continue;
            }

            pos.set(posLong);

            // Bounds update, escape checks
            touchedChunks.add(new ChunkPos(pos));
            boolean xChanged = pos.getX() < minX || pos.getX() > maxX;
            boolean zChanged = pos.getZ() < minZ || pos.getZ() > maxZ;
            if (xChanged || zChanged) {
                minX = Math.min(minX, pos.getX());
                maxX = Math.max(maxX, pos.getX());
                minZ = Math.min(minZ, pos.getZ());
                maxZ = Math.max(maxZ, pos.getZ());

                if (maxX - minX > maxHorizontalDimension || maxZ - minZ > maxHorizontalDimension) {
                    status = RoomScan.Status.ESCAPED_DIMENSION;
                    escapePoint = pos.immutable();
                    break;
                }
                if (!reader.hasChunkAt(pos)) {
                    status = RoomScan.Status.ESCAPED_UNLOADED;
                    escapePoint = pos.immutable();
                    break;
                }
            } else if (pos.getY() < minY || pos.getY() > maxY) {
                minY = Math.min(minY, pos.getY());
                maxY = Math.max(maxY, pos.getY());
                if (reader.isOutsideBuildHeight(pos.getY())) {
                    status = RoomScan.Status.ESCAPED_BUILD_HEIGHT;
                    escapePoint = pos.immutable();
                    break;
                }
            }

            BlockState blockState = reader.getBlockState(pos);
            if (blockState == null) {
                status = RoomScan.Status.ESCAPED_UNLOADED;
                escapePoint = pos.immutable();
                break;
            }

            if (blockState.is(wallTag)) {
                // Wall block
                envelope.add(posLong);
                if (blockState.getBlock() instanceof MetaMachineBlock && hasWallNeighbour(reader, pos, wallTag)) {
                    // Maybe hatch
                    partPositions.add(posLong);
                }
            } else {
                // Interior
                interior.add(posLong);
                envelope.add(posLong);
                if (interior.size() > maxBlocks) {
                    status = RoomScan.Status.BLOCK_LIMIT;
                    escapePoint = pos.immutable();
                    break;
                }

                // Push neighbors
                for (int dirInt : mask2DFSDirections(ALL_DIRECTIONS)) {
                    long neighbor = relativeLong(posLong, dirInt);
                    if (!envelope.contains(neighbor)) {
                        frontier.push(neighbor);
                    }
                }
            }
        }

        AABB bounds = interior.isEmpty() && envelope.isEmpty()
                ? new AABB(0, 0, 0, 0, 0, 0)
                : new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);

        RoomScan roomScan = new RoomScan(interior, envelope, status, escapePoint, null, bounds, touchedChunks);
        return new PressureRoomScan(roomScan, partPositions);
    }

    private static boolean hasWallNeighbour(AsyncBlockReader reader, BlockPos pos, TagKey<Block> wallTag) {
        for (Direction dir : Direction.values()) {
            BlockState neighbour = reader.getBlockState(pos.relative(dir));
            if (neighbour != null && neighbour.is(wallTag)) {
                return true;
            }
        }
        return false;
    }
}
