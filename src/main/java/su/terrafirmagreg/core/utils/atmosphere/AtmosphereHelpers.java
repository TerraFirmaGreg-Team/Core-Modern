package su.terrafirmagreg.core.utils.atmosphere;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Direction;

/** Various helpers and static vars for dealing with directions as Directions, bitmasks, or longs */
// Helper methods for all your bitfuckery, longfuckery, and any other fuckery you might want
public class AtmosphereHelpers {

    /** Static copy of all directions */
    static final Direction[] DIRECTIONS = Direction.values();
    /** Static copy of all axes */
    static final Direction.Axis[] AXES = Direction.Axis.values();

    /**
     * Direction offsets for packed long positions.
     * For example, `blockPos.relative(Direction.DOWN)` gives the same position as `longPos + DIR_LONG_OFFSETS[0]`
     */
    static final long[] DIR_LONG_OFFSETS = {
            -1L,              // DOWN (y-1)
            1L,               // UP (y+1)
            -(1L << 12),      // NORTH (z-1)
            (1L << 12),       // SOUTH (z+1)
            -(1L << 38),      // WEST (x-1)
            (1L << 38)        // EAST (x+1)
    };

    /** All directions represented as a bitmask */
    protected static final byte ALL_DIRECTIONS = 0b111111;

    /**
     * Precomputed set of Directions for each possible direction mask
     * Makes things both faster and more readable when iterating over directions
     */
    public static final Direction[][] MASK2DIRECTIONS = new Direction[64][];

    /**
     * Precomputed set of directions (as bitmask) that are perpendicular to the given directions
     */
    public static final byte[] PERPENDICULAR_DIRECTIONS = new byte[64];

    /**
     * Precomputed set of directions (as int Array) for each possible mask,
     * ordered by preferred DFS order.
     * UP is last so it's pushed last and popped first (stack is LIFO).
     * This prioritizes upward expansion for fast escape detection.
     */
    public static final int[][] MASK2DFS_DIRECTIONS = new int[64][];

    // Fill out MASK2DIRECTIONS, PERPENDICULAR_UNION, and MASK2DFS_DIRECTIONS
    static {
        /*
         * Direction order for DFS traversal.
         */
        int[] DFS_ORDER = {
                Direction.DOWN.ordinal(),
                Direction.WEST.ordinal(),
                Direction.SOUTH.ordinal(),
                Direction.EAST.ordinal(),
                Direction.NORTH.ordinal(),
                Direction.UP.ordinal()
        };

        /* All directions perpendicular to an incoming/outgoing direction, represented as a bitmask for easy unions */
        final byte[] PERPENDICULAR_MASK = {
                0b111100, // Directions perpendicular to -Y
                0b111100, // Directions perpendicular to Y
                0b110011, // Directions perpendicular to -Z
                0b110011, // Directions perpendicular to Z
                0b001111, // Directions perpendicular to -X
                0b001111  // Directions perpendicular to X
        };

        for (int mask = 0; mask < 64; mask++) {

            // MASK2DIRECTIONS
            List<Direction> dirs = new ArrayList<>();
            for (Direction dir : Direction.values()) {
                if ((mask & (1 << dir.ordinal())) != 0) {
                    dirs.add(dir);
                }
            }
            MASK2DIRECTIONS[mask] = dirs.toArray(new Direction[0]);

            // PERPENDICULAR_DIRECTIONS
            byte union = 0;
            for (int i = 0; i < 6; i++) {
                if ((mask & (1 << i)) != 0) {
                    union |= PERPENDICULAR_MASK[i];
                }
            }
            PERPENDICULAR_DIRECTIONS[mask] = union;

            // MASK2DFS_DIRECTIONS
            int count = 0;
            for (int ordinal : DFS_ORDER) {
                if ((mask & (1 << ordinal)) != 0)
                    count++;
            }
            int[] dfsDirs = new int[count];
            int idx = 0;
            for (int ordinal : DFS_ORDER) {
                if ((mask & (1 << ordinal)) != 0) {
                    dfsDirs[idx++] = ordinal;
                }
            }
            MASK2DFS_DIRECTIONS[mask] = dfsDirs;
        }
    }

    /** Equivalent to `blockPos.relative(dir)`. Returns the relative of the pos in the given direction using longs. */
    public static long relativeLong(long posLong, int dirInt) {
        return posLong + DIR_LONG_OFFSETS[dirInt];
    }

    /** Equivalent to `blockPos.relative(dir)`. Returns the relative of the pos in the given direction using longs. */
    public static long relativeLong(long posLong, Direction dir) {
        return relativeLong(posLong, dir.ordinal());
    }

    /** Return a List of Directions specified by the mask */
    public static Direction[] mask2directions(byte mask) {
        return MASK2DIRECTIONS[mask & 0b111111];
    }

    /** Return a List of Directions perpendicular to directions in the mask */
    public static Direction[] mask2perpendicularDirections(byte mask) {
        return mask2directions(PERPENDICULAR_DIRECTIONS[mask]);
    }

    /** Return a List of ints representing directions specified by the mask, ordered by our preferred DFS order (UP first) */
    public static int[] mask2DFSDirections(byte mask) {
        return MASK2DFS_DIRECTIONS[mask & 0b111111];
    }

    /** Swap adjacent bit pairs: DOWN<->UP, NORTH<->SOUTH, WEST<->EAST */
    public static byte mirrorDirs(byte mask) {
        // 0b(EAST)(WEST)(SOUTH)(NORTH)(UP)(DOWN)
        int even = mask & 0b010101;  // bits 4, 2, 0 (WEST, NORTH, DOWN)
        int odd = mask & 0b101010;   // bits 5, 3, 1 (EAST, SOUTH, UP)
        // Shift even bits left, shift odd bits right, makes them swap places.
        return (byte) ((even << 1) | (odd >> 1));
    }

    /** Turn a direction into a bitmask representing the direction */
    public static byte dir2byte(Direction dir) {
        return (byte) (1 << dir.ordinal());
    }

    /** Turn a direction Int into a bitmask representing the direction */
    public static byte int2byte(int dirInt) {
        return (byte) (1 << dirInt);
    }

    /** Returns directions in common */
    public static byte intersectDirs(byte a, byte b) {
        return (byte) (a & b);
    }

    /** Returns directions present in either argument, aka the union of both arguments */
    public static byte unionDirs(byte a, byte b) {
        return (byte) (a | b);
    }

    /** Returns directions in `from` that are not present in `remove` */
    public static byte subtractDirs(byte from, byte remove) {
        return (byte) (from & ~remove);
    }

    /** Union a with dir, aka add dir to a if it wasn't already there */
    public static byte unionDirs(byte a, Direction dir) {
        return (byte) (a | dir2byte(dir));
    }

    /** Returns directions in `from` other than `remove` */
    public static byte subtractDirs(byte from, Direction remove) {
        return (byte) (from & ~dir2byte(remove));
    }

    /** Returns the complement; for every direction, its truth value is inverted */
    public static byte invertValues(byte a) {
        return (byte) (~a & 0b111111);
    }

    /** Whether dir is present in mask */
    public static boolean containsDir(byte mask, Direction dir) {
        return (mask & dir2byte(dir)) != 0;
    }

    /** Whether there are any directions in this mask */
    public static boolean hasAnyDir(byte mask) {
        return mask != 0;
    }

    /** Whether this mask has no directions */
    public static boolean hasNoDirs(byte mask) {
        return mask == 0;
    }
}
