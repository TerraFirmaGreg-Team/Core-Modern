package su.terrafirmagreg.core.config;

import static su.terrafirmagreg.core.TFGCore.LOGGER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dries007.tfc.util.Metal;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.config.tools.PropickConfig;
import su.terrafirmagreg.core.config.tools.RenderingPropickConfig;

/**
 * Server Config - Synced from server to client, can have default config settings be customized by users. - Default to
 * this config for most things, and only use client/common when appropriate.
 */
public final class ServerConfig {

    private static final List<ResourceKey<Level>> planetDimensions = List.of(Planet.EARTH_ORBIT, Planet.MOON_ORBIT,
            Planet.MARS_ORBIT, Planet.VENUS_ORBIT, Planet.MERCURY_ORBIT, Planet.GLACIO_ORBIT, Planet.MOON, Planet.MARS,
            Planet.VENUS, Planet.MERCURY, Planet.GLACIO);
    public final HashMap<ResourceKey<Level>, ForgeConfigSpec.BooleanValue> glidersWorkOnPlanets;

    public final PropickConfig copperPropickConfig;
    public final PropickConfig bronzePropickConfig;
    public final PropickConfig wroughtIronPropickConfig;
    public final PropickConfig steelPropickConfig;
    public final PropickConfig blackSteelPropickConfig;
    public final RenderingPropickConfig blueSteelPropickConfig;
    public final RenderingPropickConfig redSteelPropickConfig;

    public final ForgeConfigSpec.IntValue CHAMELEON_SPRAY_CAN_CAPACITY;
    public final ForgeConfigSpec.IntValue CHAMELEON_SPRAY_CAN_COST_PER_OPERATION;
    public final ForgeConfigSpec.DoubleValue CHAMELEON_SPRAY_CAN_BULK_MULTIPLIER;

    public final ForgeConfigSpec.IntValue HARVEST_BASKET_RANGE;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> SYRINGE_BLACKLIST;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> worldgenOverrides;

    public final ForgeConfigSpec.IntValue sandAccumulateChance;
    public final ForgeConfigSpec.IntValue sandDecumulateChance;
    public final ForgeConfigSpec.BooleanValue enableSnowCorrection;
    public final ForgeConfigSpec.IntValue snowMaxAccumulationOnUpdate;
    public final ForgeConfigSpec.BooleanValue enableTFGFoodDebuffs;
    public final ForgeConfigSpec.BooleanValue enableTFGFoodBuffs;

    public final ForgeConfigSpec.BooleanValue enableBeneathMiningRestrictions;
    public final ForgeConfigSpec.IntValue disabledBeneathMiningYLevel;
    public final ForgeConfigSpec.BooleanValue enableHotPlanetMiningRestrictions;

    ServerConfig(ForgeConfigSpec.Builder builder) {
        builder.push("hang_glider");

        glidersWorkOnPlanets = new HashMap<>();
        for (ResourceKey<Level> dimension : planetDimensions) {

            String dimensionName = dimension.location().getPath();
            String dimensionPath = "can_glide_on_" + dimensionName;
            glidersWorkOnPlanets.put(dimension, builder
                    .comment(String.format("\nIf true, gliders will function in the Ad Astra dimension %s",
                            ConfigHelpers.toTitleCase(dimensionName)))
                    .define(dimensionPath, false));
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
                            if (!(o instanceof String s))
                                return false;
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
                        });

        builder.pop().push("world_generation");
        worldgenOverrides = builder
                .comment("""
                        Per-dimension worldgen version overrides. Normally the version used during world\s
                        creation is stored in SavedData and used automatically. Set an entry here to force\s
                        a specific version regardless of what was recorded at generation time.\s
                        Changing this for an existing world can cause chunk boundary artifacts.\s
                        Format: list of "dimension_id=version", e.g. ["minecraft:overworld=1"]""")
                .defineListAllowEmpty("worldgenOverrides", List.of(), o -> o instanceof String);

        builder.pop().push("mars_climate");
        sandAccumulateChance = builder
                .comment("The chance that sand piles will accumulate during a sandstorm. Lower values = faster sand pile accumulation, but also more block updates (aka lag).")
                .defineInRange("sandAccumulateChance", 20, 1, Integer.MAX_VALUE);

        sandDecumulateChance = builder
                .comment("The chance that sand piles will decumulate during a sandstoem. Lower values = faster sand dispersal, but also more block updates (aka lag).")
                .defineInRange("sandDecumulateChance", 36, 1, Integer.MAX_VALUE);

        builder.pop().push("overworld_climate");
        enableSnowCorrection = builder
                .comment("Enables instant snow and ice removal as chunks are loaded in and the temperature is warm enough.")
                .define("enableSnowCorrection", true);
        snowMaxAccumulationOnUpdate = builder
                .comment("The maximum amount of snow update to apply for each correction tick")
                .defineInRange("snowMaxAccumulationOnUpdate", FMLEnvironment.dist.isClient() ? 256 : 0, 0, Integer.MAX_VALUE);

        builder.pop().push("tfg_food_effects");
        enableTFGFoodDebuffs = builder
                .comment("Enables TFG food debuff effects. Allows receiving harmful effects from contaminants like Toxins, or transient nutrients like Freezing.")
                .define("enableTFGFoodDebuffs", true);
        enableTFGFoodBuffs = builder
                .comment("Enables TFG food buff effects. Allows receiving helpful effects from nutrients like Fruits, or transient nutrients like Fulfilling.")
                .define("enableTFGFoodBuffs", true);

        builder.pop().push("mining_restrictions");
        enableBeneathMiningRestrictions = builder
                .comment("Enables restrictions on automatic mining machines in the Beneath.")
                .define("enableBeneathMiningRestrictions", true);
        disabledBeneathMiningYLevel = builder
                .comment("Below this Y level, single block gregtech miners and create contraptions cannot mine ores.")
                .defineInRange("disabledBeneathMiningYLevel", 80, 1, Integer.MAX_VALUE);
        enableHotPlanetMiningRestrictions = builder
                .comment("Enables restrictions on automatic mining machines on hot planets.")
                .define("enableHotPlanetMiningRestrictions", true);

        builder.pop().push("chameleon_spray_can");
        CHAMELEON_SPRAY_CAN_CAPACITY = builder
                .comment("\nThe maximum Prismatic Paint capacity of the Chameleon Spray Can (in mB). Default: 8000")
                .defineInRange("chameleonSprayCanCapacity", 2000, 1, Integer.MAX_VALUE);

        CHAMELEON_SPRAY_CAN_COST_PER_OPERATION = builder
                .comment("\nThe amount of Prismatic Paint consumed per block/entity recolored (in mB). Default: 1")
                .defineInRange("chameleonSprayCanCostPerOperation", 1, 0, Integer.MAX_VALUE);

        CHAMELEON_SPRAY_CAN_BULK_MULTIPLIER = builder
                .comment("\nThe fluid consumption multiplier applied when chain-painting/bulk-painting blocks (e.g. 0.85 equals a 15% discount). Set to 1.0 to disable discounts.")
                .defineInRange("chameleonSprayCanBulkMultiplier", 1.0, 0.0, 10.0);

        builder.pop();
    }

    /**
     * Parses {@link #worldgenOverrides} into a map of dimension ID to version.
     * Throws if any entry is malformed — the validator should have caught these at config load time.
     */
    public Map<ResourceLocation, Integer> parsedWorldgenOverrides() {
        Map<ResourceLocation, Integer> result = new HashMap<>();
        for (String entry : worldgenOverrides.get()) {
            String[] parts = entry.split("=", 2);
            if (parts.length != 2)
                throw new IllegalStateException("[TFG] Malformed worldgen override entry: " + entry);
            ResourceLocation dim = ResourceLocation.tryParse(parts[0].trim());
            if (dim == null)
                throw new IllegalStateException("[TFG] Invalid dimension ID in worldgen override: " + parts[0].trim());
            try {
                result.put(dim, Integer.parseInt(parts[1].trim()));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("[TFG] Invalid version number in worldgen override: " + parts[1].trim(), e);
            }
        }
        return result;
    }
}
