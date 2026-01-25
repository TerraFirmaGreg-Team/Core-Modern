package su.terrafirmagreg.core.utils.atmosphere;

/**
 * Configuration for flood fill operations.
 *
 * @param maxBlocks Maximum number of blocks to fill before stopping (room size limit)
 * @param maxHorizontalDimension Maximum horizontal distance from start position
 */
public record FloodFillConfig(
        int maxBlocks,
        int maxHorizontalDimension) {
    /**
     * Tier 0 - Entry level (HV tech, first moon trip): 10K blocks
     */
    public static final FloodFillConfig TIER_0 = new FloodFillConfig(10_000, 128);

    /**
     * Tier 1 - Mid level: 100K blocks
     */
    public static final FloodFillConfig TIER_1 = new FloodFillConfig(100_000, 128);

    /**
     * Tier 2 - High level: 1M blocks
     */
    public static final FloodFillConfig TIER_2 = new FloodFillConfig(1_000_000, 128);

    /**
     * Creates a config for a specific tier.
     *
     * @param tier Atmosphere tier (0, 1, or 2)
     * @return Configuration appropriate for that tier
     */
    public static FloodFillConfig forTier(int tier) {
        return switch (tier) {
            case 0 -> TIER_0;
            case 1 -> TIER_1;
            default -> TIER_2;
        };
    }
}
