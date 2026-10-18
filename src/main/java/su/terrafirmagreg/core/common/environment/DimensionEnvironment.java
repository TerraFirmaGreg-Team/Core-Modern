package su.terrafirmagreg.core.common.environment;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;

/**
 * Defines the natural environmental properties of a dimension.
 * Used to determine baseline oxygen, gravity, temperature, and pressure
 * before checking machine-provided sources.
 *
 * <p>Pressure is in atmospheres (atm): Earth = 1.0, vacuum = 0.0, Venus = 90.0.
 * Pressure determines whether decompression/compression events occur when a
 * sealed room is breached, and how much extra oxygen is consumed during breach.
 * 
 * Dimension temperature is currently unused.
 */
public record DimensionEnvironment(
        boolean hasOxygen,
        float gravity,
        short temperature,
        float pressure) {

    /** Pressure threshold below which decompression events occur (low pressure -> air rushes out) */
    public static final float DECOMPRESSION_THRESHOLD = 0.5f;

    /** Earth's gravity in m/s^2. */
    public static final float EARTH_GRAVITY = 9.807f;

    public static final DimensionEnvironment EARTH_LIKE = new DimensionEnvironment(true, EARTH_GRAVITY, (short) 15, 1.0f);
    public static final DimensionEnvironment VACUUM = new DimensionEnvironment(false, 0.0f, (short) -270, 0.0f);

    private static final Map<ResourceKey<Level>, DimensionEnvironment> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(Level.OVERWORLD, EARTH_LIKE);
        REGISTRY.put(Level.NETHER, EARTH_LIKE);

        REGISTRY.put(Planet.MOON, new DimensionEnvironment(false, 1.625f, (short) -173, 0.0f));
        REGISTRY.put(Planet.MARS, new DimensionEnvironment(false, 3.721f, (short) -65, 0.006f));
        REGISTRY.put(Planet.VENUS, new DimensionEnvironment(false, 8.87f, (short) 464, 90.0f));
        REGISTRY.put(Planet.MERCURY, VACUUM);
        REGISTRY.put(Planet.GLACIO, new DimensionEnvironment(false, 3.721f, (short) -20, 0.0f));

        REGISTRY.put(Planet.EARTH_ORBIT, VACUUM);
        REGISTRY.put(Planet.MOON_ORBIT, VACUUM);
        REGISTRY.put(Planet.MARS_ORBIT, VACUUM);
        REGISTRY.put(Planet.VENUS_ORBIT, VACUUM);
        REGISTRY.put(Planet.MERCURY_ORBIT, VACUUM);
        REGISTRY.put(Planet.GLACIO_ORBIT, VACUUM);
    }

    /**
     * Gets the environment for a dimension. Defaults to EARTH_LIKE for unknown dimensions.
     */
    public static DimensionEnvironment get(ResourceKey<Level> dimension) {
        return REGISTRY.getOrDefault(dimension, EARTH_LIKE);
    }

    public static boolean isRegistered(ResourceKey<Level> dimension) {
        return REGISTRY.containsKey(dimension);
    }
}
