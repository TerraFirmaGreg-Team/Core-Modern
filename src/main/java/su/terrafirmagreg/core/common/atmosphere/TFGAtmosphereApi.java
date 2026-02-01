package su.terrafirmagreg.core.common.atmosphere;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Public API for atmosphere queries.
 * Replaces Ad Astra's OxygenApi calls with our custom implementation.
 */
public final class TFGAtmosphereApi {

    private TFGAtmosphereApi() {
    }

    /**
     * Checks if a position has oxygen (breathable atmosphere).
     * This is the main method to replace Ad Astra's OxygenApi.hasOxygen().
     *
     * @param level The level to check in
     * @param pos The position to check
     * @return true if the position has oxygen
     */
    public static boolean hasOxygen(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return false;
        }

        return AtmosphereSystem.hasOxygen(level, pos);
    }

    /**
     * Checks if a position has normal gravity.
     *
     * @param level The level to check in
     * @param pos The position to check
     * @return true if gravity is normalized at this position
     */
    public static boolean hasNormalGravity(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return false;
        }

        return AtmosphereSystem.hasNormalGravity(level, pos);
    }

    /**
     * Checks if a position has normal temperature.
     *
     * @param level The level to check in
     * @param pos The position to check
     * @return true if temperature is normalized at this position
     */
    public static boolean hasNormalTemperature(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return false;
        }

        return AtmosphereSystem.hasNormalTemperature(level, pos);
    }

    /**
     * Checks if a position has normal pressure.
     * Used for high-pressure environments like Europa's ocean.
     *
     * @param level The level to check in
     * @param pos The position to check
     * @return true if pressure is normalized at this position
     */
    public static boolean hasNormalPressure(Level level, BlockPos pos) {
        // For now, pressure uses the same system as oxygen
        // High-pressure environments would need inverted logic (keep pressure OUT)
        if (level.isClientSide()) {
            return false;
        }

        return AtmosphereSystem.hasOxygen(level, pos);
    }

    /**
     * Checks if any atmosphere effect is active at a position.
     * Useful for determining if a player is "protected" in any way.
     *
     * @param level The level to check in
     * @param pos The position to check
     * @return true if any atmosphere provider covers this position
     */
    public static boolean hasAnyAtmosphereProtection(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return false;
        }

        return AtmosphereSystem.hasOxygen(level, pos) ||
                AtmosphereSystem.hasNormalGravity(level, pos) ||
                AtmosphereSystem.hasNormalTemperature(level, pos);
    }
}
