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

    // =========================================================
    // WATER
    // =========================================================

    private static final Set<ResourceKey<Level>> overworld = Set.of(Level.OVERWORLD);
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
            .biomes(15, TFGTags.Biomes.EarthIsNormal)
            .biomes(15, TFGTags.Biomes.EarthIsMountain)
            .biomes(15, TFGTags.Biomes.EarthIsCold)
            .biomes(15, TFGTags.Biomes.EarthIsKarst)
            .biomes(15, TFGTags.Biomes.EarthIsShoreIsland)
            .biomes(15, TFGTags.Biomes.EarthIsFreshWater));

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

    // Ocean + Salt Water biomes and manually add salt_flats as it"s not part of the biome tag
    public static BedrockFluidDefinition SEA_WATER = create(TFGCore.id("sea_water"), vein -> vein
            .dimensions(overworld)
            .fluid(TFCFluids.SALT_WATER::getSource)
            .weight(0)
            .minimumYield(50)
            .maximumYield(500)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(25)
            .biomes(100, TFGTags.Biomes.EarthIsTrueOcean)
            .biomes(100, TFGTags.Biomes.EarthIsSaltWater)
            .biomes(100, TFGTags.Biomes.EarthIsSaltFlats));

    // =========================================================
    // MUDDY WATER - Mostly useless for now but could be used to make stone dusts from any stone
    // =========================================================

    // Common — everywhere except true ocean, shore/island, dry biomes
    // Can't exclude so make it as : is_normal + is_mountain + is_cold + is_karst + is_fresh_water + is_volcanic + is_river + is_swamp

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
            .depletedYield(25));

    // Spout - Ocean Biomes
    public static BedrockFluidDefinition LIGHT_OIL_OCEAN = create(TFGCore.id("light_oil_spout_ocean"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilLight::getFluid)
            .weight(0)
            .minimumYield(150)
            .maximumYield(250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20));

    // Common — no conditions
    public static BedrockFluidDefinition LIGHT_OIL_COMMON = create(TFGCore.id("light_oil_common"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilLight::getFluid)
            .weight(15)
            .minimumYield(10)
            .maximumYield(30)
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

    // Spout - Ocean Biomes
    public static BedrockFluidDefinition OIL_OCEAN = create(TFGCore.id("oil_spout_ocean"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.Oil::getFluid)
            .weight(0)
            .minimumYield(150)
            .maximumYield(250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(15));

    // Common — no conditions
    public static BedrockFluidDefinition OIL_COMMON = create(TFGCore.id("oil_common"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.Oil::getFluid)
            .weight(10)
            .minimumYield(5)
            .maximumYield(15)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(0));

    // =========================================================
    // HEAVY OIL
    // =========================================================

    // Spout - Hot/Dry Climate
    public static BedrockFluidDefinition HEAVY_OIL_HOT = create(TFGCore.id("heavy_oil_spout_hot"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilHeavy::getFluid)
            .weight(0)
            .minimumYield(155)
            .maximumYield(250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20));

    // Spout - Ocean Biomes
    public static BedrockFluidDefinition HEAVY_OIL_OCEAN = create(TFGCore.id("heavy_oil_spout_ocean"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilHeavy::getFluid)
            .weight(0)
            .minimumYield(125)
            .maximumYield(200)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(15));

    // Common — no conditions
    public static BedrockFluidDefinition HEAVY_OIL_COMMON = create(TFGCore.id("heavy_oil_common"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.OilHeavy::getFluid)
            .weight(5)
            .minimumYield(5)
            .maximumYield(10)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(0));

    // =========================================================
    // RAW OIL
    // =========================================================

    // Spout - Hot/Dry Climate
    public static BedrockFluidDefinition RAW_OIL_HOT = create(TFGCore.id("raw_oil_spout_hot"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.RawOil::getFluid)
            .weight(0)
            .minimumYield(200)
            .maximumYield(350)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20));

    // Spout - Ocean Biomes
    public static BedrockFluidDefinition RAW_OIL_OCEAN = create(TFGCore.id("raw_oil_spout_ocean"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.RawOil::getFluid)
            .weight(0)
            .minimumYield(150)
            .maximumYield(250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(15));

    // Common — no conditions
    public static BedrockFluidDefinition RAW_OIL_COMMON = create(TFGCore.id("raw_oil_common"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.RawOil::getFluid)
            .weight(10)
            .minimumYield(5)
            .maximumYield(15)
            .depletionAmount(10)
            .depletionChance(100)
            .depletedYield(0));

    // =========================================================
    // NATURAL GAS
    // =========================================================

    // Surface Indicator - Wet/Cold Climate
    public static BedrockFluidDefinition NATURAL_GAS_SURFACE = create(TFGCore.id("natural_gas_surface_indicator"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(0)
            .minimumYield(150)
            .maximumYield(300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(40));

    // Ocean Biomes
    public static BedrockFluidDefinition NATURAL_GAS_OCEAN = create(TFGCore.id("natural_gas_ocean"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(0)
            .minimumYield(100)
            .maximumYield(250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(10));

    // Common — no conditions
    public static BedrockFluidDefinition NATURAL_GAS_COMMON = create(TFGCore.id("natural_gas_common"), vein -> vein
            .dimensions(overworld)
            .fluid(GTMaterials.NaturalGas::getFluid)
            .weight(10)
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
