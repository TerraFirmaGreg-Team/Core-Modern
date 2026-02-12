package su.terrafirmagreg.core.common.environment;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;

/**
 * Defines the natural environmental properties of a dimension.
 * Used to determine baseline oxygen, gravity, temperature, and environment
 * before checking machine-provided sources.
 */
// TODO This should probably be floats or something instead of booleans, I'll fix it once I start implementing
//  gravity and temperature stuff.
public record DimensionEnvironment(
        boolean hasOxygen,
        boolean hasNormalGravity,
        boolean hasNormalTemperature,
        boolean hasAtmosphere) {

    public static final DimensionEnvironment EARTH_LIKE = new DimensionEnvironment(true, true, true, true);
    public static final DimensionEnvironment VACUUM = new DimensionEnvironment(false, false, false, false);

    private static final Map<ResourceKey<Level>, DimensionEnvironment> REGISTRY = new HashMap<>();

    static {
        // Earth-like dimensions
        // Fixme temp for testing Overworld earthlike
        REGISTRY.put(Level.OVERWORLD, VACUUM);
        //REGISTRY.put(Level.OVERWORLD, EARTH_LIKE);
        REGISTRY.put(Level.NETHER, EARTH_LIKE);

        REGISTRY.put(Planet.MOON, VACUUM);
        REGISTRY.put(Planet.MARS, new DimensionEnvironment(false, false, false, false));
        REGISTRY.put(Planet.VENUS, new DimensionEnvironment(false, true, true, true));
        REGISTRY.put(Planet.MERCURY, VACUUM);
        REGISTRY.put(Planet.GLACIO, VACUUM);

        // Orbits
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
