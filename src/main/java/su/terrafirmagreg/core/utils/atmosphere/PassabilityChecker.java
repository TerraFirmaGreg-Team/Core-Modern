package su.terrafirmagreg.core.utils.atmosphere;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import earth.terrarium.adastra.common.blocks.SlidingDoorBlock;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

// Passability sketch:
// If simple passable (cache, air, passable tag, collision bounding square never full from any axis)
//   add to interior
//   check volume
//     exit if volume exceeded
//   queue all neighbors except origin
// else if simple solid (cache, impassable tag, 3d collision box full)
//   add to envelope
//
// else // depends on faces and collision bounds
//   if incoming face blocked (every queued direction)
//     add to pendingShell
//     remove other queued directions
//   else // some incoming faces not blocked
//     if axis collision square not filled (any unblocked face directions)
//       add to interior, check volume
//       queue only directions where outgoing face not blocked
//
//     else // axis collision square filled (every unblocked face direction)
//       for each unblocked face direction, for each perpendicular direction (overlap?)
//         if simple passable
//           then block itself is also passable, so
//           add to interior, check volume
//           queue only directions where outgoing face not blocked
//         else // some incoming faces not blocked, but every unblocked face direction has a filled collision square, and surrounding blocks are not simple passable
//           add to pendingShell
//           remove other queued directions

/**
 * Handles checking whether atmosphere can pass through blocks.
 * Uses direction-aware passability for partial blocks like stairs.
 *
 * <p>Caches quick passability results per BlockState to avoid repeated shape analysis.
 */
public final class PassabilityChecker {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction.Axis[] AXES = Direction.Axis.values();

    /** All directions perpendicular to an incoming/outgoing direction, represented as a bitmask for easy unions */
    private static final byte[] PERPENDICULAR_MASK = {
            0b111100, // Directions perpendicular to -Y
            0b111100, // Directions perpendicular to Y
            0b110011, // Directions perpendicular to -Z
            0b110011, // Directions perpendicular to Z
            0b001111, // Directions perpendicular to -X
            0b001111  // Directions perpendicular to X
    };

    /**
     * Result of checking passability from specific directions.
     */
    public enum PassableResult {

        /** Completely open */
        PASSABLE,

        /** Completely solid */
        BLOCKED,

        /**
         * From the directions we checked, we found no open faces so no air can pass so far.
         *  This means we add it to pending shell blocks. At the end they'll become part of the shell
         *  unless we later visit it from a different direction from which it's open.
         *  Example: bottom slab only from below
         */
        NO_OPEN_FACES,

        /**
         * From a direction we checked, we found an open face with an open cross-section, which means air can pass freely through outgoing faces that are open
         * Example: bottom slab accessed from the side
         */
        OPEN_FACES_WITH_OPEN_CROSS_SECTIONS, // eg

        /**
         * From a directions with an open face, there was a solid cross-section but a neighboring block is ALWAYS_PASSABLE so air flowed around the barrier
         *  This means air can pass freely through outgoing faces that are open.
         *  Example: window pane from front or bottom slab from top, but only if a surrounding block is ALWAYS_PASSABLE
         */
        PASSABLE_WINDOW_PANE,

        /**
         * From all directions with an open face, there was a solid cross-section and no neighboring blocks that are ALWAYS_PASSABLE,
         *  so no air can flow through so far.
         *  This means we add it to pending shell blocks. At the end they'll become part of the shell
         *  unless we later visit it from a different direction from which it's open.
         *  Example: window pane surrounded by other window panes or bricks
         */
        BLOCKED_WINDOW_PANE,

        /**
         * We've already processed this BlockState+direction
         * This can happen if a block is queued from multiple directions. We check all queued directions at once but
         *  can't remove the other entries from the stack easily.
         */
        ALREADY_CHECKED
    }

    /**
     * Fast passability categories that don't require direction checking or checking of neighboring blocks.
     * Also used for checking neighbors of blocks with an open face but a closed bounding square (window panes),
     *  as air might flow around them if they're neighbored by passable blocks.
     */
    public enum SimplePassableResult {
        /** Always passable: empty/air, passable tag, collision bounding square never full from any axis */
        ALWAYS_PASSABLE,
        /** Always blocked: impassable tag or 3d collision box full */
        ALWAYS_BLOCKED,
        /** Direction dependent: Some faces and/or cross-sections full, some not */
        CHECK_DIRECTION,
        /** Result can't be cached for some reason */
        NO_CACHE
    }

    /**
     * Record to cache whether this BlockState is passable, and if necessary from which directions
     * If PassableInfo.simple == CHECK_DIRECTION then .face and .axis are populated
     * @param simple Whether this BlockState is a simple block that's passable or blocked from every direction
     * @param faces Bitmask representing which incoming directions hit a fully sealed face
     * @param crossSections Bitmask representing which directions present a fully filled cross-section
     */
    public record PassableInfo(
            SimplePassableResult simple,
            byte faces,
            byte crossSections) {

        private static PassableInfo passable() {
            return new PassableInfo(SimplePassableResult.ALWAYS_PASSABLE, (byte) 0, (byte) 0);
        }

        private static PassableInfo blocked() {
            return new PassableInfo(SimplePassableResult.ALWAYS_BLOCKED, (byte) 0, (byte) 0);
        }

        private static PassableInfo noCache() {
            return new PassableInfo(SimplePassableResult.NO_CACHE, (byte) 0, (byte) 0);
        }

        boolean isFaceBlocked(Direction dir) {
            return (faces & (1 << dir.ordinal())) != 0;
        }

        boolean isCrossSectionBlocked(Direction dir) {
            return (crossSections & (1 << dir.ordinal())) != 0;
        }
    }

    /** Cache of passability results per BlockState. */
    private static final ConcurrentHashMap<BlockState, PassableInfo> CACHE = new ConcurrentHashMap<>();

    private PassabilityChecker() {
    }

    /**
     * Gets the (cached) passability info for a BlockState.
     */
    public static PassableInfo getPassableInfo(BlockState blockState) {
        return CACHE.computeIfAbsent(blockState, PassabilityChecker::computePassableInfo);
    }

    /**
     * Computes quick passability for a block state (called once per unique state).
     */
    private static PassableInfo computePassableInfo(BlockState blockState) {
        // Air
        if (blockState.isAir()) {
            return PassableInfo.passable();
        }

        // Tagged blocks
        if (blockState.is(TFGTags.Blocks.AtmospherePassable)) {
            return PassableInfo.passable();
        }
        if (blockState.is(TFGTags.Blocks.AtmosphereImpassable)) {
            return PassableInfo.blocked();
        }

        // Airlocks
        if (blockState.getBlock() instanceof SlidingDoorBlock) {
            return PassableInfo.noCache();
        }

        // CollisionShape based
        VoxelShape shape = blockState.getCollisionShape(null, BlockPos.ZERO);

        return computeWithFacesAndCrossSections(shape);
    }

    private static PassableInfo computeWithFacesAndCrossSections(VoxelShape shape) {
        // Simple collision shapes
        if (shape.isEmpty()) {
            return PassableInfo.passable();
        }
        if (Block.isShapeFullBlock(shape)) {
            return PassableInfo.blocked();
        }

        // Compute face and axis closedness
        byte faces = 0;
        byte crossSections = 0;

        for (Direction.Axis axis : AXES) {
            Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
            Direction negative = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE);

            if (hasFullFace(shape, positive))
                faces |= (byte) ((1 << positive.ordinal()));
            if (hasFullFace(shape, negative))
                faces |= (byte) ((1 << negative.ordinal()));

            if (hasFullCrossSection(shape, axis)) {
                crossSections |= (byte) ((1 << positive.ordinal()) | (1 << negative.ordinal()));
            }
        }

        // No full cross-section on any axis (fences, pipes, etc) means it's always passable
        if (crossSections == 0) {
            return PassableInfo.passable();
        }

        // Can't tell with simple checks, depends on direction
        return new PassableInfo(SimplePassableResult.CHECK_DIRECTION, faces, crossSections);
    }

    /**
     * Checks if atmosphere can pass through a block when entering from the given direction.
     *
     * @param level Block getter for accessing block states
     * @param pos Position of the block to check
     * @param blockState Block state at the position
     * @param visitedFrom Bitmask with directions we're entering from that are currently on the frontier stack
     * @return PassableResult indicating if atmosphere can pass
     */
    public static PassableResult isPassable(Level level, BlockPos pos, BlockState blockState, byte visitedFrom) {
        PassableInfo passableInfo = getPassableInfo(blockState);
        assert passableInfo != null;

        if (passableInfo.simple == SimplePassableResult.NO_CACHE) {
            passableInfo = computeNoCache(level, pos, blockState, visitedFrom);
        }

        return switch (passableInfo.simple) {
            case ALWAYS_PASSABLE -> PassableResult.PASSABLE;
            case ALWAYS_BLOCKED -> PassableResult.BLOCKED;
            case CHECK_DIRECTION -> {
                // Get all queued directions
                if (visitedFrom == 0) {
                    // already processed all directions, the fact that we're here means it wasn't in the envelope
                    yield PassableResult.ALREADY_CHECKED;
                }

                byte openFaces = (byte) (visitedFrom & ~passableInfo.faces); // all incoming directions with open faces

                if (openFaces == 0) {
                    yield PassableResult.NO_OPEN_FACES; // add to pendingShell, remove visitedFrom
                }

                byte openCrossSections = (byte) (openFaces & ~passableInfo.crossSections); // incoming directions with open faces and non-sealing boundary box

                if (openCrossSections != 0) {
                    yield PassableResult.OPEN_FACES_WITH_OPEN_CROSS_SECTIONS; // add to interior, queue only directions where outgoing face not blocked
                }

                // Now we process window panes: Block has incoming directions with open faces but closed cross-sections.
                // Air flows around window panes if they have simple passable blocks next to them.
                // This is a simplification that's necessary to process walls entirely made of window panes.

                byte perpendicularUnion = 0;
                for (Direction incomingDir : DIRECTIONS) {
                    if ((openFaces & (1 << incomingDir.ordinal())) != 0) {
                        perpendicularUnion |= PERPENDICULAR_MASK[incomingDir.getAxis().ordinal()];
                    }
                }

                // Check perpendicular directions to find a potential path for the partial block.
                for (Direction perpDir : DIRECTIONS) {
                    if ((perpendicularUnion & (1 << perpDir.ordinal())) != 0) {
                        var adjacentState = level.getBlockState(pos.relative(perpDir));
                        if (getPassableInfo(adjacentState).simple == SimplePassableResult.ALWAYS_PASSABLE) {
                            yield PassableResult.PASSABLE_WINDOW_PANE; // add to interior, queue only directions where outgoing face not blocked
                        }
                    }
                }

                yield PassableResult.BLOCKED_WINDOW_PANE; // add to pendingShell, remove visitedFrom cos we checked those
            }
            default -> {
                TFGCore.LOGGER.error("Invalid state reached in PassabilityChecker");
                TFGCore.LOGGER.error("PassableInfo: {}", passableInfo);
                yield null;
            }
        };
    }

    /**
     * Checks if a voxel shape has a full face as seen from an incoming direction.
     * A full face means the shape fully covers that face of the block.
     *
     * @param shape The voxel shape to check
     * @param dir The incoming direction/face to check
     * @return true if the face is fully covered
     */
    public static boolean hasFullFace(VoxelShape shape, Direction dir) {
        VoxelShape faceShape = shape.getFaceShape(dir);
        return Block.isFaceFull(faceShape, dir.getOpposite()); // getOpposite because minecraft uses direction from center of block
    }

    /**
     * Checks if a shape creates a filled cross-section in a specific direction.
     * A cross-section exists if the bounding box spans full 0-1 in both perpendicular crossSections.
     * This is for blocks like glass panes that don't touch the faces but still block.
     *
     * @param shape The voxel shape to check
     * @param axis The axis to check for a cross-section
     * @return true if the shape has a full cross-section as seen from the given direction
     */
    public static boolean hasFullCrossSection(VoxelShape shape, Direction.Axis axis) {
        return switch (axis) {
            case X -> shape.min(Direction.Axis.Y) <= 0 && shape.max(Direction.Axis.Y) >= 1 &&
                    shape.min(Direction.Axis.Z) <= 0 && shape.max(Direction.Axis.Z) >= 1;
            case Y -> shape.min(Direction.Axis.X) <= 0 && shape.max(Direction.Axis.X) >= 1 &&
                    shape.min(Direction.Axis.Z) <= 0 && shape.max(Direction.Axis.Z) >= 1;
            case Z -> shape.min(Direction.Axis.X) <= 0 && shape.max(Direction.Axis.X) >= 1 &&
                    shape.min(Direction.Axis.Y) <= 0 && shape.max(Direction.Axis.Y) >= 1;
        };
    }

    /**
     * Computes PassableInfo in cases where the result can't be cached
     */
    private static PassableInfo computeNoCache(Level level, BlockPos pos, BlockState blockState, byte visitedFrom) {
        if (blockState.getBlock() instanceof SlidingDoorBlock sdb) {
            return computeWithFacesAndCrossSections(sdb.getCollisionShape(blockState, level, pos, CollisionContext.empty()));
        }

        TFGCore.LOGGER.error("Invalid state reached in computeNoCache");
        TFGCore.LOGGER.error("BlockPos: {}", pos);
        TFGCore.LOGGER.error("BlockState: {}", blockState);
        return null;
    }

    /**
     * Clears the passability cache. Call this on world unload or when block properties change.
     */
    //TODO: world unload handling
    public static void clearCache() {
        CACHE.clear();
    }
}
