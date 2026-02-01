package su.terrafirmagreg.core.common.atmosphere;

import static su.terrafirmagreg.core.common.atmosphere.AtmosphereHelpers.*;
import static su.terrafirmagreg.core.common.atmosphere.PassabilityChecker.PassCache.PassType.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import earth.terrarium.adastra.common.blocks.SlidingDoorBlock;

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
 * Handles checking whether atmosphere can pass through blocks.
 * Uses direction-aware passability for partial blocks like stairs.
 *
 * <p>Caches quick passability results per BlockState to avoid repeated shape analysis.
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
         * From the directions we checked, we found no open faces so no air can pass so far.
         *  This means we add it to pending shell blocks. At the end they'll become part of the shell
         *  unless we later visit it from a different direction from which it's open.
         *  Example: bottom slab only from below
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
         *  Example: window pane from front or bottom slab from top, but only if a surrounding block is ALWAYS_PASSABLE
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
     * Checks if atmosphere can pass through a block.
     *
     * @param level Block getter for accessing block states
     * @param pos Position of the block to check
     * @param posLong Position as long
     * @param blockState Block state at the position
     * @param state Current FloodFill state, necessary for checking which directions we're visiting from.
     * @return PassableResult indicating if atmosphere can pass
     */
    public static PassableResult isPassable(Level level, BlockPos.MutableBlockPos pos, long posLong, BlockState blockState, FloodFillState state) {
        PassCache passCache = getPassCache(level, pos, blockState);

        return switch (passCache.type) {
            case EMPTY -> PassableResult.EMPTY;
            case FULL -> PassableResult.FULL;
            case COLLISION -> isPassableFromDirections(level, pos, posLong, passCache, state);
            default -> {
                TFGCore.LOGGER.error("Invalid state reached in PassabilityChecker");
                TFGCore.LOGGER.error("PassCache: {}", passCache);
                yield PassableResult.ALREADY_CHECKED;
            }
        };
    }

    /**
     * Checks if atmosphere can pass through a block given the incoming directions registered in the FloodFillState.
     * Read also the PassableResult enum javadoc for further information on what they mean.
     * @param level Block getter for accessing block states
     * @param pos Position of the block to check
     * @param posLong Position as long
     * @param passCache Cached information on the passability of faces and silhouettes
     * @param state Current FloodFill state, necessary for checking which directions we're visiting from.
     * @return PassableResult indicating if atmosphere can pass
     */
    public static PassableResult isPassableFromDirections(Level level, BlockPos.MutableBlockPos pos, long posLong, PassCache passCache, FloodFillState state) {

        // Get all queued directions
        byte incomingDirs = state.visitDirections.get(posLong);

        if (incomingDirs == 0) {
            return PassableResult.ALREADY_CHECKED;
        }

        byte openIncomingFaces = intersectDirs(incomingDirs, passCache.openFaces());
        if (hasNoDirs(openIncomingFaces)) {
            return PassableResult.NO_OPEN_FACES;
        }

        byte openSilhouettes = intersectDirs(openIncomingFaces, passCache.openSilhouettes());
        if (hasAnyDir(openSilhouettes)) {
            return PassableResult.OPEN_SILHOUETTE;
        }

        // Window-pane-like blocks: Block has incoming directions with open faces but filled silhouettes.
        // Air flows around window panes if they have empty blocks or a completely empty face next to them.
        // This is a simplification that's necessary to process walls entirely made of window panes.
        for (Direction perpDir : mask2perpendicularDirections(openIncomingFaces)) {
            pos.move(perpDir);
            var perpState = level.getBlockState(pos);
            PassCache perpCache = getPassCache(level, pos, perpState);

            if (perpCache.type == EMPTY || (perpCache.type == COLLISION && perpCache.isFaceEmpty(perpDir))) {
                return PassableResult.PASSABLE_WINDOW_PANE;
            }
            pos.move(perpDir.getOpposite());
        }

        return PassableResult.BLOCKED_WINDOW_PANE;
    }

    /**
     * Computes face and silhouette data in cases where the result can't be cached.
     * Used for blocks that need level context (airlocks, pipes with dynamic connections, etc.)
     */
    private static PassCache computeNoCache(Level level, BlockPos pos, BlockState blockState) {
        // Airlock door needs reference to controller block
        if (blockState.getBlock() instanceof SlidingDoorBlock sdb) {
            return computeFacesAndSilhouettes(sdb.getCollisionShape(blockState, level, pos, CollisionContext.empty()));
        }

        // Generic fallback: get collision shape with level context. Shulker boxes, moving pistons, bellows
        VoxelShape shape = blockState.getCollisionShape(level, pos, CollisionContext.empty());
        return computeFacesAndSilhouettes(shape);
    }

    /** Cache of collision info per BlockState (Empty or full, or if neither faces/silhouettes full or not). */
    private static final ConcurrentHashMap<BlockState, PassCache> CACHE = new ConcurrentHashMap<>();

    /**
     * Record to cache whether this BlockState is passable, and if necessary from which directions
     * If PassCache.type == COLLISION then .face and .axis are guaranteed to be populated
     * @param type Whether this BlockState is a full, empty, or a more complex block, or whether it can't be cached and depends on the world state.
     * @param closedFaces Bitmask representing which incoming directions hit a fully sealed face. This does not include partially sealed faces.
     * @param closedSilhouettes Bitmask representing which directions present a fully filled silhouette.
     * @param emptyFaces Bitmask representing which incoming directions hit a completely empty face. Only used for passability of blocks like windowpanes. This does not include partially sealed faces.
     */
    public record PassCache(
            PassType type,
            byte closedFaces,
            byte closedSilhouettes,
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

        private static PassCache empty() {
            return new PassCache(PassType.EMPTY, (byte) 0, (byte) 0, (byte) 0);
        }

        private static PassCache full() {
            return new PassCache(PassType.FULL, (byte) 0, (byte) 0, (byte) 0);
        }

        private static PassCache noCache() {
            return new PassCache(NO_CACHE, (byte) 0, (byte) 0, (byte) 0);
        }

        boolean isFaceEmpty(Direction dir) {
            return containsDir(emptyFaces, dir);
        }

        /**
         * Bitmask representing which incoming directions hit a face is not completely full.
         * This is different from closedFaces and from emptyFaces, because it includes partially open faces.
         */
        public byte openFaces() {
            return invertValues(closedFaces);
        }

        /**
         * Bitmask representing which incoming directions show a silhouette that is not completely full.
         */
        public byte openSilhouettes() {
            return invertValues(closedSilhouettes);
        }
    }

    /**
     * Gets the (cached) passability info for a BlockState.
     */
    public static PassCache getPassCache(Level level, BlockPos pos, BlockState blockState) {
        PassCache passCache = CACHE.computeIfAbsent(blockState, PassabilityChecker::computePassCache);
        if (passCache.type == NO_CACHE) {
            return computeNoCache(level, pos, blockState);
        }
        return passCache;
    }

    /**
     * Computes the Passability cache for a block state (called once per unique state).
     */
    private static PassCache computePassCache(BlockState blockState) {
        // Air
        if (blockState.isAir()) {
            return PassCache.empty();
        }

        // Tagged blocks
        if (blockState.is(TFGTags.Blocks.AtmospherePassable)) {
            return PassCache.empty();
        }
        if (blockState.is(TFGTags.Blocks.AtmosphereImpassable)) {
            return PassCache.full();
        }

        // Airlocks
        if (blockState.getBlock() instanceof SlidingDoorBlock) {
            return PassCache.noCache();
        }

        // CollisionShape based. Try with null level content to catch uncacheable blocks.
        VoxelShape shape;
        try {
            shape = blockState.getCollisionShape(null, BlockPos.ZERO);
        } catch (NullPointerException e) {
            // Block needs level context (e.g. moving piston, shulker box, bellows, GT pipes (though pipes are tagged passable))
            return PassCache.noCache();
        }

        return computeFacesAndSilhouettes(shape);
    }

    /**
     * Compute the closed faces and silhouettes of the given shape. This is used for the cache but also during the isPassable check for uncacheable blocks
     * @param shape The BlockState's current VoxelShape
     * @return A PassCache object that has data on faces and silhouettes populated
     */
    private static PassCache computeFacesAndSilhouettes(VoxelShape shape) {
        // Simple collision shapes
        if (shape.isEmpty()) {
            return PassCache.empty();
        }
        if (Block.isShapeFullBlock(shape)) {
            return PassCache.full();
        }

        byte closedFaces = 0;
        byte silhouettes = 0;
        byte emptyFaces = 0;

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
                closedFaces |= posByte;
            } else if (isEmptyFace(positiveFaceShape)) {
                emptyFaces |= posByte;
            }

            if (isFullFace(negativeFaceShape)) {
                closedFaces |= negByte;
            } else if (isEmptyFace(negativeFaceShape)) {
                emptyFaces |= negByte;
            }

            if (hasFullSilhouette(shape, axis)) {
                silhouettes |= (byte) (posByte | negByte);
            }
        }

        // No full silhouette on any axis (fences, pipes, etc) means it's always passable
        if (silhouettes == 0) {
            return PassCache.empty();
        }

        // Can't tell with type checks, depends on direction
        return new PassCache(COLLISION, closedFaces, silhouettes, emptyFaces);
    }

    public static boolean isFullFace(VoxelShape faceShape) {
        return Block.isShapeFullBlock(faceShape);
    }

    public static boolean isEmptyFace(VoxelShape faceShape) {
        return faceShape.isEmpty();
    }

    private static final double SUBPIXEL_SIZE = (double) 1 / 16;
    private static final double EDGE_OFFSET = SUBPIXEL_SIZE / 2;

    /**
     * Checks if a shape creates a filled silhouette in a specific direction.
     * Uses edge sampling to detect gaps in the projection - if any point on the
     * perimeter of the perpendicular plane isn't covered by the shape's projection,
     * air can pass through.
     *
     * @param shape The voxel shape to check
     * @param axis The axis to check for a silhouette
     * @return true if the shape has a full silhouette as seen from the given direction
     */
    public static boolean hasFullSilhouette(VoxelShape shape, Direction.Axis axis) {
        List<AABB> boxes = shape.toAabbs();
        if (boxes.isEmpty())
            return false;

        // Check all points along the 4 edges of the perpendicular plane
        for (double t = EDGE_OFFSET; t < 1.0; t += SUBPIXEL_SIZE) {
            if (isPointOpen(boxes, axis, EDGE_OFFSET, t)
                    || isPointOpen(boxes, axis, 1.0 - EDGE_OFFSET, t)
                    || isPointOpen(boxes, axis, t, EDGE_OFFSET)
                    || isPointOpen(boxes, axis, t, 1.0 - EDGE_OFFSET)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a point (u, v) on the perpendicular plane is covered by no collision box. If that's the case, there's
     *  a hole in the silhouette through to the other side where air can pass through.
     */
    private static boolean isPointOpen(List<AABB> boxes, Direction.Axis axis, double u, double v) {
        for (AABB box : boxes) {
            if (boxTouchesPoint(box, axis, u, v)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a single AABB's projection onto the perpendicular plane covers point (u, v).
     */
    private static boolean boxTouchesPoint(AABB box, Direction.Axis axis, double u, double v) {
        return switch (axis) {
            case X -> u >= box.minY && u <= box.maxY && v >= box.minZ && v <= box.maxZ;
            case Y -> u >= box.minX && u <= box.maxX && v >= box.minZ && v <= box.maxZ;
            case Z -> u >= box.minX && u <= box.maxX && v >= box.minY && v <= box.maxY;
        };
    }

    /**
     * Clears the passability cache. Call this on world unload or when block properties change.
     */
    //TODO: world unload handling
    public static void clearCache() {
        CACHE.clear();
    }
}
