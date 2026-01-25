package su.terrafirmagreg.core.utils.atmosphere;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

/**
 * Diagnostic version of flood fill that tracks the path to the escape point.
 * Slightly slower than regular FloodFill due to parent tracking overhead.
 * Use only for debugging or showing players where the breach is.
 *
 * <p>Returns a {@link FloodFillResult} with the {@code escapePath} field populated.
 */
public final class DiagnosticFloodFill {

    private static final long NO_PARENT = Long.MIN_VALUE;

    private DiagnosticFloodFill() {
    }

    /**
     * Performs a diagnostic flood fill with path tracking.
     * Same as {@link FloodFill#fill} but populates the escape path in the result.
     *
     * @param level Block getter
     * @param heightAccessor Height accessor for build height limits
     * @param start Starting position
     * @param config Configuration for limits
     * @return FloodFillResult with escapePath populated if there's an escape
     */
    public static FloodFillResult fill(Level level, LevelHeightAccessor heightAccessor,
            BlockPos start, FloodFillConfig config) {
        // Create state with parent tracking enabled
        FloodFillState state = new FloodFillState();
        state.parentMap = new Long2LongOpenHashMap();
        state.parentMap.defaultReturnValue(NO_PARENT);

        // Use the regular fill logic with parent tracking active
        return FloodFill.fillWithState(level, heightAccessor, start, config, state);
    }
}
