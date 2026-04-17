/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.biome;

import static net.dries007.tfc.world.biome.BiomeBuilder.builder;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.biome.*;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGNoiseHelpers;
import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.surface_builders.*;

public class TFGBiomes {
    private static final Map<ResourceKey<Biome>, BiomeExtension> TFG_EXTENSIONS = new IdentityHashMap<>();

    // Aquatic biomes
    // BiomeNoise.ocean and BiomeNoise.oceanRidge are identical between 1.20 and 1.21

    // Ocean biome found near continents.
    public static final BiomeExtension OCEAN = register("ocean",
            builder().heightmap(seed -> BiomeNoise.ocean(seed, -26, -12))
                    .surface(ShoreAndOceanSurfaceBuilder.OCEAN)
                    .aquiferHeightOffset(-24)
                    .type(BiomeBlendType.OCEAN)
                    .salty().noRivers());
    // Ocean biome with reefs depending on climate. Could be interpreted as either barrier, fringe, or platform reefs.
    public static final BiomeExtension OCEAN_REEF = register("ocean_reef",
            builder().heightmap(seed -> BiomeNoise.ocean(seed, -16, -8))
                    .surface(ShoreAndOceanSurfaceBuilder.OCEAN)
                    .aquiferHeightOffset(-24)
                    .type(BiomeBlendType.OCEAN)
                    .salty().noRivers());
    // Deep ocean biome covering most all oceans.
    public static final BiomeExtension DEEP_OCEAN = register("deep_ocean",
            builder().heightmap(seed -> BiomeNoise.ocean(seed, -30, -16))
                    .surface(ShoreAndOceanSurfaceBuilder.OCEAN)
                    .aquiferHeightOffset(-24)
                    .type(BiomeBlendType.OCEAN)
                    .salty().noRivers());
    // Deeper ocean with sharp relief carving to create very deep trenches
    public static final BiomeExtension DEEP_OCEAN_TRENCH = register("deep_ocean_trench",
            builder().heightmap(seed -> BiomeNoise.oceanRidge(seed, -30, -16))
                    .surface(ShoreAndOceanSurfaceBuilder.OCEAN)
                    .aquiferHeightOffset(-24)
                    .type(BiomeBlendType.OCEAN)
                    .salty().noRivers());

    // Low biomes
    // BiomeNoise.hills, BiomeNoise.lowlands, and BiomeNoise.canyons are the same

    // Very flat, slightly above sea level.
    public static final BiomeExtension PLAINS = register("plains",
            riverType(TFGRiverBlendType.FLOODPLAIN,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, 4, 10))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    // Small hills, slightly above sea level.
    public static final BiomeExtension HILLS = register("hills",
            riverType(TFGRiverBlendType.FLOODPLAIN,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, -5, 16))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    // Flat, swamp-like, lots of shallow pools below sea level.
    public static final BiomeExtension LOWLANDS = register("lowlands",
            riverType(TFGRiverBlendType.BANKED,
                    builder().heightmap(BiomeNoise::lowlands)
                            .surface(TFGLowlandsSurfaceBuilder.INSTANCE)
                            .aquiferHeightOffset(-16)
                            .spawnable().noSandyRiverShores()));
    // Flat, swamp-like, lots of shallow pools below sea level.
    public static final BiomeExtension SALT_MARSH = register("salt_marsh",
            riverType(TFGRiverBlendType.BANKED,
                    builder().heightmap(BiomeNoise::lowlands)
                            .surface(TFGLowlandsSurfaceBuilder.INSTANCE)
                            .aquiferHeightOffset(-16)
                            .spawnable().salty().noSandyRiverShores()));
    // Sharp, small hills, with lots of water / snaking winding rivers.
    public static final BiomeExtension LOW_CANYONS = register("low_canyons",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> BiomeNoise.canyons(seed, -8, 21))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .aquiferHeightOffset(-16)
                            .spawnable().noSandyRiverShores()));

    // Mid biomes

    // Higher hills, above sea level. Some larger / steeper hills.
    public static final BiomeExtension ROLLING_HILLS = register("rolling_hills",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, -5, 28))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    // Hills with sharp, exposed rocky areas.
    public static final BiomeExtension HIGHLANDS = register("highlands",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.sharpHills(seed, -3, 28))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .spawnable()));
    // Very high flat area with steep relief carving, similar to vanilla mesas.
    public static final BiomeExtension BADLANDS = register("badlands",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.badlands(seed, 22, 19.5f))
                            .surface(TFGBadlandsSurfaceBuilder.NORMAL)
                            .spawnable()));
    // Very high area, very flat top.
    public static final BiomeExtension PLATEAU = register("plateau",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));
    // Very high area, very flat top.
    public static final BiomeExtension PLATEAU_WIDE = register("plateau_wide",
            riverType(TFGRiverBlendType.TALUS,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));
    // Medium height with snake like ridges, minor volcanic activity
    public static final BiomeExtension CANYONS = register("canyons",
            cinderConesType(TFGRiverBlendType.CANYON, 6, 14, 30, 28, false,
                    builder().heightmap(seed -> BiomeNoise.canyons(seed, -2, 40))
                            .surface(SimpleSurfaceBuilder.VOLCANIC_SOIL)
                            .spawnable().noSandyRiverShores()));

    // High biomes

    // High, picturesque mountains. Pointed peaks, low valleys well above sea level.
    public static final BiomeExtension MOUNTAINS = register("mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 70))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .spawnable()));
    // Rounded top mountains, very large hills.
    public static final BiomeExtension OLD_MOUNTAINS = register("old_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, 16, 40))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .spawnable()));
    // Mountains with high areas, and low, below sea level valleys. Water is salt water here.
    public static final BiomeExtension OCEANIC_MOUNTAINS = register("oceanic_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
                            .surface(ShoreAndOceanSurfaceBuilder.MOUNTAINS)
                            .aquiferHeightOffset(-8)
                            .salty().spawnable()));
    // Volcanic mountains - slightly smaller, but with plentiful tall volcanoes
    public static final BiomeExtension VOLCANIC_MOUNTAINS = register("volcanic_mountains",
            cinderConesType(TFGRiverBlendType.CAVE, 4, 25, 50, 40, false,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 60))
                            .surface(SimpleSurfaceBuilder.ROCKY_VOLCANIC_SOIL)));
    // Volcanic oceanic islands. Slightly smaller and lower but with very plentiful volcanoes
    public static final BiomeExtension VOLCANIC_OCEANIC_MOUNTAINS = register("volcanic_oceanic_mountains",
            cinderConesType(TFGRiverBlendType.CAVE, 2, -12, 50, 20, false,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, -24, 50))
                            .surface(ShoreAndOceanSurfaceBuilder.VOLCANIC_MOUNTAINS)
                            .aquiferHeightOffset(-8)
						.salty()));

    // Island Only
    // Mimic oceanic mountains
    public static final BiomeExtension GUANO_ISLAND = register("guano_island",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(TFGBiomeNoise::rockyIslands)
                            .surface(ShoreAndOceanSurfaceBuilder.ROCKY_SHORE)
                            .spawnable().noSandyRiverShores().salty()));

    // Shores
    // Each shore type is paired with a secondary shore type, which is sometimes applied
    // Standard shore / beach. Material will vary based on location
    public static final BiomeExtension SHORE = register("shore",
            shoreType(TFGRiverBlendType.WIDE, ShoreBlendType.SANDY, -4,
                    builder().heightmap(BiomeNoise::shore)
                            .surface(ShoreAndOceanSurfaceBuilder.SANDY)
                            .aquiferHeightOffset(-16)
                            .type(BiomeBlendType.LAND).salty().shore()
                            .noRivers().noSandyRiverShores()));
    public static final BiomeExtension TIDAL_FLATS = register("tidal_flats",
            shoreType(TFGRiverBlendType.WIDE, ShoreBlendType.SANDY, -4,
                    builder().heightmap(BiomeNoise::shore)
                            .surface(ShoreAndOceanSurfaceBuilder.SANDY)
                            .aquiferHeightOffset(-16)
                            .type(BiomeBlendType.OCEAN)
                            .salty().shore()
                            .noRivers().noSandyRiverShores()));
    // Inspired by Bay of Fundy, 12 Apostles, etc. -- High biome shore
    public static final BiomeExtension SEA_STACKS = register("sea_stacks",
            shoreType(TFGRiverBlendType.TALL_CANYON, ShoreBlendType.SEA_STACKS, -6,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, 10, 30))
                            .surface(ShoreAndOceanSurfaceBuilder.SEA_CLIFFS)
                            .aquiferHeightOffset(-40)
                            .type(BiomeBlendType.LAND)
                            .salty().shore()
                            .noRivers().noSandyRiverShores()));
    // Multiple tiers of cliffs -- High to montane biome shore
    public static final BiomeExtension TERRACE_UPPER = register("terrace_upper",
            shoreType(TFGRiverBlendType.TALL_CANYON, ShoreBlendType.UPPER_TERRACE, 0,
                    builder().heightmap(seed -> TFGBiomeNoise.constant(0))
                            .surface(ShoreAndOceanSurfaceBuilder.SEA_CLIFFS)
                            .aquiferHeightOffset(-40)
                            .type(BiomeBlendType.LAND)
                            .salty().shore()
                            .noRivers().noSandyRiverShores()));
    public static final BiomeExtension TERRACE_LOWER = register("terrace_lower",
            shoreType(TFGRiverBlendType.TALL_CANYON, ShoreBlendType.LOWER_TERRACE, 0,
                    builder().heightmap(seed -> TFGBiomeNoise.constant(0))
                            .surface(ShoreAndOceanSurfaceBuilder.SEA_CLIFFS)
                            .aquiferHeightOffset(-40)
                            .type(BiomeBlendType.LAND)
                            .salty().shore()
                            .noRivers().noSandyRiverShores()));
    // Vegetated zone below shore cliffs -- Mid-high biome shore
    public static final BiomeExtension SETBACK_CLIFFS = register("setback_cliffs",
            shoreType(TFGRiverBlendType.CANYON, ShoreBlendType.SETBACK_CLIFFS, 0,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
                            .surface(ShoreAndOceanSurfaceBuilder.SANDY)
                            .aquiferHeightOffset(-40)
                            .type(BiomeBlendType.LAND)
                            .salty().shore()
                            .noRivers().noSandyRiverShores()));
    // Vegetated coastal Dunes -- Below setback cliffs
    public static final BiomeExtension COASTAL_DUNES = register("coastal_dunes",
            shoreType(TFGRiverBlendType.WIDE_DEEP, ShoreBlendType.DUNES, 0,
                    builder().heightmap(seed -> TFGBiomeNoise.constant(0))
                            .surface(ShoreAndOceanSurfaceBuilder.SANDY)
                            .aquiferHeightOffset(-40)
                            .type(BiomeBlendType.LAND)
                            .salty().shore()
                            .noRivers().noSandyRiverShores()));
    // Chaotic rock formations, tide pools, and blowholes
    public static final BiomeExtension ROCKY_SHORES = register("rocky_shores",
            shoreType(TFGRiverBlendType.CANYON, ShoreBlendType.ROCKY_SHORES, 0,
                    builder().heightmap(seed -> TFGBiomeNoise.constant(-15))
                            .surface(ShoreAndOceanSurfaceBuilder.ROCKY_SHORE)
                            .aquiferHeightOffset(-40)
                            .type(BiomeBlendType.LAND)
                            .salty().shore()
                            .noRivers().noSandyRiverShores()));
    // Similar to Rocky Shores, but with beaches mixed in
    public static final BiomeExtension EMBAYMENTS = register("embayments",
            shoreType(TFGRiverBlendType.CANYON, ShoreBlendType.EMBAYMENTS, 0,
                    builder().heightmap(BiomeNoise::shore)
                            .surface(ShoreAndOceanSurfaceBuilder.SEA_CLIFFS)
                            .aquiferHeightOffset(-40)
                            .type(BiomeBlendType.LAND)
                            .salty().shore()
                            .noRivers().noSandyRiverShores()));

    // Water
    public static final BiomeExtension LAKE = register("lake",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(BiomeNoise::lake)
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .aquiferHeightOffset(-16)
                            .type(BiomeBlendType.LAKE)
                            .noRivers()));
    public static final BiomeExtension RIVER = register("river",
            builder().surface(TFGRiverSurfaceBuilder.INSTANCE));

    // Lakes
    // BiomeNoise.mountains and BiomeNoise.undergroundLakes are unchanged
    public static final BiomeExtension MOUNTAIN_LAKE = register("mountain_lake",
            builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 70))
                    .surface(TFGNormalSurfaceBuilder.ROCKY)
                    .carving(BiomeNoise::undergroundLakes)
                    .type(BiomeBlendType.LAKE)
                    .noRivers());
    public static final BiomeExtension OLD_MOUNTAIN_LAKE = register("old_mountain_lake",
            builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
                    .surface(TFGNormalSurfaceBuilder.ROCKY)
                    .carving(BiomeNoise::undergroundLakes)
                    .type(BiomeBlendType.LAKE)
                    .noRivers());
    public static final BiomeExtension OCEANIC_MOUNTAIN_LAKE = register("oceanic_mountain_lake",
            builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
                    .surface(ShoreAndOceanSurfaceBuilder.MOUNTAINS)
                    .carving(BiomeNoise::undergroundLakes)
                    .salty().type(BiomeBlendType.LAKE)
                    .noRivers());
    public static final BiomeExtension VOLCANIC_MOUNTAIN_LAKE = register("volcanic_mountain_lake",
            cinderConesType(TFGRiverBlendType.NONE, 4, 25, 50, 40, false,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 60))
                            .surface(SimpleSurfaceBuilder.ROCKY_VOLCANIC_SOIL)
                            .carving(BiomeNoise::undergroundLakes)
                            .type(BiomeBlendType.LAKE)
                            .noRivers()));
    public static final BiomeExtension VOLCANIC_OCEANIC_MOUNTAIN_LAKE = register("volcanic_oceanic_mountain_lake",
            cinderConesType(TFGRiverBlendType.NONE, 2, -12, 50, 20, false,
                    builder().heightmap(seed -> BiomeNoise.mountains(seed, -24, 50))
                            .surface(ShoreAndOceanSurfaceBuilder.VOLCANIC_MOUNTAINS)
                            .carving(BiomeNoise::undergroundLakes)
                            .salty()
                            .type(BiomeBlendType.LAKE)
                            .noRivers()));
    public static final BiomeExtension PLATEAU_LAKE = register("plateau_lake",
            builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
                    .surface(TFGNormalSurfaceBuilder.INSTANCE)
                    .carving(BiomeNoise::undergroundLakes)
                    .type(BiomeBlendType.LAKE)
                    .noRivers());

    // Dry Biomes
    public static final BiomeExtension MUD_FLATS = register("mud_flats",
            riverType(TFGRiverBlendType.TALL_BANKED,
                    builder().heightmap(TFGBiomeNoise::flats)
                            .surface(seed -> new FlatsSurfaceBuilder(true))
                            .aquiferHeightOffset(-16)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension SALT_FLATS = register("salt_flats",
            riverType(TFGRiverBlendType.TALL_BANKED,
                    builder().heightmap(TFGBiomeNoise::saltFlats)
                            .surface(seed -> new FlatsSurfaceBuilder(false))
                            .aquiferHeightOffset(-16)
                            .salty().spawnable().noSandyRiverShores()));
    public static final BiomeExtension DUNE_SEA = register("dune_sea",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.dunes(seed, 2, 16))
                            .surface(DuneSurfaceBuilder::new)
                            .aquiferHeightOffset(-16)
                            .spawnable()));
    public static final BiomeExtension GRASSY_DUNES = register("grassy_dunes",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.dunes(seed, 2, 16))
                            .surface(GrassyDunesSurfaceBuilder::new)
                            .aquiferHeightOffset(-16)
                            .spawnable()));
    // Zhangye danxia
    public static final BiomeExtension WHORLED_CANYONS = register("whorled_canyons",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> BiomeNoise.canyons(seed, 8, 60))
                            .surface(TFGBadlandsSurfaceBuilder.WARPED)
                            .aquiferHeightOffset(-16)
                            .spawnable()));
    public static final BiomeExtension STAIR_STEP_CANYONS = register("stair_step_canyons",
            riverType(TFGRiverBlendType.TERRACES,
                    builder().heightmap(TFGBiomeNoise::stairCanyons)
                            .surface(TFGBadlandsSurfaceBuilder.MESAS)
                            .aquiferHeightOffset(-16)
                            .spawnable()));
    public static final BiomeExtension MESAS = register("mesas",
            riverType(TFGRiverBlendType.TERRACES,
                    builder().heightmap(TFGBiomeNoise::mesas)
                            .surface(TFGBadlandsSurfaceBuilder.MESAS)
                            .aquiferHeightOffset(-16)
                            .spawnable()));
    public static final BiomeExtension BUTTES = register("buttes",
            riverType(TFGRiverBlendType.TERRACES,
                    builder().heightmap(TFGBiomeNoise::buttes)
                            .surface(TFGBadlandsSurfaceBuilder.MESAS)
                            .aquiferHeightOffset(-16)
                            .spawnable()));
    public static final BiomeExtension HOODOOS = register("hoodoos",
            riverType(TFGRiverBlendType.TERRACES,
                    builder().heightmap(TFGBiomeNoise::hoodoos)
                            .surface(TFGBadlandsSurfaceBuilder.HOODOOS)
                            .aquiferHeightOffset(-16)
                            .spawnable()));
    public static final BiomeExtension ROCKY_PLATEAU = register("rocky_plateau",
            riverType(TFGRiverBlendType.TALUS,
                    builder().heightmap(seed -> TFGNoiseHelpers.max(
                            TFGBiomeNoise.bowlDolines(seed, BiomeNoise.hills(seed, 22, 32), 16),
                            BiomeNoise.canyons(seed, 0, 52).spread(1.5)))
                            .surface(RockyPlateauSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));

    // Karst Biomes

    // Tower Karsts (Fenglin / Fengcong)
    // Plains, fenglin karsts
    public static final BiomeExtension TOWER_KARST_PLAINS = register("tower_karst_plains",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.fenglin(seed, BiomeNoise.hills(seed, 4, 8), 40))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .spawnable()));
    // Canyons, fengcong karsts
    public static final BiomeExtension TOWER_KARST_CANYONS = register("tower_karst_canyons",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.fengcong(seed, BiomeNoise.canyons(seed, -2, 30)))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .spawnable().noSandyRiverShores()));
    // Rolling hills, fengcong karsts.
    public static final BiomeExtension TOWER_KARST_HILLS = register("tower_karst_hills",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.fengcong(seed, BiomeNoise.hills(seed, -5, 22)))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .spawnable()));
    // Modified "weathered" highlands, fengcong karsts
    public static final BiomeExtension TOWER_KARST_HIGHLANDS = register("tower_karst_highlands",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.fengcong(seed, TFGBiomeNoise.sharpHills(seed, 0, 20)))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .spawnable()));
    // Shallow fresh water, fenglin karsts
    public static final BiomeExtension TOWER_KARST_LAKE = register("tower_karst_lake",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.fenglin(seed, BiomeNoise.hills(seed, -12, -4), 50))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .aquiferHeightOffset(-16)
                            .spawnable().noSandyRiverShores()));
    // Salt water, fenglin karsts
    public static final BiomeExtension TOWER_KARST_BAY = register("tower_karst_bay",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.fenglin(seed, BiomeNoise.hills(seed, -18, -8), 50))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .aquiferHeightOffset(-16)
                            .spawnable().salty().noSandyRiverShores()));

    // Karren Karsts
    // Bare, flat karst inspired by Burren, Ireland
    // Plateau
    public static final BiomeExtension BURREN_PLATEAU = register("burren_plateau",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.burren(seed, BiomeNoise.hills(seed, 22, 32), 1.4))
                            .surface(BurrenSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));
    // Badlands shape, custom surface builder
    public static final BiomeExtension BURREN_BADLANDS = register("burren_badlands",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.burren(seed, TFGBiomeNoise.badlands(seed, 22, 19.5f), 1.0))
                            .surface(BurrenSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));
    // Vertically scaled badlands, custom surface builder
    public static final BiomeExtension BURREN_BADLANDS_TALL = register("burren_badlands_tall",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.burren(seed, TFGBiomeNoise.badlands(seed, 35, 33f), 1.0))
                            .surface(BurrenSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));
    // Plains
    public static final BiomeExtension BURREN_PLAINS = register("burren_plains",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.burren(seed, BiomeNoise.hills(seed, 6, 12), 1.5))
                            .surface(BurrenSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));
    // Plains
    public static final BiomeExtension BURREN_ROCHE_MOUTONEE = register("burren_roche_moutonee",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.burren(seed, TFGBiomeNoise.drumlins(seed), 1.5))
                            .surface(BurrenSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));

    // Dense, sharp ridges inspired by "Stone Forests" in China
    public static final BiomeExtension SHILIN_PLAINS = register("shilin_plains",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.shilin(seed, BiomeNoise.hills(seed, 4, 10), 28))
                            .surface(ShilinSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension SHILIN_CANYONS = register("shilin_canyons",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.shilin(seed, BiomeNoise.canyons(seed, -2, 30), 26))
                            .surface(ShilinSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension SHILIN_HILLS = register("shilin_hills",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.shilin(seed, BiomeNoise.hills(seed, -2, 16), 26))
                            .surface(ShilinSurfaceBuilder.INSTANCE)
                            .spawnable()));
    // Modified "weathered" highlands.
    public static final BiomeExtension SHILIN_HIGHLANDS = register("shilin_highlands",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.shilin(seed, TFGBiomeNoise.sharpHills(seed, 0, 32), 32))
                            .surface(ShilinSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension SHILIN_PLATEAU = register("shilin_plateau",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.shilin(seed, BiomeNoise.hills(seed, 12, 22), 32))
                            .surface(ShilinSurfaceBuilder.INSTANCE)
                            .spawnable()));

    // Doline (Sinkhole) Karsts
    // Small, bowl-shaped dolines
    public static final BiomeExtension DOLINE_PLAINS = register("doline_plains",
            riverType(TFGRiverBlendType.FLOODPLAIN,
                    builder().heightmap(seed -> TFGBiomeNoise.bowlDolines(seed, BiomeNoise.hills(seed, 4, 10), 6))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension DOLINE_HILLS = register("doline_hills",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.bowlDolines(seed, BiomeNoise.hills(seed, -5, 16), 10))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension DOLINE_ROLLING_HILLS = register("doline_rolling_hills",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.bowlDolines(seed, BiomeNoise.hills(seed, -5, 28), 18))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    // Modified "weathered" highlands.
    public static final BiomeExtension DOLINE_HIGHLANDS = register("doline_highlands",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.bowlDolines(seed, TFGBiomeNoise.sharpHills(seed, -3, 20), 22))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension DOLINE_PLATEAU = register("doline_plateau",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.bowlDolines(seed, BiomeNoise.hills(seed, 22, 32), 22))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension DOLINE_CANYONS = register("doline_canyons",
            cinderConesType(TFGRiverBlendType.CANYON, 6, 14, 30, 28, false,
                    builder().heightmap(seed -> TFGBiomeNoise.bowlDolines(seed, BiomeNoise.canyons(seed, -2, 34), 15))
                            .surface(SimpleSurfaceBuilder.VOLCANIC_SOIL)
                            .spawnable().noSandyRiverShores()));

    // Small-medium cylindrical dolines
    public static final BiomeExtension CENOTE_PLAINS = register("cenote_plains",
            riverType(TFGRiverBlendType.FLOODPLAIN,
                    builder().heightmap(seed -> TFGBiomeNoise.cenotes(seed, BiomeNoise.hills(seed, 4, 10), 11, 8))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension CENOTE_HILLS = register("cenote_hills",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> TFGBiomeNoise.cenotes(seed, BiomeNoise.hills(seed, -5, 16), 16, 10))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension CENOTE_ROLLING_HILLS = register("cenote_rolling_hills",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.cenotes(seed, BiomeNoise.hills(seed, -5, 28), 22, 14))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension CENOTE_CANYONS = register("cenote_canyons",
            riverType(TFGRiverBlendType.CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.cenotes(seed, BiomeNoise.canyons(seed, 2, 28), 18, 10))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));
    // Modified "weathered" highlands. Cenotes may not reach water level.
    public static final BiomeExtension CENOTE_HIGHLANDS = register("cenote_highlands",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.cenotes(seed, TFGBiomeNoise.sharpHills(seed, 0, 24), 20, 10))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));
    // Very high area, dry cenotes.
    public static final BiomeExtension CENOTE_PLATEAU = register("cenote_plateau",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.cenotes(seed, BiomeNoise.hills(seed, 20, 30), 22, 20))
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable().noSandyRiverShores()));

    // Large dolines with steep sides
    public static final BiomeExtension EXTREME_DOLINE_PLATEAU = register("extreme_doline_plateau",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(seed -> TFGBiomeNoise.tiankeng(seed, BiomeNoise.hills(seed, 24, 34)))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .spawnable()));
    public static final BiomeExtension EXTREME_DOLINE_MOUNTAINS = register("extreme_doline_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> TFGBiomeNoise.tiankeng(seed, BiomeNoise.mountains(seed, 16, 40)))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .spawnable()));

    // Shield Volcanoes
    public static final BiomeExtension ACTIVE_SHIELD_VOLCANO = register("active_shield_volcano",
            cinderConesType(TFGRiverBlendType.CAVE, 4, 15, 25, 28, true,
                    builder().heightmap(seed -> TFGBiomeNoise.activeShieldVolcano(seed, TFGBiomeNoise.activeHotSpots(seed)))
                            .surface(ShieldVolcanoSurfaceBuilder.ACTIVE)
                            .aquiferHeightOffset(-16)
                            .spawnable()));
    public static final BiomeExtension DORMANT_SHIELD_VOLCANO = register("dormant_shield_volcano",
            tuffRingsType(TFGRiverBlendType.CAVE, 2, 0, 36,
                    builder().heightmap(seed -> TFGBiomeNoise.dormantShieldVolcano(seed, TFGBiomeNoise.dormantHotSpots(seed)))
                            .surface(ShieldVolcanoSurfaceBuilder.DORMANT)
                            .aquiferHeightOffset(-16)
                            .spawnable()));
    public static final BiomeExtension EXTINCT_SHIELD_VOLCANO = register("extinct_shield_volcano",
            tuffRingsType(TFGRiverBlendType.CAVE, 2, 0, 26,
                    builder().heightmap(seed -> TFGBiomeNoise.extinctShieldVolcano(seed, TFGBiomeNoise.extinctHotSpots(seed)))
                            .surface(ShieldVolcanoSurfaceBuilder.DORMANT)
                            .aquiferHeightOffset(-16)
                            .spawnable()));
    public static final BiomeExtension ANCIENT_SHIELD_VOLCANO = register("ancient_shield_volcano",
            tuffRingsType(TFGRiverBlendType.CAVE, 3, -16, 30,
                    builder().heightmap(seed -> TFGBiomeNoise.ancientShieldVolcano(seed, 90, 130, TFGBiomeNoise.ancientHotSpots(seed)))
                            .surface(ShieldVolcanoSurfaceBuilder.DORMANT)
                            .aquiferHeightOffset(-16)
                            .spawnable()));
    public static final BiomeExtension SUNKEN_SHIELD_VOLCANO = register("sunken_shield_volcano",
            tuffRingsType(TFGRiverBlendType.CAVE, 2, -8, 24,
                    builder().heightmap(seed -> TFGBiomeNoise.sunkenShieldVolcano(seed, TFGBiomeNoise.ancientHotSpots(seed)))
                            .surface(ShieldVolcanoSurfaceBuilder.DORMANT)
                            .aquiferHeightOffset(-16)
                            .salty()));

    public static final BiomeExtension SHIELD_VOLCANO_SHORE = register("shield_volcano_shore",
            shoreType(TFGRiverBlendType.TALL_CANYON, ShoreBlendType.EMBAYMENTS, 0,
                    builder().heightmap(BiomeNoise::shore)
                            .surface(ShoreAndOceanSurfaceBuilder.ACTIVE_SHIELD_VOLCANO)
                            .salty().shore()));
    public static final BiomeExtension OLD_SHIELD_VOLCANO_SHORE = register("old_shield_volcano_shore",
            tuffShoreType(TFGRiverBlendType.TALL_CANYON, ShoreBlendType.SANDY, 3, -8, 26,
                    builder().heightmap(BiomeNoise::shore)
                            .surface(ShoreAndOceanSurfaceBuilder.OLD_SHIELD_VOLCANO)
                            .salty().shore()));

    // Full Ice Sheet Biomes
    public static final BiomeExtension ICE_SHEET = register("ice_sheet",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> TFGBiomeNoise.iceSheetSurfaceHeight(seed)
                            .add(TFGBiomeNoise.glacialSurfaceTexture(seed)))
                            .surface(IceSheetSurfaceBuilder.NORMAL)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension ICE_SHEET_MOUNTAINS = register("ice_sheet_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> TFGNoiseHelpers.max(TFGNoiseHelpers.max(TFGBiomeNoise.montaneIceSheetSurfaceHeight(seed)
                            .add(TFGBiomeNoise.glacialSurfaceTexture(seed)),
                            TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialCirques(seed), 39)),
                            TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialCirquesIceSurfaceHeight(seed), 39)))
                            .surface(IceSheetSurfaceBuilder.ICE_SHEET_MOUNTAINS)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension ICE_SHEET_OCEANIC_MOUNTAINS = register("ice_sheet_oceanic_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> TFGNoiseHelpers.max(TFGNoiseHelpers.max(TFGBiomeNoise.oceanicIceSheetSurfaceHeight(seed)
                            .add(TFGBiomeNoise.glacialSurfaceTexture(seed)),
                            TFGBiomeNoise.glacialCirquesIceSurfaceHeight(seed)),
                            TFGBiomeNoise.glacialCirques(seed)))
                            .surface(IceSheetSurfaceBuilder.ICE_SHEET_OCEANIC_MOUNTAINS)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension ICE_SHEET_SHIELD_VOLCANO = register("ice_sheet_shield_volcano",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> TFGNoiseHelpers.max(TFGBiomeNoise.glaciatedShieldVolcano(seed, TFGBiomeNoise.hotSpotIntensity(seed)),
                            TFGBiomeNoise.shieldVolcanoIceSheetSurface(seed, TFGBiomeNoise.hotSpotIntensity(seed))
                                    .add(TFGBiomeNoise.glacialSurfaceTexture(seed))))
                            .surface(IceSheetShieldVolcanoSurfaceBuilder.ICE_SHEET)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension ICE_SHEET_TUYAS = register("ice_sheet_tuyas",
            tuyasType(TFGRiverBlendType.CAVE, 3, 0, 35, -6, true,
                    builder().heightmap(seed -> TFGBiomeNoise.iceSheetSurfaceHeight(seed)
                            .add(TFGBiomeNoise.glacialSurfaceTexture(seed)))
                            .surface(IceSheetSurfaceBuilder.NORMAL)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension SUBGLACIAL_LAKE = register("subglacial_lake",
            builder().heightmap(seed -> TFGBiomeNoise.iceSheetSurfaceHeight(seed)
                    .add(TFGBiomeNoise.glacialSurfaceTexture(seed)))
                    .surface(IceSheetSurfaceBuilder.HIDDEN_LAKE)
                    .carving(BiomeNoise::undergroundLakes)
                    .type(BiomeBlendType.LAKE).noRivers());

    // Ice Sheet Edge Biomes
    public static final BiomeExtension ICE_SHEET_EDGE = register("ice_sheet_edge",
            riverType(TFGRiverBlendType.TALL_CANYON,
                    builder().heightmap(TFGBiomeNoise::glacialBase)
                            .surface(IceSheetSurfaceBuilder.EDGE)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension ICE_SHEET_TUYAS_EDGE = register("ice_sheet_tuyas_edge",
            tuyasType(TFGRiverBlendType.TALL_CANYON, 3, 0, 35, -6, true,
                    builder().heightmap(TFGBiomeNoise::glacialBase)
                            .surface(IceSheetSurfaceBuilder.EDGE)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension ICE_SHEET_MOUNTAINS_EDGE = register("ice_sheet_mountains_edge",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> TFGNoiseHelpers.max(TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialCirques(seed), 39),
                            TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialCirquesIceSurfaceHeight(seed), 39)))
                            .surface(IceSheetSurfaceBuilder.ICE_SHEET_MOUNTAINS)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension ICE_SHEET_OCEANIC_MOUNTAINS_EDGE = register("ice_sheet_oceanic_mountains_edge",
            shoreType(TFGRiverBlendType.CAVE, ShoreBlendType.CLASSIC, -16,
                    builder().heightmap(seed -> TFGNoiseHelpers.max(TFGBiomeNoise.glacialCirquesIceSurfaceHeight(seed), TFGBiomeNoise.glacialCirques(seed)))
                            .surface(IceSheetSurfaceBuilder.ICE_SHEET_OCEANIC_MOUNTAINS)
                            .aquiferHeightOffset(-24)
                            .spawnable().noSandyRiverShores().shore().salty()));
    public static final BiomeExtension MELTWATER_LAKE = register("meltwater_lake",
            shoreType(TFGRiverBlendType.WIDE, ShoreBlendType.CLASSIC, -16,
                    builder().heightmap(BiomeNoise::lake)
                            .surface(IceSheetSurfaceBuilder.EDGE_LAKE)
                            .aquiferHeightOffset(-16)
                            .type(BiomeBlendType.LAKE).noRivers().shore()));
    public static final BiomeExtension ICE_SHEET_OCEANIC = register("ice_sheet_oceanic",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> TFGBiomeNoise.oceanicIceSheetSurfaceHeight(seed)
                            .add(TFGBiomeNoise.glacialSurfaceTexture(seed)))
                            .surface(IceSheetSurfaceBuilder.OCEANIC)
                            .spawnable().salty().noSandyRiverShores()));
    public static final BiomeExtension ICE_SHEET_SHORE = register("ice_sheet_shore",
            shoreType(TFGRiverBlendType.TALL_CANYON, ShoreBlendType.CLASSIC, -12,
                    builder().heightmap(seed -> BiomeNoise.ocean(seed, -16, -8))
                            .surface(IceSheetSurfaceBuilder.OCEANIC)
                            .aquiferHeightOffset(-24)
                            .spawnable().noSandyRiverShores().shore().salty()));

    // Glaciated Biomes
    public static final BiomeExtension GLACIATED_MOUNTAINS = register("glaciated_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> TFGNoiseHelpers.max(TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialCirques(seed), 39),
                            TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialCirquesIceSurfaceHeight(seed), 39)))
                            .surface(IceSheetSurfaceBuilder.GLACIATED_MOUNTAINS)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension GLACIATED_OCEANIC_MOUNTAINS = register("glaciated_oceanic_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> TFGNoiseHelpers.max(TFGBiomeNoise.glacialCirques(seed),
                            TFGBiomeNoise.glacialCirquesIceSurfaceHeight(seed)))
                            .surface(IceSheetSurfaceBuilder.GLACIATED_OCEANIC_MOUNTAINS)
                            .aquiferHeightOffset(-24)
                            .spawnable().noSandyRiverShores().salty()));
    public static final BiomeExtension GLACIATED_SHIELD_VOLCANO = register("glaciated_shield_volcano",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> TFGNoiseHelpers.max(TFGBiomeNoise.glaciatedShieldVolcano(seed, TFGBiomeNoise.hotSpotIntensity(seed)),
                            TFGBiomeNoise.shieldVolcanoGlacierSurface(seed, TFGBiomeNoise.hotSpotIntensity(seed))
                                    .add(TFGBiomeNoise.glacialSurfaceTexture(seed))))
                            .surface(IceSheetShieldVolcanoSurfaceBuilder.GLACIATED)
                            .spawnable().noSandyRiverShores()));

    // Peri/Paleoglacial Biomes
    // Montane biomes
    public static final BiomeExtension GLACIALLY_CARVED_MOUNTAINS = register("glacially_carved_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(seed -> TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialCirques(seed), 39))
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .spawnable().noSandyRiverShores()));
    public static final BiomeExtension GLACIALLY_CARVED_OCEANIC_MOUNTAINS = register("glacially_carved_oceanic_mountains",
            riverType(TFGRiverBlendType.CAVE,
                    builder().heightmap(TFGBiomeNoise::glacialCirques)
                            .surface(TFGNormalSurfaceBuilder.ROCKY)
                            .aquiferHeightOffset(-24)
                            .spawnable().noSandyRiverShores().salty()));

    // Mid-elevation biomes
    public static final BiomeExtension DRUMLINS = register("drumlins",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(TFGBiomeNoise::drumlins)
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension TUYAS = register("tuyas",
            tuyasType(TFGRiverBlendType.CANYON, 2, 0, 35, -6, false,
                    builder().heightmap(TFGBiomeNoise::drumlins)
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));

    // Low-elevation biomes
    public static final BiomeExtension KNOB_AND_KETTLE = register("knob_and_kettle",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(TFGBiomeNoise::knobAndKettle)
                            .surface(TFGNormalSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension PATTERNED_GROUND = register("patterned_ground",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, -4, 3)
                            .add(TFGBiomeNoise.patternedGround(seed)))
                            .surface(PatternedGroundSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension INVERTED_PATTERNED_GROUND = register("inverted_patterned_ground",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(TFGBiomeNoise::invertedPatternedGround)
                            .surface(PatternedGroundSurfaceBuilder.INSTANCE)
                            .spawnable()));
    public static final BiomeExtension STONE_CIRCLES = register("stone_circles",
            riverType(TFGRiverBlendType.WIDE,
                    builder().heightmap(seed -> BiomeNoise.hills(seed, -2, 4)
                            .add(TFGBiomeNoise.stoneCircles(seed)))
                            .surface(StoneCirclesSurfaceBuilder.INSTANCE)
                            .spawnable()));

    private static BiomeBuilder riverType(TFGRiverBlendType river, BiomeBuilder builder) {
        var ib = (IBiomeBuilder) builder;
        return ib.tfg$type(river);
    }

    private static BiomeBuilder shoreType(TFGRiverBlendType river, ShoreBlendType shore, int shoreHeight, BiomeBuilder builder) {
        var ib = (IBiomeBuilder) builder;
        ib = (IBiomeBuilder) ib.tfg$type(river);
        ib = (IBiomeBuilder) ib.tfg$type(shore);
        return ib.tfg$setShoreBaseHeight(shoreHeight);
    }

    private static BiomeBuilder tuyasType(TFGRiverBlendType river, int frequency, int baseHeight, int scaleHeight, int volcanoBasaltHeight, boolean icy, BiomeBuilder builder) {
        var ib = (IBiomeBuilder) builder;
        ib = (IBiomeBuilder) ib.tfg$type(river);
        return ib.tfg$tuyas(frequency, baseHeight, scaleHeight, volcanoBasaltHeight, icy);
    }

    private static BiomeBuilder cinderConesType(TFGRiverBlendType river, int frequency, int baseHeight, int scaleHeight, int volcanoBasaltHeight, boolean additive, BiomeBuilder builder) {
        var ib = (IBiomeBuilder) builder;
        ib = (IBiomeBuilder) ib.tfg$type(river);
        return ib.tfg$cinderCones(frequency, baseHeight, scaleHeight, volcanoBasaltHeight, additive);
    }

    private static BiomeBuilder tuffRingsType(TFGRiverBlendType river, int frequency, int baseHeight, int scaleHeight, BiomeBuilder builder) {
        var ib = (IBiomeBuilder) builder;
        ib = (IBiomeBuilder) ib.tfg$type(river);
        return ib.tfg$tuffRings(frequency, baseHeight, scaleHeight);
    }

    private static BiomeBuilder tuffShoreType(TFGRiverBlendType river, ShoreBlendType shore, int frequency, int baseHeight, int scaleHeight, BiomeBuilder builder) {
        var ib = (IBiomeBuilder) builder;
        ib = (IBiomeBuilder) ib.tfg$type(river);
        ib = (IBiomeBuilder) ib.tfg$type(shore);
        return ib.tfg$tuffRings(frequency, baseHeight, scaleHeight);
    }

    private static BiomeExtension register(String name, BiomeBuilder builder) {
        final ResourceLocation id = TFGCore.id("earth/" + name);
        final ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, id);
        final BiomeExtension variants = builder.build(key);

        TFG_EXTENSIONS.put(key, variants);

        return variants;
    }

    public static BiomeExtension getExtensionOrThrow(LevelAccessor level, Biome biome) {
        return Objects.requireNonNull(getExtension(level, biome), () -> "Biome: " + level.registryAccess().registryOrThrow(Registries.BIOME).getId(biome));
    }

    public static boolean hasExtension(CommonLevelAccessor level, Biome biome) {
        return getExtension(level, biome) != null;
    }

    @Nullable
    public static BiomeExtension getExtension(CommonLevelAccessor level, Biome biome) {
        return ((BiomeBridge) (Object) biome).tfc$getExtension(() -> findExtension(level, biome));
    }

    public static Collection<ResourceKey<Biome>> getAllKeys() {
        return TFG_EXTENSIONS.keySet();
    }

    public static Collection<BiomeExtension> getExtensions() {
        return TFG_EXTENSIONS.values();
    }

    public static Collection<ResourceLocation> getExtensionKeys() {
        return TFG_EXTENSIONS.keySet().stream().map(ResourceKey::location).toList();
    }

    @Nullable
    public static BiomeExtension getById(ResourceLocation id) {
        return TFG_EXTENSIONS.get(ResourceKey.create(Registries.BIOME, id));
    }

    @Nullable
    public static BiomeExtension findExtension(CommonLevelAccessor level, Biome biome) {
        final RegistryAccess registryAccess = level.registryAccess();
        final Registry<Biome> registry = registryAccess.registryOrThrow(Registries.BIOME);
        return registry.getResourceKey(biome).map(TFG_EXTENSIONS::get).orElse(null);
    }
}
