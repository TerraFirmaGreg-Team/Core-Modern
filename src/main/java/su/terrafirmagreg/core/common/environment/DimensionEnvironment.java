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
 */
public record DimensionEnvironment(
        boolean hasOxygen,
        float gravity,
        short temperature,
        float pressure) {

    /** Pressure threshold below which decompression events occur (low pressure → air rushes out) */
    public static final float DECOMPRESSION_THRESHOLD = 0.5f;

    /** Earth-normalized gravity (1.0 = Earth). Used by Ad Astra's gravity system. */
    public static final float EARTH_GRAVITY = 1.0f;

    /** Ad Astra liveable range: -50°C to 70°C */
    public static final short MIN_LIVEABLE_TEMPERATURE = -50;
    public static final short MAX_LIVEABLE_TEMPERATURE = 70;

    public boolean hasNormalGravity() {
        return gravity == EARTH_GRAVITY;
    }

    public boolean hasNormalTemperature() {
        return temperature >= MIN_LIVEABLE_TEMPERATURE && temperature <= MAX_LIVEABLE_TEMPERATURE;
    }

    public static final DimensionEnvironment EARTH_LIKE = new DimensionEnvironment(true, 1.0f, (short) 15, 1.0f);
    public static final DimensionEnvironment VACUUM = new DimensionEnvironment(false, 0.0f, (short) -270, 0.0f);

    private static final Map<ResourceKey<Level>, DimensionEnvironment> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(Level.OVERWORLD, EARTH_LIKE);
        REGISTRY.put(Level.NETHER, EARTH_LIKE);

        REGISTRY.put(Planet.MOON, VACUUM);
        REGISTRY.put(Planet.MARS, new DimensionEnvironment(false, 0.379f, (short) -65, 0.006f));
        REGISTRY.put(Planet.VENUS, new DimensionEnvironment(false, 0.904f, (short) 464, 90.0f));
        REGISTRY.put(Planet.MERCURY, new DimensionEnvironment(false, 0.377f, (short) 167, 0.0f));
        REGISTRY.put(Planet.GLACIO, new DimensionEnvironment(false, 0.379f, (short) -20, 0.0f));

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
}
