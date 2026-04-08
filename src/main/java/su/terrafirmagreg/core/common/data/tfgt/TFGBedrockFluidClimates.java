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

    // =========================================================
    // BIOME SETS
    // =========================================================

    private static final Set<ResourceKey<Biome>> TRUE_OCEAN_BIOMES = Set.of(
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ocean")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ocean_reef")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/deep_ocean")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/deep_ocean_trench")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/sunken_shield_volcano")));

    private static final Set<ResourceKey<Biome>> SANDSTONE_BIOMES = Set.of(
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/badlands")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/canyons")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/low_canyons")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/whorled_canyons")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/stair_step_canyons")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/mesas")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/buttes")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/hoodoos")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/burren_badlands")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/burren_badlands_tall")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/shilin_canyons")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/doline_canyons")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/cenote_canyons")));

    private static final Set<ResourceKey<Biome>> DRY_BIOMES = Set.of(
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/mud_flats")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/salt_flats")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/dune_sea")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/grassy_dunes")));

    private static final Set<ResourceKey<Biome>> COLD_BIOMES = Set.of(
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/burren_roche_moutonee")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_mountains")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_oceanic_mountains")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_shield_volcano")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_tuyas")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/subglacial_lake")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_edge")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_tuyas_edge")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_mountains_edge")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_oceanic_mountains_edge")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/meltwater_lake")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_oceanic")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_shore")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/glaciated_mountains")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/glaciated_oceanic_mountains")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/glaciated_shield_volcano")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/tuyas")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/drumlins")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/knob_and_kettle")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/patterned_ground")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/inverted_patterned_ground")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/stone_circles")));

    private static final Set<ResourceKey<Biome>> NORMAL_BIOMES = Set.of(
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/plains")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/hills")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/lowlands")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/rolling_hills")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/highlands")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/badlands")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/plateau")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/plateau_wide")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/plateau_lake")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/rocky_plateau")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/tower_karst_plains")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/tower_karst_canyons")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/tower_karst_hills")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/tower_karst_highlands")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/burren_plateau")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/burren_plains")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/shilin_hills")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/shilin_highlands")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/shilin_plateau")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/doline_plains")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/doline_hills")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/doline_rolling_hills")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/doline_highlands")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/doline_plateau")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/cenote_plains")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/cenote_hills")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/cenote_rolling_hills")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/cenote_highlands")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/cenote_plateau")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/extreme_doline_plateau")));

    private static final Set<ResourceKey<Biome>> SWAMP_BIOMES = Set.of(
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/salt_marsh")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/lowlands")));

    private static final Set<ResourceKey<Biome>> VOLCANIC_BIOMES = Set.of(
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/canyons")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/volcanic_mountains")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/volcanic_oceanic_mountains")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/active_shield_volcano")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/dormant_shield_volcano")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/extinct_shield_volcano")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ancient_shield_volcano")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/sunken_shield_volcano")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/shield_volcano_shore")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/old_shield_volcano_shore")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_shield_volcano")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_tuyas")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/ice_sheet_tuyas_edge")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/glaciated_shield_volcano")),
            ResourceKey.create(Registries.BIOME, TFGCore.id("earth/tuyas")));

    static {
        // =========================================================
        // NATURAL GAS
        // =========================================================

        // Surface Indicator - Wet/Cold Climate
        climateModifier(TFGCore.id("natural_gas_surface_indicator"), -20, 0, 300, 500, 50);

        // Ocean Biomes - Cold/Wet Climate
        climateAndBiomeModifier(TFGCore.id("natural_gas_ocean"), -20, 10, 200, 500, TRUE_OCEAN_BIOMES, 20);

        // Surface Indicator - Permafrost (is_cold biomes, full temp range)
        climateAndBiomeModifier(TFGCore.id("natural_gas_permafrost"), -20, 30, 250, 500, COLD_BIOMES, 30);

        // Surface Indicator - Swamp
        climateAndBiomeModifier(TFGCore.id("natural_gas_swamp"), -20, 30, 200, 500, SWAMP_BIOMES, 10);

        // Surface Indicator - Volcanic
        climateAndBiomeModifier(TFGCore.id("natural_gas_volcanic"), -20, 30, 0, 300, VOLCANIC_BIOMES, 5);

        // =========================================================
        // LIGHT OIL
        // =========================================================

        // Spout - Hot/Dry Climate
        climateModifier(TFGCore.id("light_oil_spout_hot"), 20, 30, 0, 50, 50);

        // Spill - Ocean Biomes
        climateAndBiomeModifier(TFGCore.id("light_oil_spill_ocean"), 15, 30, 0, 100, TRUE_OCEAN_BIOMES, 50);

        // Normal Biomes - Low Rainfall
        climateAndBiomeModifier(TFGCore.id("light_oil_normal"), -20, 30, 0, 350, NORMAL_BIOMES, 2);

        // =========================================================
        // OIL
        // =========================================================

        // Spout - Hot/Dry Climate
        climateModifier(TFGCore.id("oil_spout_hot"), 20, 30, 0, 50, 30);

        // Spill - Ocean Biomes
        climateAndBiomeModifier(TFGCore.id("oil_spill_ocean"), 15, 30, 0, 100, TRUE_OCEAN_BIOMES, 30);

        // Normal Biomes - Temperate
        climateAndBiomeModifier(TFGCore.id("oil_normal"), 5, 25, 0, 300, NORMAL_BIOMES, 1);

        // =========================================================
        // RAW OIL
        // =========================================================

        // Spout - Hot/Dry Climate
        climateModifier(TFGCore.id("raw_oil_spout_hot"), 20, 30, 0, 50, 30);

        // Spill - Ocean Biomes
        climateAndBiomeModifier(TFGCore.id("raw_oil_spill_ocean"), 15, 30, 0, 100, TRUE_OCEAN_BIOMES, 30);

        // Spout - Hot Sandstone
        climateAndBiomeModifier(TFGCore.id("raw_oil_spout_sandstone"), 10, 30, 0, 100, SANDSTONE_BIOMES, 50);

        // =========================================================
        // HEAVY OIL
        // =========================================================

        // Spout - Hot/Dry Climate
        climateModifier(TFGCore.id("heavy_oil_spout_hot"), 20, 30, 0, 50, 20);

        // Spout - Hot Dry Biomes
        climateAndBiomeModifier(TFGCore.id("heavy_oil_spout_dry"), 15, 30, 0, 100, DRY_BIOMES, 20);
    }
}