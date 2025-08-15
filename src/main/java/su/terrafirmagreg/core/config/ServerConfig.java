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
    public final ForgeConfigSpec.IntValue COPPER_ORE_PROSPECTOR_LENGTH;
    public final ForgeConfigSpec.IntValue COPPER_ORE_PROSPECTOR_WIDTH;
    public final ForgeConfigSpec.IntValue BRONZE_ORE_PROSPECTOR_LENGTH;
    public final ForgeConfigSpec.IntValue BRONZE_ORE_PROSPECTOR_WIDTH;
    public final ForgeConfigSpec.IntValue WROUGHT_IRON_ORE_PROSPECTOR_LENGTH;
    public final ForgeConfigSpec.IntValue WROUGHT_IRON_ORE_PROSPECTOR_WIDTH;
    public final ForgeConfigSpec.IntValue STEEL_ORE_PROSPECTOR_LENGTH;
    public final ForgeConfigSpec.IntValue STEEL_ORE_PROSPECTOR_WIDTH;
    public final ForgeConfigSpec.IntValue BLACK_STEEL_ORE_PROSPECTOR_LENGTH;
    public final ForgeConfigSpec.IntValue BLACK_STEEL_ORE_PROSPECTOR_WIDTH;
    public final ForgeConfigSpec.IntValue BLUE_STEEL_ORE_PROSPECTOR_LENGTH;
    public final ForgeConfigSpec.IntValue BLUE_STEEL_ORE_PROSPECTOR_WIDTH;
    public final ForgeConfigSpec.BooleanValue BLUE_STEEL_ORE_PROSPECTOR_RENDER;
    public final ForgeConfigSpec.IntValue RED_STEEL_ORE_PROSPECTOR_WIDTH;
    public final ForgeConfigSpec.IntValue RED_STEEL_ORE_PROSPECTOR_LENGTH;
    public final ForgeConfigSpec.BooleanValue RED_STEEL_ORE_PROSPECTOR_RENDER;
    public final ForgeConfigSpec.IntValue HARVEST_BASKET_RANGE;

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
        builder.push("tools");
        COPPER_ORE_PROSPECTOR_LENGTH = builder
                .comment("\n\nLength of search area. Default: 50")
                .defineInRange("CopperOreProspectorLength", 50, 0, 200);
        COPPER_ORE_PROSPECTOR_WIDTH = builder
                .comment("\nHalf of the width of the search area. Default: 5", "Example. If you want a 20x20 set the value to 10")
                .defineInRange("CopperOreProspectorHalfWidth", 5, 0, 50);
        BRONZE_ORE_PROSPECTOR_LENGTH = builder
                .comment("\n\nLength of search area. Default: 60")
                .defineInRange("BronzeOreProspectorLength", 60, 0, 200);
        BRONZE_ORE_PROSPECTOR_WIDTH = builder
                .comment("\nHalf of the width of the search area. Default: 8")
                .defineInRange("BronzeOreProspectorHalfWidth", 8, 0, 50);
        WROUGHT_IRON_ORE_PROSPECTOR_LENGTH = builder
                .comment("\n\nLength of search area. Default: 70")
                .defineInRange("WroughtIronOreProspectorLength", 70, 0, 200);
        WROUGHT_IRON_ORE_PROSPECTOR_WIDTH = builder
                .comment("\nHalf of the width of the search area. Default: 10")
                .defineInRange("WroughtIronOreProspectorHalfWidth", 10, 0, 50);
        STEEL_ORE_PROSPECTOR_LENGTH = builder
                .comment("\n\nLength of search area. Default: 80")
                .defineInRange("SteelOreProspectorLength", 80, 0, 200);
        STEEL_ORE_PROSPECTOR_WIDTH = builder
                .comment("\nHalf of the width of the search area. Default: 12")
                .defineInRange("SteelOreProspectorHalfWidth", 12, 0, 50);
        BLACK_STEEL_ORE_PROSPECTOR_LENGTH = builder
                .comment("\n\nLength of search area. Default: 90")
                .defineInRange("BlackSteelOreProspectorLength", 90, 0, 200);
        BLACK_STEEL_ORE_PROSPECTOR_WIDTH = builder
                .comment("\nHalf of the width of the search area. Default: 15")
                .defineInRange("BlackSteelOreProspectorHalfWidth", 15, 0, 50);
        BLUE_STEEL_ORE_PROSPECTOR_LENGTH = builder
                .comment("\n\nLength of search area. Default: 1400")
                .defineInRange("BlueSteelOreProspectorLength", 140, 0, 200);
        BLUE_STEEL_ORE_PROSPECTOR_WIDTH = builder
                .comment("\nHalf of the width of the search area. Default: 15")
                .defineInRange("BlueSteelOreProspectorHalfWidth", 15, 0, 50);
        BLUE_STEEL_ORE_PROSPECTOR_RENDER = builder
                .comment("\nShould the blue steel propick render particles per vein (vague)?", "Setting false will render particles per block (precise). Default: true")
                .define("BlueSteelOreProspectorRender", true);
        RED_STEEL_ORE_PROSPECTOR_LENGTH = builder
                .comment("\n\nLength of search area. Default: 90")
                .defineInRange("RedSteelOreProspectorLength", 90, 0, 200);
        RED_STEEL_ORE_PROSPECTOR_WIDTH = builder
                .comment("\nHalf of the width of the search area. Default: 25")
                .defineInRange("RedSteelOreProspectorHalfWidth", 25, 0, 50);
        RED_STEEL_ORE_PROSPECTOR_RENDER = builder
                .comment("\nShould the red steel propick render particles per vein (vague)?", "Setting false will render particles per block (precise). Default: false")
                .define("RedSteelOreProspectorRender", false);

        HARVEST_BASKET_RANGE = builder
                .comment("\n\nRadius of the harvest basket collection. Set to 0 to disable. Default: 7")
                .defineInRange("HarvestBasketRange", 7, 0, 20);
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
