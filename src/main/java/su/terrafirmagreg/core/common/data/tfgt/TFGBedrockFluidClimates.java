package su.terrafirmagreg.core.common.data.tfgt;

import java.util.Set;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.tfgt.worldgen.ClimateWeightModifier;
import su.terrafirmagreg.core.common.tfgt.worldgen.TFGBedrockFluidRegistry;

public class TFGBedrockFluidClimates {

    public static void init() {
    }

    /// Add a fluid vein dependent on only average temperature
    private static void temperatureModifier(ResourceLocation id, int min, int max, int weight) {
        TFGBedrockFluidRegistry.addClimate(id, new ClimateWeightModifier(ClimateWeightModifier.Mode.TEMPERATURE, min, max, weight));
    }

    /// Add a fluid vein dependent on only rainfall
    private static void rainfallModifier(ResourceLocation id, int min, int max, int weight) {
        TFGBedrockFluidRegistry.addClimate(id, new ClimateWeightModifier(ClimateWeightModifier.Mode.RAINFALL, min, max, weight));
    }

    /// Add a fluid vein dependent on both average temperature and rainfall
    private static void climateModifier(ResourceLocation id, int tempMin, int tempMax, int rainMin, int rainMax, int weight) {
        TFGBedrockFluidRegistry.addClimate(id, ClimateWeightModifier.combined(tempMin, tempMax, rainMin, rainMax, weight));
    }

    /// Add a fluid vein dependent on average temperature, rainfall, and biome
    private static void climateAndBiomeModifier(ResourceLocation id, int tempMin, int tempMax, int rainMin, int rainMax, Set<ResourceKey<Biome>> biomes, int weight) {
        TFGBedrockFluidRegistry.addClimate(id, ClimateWeightModifier.combinedWithBiome(tempMin, tempMax, rainMin, rainMax, biomes, weight));
    }

    private static final Set<ResourceKey<Biome>> TRUE_OCEAN_BIOMES = Set.of(
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ocean")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ocean_reef")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/deep_ocean")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/deep_ocean_trench")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/sunken_shield_volcano")));

    static {
        // =========================================================
        // NATURAL GAS
        // =========================================================

        // Surface Indicator - Wet/Cold Climate
        climateModifier(TFGCore.id("natural_gas_surface_indicator"), -20, 0, 300, 500, 50);

        // Ocean Biomes - Cold/Wet Climate
        climateAndBiomeModifier(TFGCore.id("natural_gas_ocean"), -20, 10, 200, 500, TRUE_OCEAN_BIOMES, 50);

        // =========================================================
        // LIGHT OIL
        // =========================================================

        // Spout - Hot/Dry Climate
        climateModifier(TFGCore.id("light_oil_spout_hot"), 20, 30, 0, 50, 50);

        // Spout - Ocean Biomes
        climateAndBiomeModifier(TFGCore.id("light_oil_spout_ocean"), 15, 30, 0, 100, TRUE_OCEAN_BIOMES, 100);

        // =========================================================
        // OIL
        // =========================================================

        // Spout - Hot/Dry Climate
        climateModifier(TFGCore.id("oil_spout_hot"), 20, 30, 0, 50, 30);

        // Spout - Ocean Biomes
        climateAndBiomeModifier(TFGCore.id("oil_spout_ocean"), 15, 30, 0, 100, TRUE_OCEAN_BIOMES, 30);

        // =========================================================
        // HEAVY OIL
        // =========================================================

        // Spout - Hot/Dry Climate
        climateModifier(TFGCore.id("heavy_oil_spout_hot"), 20, 30, 0, 50, 20);

        // Spout - Ocean Biomes
        climateAndBiomeModifier(TFGCore.id("heavy_oil_spout_ocean"), 15, 30, 0, 100, TRUE_OCEAN_BIOMES, 20);

        // =========================================================
        // RAW OIL
        // =========================================================

        // Spout - Hot/Dry Climate
        climateModifier(TFGCore.id("raw_oil_spout_hot"), 20, 30, 0, 50, 30);

        // Spout - Ocean Biomes
        climateAndBiomeModifier(TFGCore.id("raw_oil_spout_ocean"), 15, 30, 0, 100, TRUE_OCEAN_BIOMES, 30);
    }
}
