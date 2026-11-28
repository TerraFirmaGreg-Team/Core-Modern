package su.terrafirmagreg.core.common.environment;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.utils.TFGHelpers;

/**
 * Defines the natural environmental properties of a dimension.
 * Used to determine baseline atmosphere, gravity, temperature, and pressure
 * before checking machine-provided sources.
 *
 * <p>Pressure is in pascals (Pa).
 * Pressure determines whether decompression/compression events occur when a
 * sealed room is breached, and how much extra oxygen is consumed during breach.
 * 
 * Dimension temperature is currently unused.
 */
public record DimensionEnvironment(
        @Nullable Fluid atmosphere,
        float gravity,
        short temperature,
        float pressure) {

    /** Hot Temperature for Ad Astra, vanilla is 70 */
    public static final int HOT_TEMPERATURE_THRESHOLD = 150;

    /** Earth's atmospheric pressure in Pa. */
    public static final float EARTH_PRESSURE = 101_325.0f;

    /** Earth's gravity in m/s^2. */
    public static final float EARTH_GRAVITY = 9.807f;

    public static final DimensionEnvironment EARTH_LIKE = new DimensionEnvironment(
            fluid("gtceu:air"), EARTH_GRAVITY, (short) 15, EARTH_PRESSURE);
    public static final DimensionEnvironment VACUUM = new DimensionEnvironment(null, 0.0f, (short) -270, 0.0f);

    private static final Map<ResourceKey<Level>, DimensionEnvironment> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(Level.OVERWORLD, EARTH_LIKE);
        REGISTRY.put(Level.NETHER, EARTH_LIKE);

        REGISTRY.put(Planet.MOON, new DimensionEnvironment(
                fluid("gtceu:argon"), 1.625f, (short) -173, 0.0f));
        REGISTRY.put(Planet.MARS, new DimensionEnvironment(
                fluid("tfg:mars_air"), 3.721f, (short) -65, 636.0f));
        REGISTRY.put(Planet.VENUS, new DimensionEnvironment(
                fluid("tfg:venus_air"), 8.87f, (short) 464, 9_200_000.0f));
        REGISTRY.put(Planet.MERCURY, VACUUM);
        REGISTRY.put(Planet.GLACIO, new DimensionEnvironment(
                fluid("gtceu:water"), 3.721f, (short) -20, 140_000_000.0f));

        REGISTRY.put(Planet.EARTH_ORBIT, VACUUM);
        REGISTRY.put(Planet.MOON_ORBIT, VACUUM);
        REGISTRY.put(Planet.MARS_ORBIT, VACUUM);
        REGISTRY.put(Planet.VENUS_ORBIT, VACUUM);
        REGISTRY.put(Planet.MERCURY_ORBIT, VACUUM);
        REGISTRY.put(Planet.GLACIO_ORBIT, VACUUM);
    }

    private static Fluid fluid(String name) {
        return TFGHelpers.getMaterial(name).getFluid();
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

    /**
     * Use atmosphere instead.
     */
    @Deprecated
    public boolean hasOxygen() {
        return atmosphere != null && atmosphere.is(TFGTags.Fluids.BreathableAir);
    }
}
