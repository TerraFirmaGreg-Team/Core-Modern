/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.biome;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import net.dries007.tfc.world.surface.builder.*;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.river.RiverBlendType;
import su.terrafirmagreg.core.TFGCore;
import net.dries007.tfc.world.shores.ShoreBlendType;

import static net.dries007.tfc.world.biome.BiomeBuilder.*;

public final class TFCBiomes
{
    public static final Map<ResourceKey<Biome>, BiomeExtension> EXTENSIONS = new IdentityHashMap<>();


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
		builder().heightmap(seed -> BiomeNoise.hills(seed, 4, 10))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.FLOODPLAIN));
	// Small hills, slightly above sea level.
	public static final BiomeExtension HILLS = register("hills",

		builder().heightmap(seed -> BiomeNoise.hills(seed, -5, 16))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.FLOODPLAIN));
	// Flat, swamp-like, lots of shallow pools below sea level.
	public static final BiomeExtension LOWLANDS = register("lowlands",
		builder().heightmap(BiomeNoise::lowlands)
			.surface(LowlandsSurfaceBuilder.INSTANCE)
			.aquiferHeightOffset(-16)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.BANKED));
	// Flat, swamp-like, lots of shallow pools below sea level.
	public static final BiomeExtension SALT_MARSH = register("salt_marsh",
		builder().heightmap(BiomeNoise::lowlands)
			.surface(LowlandsSurfaceBuilder.INSTANCE)
			.aquiferHeightOffset(-16)
			.spawnable().salty().noSandyRiverShores()
			.type(RiverBlendType.BANKED));
	// Sharp, small hills, with lots of water / snaking winding rivers.
	public static final BiomeExtension LOW_CANYONS = register("low_canyons",
		builder().heightmap(seed -> BiomeNoise.canyons(seed, -8, 21))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.aquiferHeightOffset(-16)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.WIDE));

	// Mid biomes

	// Higher hills, above sea level. Some larger / steeper hills.
	public static final BiomeExtension ROLLING_HILLS = register("rolling_hills",
		builder().heightmap(seed -> BiomeNoise.hills(seed, -5, 28))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.CANYON));
	// Hills with sharp, exposed rocky areas.
	public static final BiomeExtension HIGHLANDS = register("highlands",
		builder().heightmap(seed -> BiomeNoise.sharpHills(seed, -3, 28))
			.surface(NormalSurfaceBuilder.ROCKY)
			.spawnable()
			.type(RiverBlendType.CANYON));
	// Very high flat area with steep relief carving, similar to vanilla mesas.
	public static final BiomeExtension BADLANDS = register("badlands",
		builder().heightmap(seed -> BiomeNoise.badlands(seed, 22, 19.5f))
			.surface(BadlandsSurfaceBuilder.NORMAL)
			.spawnable()
			.type(RiverBlendType.CANYON));
	// Very high area, very flat top.
	public static final BiomeExtension PLATEAU = register("plateau",
		builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON));
	// Very high area, very flat top.
	public static final BiomeExtension PLATEAU_WIDE = register("plateau_wide",
		builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALUS));
	// Medium height with snake like ridges, minor volcanic activity
	public static final BiomeExtension CANYONS = register("canyons",
		builder().heightmap(seed -> BiomeNoise.canyons(seed, -2, 40))
			.surface(SimpleSurfaceBuilder.VOLCANIC_SOIL)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CANYON)
			.cinderCones(6, 14, 30, 28, false));

	// High biomes

	// High, picturesque mountains. Pointed peaks, low valleys well above sea level.
	public static final BiomeExtension MOUNTAINS = register("mountains",
		builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 70))
			.surface(NormalSurfaceBuilder.ROCKY)
			.spawnable()
			.type(RiverBlendType.CAVE));
	// Rounded top mountains, very large hills.
	public static final BiomeExtension OLD_MOUNTAINS = register("old_mountains",
		builder().heightmap(seed -> BiomeNoise.mountains(seed, 16, 40))
			.surface(NormalSurfaceBuilder.ROCKY)
			.spawnable()
			.type(RiverBlendType.CAVE));
	// Mountains with high areas, and low, below sea level valleys. Water is salt water here.
	public static final BiomeExtension OCEANIC_MOUNTAINS = register("oceanic_mountains",
		builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
			.surface(ShoreAndOceanSurfaceBuilder.MOUNTAINS)
			.aquiferHeightOffset(-8)
			.salty().spawnable()
			.type(RiverBlendType.CAVE));
	// Volcanic mountains - slightly smaller, but with plentiful tall volcanoes
	public static final BiomeExtension VOLCANIC_MOUNTAINS = register("volcanic_mountains",
		builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 60))
			.surface(SimpleSurfaceBuilder.ROCKY_VOLCANIC_SOIL)
			.type(RiverBlendType.CAVE)
			.cinderCones(4, 25, 50, 40, false));
	// Volcanic oceanic islands. Slightly smaller and lower but with very plentiful volcanoes
	public static final BiomeExtension VOLCANIC_OCEANIC_MOUNTAINS = register("volcanic_oceanic_mountains",
		builder().heightmap(seed -> BiomeNoise.mountains(seed, -24, 50))
			.surface(ShoreAndOceanSurfaceBuilder.VOLCANIC_MOUNTAINS)
			.aquiferHeightOffset(-8)
			.salty()
			.type(RiverBlendType.CAVE)
			.cinderCones(2, -12, 50, 20, false));

	// Island Only
	// Mimic oceanic mountains
	public static final BiomeExtension GUANO_ISLAND = register("guano_island",
		builder().heightmap(BiomeNoise::rockyIslands)
			.surface(ShoreAndOceanSurfaceBuilder.ROCKY_SHORE)
			.spawnable().noSandyRiverShores().salty()
			.type(RiverBlendType.CAVE));

	// Shores
	// Each shore type is paired with a secondary shore type, which is sometimes applied
	// Standard shore / beach. Material will vary based on location
	public static final BiomeExtension SHORE = register("shore",
		builder().heightmap(BiomeNoise::shore)
			.surface(ShoreAndOceanSurfaceBuilder.SANDY)
			.aquiferHeightOffset(-16)
			.type(BiomeBlendType.LAND).salty().shore()
			.noRivers().noSandyRiverShores()
			.type(RiverBlendType.WIDE)
			.type(ShoreBlendType.SANDY).setShoreBaseHeight(-4));
	public static final BiomeExtension TIDAL_FLATS = register("tidal_flats",
		builder().heightmap(BiomeNoise::shore)
			.surface(ShoreAndOceanSurfaceBuilder.SANDY)
			.aquiferHeightOffset(-16)
			.type(BiomeBlendType.OCEAN)
			.salty().shore()
			.noRivers().noSandyRiverShores().type(RiverBlendType.WIDE)
			.type(ShoreBlendType.SANDY).setShoreBaseHeight(-4));
	// Inspired by Bay of Fundy, 12 Apostles, etc. -- High biome shore
	public static final BiomeExtension SEA_STACKS = register("sea_stacks",
		builder().heightmap(seed -> BiomeNoise.hills(seed, 10, 30))
			.surface(ShoreAndOceanSurfaceBuilder.SEA_CLIFFS)
			.aquiferHeightOffset(-40)
			.type(BiomeBlendType.LAND)
			.salty().shore()
			.noRivers().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON)
			.type(ShoreBlendType.SEA_STACKS).setShoreBaseHeight(-6));
	// Multiple tiers of cliffs -- High to montane biome shore
	public static final BiomeExtension TERRACE_UPPER = register("terrace_upper",
		builder().heightmap(seed -> BiomeNoise.constant(0))
			.surface(ShoreAndOceanSurfaceBuilder.SEA_CLIFFS)
			.aquiferHeightOffset(-40)
			.type(BiomeBlendType.LAND)
			.salty().shore()
			.noRivers().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON)
			.type(ShoreBlendType.UPPER_TERRACE).setShoreBaseHeight(0));
	public static final BiomeExtension TERRACE_LOWER = register("terrace_lower",
		builder().heightmap(seed -> BiomeNoise.constant(0))
			.surface(ShoreAndOceanSurfaceBuilder.SEA_CLIFFS)
			.aquiferHeightOffset(-40)
			.type(BiomeBlendType.LAND)
			.salty().shore()
			.noRivers().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON)
			.type(ShoreBlendType.LOWER_TERRACE).setShoreBaseHeight(0));
	// Vegetated zone below shore cliffs -- Mid-high biome shore
	public static final BiomeExtension SETBACK_CLIFFS = register("setback_cliffs",
		builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
			.surface(ShoreAndOceanSurfaceBuilder.SANDY)
			.aquiferHeightOffset(-40)
			.type(BiomeBlendType.LAND)
			.salty().shore()
			.noRivers().noSandyRiverShores()
			.type(RiverBlendType.CANYON)
			.type(ShoreBlendType.SETBACK_CLIFFS).setShoreBaseHeight(0));
	// Vegetated coastal Dunes -- Below setback cliffs
	public static final BiomeExtension COASTAL_DUNES = register("coastal_dunes",
		builder().heightmap(seed -> BiomeNoise.constant(0))
			.surface(ShoreAndOceanSurfaceBuilder.SANDY)
			.aquiferHeightOffset(-40)
			.type(BiomeBlendType.LAND)
			.salty().shore()
			.noRivers().noSandyRiverShores()
			.type(RiverBlendType.WIDE_DEEP)
			.type(ShoreBlendType.DUNES).setShoreBaseHeight(0));
	// Chaotic rock formations, tide pools, and blowholes
	public static final BiomeExtension ROCKY_SHORES = register("rocky_shores",
		builder().heightmap(seed -> BiomeNoise.constant(-15))
			.surface(ShoreAndOceanSurfaceBuilder.ROCKY_SHORE)
			.aquiferHeightOffset(-40)
			.type(BiomeBlendType.LAND)
			.salty().shore()
			.noRivers().noSandyRiverShores()
			.type(RiverBlendType.CANYON)
			.type(ShoreBlendType.ROCKY_SHORES).setShoreBaseHeight(0));
	// Similar to Rocky Shores, but with beaches mixed in
	public static final BiomeExtension EMBAYMENTS = register("embayments",
		builder().heightmap(BiomeNoise::shore)
			.surface(ShoreAndOceanSurfaceBuilder.SEA_CLIFFS)
			.aquiferHeightOffset(-40)
			.type(BiomeBlendType.LAND)
			.salty().shore()
			.noRivers().noSandyRiverShores()
			.type(RiverBlendType.CANYON)
			.type(ShoreBlendType.EMBAYMENTS).setShoreBaseHeight(0));

	// Water
	public static final BiomeExtension LAKE = register("lake",
		builder().heightmap(BiomeNoise::lake)
			.surface(NormalSurfaceBuilder.INSTANCE)
			.aquiferHeightOffset(-16)
			.type(BiomeBlendType.LAKE)
			.noRivers()
			.type(RiverBlendType.WIDE));
	public static final BiomeExtension RIVER = register("river",
		builder().surface(RiverSurfaceBuilder.INSTANCE));

	// Lakes
	// BiomeNoise.mountains and BiomeNoise.undergroundLakes are unchanged
	public static final BiomeExtension MOUNTAIN_LAKE = register("mountain_lake",
		builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 70))
			.surface(NormalSurfaceBuilder.ROCKY)
			.carving(BiomeNoise::undergroundLakes)
			.type(BiomeBlendType.LAKE)
			.noRivers());
	public static final BiomeExtension OLD_MOUNTAIN_LAKE = register("old_mountain_lake",
		builder().heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
			.surface(NormalSurfaceBuilder.ROCKY)
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
		builder().heightmap(seed -> BiomeNoise.mountains(seed, 10, 60))
			.surface(SimpleSurfaceBuilder.ROCKY_VOLCANIC_SOIL)
			.carving(BiomeNoise::undergroundLakes)
			.type(BiomeBlendType.LAKE)
			.noRivers()
			.type(RiverBlendType.NONE)
			.cinderCones(4, 25, 50, 40, false));
	public static final BiomeExtension VOLCANIC_OCEANIC_MOUNTAIN_LAKE = register("volcanic_oceanic_mountain_lake",
		builder().heightmap(seed -> BiomeNoise.mountains(seed, -24, 50))
			.surface(ShoreAndOceanSurfaceBuilder.VOLCANIC_MOUNTAINS)
			.carving(BiomeNoise::undergroundLakes)
			.salty()
			.type(BiomeBlendType.LAKE)
			.noRivers()
			.type(RiverBlendType.NONE)
			.cinderCones(2, -12, 50, 20, false));
	public static final BiomeExtension PLATEAU_LAKE = register("plateau_lake",
		builder().heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.carving(BiomeNoise::undergroundLakes)
			.type(BiomeBlendType.LAKE)
			.noRivers());

	// Dry Biomes
	public static final BiomeExtension MUD_FLATS = register("mud_flats",
		builder().heightmap(BiomeNoise::flats)
			.surface(FlatsSurfaceBuilder.MUDDY)
			.aquiferHeightOffset(-16)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_BANKED));
	public static final BiomeExtension SALT_FLATS = register("salt_flats",
		builder().heightmap(BiomeNoise::saltFlats)
			.surface(FlatsSurfaceBuilder.SALTY)
			.aquiferHeightOffset(-16)
			.salty().spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_BANKED));
	public static final BiomeExtension DUNE_SEA = register("dune_sea",
		builder().heightmap(seed -> BiomeNoise.dunes(seed, 2, 16))
			.surface(DuneSurfaceBuilder.INSTANCE)
			.aquiferHeightOffset(-16)
			.spawnable()
			.type(RiverBlendType.WIDE));
	public static final BiomeExtension GRASSY_DUNES = register("grassy_dunes",
		builder().heightmap(seed -> BiomeNoise.dunes(seed, 2, 16))
			.surface(GrassyDunesSurfaceBuilder.INSTANCE)
			.aquiferHeightOffset(-16)
			.spawnable()
			.type(RiverBlendType.WIDE));
	// Zhangye danxia
	public static final BiomeExtension WHORLED_CANYONS = register("whorled_canyons",
		builder().heightmap(seed -> BiomeNoise.canyons(seed, 8, 60))
			.surface(BadlandsSurfaceBuilder.WARPED)
			.aquiferHeightOffset(-16)
			.spawnable()
			.type(RiverBlendType.TALL_CANYON));
	public static final BiomeExtension STAIR_STEP_CANYONS = register("stair_step_canyons",
		builder().heightmap(BiomeNoise::stairCanyons)
			.surface(BadlandsSurfaceBuilder.MESAS)
			.aquiferHeightOffset(-16)
			.spawnable()
			.type(RiverBlendType.TERRACES));
	public static final BiomeExtension MESAS = register("mesas",
		builder().heightmap(BiomeNoise::mesas)
			.surface(BadlandsSurfaceBuilder.MESAS)
			.aquiferHeightOffset(-16)
			.spawnable()
			.type(RiverBlendType.TERRACES));
	public static final BiomeExtension BUTTES = register("buttes",
		builder().heightmap(BiomeNoise::buttes)
			.surface(BadlandsSurfaceBuilder.MESAS)
			.aquiferHeightOffset(-16)
			.spawnable()
			.type(RiverBlendType.TERRACES));
	public static final BiomeExtension HOODOOS = register("hoodoos",
		builder().heightmap(BiomeNoise::hoodoos)
			.surface(BadlandsSurfaceBuilder.HOODOOS)
			.aquiferHeightOffset(-16)
			.spawnable()
			.type(RiverBlendType.TERRACES));
	public static final BiomeExtension ROCKY_PLATEAU = register("rocky_plateau",
		builder().heightmap(seed ->
				BiomeNoise.bowlDolines(seed, BiomeNoise.hills(seed, 22, 32), 16).max(
				BiomeNoise.canyons(seed, 0, 52).spread(1.5)))
			.surface(RockyPlateauSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALUS));

	// Karst Biomes

	// Tower Karsts (Fenglin / Fengcong)
	// Plains, fenglin karsts
	public static final BiomeExtension TOWER_KARST_PLAINS = register("tower_karst_plains",
		builder().heightmap(seed -> BiomeNoise.fenglin(seed, BiomeNoise.hills(seed, 4, 8), 40))
			.surface(NormalSurfaceBuilder.ROCKY)
			.spawnable()
			.type(RiverBlendType.TALL_CANYON));
	// Canyons, fengcong karsts
	public static final BiomeExtension TOWER_KARST_CANYONS = register("tower_karst_canyons",
		builder().heightmap(seed -> BiomeNoise.fengcong(seed, BiomeNoise.canyons(seed, -2, 30)))
			.surface(NormalSurfaceBuilder.ROCKY)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON));
	// Rolling hills, fengcong karsts.
	public static final BiomeExtension TOWER_KARST_HILLS = register("tower_karst_hills",
		builder().heightmap(seed -> BiomeNoise.fengcong(seed, BiomeNoise.hills(seed, -5, 22)))
			.surface(NormalSurfaceBuilder.ROCKY)
			.spawnable()
			.type(RiverBlendType.TALL_CANYON));
	// Modified "weathered" highlands, fengcong karsts
	public static final BiomeExtension TOWER_KARST_HIGHLANDS = register("tower_karst_highlands",
		builder().heightmap(seed -> BiomeNoise.fengcong(seed, BiomeNoise.sharpHills(seed, 0, 20)))
			.surface(NormalSurfaceBuilder.ROCKY)
			.spawnable()
			.type(RiverBlendType.TALL_CANYON));
	// Shallow fresh water, fenglin karsts
	public static final BiomeExtension TOWER_KARST_LAKE = register("tower_karst_lake",
		builder().heightmap(seed -> BiomeNoise.fenglin(seed, BiomeNoise.hills(seed, -12, -4), 50))
			.surface(NormalSurfaceBuilder.ROCKY)
			.aquiferHeightOffset(-16)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON));
	// Salt water, fenglin karsts
	public static final BiomeExtension TOWER_KARST_BAY = register("tower_karst_bay",
		builder().heightmap(seed -> BiomeNoise.fenglin(seed, BiomeNoise.hills(seed, -18, -8), 50))
			.surface(NormalSurfaceBuilder.ROCKY)
			.aquiferHeightOffset(-16)
			.spawnable().salty().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON));

	// Karren Karsts
	// Bare, flat karst inspired by Burren, Ireland
	// Plateau
	public static final BiomeExtension BURREN_PLATEAU = register("burren_plateau",
		builder().heightmap(seed -> BiomeNoise.burren(seed, BiomeNoise.hills(seed, 22, 32), 1.4))
			.surface(BurrenSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON));
	// Badlands shape, custom surface builder
	public static final BiomeExtension BURREN_BADLANDS = register("burren_badlands",
		builder().heightmap(seed -> BiomeNoise.burren(seed, BiomeNoise.badlands(seed, 22, 19.5f), 1.0))
			.surface(BurrenSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CANYON));
	// Vertically scaled badlands, custom surface builder
	public static final BiomeExtension BURREN_BADLANDS_TALL = register("burren_badlands_tall",
		builder().heightmap(seed -> BiomeNoise.burren(seed, BiomeNoise.badlands(seed, 35, 33f), 1.0))
			.surface(BurrenSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON));
	// Plains
	public static final BiomeExtension BURREN_PLAINS = register("burren_plains",
		builder().heightmap(seed -> BiomeNoise.burren(seed, BiomeNoise.hills(seed, 6, 12), 1.5))
			.surface(BurrenSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.WIDE));
	// Plains
	public static final BiomeExtension BURREN_ROCHE_MOUTONEE = register("burren_roche_moutonee",
		builder().heightmap(seed -> BiomeNoise.burren(seed, BiomeNoise.drumlins(seed), 1.5))
			.surface(BurrenSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.WIDE));

	// Dense, sharp ridges inspired by "Stone Forests" in China
	public static final BiomeExtension SHILIN_PLAINS = register("shilin_plains",
		builder().heightmap(seed -> BiomeNoise.shilin(seed, BiomeNoise.hills(seed, 4, 10), 28))
			.surface(ShilinSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));
	public static final BiomeExtension SHILIN_CANYONS = register("shilin_canyons",
		builder().heightmap(seed -> BiomeNoise.shilin(seed, BiomeNoise.canyons(seed, -2, 30), 26))
			.surface(ShilinSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));
	public static final BiomeExtension SHILIN_HILLS = register("shilin_hills",
		builder().heightmap(seed -> BiomeNoise.shilin(seed, BiomeNoise.hills(seed, -2, 16), 26))
			.surface(ShilinSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));
	// Modified "weathered" highlands.
	public static final BiomeExtension SHILIN_HIGHLANDS = register("shilin_highlands",
		builder().heightmap(seed -> BiomeNoise.shilin(seed, BiomeNoise.sharpHills(seed, 0, 32), 32))
			.surface(ShilinSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));
	public static final BiomeExtension SHILIN_PLATEAU = register("shilin_plateau",
		builder().heightmap(seed -> BiomeNoise.shilin(seed, BiomeNoise.hills(seed, 12, 22), 32))
			.surface(ShilinSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));

	// Doline (Sinkhole) Karsts
	// Small, bowl-shaped dolines
	public static final BiomeExtension DOLINE_PLAINS = register("doline_plains",
		builder().heightmap(seed -> BiomeNoise.bowlDolines(seed, BiomeNoise.hills(seed, 4, 10), 6))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.FLOODPLAIN));
	public static final BiomeExtension DOLINE_HILLS = register("doline_hills",
		builder().heightmap(seed -> BiomeNoise.bowlDolines(seed, BiomeNoise.hills(seed, -5, 16), 10))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));
	public static final BiomeExtension DOLINE_ROLLING_HILLS = register("doline_rolling_hills",
		builder().heightmap(seed -> BiomeNoise.bowlDolines(seed, BiomeNoise.hills(seed, -5, 28), 18))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.CANYON));
	// Modified "weathered" highlands.
	public static final BiomeExtension DOLINE_HIGHLANDS = register("doline_highlands",
		builder().heightmap(seed -> BiomeNoise.bowlDolines(seed, BiomeNoise.sharpHills(seed, -3, 20), 22))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.CANYON));
	public static final BiomeExtension DOLINE_PLATEAU = register("doline_plateau",
		builder().heightmap(seed -> BiomeNoise.bowlDolines(seed, BiomeNoise.hills(seed, 22, 32), 22))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON));
	public static final BiomeExtension DOLINE_CANYONS = register("doline_canyons",
		builder().heightmap(seed -> BiomeNoise.bowlDolines(seed, BiomeNoise.canyons(seed, -2, 34), 15))
			.surface(SimpleSurfaceBuilder.VOLCANIC_SOIL)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CANYON)
			.cinderCones(6, 14, 30, 28, false));

	// Small-medium cylindrical dolines
	public static final BiomeExtension CENOTE_PLAINS = register("cenote_plains",
		builder().heightmap(seed -> BiomeNoise.cenotes(seed, BiomeNoise.hills(seed, 4, 10), 11, 8))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.FLOODPLAIN));
	public static final BiomeExtension CENOTE_HILLS = register("cenote_hills",
		builder().heightmap(seed -> BiomeNoise.cenotes(seed, BiomeNoise.hills(seed, -5, 16), 16, 10))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));
	public static final BiomeExtension CENOTE_ROLLING_HILLS = register("cenote_rolling_hills",
		builder().heightmap(seed -> BiomeNoise.cenotes(seed, BiomeNoise.hills(seed, -5, 28), 22, 14))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.CANYON));
	public static final BiomeExtension CENOTE_CANYONS = register("cenote_canyons",
		builder().heightmap(seed -> BiomeNoise.cenotes(seed, BiomeNoise.canyons(seed, 2, 28), 18, 10))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CANYON));
	// Modified "weathered" highlands. Cenotes may not reach water level.
	public static final BiomeExtension CENOTE_HIGHLANDS = register("cenote_highlands",
		builder().heightmap(seed -> BiomeNoise.cenotes(seed, BiomeNoise.sharpHills(seed, 0, 24), 20, 10))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON));
	// Very high area, dry cenotes.
	public static final BiomeExtension CENOTE_PLATEAU = register("cenote_plateau",
		builder().heightmap(seed -> BiomeNoise.cenotes(seed, BiomeNoise.hills(seed, 20, 30), 22, 20))
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON));

	// Large dolines with steep sides
	public static final BiomeExtension EXTREME_DOLINE_PLATEAU = register("extreme_doline_plateau",
		builder().heightmap(seed -> BiomeNoise.tiankeng(seed, BiomeNoise.hills(seed, 24, 34)))
			.surface(NormalSurfaceBuilder.ROCKY)
			.spawnable()
			.type(RiverBlendType.TALL_CANYON));
	public static final BiomeExtension EXTREME_DOLINE_MOUNTAINS = register("extreme_doline_mountains",
		builder().heightmap(seed -> BiomeNoise.tiankeng(seed, BiomeNoise.mountains(seed, 16, 40)))
			.surface(NormalSurfaceBuilder.ROCKY)
			.spawnable()
			.type(RiverBlendType.CAVE));

	// Shield Volcanoes
	public static final BiomeExtension ACTIVE_SHIELD_VOLCANO = register("active_shield_volcano",
		builder().heightmap(seed -> BiomeNoise.activeShieldVolcano(seed, BiomeNoise.activeHotSpots(seed)))
			.surface(ShieldVolcanoSurfaceBuilder.ACTIVE)
			.aquiferHeightOffset(-16)
			.spawnable()
			.type(RiverBlendType.CAVE)
			.cinderCones(4, 15, 25, 28, true));
	public static final BiomeExtension DORMANT_SHIELD_VOLCANO = register("dormant_shield_volcano",
		builder().heightmap(seed -> BiomeNoise.dormantShieldVolcano(seed, BiomeNoise.dormantHotSpots(seed)))
			.surface(ShieldVolcanoSurfaceBuilder.DORMANT)
			.aquiferHeightOffset(-16)
			.spawnable()
			.type(RiverBlendType.CAVE)
			.tuffRings(2, 0, 36));
	public static final BiomeExtension EXTINCT_SHIELD_VOLCANO = register("extinct_shield_volcano",
		builder().heightmap(seed -> BiomeNoise.extinctShieldVolcano(seed, BiomeNoise.extinctHotSpots(seed)))
			.surface(ShieldVolcanoSurfaceBuilder.DORMANT)
			.aquiferHeightOffset(-16)
			.spawnable()
			.type(RiverBlendType.CAVE)
			.tuffRings(2, 0, 26));
	public static final BiomeExtension ANCIENT_SHIELD_VOLCANO = register("ancient_shield_volcano",
		builder().heightmap(seed -> BiomeNoise.ancientShieldVolcano(seed, 90, 130, BiomeNoise.ancientHotSpots(seed)))
			.surface(ShieldVolcanoSurfaceBuilder.DORMANT)
			.aquiferHeightOffset(-16)
			.spawnable()
			.type(RiverBlendType.CAVE)
			.tuffRings(3, -16, 30));
	public static final BiomeExtension SUNKEN_SHIELD_VOLCANO = register("sunken_shield_volcano",
		builder().heightmap(seed -> BiomeNoise.sunkenShieldVolcano(seed, BiomeNoise.ancientHotSpots(seed)))
			.surface(ShieldVolcanoSurfaceBuilder.DORMANT)
			.aquiferHeightOffset(-16)
			.salty()
			.type(RiverBlendType.CAVE)
			.tuffRings(2, -8, 24));

	public static final BiomeExtension SHIELD_VOLCANO_SHORE = register("shield_volcano_shore",
		builder().heightmap(BiomeNoise::shore)
			.surface(ShoreAndOceanSurfaceBuilder.ACTIVE_SHIELD_VOLCANO)
			.salty().shore()
			.type(RiverBlendType.TALL_CANYON)
			.type(ShoreBlendType.EMBAYMENTS).setShoreBaseHeight(0));
	public static final BiomeExtension OLD_SHIELD_VOLCANO_SHORE = register("old_shield_volcano_shore",
		builder().heightmap(BiomeNoise::shore)
			.surface(ShoreAndOceanSurfaceBuilder.OLD_SHIELD_VOLCANO)
			.salty().shore()
			.type(RiverBlendType.TALL_CANYON)
			.type(ShoreBlendType.SANDY)
			.tuffRings(3, -8, 26));

	// Full Ice Sheet Biomes
	public static final BiomeExtension ICE_SHEET = register("ice_sheet",
		builder().heightmap(seed -> BiomeNoise.iceSheetSurfaceHeight(seed)
										.add(BiomeNoise.glacialSurfaceTexture(seed)))
			.surface(IceSheetSurfaceBuilder.NORMAL)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CAVE));
	public static final BiomeExtension ICE_SHEET_MOUNTAINS = register("ice_sheet_mountains",
		builder().heightmap(seed -> BiomeNoise.montaneIceSheetSurfaceHeight(seed)
										.add(BiomeNoise.glacialSurfaceTexture(seed))
										.max(BiomeNoise.glacialCirques(seed).addConstant(39))
										.max(BiomeNoise.glacialCirquesIceSurfaceHeight(seed).addConstant(39)))
			.surface(IceSheetSurfaceBuilder.ICE_SHEET_MOUNTAINS)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CAVE));
	public static final BiomeExtension ICE_SHEET_OCEANIC_MOUNTAINS = register("ice_sheet_oceanic_mountains",
		builder().heightmap(seed -> BiomeNoise.oceanicIceSheetSurfaceHeight(seed).add(BiomeNoise.glacialSurfaceTexture(seed))
										.max(BiomeNoise.glacialCirquesIceSurfaceHeight(seed))
										.max(BiomeNoise.glacialCirques(seed)))
			.surface(IceSheetSurfaceBuilder.ICE_SHEET_OCEANIC_MOUNTAINS)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CAVE));
	public static final BiomeExtension ICE_SHEET_SHIELD_VOLCANO = register("ice_sheet_shield_volcano",
		builder().heightmap(seed -> BiomeNoise.glaciatedShieldVolcano(seed, BiomeNoise.hotSpotIntensity(seed))
										.max(BiomeNoise.shieldVolcanoIceSheetSurface(seed, BiomeNoise.hotSpotIntensity(seed))
										.add(BiomeNoise.glacialSurfaceTexture(seed))))
			.surface(IceSheetShieldVolcanoSurfaceBuilder.ICE_SHEET)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CAVE));
	public static final BiomeExtension ICE_SHEET_TUYAS = register("ice_sheet_tuyas",
		builder().heightmap(seed -> BiomeNoise.iceSheetSurfaceHeight(seed)
										.add(BiomeNoise.glacialSurfaceTexture(seed)))
			.surface(IceSheetSurfaceBuilder.NORMAL)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CAVE)
			.tuyas(3, 0, 35, -6, true));
	public static final BiomeExtension SUBGLACIAL_LAKE = register("subglacial_lake",
		builder().heightmap(seed -> BiomeNoise.iceSheetSurfaceHeight(seed)
										.add(BiomeNoise.glacialSurfaceTexture(seed)))
			.surface(IceSheetSurfaceBuilder.HIDDEN_LAKE)
			.carving(BiomeNoise::undergroundLakes)
			.type(BiomeBlendType.LAKE).noRivers());

	// Ice Sheet Edge Biomes
	public static final BiomeExtension ICE_SHEET_EDGE = register("ice_sheet_edge",
		builder().heightmap(BiomeNoise::glacialBase)
			.surface(IceSheetSurfaceBuilder.EDGE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON));
	public static final BiomeExtension ICE_SHEET_TUYAS_EDGE = register("ice_sheet_tuyas_edge",
		builder().heightmap(BiomeNoise::glacialBase)
			.surface(IceSheetSurfaceBuilder.EDGE)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.TALL_CANYON)
			.tuyas(3, 0, 35, -6, true));
	public static final BiomeExtension ICE_SHEET_MOUNTAINS_EDGE = register("ice_sheet_mountains_edge",
		builder().heightmap(seed -> BiomeNoise.glacialCirques(seed)
										.addConstant(39)
										.max(BiomeNoise.glacialCirquesIceSurfaceHeight(seed).addConstant(39)))
			.surface(IceSheetSurfaceBuilder.ICE_SHEET_MOUNTAINS)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CAVE));
	public static final BiomeExtension ICE_SHEET_OCEANIC_MOUNTAINS_EDGE = register("ice_sheet_oceanic_mountains_edge",
		builder().heightmap(seed -> BiomeNoise.glacialCirquesIceSurfaceHeight(seed).max(BiomeNoise.glacialCirques(seed)))
			.surface(IceSheetSurfaceBuilder.ICE_SHEET_OCEANIC_MOUNTAINS)
			.aquiferHeightOffset(-24)
			.spawnable().noSandyRiverShores().shore().salty()
			.type(RiverBlendType.CAVE)
			.type(ShoreBlendType.CLASSIC)
			.setShoreBaseHeight(-16));
	public static final BiomeExtension MELTWATER_LAKE = register("meltwater_lake",
		builder().heightmap(BiomeNoise::lake)
			.surface(IceSheetSurfaceBuilder.EDGE_LAKE)
			.aquiferHeightOffset(-16)
			.type(BiomeBlendType.LAKE).noRivers().shore()
			.type(RiverBlendType.WIDE)
			.type(ShoreBlendType.CLASSIC).setShoreBaseHeight(-16));
	public static final BiomeExtension ICE_SHEET_OCEANIC = register("ice_sheet_oceanic",
		builder().heightmap(seed -> BiomeNoise.oceanicIceSheetSurfaceHeight(seed)
										.add(BiomeNoise.glacialSurfaceTexture(seed)))
			.surface(IceSheetSurfaceBuilder.OCEANIC)
			.spawnable().salty().noSandyRiverShores()
			.type(RiverBlendType.CAVE));
	public static final BiomeExtension ICE_SHEET_SHORE = register("ice_sheet_shore",
		builder().heightmap(seed -> BiomeNoise.ocean(seed, -16, -8))
			.surface(IceSheetSurfaceBuilder.OCEANIC)
			.aquiferHeightOffset(-24)
			.spawnable().noSandyRiverShores().shore().salty()
			.type(RiverBlendType.TALL_CANYON)
			.type(ShoreBlendType.CLASSIC).setShoreBaseHeight(-12));

	// Glaciated Biomes
	public static final BiomeExtension GLACIATED_MOUNTAINS = register("glaciated_mountains",
		builder().heightmap(seed -> BiomeNoise.glacialCirques(seed)
										.addConstant(39)
										.max(BiomeNoise.glacialCirquesIceSurfaceHeight(seed).addConstant(39)))
			.surface(IceSheetSurfaceBuilder.GLACIATED_MOUNTAINS)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CAVE));
	public static final BiomeExtension GLACIATED_OCEANIC_MOUNTAINS = register("glaciated_oceanic_mountains",
		builder().heightmap(seed -> BiomeNoise.glacialCirques(seed)
										.max(BiomeNoise.glacialCirquesIceSurfaceHeight(seed)))
			.surface(IceSheetSurfaceBuilder.GLACIATED_OCEANIC_MOUNTAINS)
			.aquiferHeightOffset(-24)
			.spawnable().noSandyRiverShores().salty()
			.type(RiverBlendType.CAVE));
	public static final BiomeExtension GLACIATED_SHIELD_VOLCANO = register("glaciated_shield_volcano",
		builder().heightmap(seed -> BiomeNoise.glaciatedShieldVolcano(seed, BiomeNoise.hotSpotIntensity(seed))
										.max(BiomeNoise.shieldVolcanoGlacierSurface(seed, BiomeNoise.hotSpotIntensity(seed))
					.add(BiomeNoise.glacialSurfaceTexture(seed))))
			.surface(IceSheetShieldVolcanoSurfaceBuilder.GLACIATED)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CAVE));

	// Peri/Paleoglacial Biomes
	// Montane biomes
	public static final BiomeExtension GLACIALLY_CARVED_MOUNTAINS = register("glacially_carved_mountains",
		builder().heightmap(seed -> BiomeNoise.glacialCirques(seed).addConstant(39))
			.surface(NormalSurfaceBuilder.ROCKY)
			.spawnable().noSandyRiverShores()
			.type(RiverBlendType.CAVE));
	public static final BiomeExtension GLACIALLY_CARVED_OCEANIC_MOUNTAINS = register("glacially_carved_oceanic_mountains",
		builder().heightmap(BiomeNoise::glacialCirques)
			.surface(NormalSurfaceBuilder.ROCKY)
			.aquiferHeightOffset(-24)
			.spawnable().noSandyRiverShores().salty()
			.type(RiverBlendType.CAVE));

	// Mid-elevation biomes
	public static final BiomeExtension DRUMLINS = register("drumlins",
		builder().heightmap(BiomeNoise::drumlins)
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));
	public static final BiomeExtension TUYAS = register("tuyas",
		builder().heightmap(BiomeNoise::drumlins)
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.CANYON)
			.tuyas(2, 0, 35, -6, false));

	// Low-elevation biomes
	public static final BiomeExtension KNOB_AND_KETTLE = register("knob_and_kettle",
		builder().heightmap(BiomeNoise::knobAndKettle)
			.surface(NormalSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));
	public static final BiomeExtension PATTERNED_GROUND = register("patterned_ground",
		builder().heightmap(seed -> BiomeNoise.hills(seed, -4, 3)
										.add(BiomeNoise.patternedGround(seed)))
			.surface(PatternedGroundSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));
	public static final BiomeExtension INVERTED_PATTERNED_GROUND = register("inverted_patterned_ground",
		builder().heightmap(BiomeNoise::invertedPatternedGround)
			.surface(PatternedGroundSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));
	public static final BiomeExtension STONE_CIRCLES = register("stone_circles",
		builder().heightmap(seed -> BiomeNoise.hills(seed, -2, 4)
										.add(BiomeNoise.stoneCircles(seed)))
			.surface(StoneCirclesSurfaceBuilder.INSTANCE)
			.spawnable()
			.type(RiverBlendType.WIDE));

    public static BiomeExtension getExtensionOrThrow(LevelAccessor level, Biome biome)
    {
        return Objects.requireNonNull(getExtension(level, biome), () -> "Biome: " + level.registryAccess().registryOrThrow(Registries.BIOME).getId(biome));
    }

    public static boolean hasExtension(CommonLevelAccessor level, Biome biome)
    {
        return getExtension(level, biome) != null;
    }

    @Nullable
    @SuppressWarnings("ConstantConditions")
    public static BiomeExtension getExtension(CommonLevelAccessor level, Biome biome)
    {
        return ((BiomeBridge) (Object) biome).tfc$getExtension(() -> findExtension(level, biome));
    }

    public static Collection<ResourceKey<Biome>> getAllKeys()
    {
        return EXTENSIONS.keySet();
    }

    public static Collection<BiomeExtension> getExtensions()
    {
        return EXTENSIONS.values();
    }

    public static Collection<ResourceLocation> getExtensionKeys()
    {
        return EXTENSIONS.keySet().stream().map(ResourceKey::location).toList();
    }

    @Nullable
    public static BiomeExtension getById(ResourceLocation id)
    {
        return EXTENSIONS.get(ResourceKey.create(Registries.BIOME, id));
    }

    @Nullable
    private static BiomeExtension findExtension(CommonLevelAccessor level, Biome biome)
    {
        final RegistryAccess registryAccess = level.registryAccess();
        final Registry<Biome> registry = registryAccess.registryOrThrow(Registries.BIOME);
        return registry.getResourceKey(biome).map(EXTENSIONS::get).orElse(null);
    }

    private static BiomeExtension register(String name, BiomeBuilder builder)
    {
        final ResourceLocation id = TFGCore.id("earth/" + name);
        final ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, id);
        final BiomeExtension variants = builder.build(key);

        EXTENSIONS.put(key, variants);

        return variants;
    }
}