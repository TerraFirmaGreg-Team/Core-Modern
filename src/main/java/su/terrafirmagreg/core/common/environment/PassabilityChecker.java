package su.terrafirmagreg.core.common.environment;

import static su.terrafirmagreg.core.common.environment.FloodFillHelpers.*;
import static su.terrafirmagreg.core.common.environment.PassabilityChecker.PassInfo.PassType.*;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import earth.terrarium.adastra.common.blocks.SlidingDoorBlock;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

// Passability logic sketch:
// If empty (air, passable tag, silhouette never full from any axis)
//   add to interior & envelope
//   queue all neighbors except origin
// else if full (impassable tag, 3d collision box full)
//   add to envelope
//
// else // depends on collision box (faces and silhouettes)
//   consider all incoming directions:
//   if incoming face blocked for every incoming direction
//     add to pendingShell
//     remove all incoming directions from memory because we already checked them
//   else // some incoming faces not blocked
//     if any silhouette not filled
//       add to interior & envelope
//       queue only directions where outgoing face not blocked
//
//     else // silhouette filled for every incoming direction
//       for each perpendicular direction
//         check if neighbor is empty block or has empty face
//           if so, mark this block as passable, so
//           add to interior & envelope
//           queue only directions where outgoing face not blocked
//         else // some incoming faces not blocked, but every unblocked face has a filled silhouette, and surrounding blocks are not type passable
//           add to pendingShell
//           remove incoming directions from memory

// TODO: Waterlocks?
/**
 * Handles checking whether environment can pass through blocks.
 * Uses direction-aware passability for partial blocks like stairs.
 *
 * <p>Caches quick passability results per BlockState to avoid repeated collision shape analysis.
 */
public final class PassabilityChecker {

    private PassabilityChecker() {
    }

    /**
     * Result of checking passability from specific directions.
     */
    public enum PassableResult {

        /** Completely open */
        EMPTY,

        /** Completely solid */
        FULL,

        /**
         * From all directions we checked, we found no open faces so no air can pass so far.
         *  This means we add it to pending shell blocks. At the end they'll become part of the shell
         *  unless we later visit it from a different direction from which it's open.
         *  Example: bottom slab visited only from below
         */
        NO_OPEN_FACES,

        /**
         * From a direction we checked we found an open silhouette, which means air can pass freely through
         *  outgoing faces that are open.
         *  Example: bottom slab accessed from the side allows air in all directions except DOWN.
         */
        OPEN_SILHOUETTE,

        /**
         * From a directions with an open face, there was a filled silhouette but a neighboring block is
         *  ALWAYS_PASSABLE or had an empty face, so air flowed around the barrier.
         *  This means air can pass freely through outgoing faces that are open.
         *  Example: window pane from front or bottom slab from top, with a neighboring block that's air
         */
        PASSABLE_WINDOW_PANE,

        /**
         * From all directions with an open face, there was a filled silhouette and no neighboring blocks
         *  that are ALWAYS_PASSABLE or have an empty face, so no air can flow through so far.
         *  This means we add it to pending shell blocks. At the end they'll become part of the shell
         *  unless we later visit it from a different direction from which it's open.
         *  Example: window pane surrounded by other window panes or bricks
         */
        BLOCKED_WINDOW_PANE,

        /**
         * We've already processed this combination of BlockPos and direction
         * This can happen if a block is queued from multiple directions. We check all queued directions at once but
         *  can't remove the other entries from the stack easily, so the stack will occasionally pop blocks that have been
         *  processed already.
         */
        ALREADY_CHECKED
    }

    /**
     * Checks if environment can pass through a block.
     *
     * @param reader           Block getter for accessing block states
     * @param pos             Position of the block to check
     * @param posLong         Position as long
     * @param blockState      Block state at the position
     * @param visitDirections Current FloodFill state, necessary for checking which directions we're visiting from.
     * @return PassableResult indicating if environment can pass
     */
    public static PassableResult isPassable(AsyncBlockReader reader, MutableBlockPos pos, long posLong, BlockState blockState, Long2ByteOpenHashMap visitDirections) {
        PassInfo passInfo = getPassInfo(reader, pos, blockState);

        return switch (passInfo.type) {
            case EMPTY -> PassableResult.EMPTY;
            case FULL -> PassableResult.FULL;
            case COLLISION -> isPassableFromDirections(reader, pos, passInfo, visitDirections.get(posLong));
            default -> {
                TFGCore.LOGGER.error("Invalid state reached in PassabilityChecker");
                TFGCore.LOGGER.error("PassInfo: {}", passInfo);
                yield PassableResult.ALREADY_CHECKED;
            }
        };
    }

    /**
     * Checks if environment can pass through a block given specific incoming directions.
     * Read also the {@link PassableResult} values javadoc for further information on what they mean.
     * @param reader Block getter for accessing block states
     * @param pos Position of the block to check
     * @param passInfo Cached information on the passability of faces and silhouettes
     * @param incomingDirs Bitmask of directions we're checking passability from
     * @return PassableResult indicating if environment can pass
     */
    public static PassableResult isPassableFromDirections(AsyncBlockReader reader, MutableBlockPos pos, PassInfo passInfo, byte incomingDirs) {
        if (incomingDirs == 0) {
            return PassableResult.ALREADY_CHECKED;
        }

        byte openIncomingFaces = intersectDirs(incomingDirs, passInfo.openFaces());
        if (hasNoDirs(openIncomingFaces)) {
            return PassableResult.NO_OPEN_FACES;
        }

        byte openSilhouettes = intersectDirs(openIncomingFaces, passInfo.openSilhouettes());
        if (hasAnyDir(openSilhouettes)) {
            return PassableResult.OPEN_SILHOUETTE;
        }

        // Window-pane-like blocks: Block has incoming directions with open faces but filled silhouettes.
        // Air flows around window panes if they have empty blocks or a completely empty face next to them.
        // This is a simplification that's necessary to process walls entirely made of window panes.
        for (Direction perpDir : mask2perpendicularDirections(openIncomingFaces)) {
            pos.move(perpDir);
            var perpState = reader.getBlockState(pos);
            // Treat unloaded chunk as solid (conservative — won't cause false passability)
            PassInfo perpInfo = perpState != null ? getPassInfo(reader, pos, perpState) : PassInfo.full();
            pos.move(perpDir.getOpposite());

            if (perpInfo.type == EMPTY || (perpInfo.type == COLLISION && perpInfo.isFaceEmpty(perpDir))) {
                return PassableResult.PASSABLE_WINDOW_PANE;
            }
        }

        return PassableResult.BLOCKED_WINDOW_PANE;
    }

    /**
     * Computes face and silhouette data in cases where the result can't be cached.
     * Used for blocks that need level context (airlocks, pipes with dynamic connections, etc.)
     * Falls back to the underlying Level for collision shape queries (may bounce to main thread).
     */
    private static PassInfo computeNoCache(AsyncBlockReader reader, BlockPos pos, BlockState blockState) {
        Level level = reader.getLevel();
        // Airlock door needs reference to controller block
        if (blockState.getBlock() instanceof SlidingDoorBlock sdb) {
            return computeFacesAndSilhouettes(sdb.getCollisionShape(blockState, level, pos, CollisionContext.empty()));
        }

        // Generic fallback: get collision shape with level context. Shulker boxes, moving pistons, bellows
        VoxelShape shape = blockState.getCollisionShape(level, pos, CollisionContext.empty());
        return computeFacesAndSilhouettes(shape);
    }

    // ==================== Passability info cache ====================

    /** Cache of collision info per BlockState (Empty or full, or if neither faces/silhouettes full or not). */
    private static final ConcurrentHashMap<BlockState, PassInfo> CACHE = new ConcurrentHashMap<>();

    /**
     * Record to cache whether this BlockState is passable, and if necessary from which directions
     * If PassInfo.type == COLLISION then .face and .axis are guaranteed to be populated
     * @param type Whether this BlockState is a full, empty, or a more complex block, or whether it can't be cached and depends on the world state.
     * @param fullFaces Bitmask representing which incoming directions hit a fully sealed face. This does not include partially sealed faces.
     * @param fullSilhouettes Bitmask representing which directions present a fully filled silhouette.
     * @param emptyFaces Bitmask representing which incoming directions hit a completely empty face. Only used for passability of blocks like windowpanes. This does not include partially sealed faces.
     */
    public record PassInfo(
            PassType type,
            byte fullFaces,
            byte fullSilhouettes,
            byte emptyFaces) {

        /**
         * Whether the result is a type block that doesn't require collision info, a complex block, or an uncacheable block requiring world state.
         * Also used for checking neighbors of blocks with an open face but a closed silhouette (window panes),
         *  as air might flow around them if they're neighbored by passable blocks.
         */
        public enum PassType {
            /** Always passable: empty/air, passable tag, or silhouette never full from any axis */
            EMPTY,
            /** Always blocked: impassable tag or 3d collision box full */
            FULL,
            /** Collision box dependent, have to check incoming directions. Some faces and/or silhouettes full, some not */
            COLLISION,
            /** Result can't be cached for some reason, eg airlock doors, moving pistons, bellows, shulker boxes. */
            NO_CACHE
        }

        private static PassInfo empty() {
            return new PassInfo(PassType.EMPTY, (byte) 0, (byte) 0, (byte) 0);
        }

        static PassInfo full() {
            return new PassInfo(PassType.FULL, (byte) 0, (byte) 0, (byte) 0);
        }

        private static PassInfo noCache() {
            return new PassInfo(NO_CACHE, (byte) 0, (byte) 0, (byte) 0);
        }

        boolean isFaceEmpty(Direction dir) {
            return containsDir(emptyFaces, dir);
        }

        /**
         * Bitmask representing which incoming directions hit a face is not completely full.
         * This is different from fullFaces and from emptyFaces, because it includes partially open faces.
         */
        public byte openFaces() {
            return invertValues(fullFaces);
        }

        /**
         * Bitmask representing which incoming directions show a silhouette that is not completely full.
         */
        public byte openSilhouettes() {
            return invertValues(fullSilhouettes);
        }
    }

    /**
     * Returns a snapshot of all cached passability entries.
     */
    public static ConcurrentHashMap<BlockState, PassInfo> getCache() {
        return CACHE;
    }

    /**
     * Gets the raw passability info for a BlockState.
     */
    public static PassInfo getCachedPassInfo(BlockState blockState) {
        return CACHE.computeIfAbsent(blockState, PassabilityChecker::computePassInfo);
    }

    /**
     * Gets the passability info for a BlockState, computing current state for no_cache blocks
     */
    public static PassInfo getPassInfo(AsyncBlockReader reader, BlockPos pos, BlockState blockState) {
        PassInfo passInfo = getCachedPassInfo(blockState);
        if (passInfo.type == NO_CACHE) {
            return computeNoCache(reader, pos, blockState);
        }
        return passInfo;
    }

    /**
     * Computes the Passability info for a block state (called once per unique state).
     */
    private static PassInfo computePassInfo(BlockState blockState) {
        // Air
        if (blockState.isAir()) {
            return PassInfo.empty();
        }

        // Tagged blocks
        if (blockState.is(TFGTags.Blocks.AtmosphereImpassable)) {
            return PassInfo.full();
        }
        if (blockState.is(TFGTags.Blocks.AtmospherePassable)) {
            return PassInfo.empty();
        }

        // Airlocks
        if (blockState.getBlock() instanceof SlidingDoorBlock) {
            return PassInfo.noCache();
        }

        // Use outline shape instead of collision for tagged blocks (e.g. pillar blocks with
        // full collision but visually open sides)
        boolean useOutline = blockState.is(TFGTags.Blocks.AtmosphereUseOutline);

        VoxelShape shape;
        try {
            shape = useOutline
                    ? blockState.getShape(null, BlockPos.ZERO)
                    : blockState.getCollisionShape(null, BlockPos.ZERO);
        } catch (NullPointerException e) {
            // Block needs level context (e.g. moving piston, shulker box, bellows, GT pipes (though pipes are tagged passable))
            return PassInfo.noCache();
        }

        return computeFacesAndSilhouettes(shape);
    }

    /**
     * Compute the faces and silhouettes of the given shape. This is used for the cache but also during the isPassable check for uncacheable blocks
     * @param shape The BlockState's current VoxelShape (collisionshape)
     * @return A PassInfo object that has data on faces and silhouettes populated
     */
    private static PassInfo computeFacesAndSilhouettes(VoxelShape shape) {
        // Simple collision shapes
        if (shape.isEmpty()) {
            return PassInfo.empty();
        }
        if (Block.isShapeFullBlock(shape)) {
            return PassInfo.full();
        }

        byte fullFaces = 0;
        byte silhouettes = 0;
        byte emptyFaces = 0;

        // For every axis, compute front and back face, and silhouette
        for (Direction.Axis axis : AXES) {
            Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
            Direction negative = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);
            byte posByte = dir2byte(positive);
            byte negByte = dir2byte(negative);

            // getFaceShape(negative) and getFaceShape(positive) are intentionally reversed here,
            //  because minecraft looks from the center of the block outwards, but our cache
            //  represents the faces when you're coming into the block
            VoxelShape positiveFaceShape = shape.getFaceShape(negative);
            VoxelShape negativeFaceShape = shape.getFaceShape(positive);

            if (isFullFace(positiveFaceShape)) {
                fullFaces |= posByte;
            } else if (isEmptyFace(positiveFaceShape)) {
                emptyFaces |= posByte;
            }

            if (isFullFace(negativeFaceShape)) {
                fullFaces |= negByte;
            } else if (isEmptyFace(negativeFaceShape)) {
                emptyFaces |= negByte;
            }

            if (hasFullSilhouette(shape, axis)) {
                silhouettes |= (byte) (posByte | negByte);
            }
        }

        // No full silhouette on any axis (fences, pipes, etc) means it's always passable
        if (silhouettes == 0) {
            return PassInfo.empty();
        }

        // Can't tell with type checks, depends on direction
        return new PassInfo(COLLISION, fullFaces, silhouettes, emptyFaces);
    }

    public static boolean isFullFace(VoxelShape faceShape) {
        return Block.isShapeFullBlock(faceShape);
    }

    public static boolean isEmptyFace(VoxelShape faceShape) {
        return faceShape.isEmpty();
    }

    private static final double SUBPIXEL_SIZE = (double) 1 / 16;
    private static final double EDGE_OFFSET = SUBPIXEL_SIZE / 2;
    private static final double NO_RAY_COLLISION = Double.POSITIVE_INFINITY;

    /**
     * Checks if a shape creates a filled silhouette in a specific direction.
     * Assumes silhouettes can't have holes in them.
     * Sample along the outside edge to detect gaps in the projection. If any point on the
     * perimeter of the perpendicular plane isn't covered by the shape's silhouette,
     * air can pass through.
     *
     * @param shape The voxel shape to check
     * @param axis The axis to check for a silhouette
     * @return true if the shape has a full silhouette as seen from the given direction
     */
    public static boolean hasFullSilhouette(VoxelShape shape, Direction.Axis axis) {
        if (shape.isEmpty())
            return false;

        // shape.min(axis, u, v) raycasts along `axis` at perpendicular coordinates (u, v)
        //  and returns the coordinate of the first filled voxel, or POSITIVE_INFINITY if empty.
        // We cast rays along all 4 edges to see if there's any holes anywhere.
        for (double t = EDGE_OFFSET; t < 1.0; t += SUBPIXEL_SIZE) {
            // spotless:off
            if (shape.min(axis, EDGE_OFFSET, t)                       == NO_RAY_COLLISION
             || shape.min(axis, 1.0 - EDGE_OFFSET, t)   == NO_RAY_COLLISION
             || shape.min(axis, t, EDGE_OFFSET)                       == NO_RAY_COLLISION
             || shape.min(axis, t, 1.0 - EDGE_OFFSET) == NO_RAY_COLLISION) {
                return false;
            }
            // spotless:on
        }
        return true;
    }

    /**
     * Clears the passability cache. Call this on world unload or when block properties change.
     */
    //TODO: world unload handling
    public static void clearCache() {
        CACHE.clear();
    }
}
