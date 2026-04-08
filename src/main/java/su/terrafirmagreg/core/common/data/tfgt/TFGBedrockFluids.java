package su.terrafirmagreg.core.common.data.tfgt;

import java.util.Set;
import java.util.function.Consumer;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.common.data.GTBedrockFluids;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.common.data.TFGTags;

public class TFGBedrockFluids {

    public static BedrockFluidDefinition create(ResourceLocation id, Consumer<BedrockFluidDefinition.Builder> consumer) {
        return GTBedrockFluids.create(id, consumer);
    }

    public static void init() {
    }

    // By the way, if you see a vein have zero weight, it's controlled by the biome/climate instead

    private static final Set<ResourceKey<Level>> overworld = Set.of(Level.OVERWORLD);

    // =========================================================
    // WATER
    // =========================================================

    // Common — everywhere except salt water, true ocean, dry biomes
    // Can't exclude so make it as : is_normal + is_mountain + is_cold + is_karst + is_shore_island + is_fresh_water
    public static BedrockFluidDefinition WATER_COMMON = create(TFGCore.id("water_common"), vein -> vein
            .dimensions(overworld)
            .fluid(Fluids.WATER::getSource)
            .weight(0)
            .minimumYield(100)
            .maximumYield(200)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(100)
            .biomes(10, TFGTags.Biomes.EarthIsNormal)
            .biomes(10, TFGTags.Biomes.EarthIsMountain)
            .biomes(10, TFGTags.Biomes.EarthIsCold)
            .biomes(10, TFGTags.Biomes.EarthIsKarst)
            .biomes(10, TFGTags.Biomes.EarthIsShoreIsland)
            .biomes(10, TFGTags.Biomes.EarthIsFreshWater));

    // River
    public static BedrockFluidDefinition WATER_RIVER = create(TFGCore.id("water_river"), vein -> vein
            .dimensions(overworld)
            .fluid(Fluids.WATER::getSource)
            .weight(0)
            .minimumYield(100)
            .maximumYield(500)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(100)
            .biomes(500, TFGTags.Biomes.EarthIsRiver));

    // =========================================================
    // SEA WATER
    // =========================================================

    // Ocean + Salt Water biomes and manually add salt_flats as it's not part of the biome tag
    public static BedrockFluidDefinition SEA_WATER = create(TFGCore.id("sea_water"), vein -> vein
            .dimensions(overworld)
            .fluid(TFCFluids.SALT_WATER::getSource)
            .weight(0)
            .minimumYield(50)
            .maximumYield(500)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(50)
            .biomes(100, TFGTags.Biomes.EarthIsTrueOcean)
            .biomes(100, TFGTags.Biomes.EarthIsSaltWater)
            .biomes(100, TFGTags.Biomes.EarthIsSaltFlats));

    // =========================================================
    // MUDDY WATER
    // =========================================================

    // Common — everywhere except true ocean, shore/island, dry biomes
    public static BedrockFluidDefinition MUDDY_WATER = create(TFGCore.id("muddy_water"), vein -> vein
            .dimensions(overworld)
            .fluid(TFGFluids.MUDDY_WATER::getSource)
            .weight(0)
            .minimumYield(5)
            .maximumYield(100)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(10)
            .biomes(5, TFGTags.Biomes.EarthIsNormal)
            .biomes(5, TFGTags.Biomes.EarthIsMountain)
            .biomes(5, TFGTags.Biomes.EarthIsCold)
            .biomes(5, TFGTags.Biomes.EarthIsKarst)
            .biomes(5, TFGTags.Biomes.EarthIsFreshWater)
            .biomes(5, TFGTags.Biomes.EarthIsVolcanic)
            .biomes(5, TFGTags.Biomes.EarthIsRiver));

    public static BedrockFluidDefinition MUDDY_WATER_SWAMP = create(TFGCore.id("muddy_water_swamp"), vein -> vein
            .dimensions(overworld)
            .fluid(TFGFluids.MUDDY_WATER::getSource)
            .weight(0)
            .minimumYield(5)
            .maximumYield(100)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(10)
            .biomes(10, TFGTags.Biomes.EarthIsSwamp));

    // =========================================================
    // SPRING WATER
    // =========================================================

    // Volcanic biomes only
    public static BedrockFluidDefinition SPRING_WATER = create(TFGCore.id("spring_water"), vein -> vein
            .dimensions(overworld)
            .fluid(TFCFluids.SPRING_WATER::getSource)
            .weight(0)
            .minimumYield(100)
            .maximumYield(200)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(5)
            .biomes(20, TFGTags.Biomes.EarthIsVolcanic));

    // =========================================================
    // LIGHT OIL
    // =========================================================

    // Spout - Hot/Dry Climate — climate only
    public static BedrockFluidDefinition LIGHT_OIL_HOT = create(TFGCore.id("light_oil_spout_hot"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilLight::getFluid)
            .weight(0)
            .minimumYield(175)
            .maximumYield(300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(55));

    // Spill - Ocean Biomes — climate + biome (defined in Climates)
    public static BedrockFluidDefinition LIGHT_OIL_OCEAN = create(TFGCore.id("light_oil_spill_ocean"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilLight::getFluid)
            .weight(0)
            .minimumYield(150)
            .maximumYield(250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20));

    // Shore — biome only
    public static BedrockFluidDefinition LIGHT_OIL_SHORE = create(TFGCore.id("light_oil_shore"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilLight::getFluid)
            .weight(0)
            .minimumYield(75)
            .maximumYield(150)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(5)
            .biomes(5, TFGTags.Biomes.EarthIsShoreIsland));

    // Normal Biomes — climate + biome (defined in Climates)
    public static BedrockFluidDefinition LIGHT_OIL_NORMAL = create(TFGCore.id("light_oil_normal"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilLight::getFluid)
            .weight(0)
            .minimumYield(75)
            .maximumYield(150)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(5));

    // Common - depletedYield (0)
    public static BedrockFluidDefinition LIGHT_OIL_COMMON = create(TFGCore.id("light_oil_common"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilLight::getFluid)
            .weight(10)
            .minimumYield(10)
            .maximumYield(20)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(0));

    // =========================================================
    // OIL
    // =========================================================

    // Spout - Hot/Dry Climate — climate only
    public static BedrockFluidDefinition OIL_HOT = create(TFGCore.id("oil_spout_hot"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.Oil::getFluid)
            .weight(0)
            .minimumYield(175)
            .maximumYield(300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20));

    // Spill - Ocean Biomes — climate + biome (defined in Climates)
    public static BedrockFluidDefinition OIL_OCEAN = create(TFGCore.id("oil_spill_ocean"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.Oil::getFluid)
            .weight(0)
            .minimumYield(175)
            .maximumYield(250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(15));

    // Normal Biomes — climate + biome (defined in Climates)
    public static BedrockFluidDefinition OIL_NORMAL = create(TFGCore.id("oil_normal"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.Oil::getFluid)
            .weight(0)
            .minimumYield(75)
            .maximumYield(150)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(5));

    // Common - depletedYield (0)
    public static BedrockFluidDefinition OIL_COMMON = create(TFGCore.id("oil_common"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.Oil::getFluid)
            .weight(5)
            .minimumYield(5)
            .maximumYield(15)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(0));

    // =========================================================
    // RAW OIL
    // =========================================================

    // Spout - Hot/Dry Climate — climate only
    public static BedrockFluidDefinition RAW_OIL_HOT = create(TFGCore.id("raw_oil_spout_hot"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.RawOil::getFluid)
            .weight(0)
            .minimumYield(200)
            .maximumYield(350)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20));

    // Spill - Ocean Biomes — climate + biome (defined in Climates)
    public static BedrockFluidDefinition RAW_OIL_OCEAN = create(TFGCore.id("raw_oil_spill_ocean"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.RawOil::getFluid)
            .weight(0)
            .minimumYield(150)
            .maximumYield(250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(15));

    // Spout - Hot Sandstone — climate + biome (defined in Climates)
    public static BedrockFluidDefinition RAW_OIL_SANDSTONE = create(TFGCore.id("raw_oil_spout_sandstone"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.RawOil::getFluid)
            .weight(0)
            .minimumYield(225)
            .maximumYield(400)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(30));

    // Common - depletedYield (0)
    public static BedrockFluidDefinition RAW_OIL_COMMON = create(TFGCore.id("raw_oil_common"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.RawOil::getFluid)
            .weight(5)
            .minimumYield(5)
            .maximumYield(15)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(0));

    // =========================================================
    // HEAVY OIL
    // =========================================================

    // Spout - Hot/Dry Climate — climate only
    public static BedrockFluidDefinition HEAVY_OIL_HOT = create(TFGCore.id("heavy_oil_spout_hot"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilHeavy::getFluid)
            .weight(0)
            .minimumYield(150)
            .maximumYield(250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20));

    // Spout - Hot Dry Biomes — climate + biome (defined in Climates)
    public static BedrockFluidDefinition HEAVY_OIL_DRY = create(TFGCore.id("heavy_oil_spout_dry"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilHeavy::getFluid)
            .weight(0)
            .minimumYield(125)
            .maximumYield(275)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(25));

    // Common - depletedYield (0)
    public static BedrockFluidDefinition HEAVY_OIL_COMMON = create(TFGCore.id("heavy_oil_common"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilHeavy::getFluid)
            .weight(1)
            .minimumYield(5)
            .maximumYield(10)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(0));

    // =========================================================
    // NATURAL GAS
    // =========================================================

    // Surface Indicator - Wet/Cold Climate — climate only
    public static BedrockFluidDefinition NATURAL_GAS_SURFACE = create(TFGCore.id("natural_gas_surface_indicator"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(0)
            .minimumYield(150)
            .maximumYield(300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(40));

    // Ocean Biomes — climate + biome (defined in Climates)
    public static BedrockFluidDefinition NATURAL_GAS_OCEAN = create(TFGCore.id("natural_gas_ocean"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(0)
            .minimumYield(100)
            .maximumYield(250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(10));

    // Surface Indicator - Permafrost — rainfall only (defined in Climates)
    public static BedrockFluidDefinition NATURAL_GAS_PERMAFROST = create(TFGCore.id("natural_gas_permafrost"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(0)
            .minimumYield(150)
            .maximumYield(300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(40));

    // Surface Indicator - Swamp — climate + biome (defined in Climates)
    public static BedrockFluidDefinition NATURAL_GAS_SWAMP = create(TFGCore.id("natural_gas_swamp"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(0)
            .minimumYield(50)
            .maximumYield(150)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(5));

    // Surface Indicator - Volcanic — climate + biome (defined in Climates)
    public static BedrockFluidDefinition NATURAL_GAS_VOLCANIC = create(TFGCore.id("natural_gas_volcanic"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(0)
            .minimumYield(50)
            .maximumYield(150)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(0));

    // Common - depletedYield (0)
    public static BedrockFluidDefinition NATURAL_GAS_COMMON = create(TFGCore.id("natural_gas_common"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(1)
            .minimumYield(5)
            .maximumYield(15)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(0));

    // =========================================================
    // LAVA
    // =========================================================

    // Volcanic biomes
    public static BedrockFluidDefinition LAVA_VOLCANIC = create(TFGCore.id("lava_volcanic"), vein -> vein
            .dimensions(overworld)
            .fluid(Fluids.LAVA::getSource)
            .weight(0)
            .minimumYield(50)
            .maximumYield(100)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(0)
            .biomes(20, TFGTags.Biomes.EarthIsVolcanic));

    // Mountain biomes
    public static BedrockFluidDefinition LAVA_MOUNTAIN = create(TFGCore.id("lava_mountain"), vein -> vein
            .dimensions(overworld)
            .fluid(Fluids.LAVA::getSource)
            .weight(0)
            .minimumYield(5)
            .maximumYield(35)
            .depletionAmount(100)
            .depletionChance(100)
            .depletedYield(0)
            .biomes(10, TFGTags.Biomes.EarthIsMountain));
}