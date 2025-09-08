package su.terrafirmagreg.core.config;

import com.mojang.logging.LogUtils;
import earth.terrarium.adastra.api.planets.Planet;
import net.dries007.tfc.util.Metal;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import su.terrafirmagreg.core.config.tools.PropickConfig;
import su.terrafirmagreg.core.config.tools.RenderingPropickConfig;

import java.util.HashMap;
import java.util.List;

/**
 * Server Config
 *  - Synced from server to client, can have default config settings be customized by users.
 *  - Default to this config for most things, and only use client/common when appropriate.
 */
public final class ServerConfig {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<ResourceKey<Level>> planetDimensions = List.of(Planet.EARTH_ORBIT, Planet.MOON_ORBIT, Planet.MARS_ORBIT, Planet.VENUS_ORBIT, Planet.MERCURY_ORBIT, Planet.GLACIO_ORBIT, Planet.MOON, Planet.MARS, Planet.VENUS, Planet.MERCURY, Planet.GLACIO);
    public final HashMap<ResourceKey<Level>, ForgeConfigSpec.BooleanValue> glidersWorkOnPlanets;

    public final PropickConfig copperPropickConfig;
    public final PropickConfig bronzePropickConfig;
    public final PropickConfig wroughtIronPropickConfig;
    public final PropickConfig steelPropickConfig;
    public final PropickConfig blackSteelPropickConfig;
    public final RenderingPropickConfig blueSteelPropickConfig;
    public final RenderingPropickConfig redSteelPropickConfig;

    public final ForgeConfigSpec.IntValue HARVEST_BASKET_RANGE;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> SYRINGE_BLACKLIST;

    ServerConfig(ForgeConfigSpec.Builder builder) {
        builder.push("hang_glider");

        glidersWorkOnPlanets = new HashMap<>();
        for (ResourceKey<Level> dimension : planetDimensions) {

            String dimensionName = dimension.location().getPath();
            String dimensionPath = "can_glide_on_" + dimensionName;
            glidersWorkOnPlanets.put(dimension, builder
                    .comment(String.format("\nIf true, gliders will function in the Ad Astra dimension %s", ConfigHelpers.toTitleCase(dimensionName)))
                    .define(dimensionPath, false)
            );
        }

        builder.pop().push("prospector_picks").push("copper");
        copperPropickConfig = PropickConfig.build(builder, Metal.Default.COPPER, 15, 5);
        builder.pop().push("bronze");
        bronzePropickConfig = PropickConfig.build(builder, Metal.Default.BRONZE, 20, 8);
        builder.pop().push("wrought_iron");
        wroughtIronPropickConfig = PropickConfig.build(builder, Metal.Default.WROUGHT_IRON, 30, 10);
        builder.pop().push("steel");
        steelPropickConfig = PropickConfig.build(builder, Metal.Default.STEEL, 40, 12);
        builder.pop().push("black_steel");
        blackSteelPropickConfig = PropickConfig.build(builder, Metal.Default.BLACK_STEEL, 50, 15);
        builder.pop().push("blue_steel");
        blueSteelPropickConfig = RenderingPropickConfig.build(builder, Metal.Default.BLUE_STEEL, 75, 15, true);
        builder.pop().push("red_steel");
        redSteelPropickConfig = RenderingPropickConfig.build(builder, Metal.Default.RED_STEEL, 50, 25, false);


        builder.pop(2).push("harvest_basket");
        HARVEST_BASKET_RANGE = builder
                .comment("\nRadius of the harvest basket collection. Set to 0 to disable. Default: 7")
                .defineInRange("HarvestBasketRange", 7, 0, 20);

        builder.pop().push("syringe_blacklist");
        SYRINGE_BLACKLIST = builder
                .comment("Blacklist of entity IDs that cannot be sampled by the DNA syringe. Can be empty.")
                .defineListAllowEmpty(
                        "syringeBlacklist", List.of(),
                        o -> {
                            if (!(o instanceof String s)) return false;
                            ResourceLocation id = ResourceLocation.tryParse(s);
                            if (id == null) {
                                LOGGER.warn("[TFG Config] Invalid entity ID syntax in syringeBlacklist: {}", s);
                                return false;
                            }
                            if (!ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
                                LOGGER.warn("[TFG Config] Unknown entity ID in syringeBlacklist: {}", id);
                                return false;
                            }
                            return true;
                        }
                );
        builder.pop();
    }

}
