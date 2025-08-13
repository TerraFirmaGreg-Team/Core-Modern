package su.terrafirmagreg.core.config;

import earth.terrarium.adastra.api.planets.Planet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.List;

public final class ServerConfig {

    private static final List<ResourceKey<Level>> planetDimensions = List.of(Planet.EARTH_ORBIT, Planet.MOON_ORBIT, Planet.MARS_ORBIT, Planet.VENUS_ORBIT, Planet.MERCURY_ORBIT, Planet.GLACIO_ORBIT, Planet.MOON, Planet.MARS, Planet.VENUS, Planet.MERCURY, Planet.GLACIO);
    public final HashMap<ResourceKey<Level>, ForgeConfigSpec.BooleanValue> glidersWorkOnPlanets;

    ServerConfig(ForgeConfigSpec.Builder builder) {
        builder.push("hang_glider");

        glidersWorkOnPlanets = new HashMap<>();
        for (ResourceKey<Level> dimension : planetDimensions) {

            String dimensionName = dimension.location().getPath();
            String dimensionPath = "can_glide_on_" + dimensionName;
            glidersWorkOnPlanets.put(dimension, builder
                    .comment(String.format("If true, gliders will function in the Ad Astra dimension %s", toTitleCase(dimensionName)))
                    .define(dimensionPath, false)
            );
        }

        builder.pop();
    }

    private static String toTitleCase(String input) {
        String[] parts = input.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            result.append(part.substring(0, 1).toUpperCase())
                    .append(part.substring(1).toLowerCase())
                    .append(" ");
        }
        return result.toString().trim();
    }
}
