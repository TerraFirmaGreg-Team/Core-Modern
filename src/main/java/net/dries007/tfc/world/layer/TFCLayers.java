/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.layer;

import java.util.Random;
import java.util.function.Supplier;

import org.apache.commons.lang3.mutable.MutableInt;

import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.IArtist;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.dries007.tfc.world.layer.framework.AreaFactory;
import net.dries007.tfc.world.layer.framework.TypedAreaFactory;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.Units;
import net.dries007.tfc.world.Seed;

public class TFCLayers
{
    /**
     * These are the int IDs that are used for forest layer generation
     */
    public static final int FOREST_NONE = ForestType.NONE.ordinal();
    public static final int FOREST_NORMAL = ForestType.NORMAL.ordinal();
    public static final int FOREST_SPARSE = ForestType.SPARSE.ordinal();
    public static final int FOREST_EDGE = ForestType.EDGE.ordinal();
    public static final int FOREST_OLD = ForestType.OLD_GROWTH.ordinal();

    /**
     * These are the int IDs that are used for biome layer generation
     * They are mapped to {@link BiomeExtension} through the internal registry
     */

	public static final int DEEP_OCEAN_TRENCH, DEEP_OCEAN, OCEAN, OCEAN_REEF,
		PLAINS, HILLS, LOWLANDS, SALT_MARSH, LOW_CANYONS,
		ROLLING_HILLS, HIGHLANDS, BADLANDS, PLATEAU, PLATEAU_WIDE, CANYONS,
		MOUNTAINS, OLD_MOUNTAINS, OCEANIC_MOUNTAINS, VOLCANIC_MOUNTAINS, VOLCANIC_OCEANIC_MOUNTAINS,
		GUANO_ISLAND, SHORE, TIDAL_FLATS, SEA_STACKS, TERRACE_UPPER, TERRACE_LOWER, SETBACK_CLIFFS, COASTAL_DUNES, ROCKY_SHORES, EMBAYMENTS,
		LAKE, RIVER,
		MOUNTAIN_LAKE, OLD_MOUNTAIN_LAKE, OCEANIC_MOUNTAIN_LAKE, VOLCANIC_MOUNTAIN_LAKE, VOLCANIC_OCEANIC_MOUNTAIN_LAKE, PLATEAU_LAKE,
		MUD_FLATS, SALT_FLATS, DUNE_SEA, GRASSY_DUNES,
		WHORLED_CANYONS, STAIR_STEP_CANYONS, MESAS, BUTTES, HOODOOS, ROCKY_PLATEAU,
		TOWER_KARST_PLAINS, TOWER_KARST_CANYONS, TOWER_KARST_HILLS, TOWER_KARST_HIGHLANDS, TOWER_KARST_LAKE, TOWER_KARST_BAY,
		BURREN_PLATEAU, BURREN_BADLANDS, BURREN_BADLANDS_TALL, BURREN_PLAINS, BURREN_ROCHE_MOUTONEE,
		SHILIN_PLAINS, SHILIN_CANYONS, SHILIN_HILLS, SHILIN_HIGHLANDS, SHILIN_PLATEAU,
		DOLINE_PLAINS, DOLINE_HILLS, DOLINE_ROLLING_HILLS, DOLINE_HIGHLANDS, DOLINE_PLATEAU, DOLINE_CANYONS,
		CENOTE_PLAINS, CENOTE_HILLS, CENOTE_ROLLING_HILLS, CENOTE_CANYONS, CENOTE_HIGHLANDS, CENOTE_PLATEAU,
		EXTREME_DOLINE_PLATEAU, EXTREME_DOLINE_MOUNTAINS,
		ACTIVE_SHIELD_VOLCANO, DORMANT_SHIELD_VOLCANO, EXTINCT_SHIELD_VOLCANO, ANCIENT_SHIELD_VOLCANO, SUNKEN_SHIELD_VOLCANO,
		SHIELD_VOLCANO_SHORE, OLD_SHIELD_VOLCANO_SHORE,
		ICE_SHEET, ICE_SHEET_MOUNTAINS, ICE_SHEET_OCEANIC_MOUNTAINS, ICE_SHEET_SHIELD_VOLCANO, ICE_SHEET_TUYAS, SUBGLACIAL_LAKE,
		ICE_SHEET_EDGE, ICE_SHEET_TUYAS_EDGE, ICE_SHEET_MOUNTAINS_EDGE, ICE_SHEET_OCEANIC_MOUNTAINS_EDGE, MELTWATER_LAKE, ICE_SHEET_OCEANIC, ICE_SHEET_SHORE,
		GLACIATED_MOUNTAINS, GLACIATED_OCEANIC_MOUNTAINS, GLACIATED_SHIELD_VOLCANO,
		GLACIALLY_CARVED_MOUNTAINS, GLACIALLY_CARVED_OCEANIC_MOUNTAINS,
		DRUMLINS, TUYAS,
		KNOB_AND_KETTLE, PATTERNED_GROUND, INVERTED_PATTERNED_GROUND, STONE_CIRCLES;

    private static final BiomeExtension[] BIOME_LAYERS = new BiomeExtension[128];
    private static final MutableInt BIOME_LAYER_INDEX = new MutableInt(0);

    static
    {
		DEEP_OCEAN_TRENCH = register(() -> TFCBiomes.DEEP_OCEAN_TRENCH);
		DEEP_OCEAN = register(() -> TFCBiomes.DEEP_OCEAN);
		OCEAN = register(() -> TFCBiomes.OCEAN);
		OCEAN_REEF = register(() -> TFCBiomes.OCEAN_REEF);

		PLAINS = register(() -> TFCBiomes.PLAINS);
		HILLS = register(() -> TFCBiomes.HILLS);
		LOWLANDS = register(() -> TFCBiomes.LOWLANDS);
		SALT_MARSH = register(() -> TFCBiomes.SALT_MARSH);
		LOW_CANYONS = register(() -> TFCBiomes.LOW_CANYONS);

		ROLLING_HILLS = register(() -> TFCBiomes.ROLLING_HILLS);
		HIGHLANDS = register(() -> TFCBiomes.HIGHLANDS);
		BADLANDS = register(() -> TFCBiomes.BADLANDS);
		PLATEAU = register(() -> TFCBiomes.PLATEAU);
		PLATEAU_WIDE = register(() -> TFCBiomes.PLATEAU_WIDE);
		CANYONS = register(() -> TFCBiomes.CANYONS);

		MOUNTAINS = register(() -> TFCBiomes.MOUNTAINS);
		OLD_MOUNTAINS = register(() -> TFCBiomes.OLD_MOUNTAINS);
		OCEANIC_MOUNTAINS = register(() -> TFCBiomes.OCEANIC_MOUNTAINS);
		VOLCANIC_MOUNTAINS = register(() -> TFCBiomes.VOLCANIC_MOUNTAINS);
		VOLCANIC_OCEANIC_MOUNTAINS = register(() -> TFCBiomes.VOLCANIC_OCEANIC_MOUNTAINS);

		GUANO_ISLAND = register(() -> TFCBiomes.GUANO_ISLAND);
		SHORE = register(() -> TFCBiomes.SHORE);
		TIDAL_FLATS = register(() -> TFCBiomes.TIDAL_FLATS);
		SEA_STACKS = register(() -> TFCBiomes.SEA_STACKS);
		TERRACE_UPPER = register(() -> TFCBiomes.TERRACE_UPPER);
		TERRACE_LOWER = register(() -> TFCBiomes.TERRACE_LOWER);
		SETBACK_CLIFFS = register(() -> TFCBiomes.SETBACK_CLIFFS);
		COASTAL_DUNES = register(() -> TFCBiomes.COASTAL_DUNES);
		ROCKY_SHORES = register(() -> TFCBiomes.ROCKY_SHORES);
		EMBAYMENTS = register(() -> TFCBiomes.EMBAYMENTS);

		LAKE = register(() -> TFCBiomes.LAKE);
		RIVER = register(() -> TFCBiomes.RIVER);

		MOUNTAIN_LAKE = register(() -> TFCBiomes.MOUNTAIN_LAKE);
		OLD_MOUNTAIN_LAKE = register(() -> TFCBiomes.OLD_MOUNTAIN_LAKE);
		OCEANIC_MOUNTAIN_LAKE = register(() -> TFCBiomes.OCEANIC_MOUNTAIN_LAKE);
		VOLCANIC_MOUNTAIN_LAKE = register(() -> TFCBiomes.VOLCANIC_MOUNTAIN_LAKE);
		VOLCANIC_OCEANIC_MOUNTAIN_LAKE = register(() -> TFCBiomes.VOLCANIC_OCEANIC_MOUNTAIN_LAKE);
		PLATEAU_LAKE = register(() -> TFCBiomes.PLATEAU_LAKE);

		MUD_FLATS = register(() -> TFCBiomes.MUD_FLATS);
		SALT_FLATS = register(() -> TFCBiomes.SALT_FLATS);
		DUNE_SEA = register(() -> TFCBiomes.DUNE_SEA);
		GRASSY_DUNES = register(() -> TFCBiomes.GRASSY_DUNES);
		WHORLED_CANYONS = register(() -> TFCBiomes.WHORLED_CANYONS);
		STAIR_STEP_CANYONS = register(() -> TFCBiomes.STAIR_STEP_CANYONS);
		MESAS = register(() -> TFCBiomes.MESAS);
		BUTTES = register(() -> TFCBiomes.BUTTES);
		HOODOOS = register(() -> TFCBiomes.HOODOOS);
		ROCKY_PLATEAU = register(() -> TFCBiomes.ROCKY_PLATEAU);

		TOWER_KARST_PLAINS = register(() -> TFCBiomes.TOWER_KARST_PLAINS);
		TOWER_KARST_CANYONS = register(() -> TFCBiomes.TOWER_KARST_CANYONS);
		TOWER_KARST_HILLS = register(() -> TFCBiomes.TOWER_KARST_HILLS);
		TOWER_KARST_HIGHLANDS = register(() -> TFCBiomes.TOWER_KARST_HIGHLANDS);
		TOWER_KARST_LAKE = register(() -> TFCBiomes.TOWER_KARST_LAKE);
		TOWER_KARST_BAY = register(() -> TFCBiomes.TOWER_KARST_BAY);

		BURREN_PLATEAU = register(() -> TFCBiomes.BURREN_PLATEAU);
		BURREN_BADLANDS = register(() -> TFCBiomes.BURREN_BADLANDS);
		BURREN_BADLANDS_TALL = register(() -> TFCBiomes.BURREN_BADLANDS_TALL);
		BURREN_PLAINS = register(() -> TFCBiomes.BURREN_PLAINS);
		BURREN_ROCHE_MOUTONEE = register(() -> TFCBiomes.BURREN_ROCHE_MOUTONEE);

		SHILIN_PLAINS = register(() -> TFCBiomes.SHILIN_PLAINS);
		SHILIN_CANYONS = register(() -> TFCBiomes.SHILIN_CANYONS);
		SHILIN_HILLS = register(() -> TFCBiomes.SHILIN_HILLS);
		SHILIN_HIGHLANDS = register(() -> TFCBiomes.SHILIN_HIGHLANDS);
		SHILIN_PLATEAU = register(() -> TFCBiomes.SHILIN_PLATEAU);

		DOLINE_PLAINS = register(() -> TFCBiomes.DOLINE_PLAINS);
		DOLINE_HILLS = register(() -> TFCBiomes.DOLINE_HILLS);
		DOLINE_ROLLING_HILLS = register(() -> TFCBiomes.DOLINE_ROLLING_HILLS);
		DOLINE_HIGHLANDS = register(() -> TFCBiomes.DOLINE_HIGHLANDS);
		DOLINE_PLATEAU = register(() -> TFCBiomes.DOLINE_PLATEAU);
		DOLINE_CANYONS = register(() -> TFCBiomes.DOLINE_CANYONS);

		CENOTE_PLAINS = register(() -> TFCBiomes.CENOTE_PLAINS);
		CENOTE_HILLS = register(() -> TFCBiomes.CENOTE_HILLS);
		CENOTE_ROLLING_HILLS = register(() -> TFCBiomes.CENOTE_ROLLING_HILLS);
		CENOTE_CANYONS = register(() -> TFCBiomes.CENOTE_CANYONS);
		CENOTE_HIGHLANDS = register(() -> TFCBiomes.CENOTE_HIGHLANDS);
		CENOTE_PLATEAU = register(() -> TFCBiomes.CENOTE_PLATEAU);

		EXTREME_DOLINE_PLATEAU = register(() -> TFCBiomes.EXTREME_DOLINE_PLATEAU);
		EXTREME_DOLINE_MOUNTAINS = register(() -> TFCBiomes.EXTREME_DOLINE_MOUNTAINS);

		ACTIVE_SHIELD_VOLCANO = register(() -> TFCBiomes.ACTIVE_SHIELD_VOLCANO);
		DORMANT_SHIELD_VOLCANO = register(() -> TFCBiomes.DORMANT_SHIELD_VOLCANO);
		EXTINCT_SHIELD_VOLCANO = register(() -> TFCBiomes.EXTINCT_SHIELD_VOLCANO);
		ANCIENT_SHIELD_VOLCANO = register(() -> TFCBiomes.ANCIENT_SHIELD_VOLCANO);
		SUNKEN_SHIELD_VOLCANO = register(() -> TFCBiomes.SUNKEN_SHIELD_VOLCANO);

		SHIELD_VOLCANO_SHORE = register(() -> TFCBiomes.SHIELD_VOLCANO_SHORE);
		OLD_SHIELD_VOLCANO_SHORE = register(() -> TFCBiomes.OLD_SHIELD_VOLCANO_SHORE);

		ICE_SHEET = register(() -> TFCBiomes.ICE_SHEET);
		ICE_SHEET_MOUNTAINS = register(() -> TFCBiomes.ICE_SHEET_MOUNTAINS);
		ICE_SHEET_OCEANIC_MOUNTAINS = register(() -> TFCBiomes.ICE_SHEET_OCEANIC_MOUNTAINS);
		ICE_SHEET_SHIELD_VOLCANO = register(() -> TFCBiomes.ICE_SHEET_SHIELD_VOLCANO);
		ICE_SHEET_TUYAS = register(() -> TFCBiomes.ICE_SHEET_TUYAS);
		SUBGLACIAL_LAKE = register(() -> TFCBiomes.SUBGLACIAL_LAKE);

		ICE_SHEET_EDGE = register(() -> TFCBiomes.ICE_SHEET_EDGE);
		ICE_SHEET_TUYAS_EDGE = register(() -> TFCBiomes.ICE_SHEET_TUYAS_EDGE);
		ICE_SHEET_MOUNTAINS_EDGE = register(() -> TFCBiomes.ICE_SHEET_MOUNTAINS_EDGE);
		ICE_SHEET_OCEANIC_MOUNTAINS_EDGE = register(() -> TFCBiomes.ICE_SHEET_OCEANIC_MOUNTAINS_EDGE);
		MELTWATER_LAKE = register(() -> TFCBiomes.MELTWATER_LAKE);
		ICE_SHEET_OCEANIC = register(() -> TFCBiomes.ICE_SHEET_OCEANIC);
		ICE_SHEET_SHORE = register(() -> TFCBiomes.ICE_SHEET_SHORE);

		GLACIATED_MOUNTAINS = register(() -> TFCBiomes.GLACIATED_MOUNTAINS);
		GLACIATED_OCEANIC_MOUNTAINS = register(() -> TFCBiomes.GLACIATED_OCEANIC_MOUNTAINS);
		GLACIATED_SHIELD_VOLCANO = register(() -> TFCBiomes.GLACIATED_SHIELD_VOLCANO);

		GLACIALLY_CARVED_MOUNTAINS = register(() -> TFCBiomes.GLACIALLY_CARVED_MOUNTAINS);
		GLACIALLY_CARVED_OCEANIC_MOUNTAINS = register(() -> TFCBiomes.GLACIALLY_CARVED_OCEANIC_MOUNTAINS);

		DRUMLINS = register(() -> TFCBiomes.DRUMLINS);
		TUYAS = register(() -> TFCBiomes.TUYAS);

		KNOB_AND_KETTLE = register(() -> TFCBiomes.KNOB_AND_KETTLE);
		PATTERNED_GROUND = register(() -> TFCBiomes.PATTERNED_GROUND);
		INVERTED_PATTERNED_GROUND = register(() -> TFCBiomes.INVERTED_PATTERNED_GROUND);
		STONE_CIRCLES = register(() -> TFCBiomes.STONE_CIRCLES);
    }

    public static BiomeExtension getFromLayerId(int id)
    {
        final BiomeExtension v = BIOME_LAYERS[id];
        if (v == null)
        {
            throw new NullPointerException("Layer id = " + id + " returned null!");
        }
        return v;
    }

    public static AreaFactory createOverworldForestLayer(Seed seed, IArtist<AreaFactory> artist)
    {
        AreaFactory layer;

        layer = new ForestInitLayer(new OpenSimplex2D(seed.next()).spread(0.3f)).apply(seed.next());
        artist.draw("forest", 1, layer);
        layer = ForestRandomizeLayer.INSTANCE.apply(seed.next(), layer);
        artist.draw("forest", 2, layer);
        layer = ZoomLayer.FUZZY.apply(seed.next(), layer);
        artist.draw("forest", 3, layer);
        layer = ForestRandomizeLayer.INSTANCE.apply(seed.next(), layer);
        artist.draw("forest", 4, layer);
        layer = ZoomLayer.FUZZY.apply(seed.next(), layer);
        artist.draw("forest", 5, layer);
        layer = ZoomLayer.NORMAL.apply(seed.next(), layer);
        artist.draw("forest", 6, layer);
        layer = ForestEdgeLayer.INSTANCE.apply(seed.next(), layer);
        artist.draw("forest", 7, layer);
        layer = ForestRandomizeSmallLayer.INSTANCE.apply(seed.next(), layer);
        artist.draw("forest", 8, layer);

        for (int i = 0; i < 2; i++)
        {
            layer = ZoomLayer.NORMAL.apply(seed.next(), layer);
            artist.draw("forest", 9 + i, layer);
        }

        return layer;
    }

    public static AreaFactory createOverworldRockLayer(RegionGenerator generator, long seed)
    {
        final Random random = new Random(seed);
        final TypedAreaFactory<Region.Point> regionLayer = new RegionLayer(generator).apply(random.nextLong());

        AreaFactory layer;

        layer = RegionRockLayer.INSTANCE.apply(regionLayer); // Grid scale (128x)
        for (int i = 0; i < Units.GRID_BITS - 1; i++)
        {
            layer = ZoomLayer.NORMAL.apply(seed, layer);
        }
        layer = SmoothLayer.INSTANCE.apply(seed, layer);
        layer = ZoomLayer.NORMAL.apply(seed, layer);
        layer = SmoothLayer.INSTANCE.apply(seed, layer);

        return layer;
    }

    public static AreaFactory createRegionBiomeLayer(RegionGenerator generator, Seed seed)
    {
		final TypedAreaFactory<Region.Point> regionLayer = new RegionLayer(generator).apply(seed.next());

		AreaFactory mainLayer;

		mainLayer = RegionBiomeLayer.INSTANCE.apply(regionLayer);

		// Grid scale

		mainLayer = RegionEdgeBiomeLayer.INSTANCE.apply(seed.next(), mainLayer);
		mainLayer = ZoomLayer.NORMAL.apply(seed.next(), mainLayer);

		// 4x4 Chunk Scale
		mainLayer = ShoreLayer.INSTANCE.apply(seed.next(), mainLayer);
		mainLayer = MoreShoresLayer.INSTANCE.apply(seed.next(), mainLayer);
		mainLayer = IceSheetEdgeLayer.INSTANCE.apply(seed.next(), mainLayer);
		mainLayer = ZoomLayer.NORMAL.apply(seed.next(), mainLayer);
		mainLayer = ZoomLayer.NORMAL.apply(seed.next(), mainLayer);

		// Chunk scale

		mainLayer = ZoomLayer.NORMAL.apply(seed.next(), mainLayer);
		mainLayer = ZoomLayer.NORMAL.apply(seed.next(), mainLayer);

		// Quart scale

		mainLayer = SmoothLayer.INSTANCE.apply(seed.next(), mainLayer);

		return mainLayer;
    }

	public static AreaFactory createUniformLayer(Seed seed, int zoomLevels)
	{
		AreaFactory layer;

		layer = UniformLayer.INSTANCE.apply(seed.next());
		for (int i = 0; i < zoomLevels; i++)
		{
			layer = ZoomLayer.NORMAL.apply(seed.next(), layer);
			layer = SmoothLayer.INSTANCE.apply(seed.next(), layer);
		}

		return layer;
	}

	public static boolean hasShore(int value) {
		return value != LOW_CANYONS && value != CANYONS && value != OCEANIC_MOUNTAINS && value != VOLCANIC_OCEANIC_MOUNTAINS
				   && value != TOWER_KARST_BAY && value != SUNKEN_SHIELD_VOLCANO && value != GLACIALLY_CARVED_OCEANIC_MOUNTAINS && value != GLACIATED_OCEANIC_MOUNTAINS
				   && value != ICE_SHEET_OCEANIC_MOUNTAINS_EDGE
				   && value != ICE_SHEET_SHIELD_VOLCANO && value != GLACIATED_SHIELD_VOLCANO
				   && value != GUANO_ISLAND;
	}

	public static int shoreFor(int value) {
		if (value == LOWLANDS || value == SALT_MARSH) {
			return SALT_MARSH;
		}
		if (value == MOUNTAINS) {
			return OCEANIC_MOUNTAINS;
		}
		if (value == VOLCANIC_MOUNTAINS) {
			return VOLCANIC_OCEANIC_MOUNTAINS;
		}
		if (value == TOWER_KARST_LAKE) {
			return TOWER_KARST_BAY;
		}
		if (value == ACTIVE_SHIELD_VOLCANO) {
			return SHIELD_VOLCANO_SHORE;
		}
		if (value == DORMANT_SHIELD_VOLCANO || value == EXTINCT_SHIELD_VOLCANO || value == ANCIENT_SHIELD_VOLCANO) {
			return OLD_SHIELD_VOLCANO_SHORE;
		}
		if (isFlatIceSheet(value) || value == ICE_SHEET_EDGE || value == ICE_SHEET_OCEANIC) {
			return ICE_SHEET_SHORE;
		}
		if (value == ICE_SHEET_OCEANIC_MOUNTAINS) {
			return ICE_SHEET_OCEANIC_MOUNTAINS_EDGE;
		}
		if (value == GLACIALLY_CARVED_OCEANIC_MOUNTAINS || value == GLACIALLY_CARVED_MOUNTAINS) {
			return GLACIATED_OCEANIC_MOUNTAINS;
		}
		if (value == OLD_MOUNTAINS || value == EXTREME_DOLINE_MOUNTAINS) {
			return TERRACE_LOWER;
		}
		if (value == PLATEAU || value == EXTREME_DOLINE_PLATEAU || value == BURREN_PLATEAU || value == SHILIN_PLATEAU) {
			return SEA_STACKS;
		}
		if (value == PLATEAU_WIDE || value == ROCKY_PLATEAU || value == DOLINE_PLATEAU) {
			return SETBACK_CLIFFS;
		}
		if (value == HIGHLANDS || value == CENOTE_HIGHLANDS || value == DOLINE_HIGHLANDS || value == SHILIN_HIGHLANDS || value == TOWER_KARST_HIGHLANDS) {
			return ROCKY_SHORES;
		}
		if (value == ROLLING_HILLS || value == DOLINE_ROLLING_HILLS || value == CENOTE_ROLLING_HILLS) {
			return EMBAYMENTS;
		}
		if (value == HILLS || value == CENOTE_HILLS || value == DOLINE_HILLS || value == SHILIN_HILLS || value == TOWER_KARST_HILLS || value == GRASSY_DUNES || value == DUNE_SEA) {
			return COASTAL_DUNES;
		}
		return TIDAL_FLATS;
	}

	public static boolean hasLake(int value) {
		return !isOcean(value) && value != BADLANDS
				   && value != ACTIVE_SHIELD_VOLCANO && value != DORMANT_SHIELD_VOLCANO && value != EXTINCT_SHIELD_VOLCANO
				   && value != ANCIENT_SHIELD_VOLCANO && value != ICE_SHEET_MOUNTAINS && value != ICE_SHEET_MOUNTAINS_EDGE
				   && value != ICE_SHEET_OCEANIC_MOUNTAINS && value != ICE_SHEET_OCEANIC_MOUNTAINS_EDGE
				   && value != ICE_SHEET_SHIELD_VOLCANO && value != ICE_SHEET_SHORE && value != GLACIATED_SHIELD_VOLCANO
				   && value != GLACIATED_MOUNTAINS && value != GLACIATED_OCEANIC_MOUNTAINS && value != GLACIALLY_CARVED_MOUNTAINS
				   && value != GLACIALLY_CARVED_OCEANIC_MOUNTAINS;
	}

	public static int lakeFor(int value) {
		if (value == MOUNTAINS) {
			return MOUNTAIN_LAKE;
		}
		if (value == VOLCANIC_MOUNTAINS) {
			return VOLCANIC_MOUNTAIN_LAKE;
		}
		if (value == OLD_MOUNTAINS) {
			return OLD_MOUNTAIN_LAKE;
		}
		if (value == OCEANIC_MOUNTAINS) {
			return OCEANIC_MOUNTAIN_LAKE;
		}
		if (value == VOLCANIC_OCEANIC_MOUNTAINS) {
			return VOLCANIC_OCEANIC_MOUNTAIN_LAKE;
		}
		if (value == PLATEAU) {
			return PLATEAU_LAKE;
		}
		if (isFlatIceSheet(value)) {
			return SUBGLACIAL_LAKE;
		}
		if (value == ICE_SHEET_EDGE) {
			return MELTWATER_LAKE;
		}
		return LAKE;
	}

	public static boolean isOcean(int value) {
		return value == OCEAN || value == DEEP_OCEAN || value == DEEP_OCEAN_TRENCH || value == OCEAN_REEF;
	}

	public static boolean isFlats(int value) {
		return value == MUD_FLATS || value == SALT_FLATS;
	}

	public static boolean isFlatIceSheet(int value) {
		return value == ICE_SHEET || value == ICE_SHEET_TUYAS || value == SUBGLACIAL_LAKE;
	}

	public static boolean isMountains(int value) {
		return value == MOUNTAINS || value == OCEANIC_MOUNTAINS || value == OLD_MOUNTAINS || value == VOLCANIC_MOUNTAINS || value == VOLCANIC_OCEANIC_MOUNTAINS;
	}

	public static boolean isLow(int value) {
		return value == PLAINS || value == HILLS || value == LOW_CANYONS || value == LOWLANDS || value == SALT_MARSH || value == MUD_FLATS || value == SALT_FLATS || value == DUNE_SEA;
	}

    public static int register(Supplier<BiomeExtension> variants)
    {
        final int index = BIOME_LAYER_INDEX.getAndIncrement();
        if (index >= BIOME_LAYERS.length)
        {
            throw new IllegalStateException("Tried to register layer id " + index + " but only had space for " + BIOME_LAYERS.length + " layers");
        }
        BIOME_LAYERS[index] = Helpers.BOOTSTRAP_ENVIRONMENT ? null : variants.get();
        return index;
    }
}