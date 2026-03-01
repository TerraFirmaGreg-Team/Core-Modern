package su.terrafirmagreg.core.world.new_ow_wg;

import java.util.function.Supplier;

import org.apache.commons.lang3.mutable.MutableInt;

import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.layer.RegionBiomeLayer;
import net.dries007.tfc.world.layer.RegionLayer;
import net.dries007.tfc.world.layer.SmoothLayer;
import net.dries007.tfc.world.layer.ZoomLayer;
import net.dries007.tfc.world.layer.framework.AreaFactory;
import net.dries007.tfc.world.layer.framework.TypedAreaFactory;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;

import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;
import su.terrafirmagreg.core.world.new_ow_wg.layers.TFGMoreShoresLayer;
import su.terrafirmagreg.core.world.new_ow_wg.layers.TFGRegionEdgeBiomeLayer;
import su.terrafirmagreg.core.world.new_ow_wg.layers.TFGShoreLayer;

public class TFGLayers {

    public static void init() {
    }

    private static final BiomeExtension[] BIOME_LAYERS;
    private static final MutableInt BIOME_LAYER_INDEX;

    public static final int DEEP_OCEAN_TRENCH;
    public static final int DEEP_OCEAN;
    public static final int OCEAN;
    public static final int OCEAN_REEF;

    public static final int PLAINS;
    public static final int HILLS;
    public static final int LOWLANDS;
    public static final int SALT_MARSH;
    public static final int LOW_CANYONS;

    public static final int ROLLING_HILLS;
    public static final int HIGHLANDS;
    public static final int BADLANDS;
    public static final int PLATEAU;
    public static final int PLATEAU_WIDE;
    public static final int CANYONS;

    public static final int MOUNTAINS;
    public static final int OLD_MOUNTAINS;
    public static final int OCEANIC_MOUNTAINS;
    public static final int VOLCANIC_MOUNTAINS;
    public static final int VOLCANIC_OCEANIC_MOUNTAINS;

    public static final int GUANO_ISLAND;
    public static final int SHORE;
    public static final int TIDAL_FLATS;
    public static final int SEA_STACKS;
    public static final int TERRACE_UPPER;
    public static final int TERRACE_LOWER;
    public static final int SETBACK_CLIFFS;
    public static final int COASTAL_DUNES;
    public static final int ROCKY_SHORES;
    public static final int EMBAYMENTS;

    public static final int LAKE;
    public static final int RIVER;

    public static final int MOUNTAIN_LAKE;
    public static final int OLD_MOUNTAIN_LAKE;
    public static final int OCEANIC_MOUNTAIN_LAKE;
    public static final int VOLCANIC_MOUNTAIN_LAKE;
    public static final int VOLCANIC_OCEANIC_MOUNTAIN_LAKE;
    public static final int PLATEAU_LAKE;

    public static final int MUD_FLATS;
    public static final int SALT_FLATS;
    public static final int DUNE_SEA;
    public static final int GRASSY_DUNES;
    public static final int WHORLED_CANYONS;
    public static final int STAIR_STEP_CANYONS;
    public static final int MESAS;
    public static final int BUTTES;
    public static final int HOODOOS;
    public static final int ROCKY_PLATEAU;

    public static final int TOWER_KARST_PLAINS;
    public static final int TOWER_KARST_CANYONS;
    public static final int TOWER_KARST_HILLS;
    public static final int TOWER_KARST_HIGHLANDS;
    public static final int TOWER_KARST_LAKE;
    public static final int TOWER_KARST_BAY;

    public static final int BURREN_PLATEAU;
    public static final int BURREN_BADLANDS;
    public static final int BURREN_BADLANDS_TALL;
    public static final int BURREN_PLAINS;
    public static final int BURREN_ROCHE_MOUTONEE;

    public static final int SHILIN_PLAINS;
    public static final int SHILIN_CANYONS;
    public static final int SHILIN_HILLS;
    public static final int SHILIN_HIGHLANDS;
    public static final int SHILIN_PLATEAU;

    public static final int DOLINE_PLAINS;
    public static final int DOLINE_HILLS;
    public static final int DOLINE_ROLLING_HILLS;
    public static final int DOLINE_HIGHLANDS;
    public static final int DOLINE_PLATEAU;
    public static final int DOLINE_CANYONS;

    public static final int CENOTE_PLAINS;
    public static final int CENOTE_HILLS;
    public static final int CENOTE_ROLLING_HILLS;
    public static final int CENOTE_CANYONS;
    public static final int CENOTE_HIGHLANDS;
    public static final int CENOTE_PLATEAU;

    public static final int EXTREME_DOLINE_PLATEAU;
    public static final int EXTREME_DOLINE_MOUNTAINS;

    static {
        BIOME_LAYERS = new BiomeExtension[128];
        BIOME_LAYER_INDEX = new MutableInt(0);

        DEEP_OCEAN_TRENCH = TFGLayers.register(() -> TFGBiomes.DEEP_OCEAN_TRENCH);
        DEEP_OCEAN = TFGLayers.register(() -> TFGBiomes.DEEP_OCEAN);
        OCEAN = TFGLayers.register(() -> TFGBiomes.OCEAN);
        OCEAN_REEF = TFGLayers.register(() -> TFGBiomes.OCEAN_REEF);

        PLAINS = TFGLayers.register(() -> TFGBiomes.PLAINS);
        HILLS = TFGLayers.register(() -> TFGBiomes.HILLS);
        LOWLANDS = TFGLayers.register(() -> TFGBiomes.LOWLANDS);
        SALT_MARSH = TFGLayers.register(() -> TFGBiomes.SALT_MARSH);
        LOW_CANYONS = TFGLayers.register(() -> TFGBiomes.LOW_CANYONS);

        ROLLING_HILLS = TFGLayers.register(() -> TFGBiomes.ROLLING_HILLS);
        HIGHLANDS = TFGLayers.register(() -> TFGBiomes.HIGHLANDS);
        BADLANDS = TFGLayers.register(() -> TFGBiomes.BADLANDS);
        PLATEAU = TFGLayers.register(() -> TFGBiomes.PLATEAU);
        PLATEAU_WIDE = TFGLayers.register(() -> TFGBiomes.PLATEAU_WIDE);
        CANYONS = TFGLayers.register(() -> TFGBiomes.CANYONS);

        MOUNTAINS = TFGLayers.register(() -> TFGBiomes.MOUNTAINS);
        OLD_MOUNTAINS = TFGLayers.register(() -> TFGBiomes.OLD_MOUNTAINS);
        OCEANIC_MOUNTAINS = TFGLayers.register(() -> TFGBiomes.OCEANIC_MOUNTAINS);
        VOLCANIC_MOUNTAINS = TFGLayers.register(() -> TFGBiomes.VOLCANIC_MOUNTAINS);
        VOLCANIC_OCEANIC_MOUNTAINS = TFGLayers.register(() -> TFGBiomes.VOLCANIC_OCEANIC_MOUNTAINS);

        GUANO_ISLAND = TFGLayers.register(() -> TFGBiomes.GUANO_ISLAND);
        SHORE = TFGLayers.register(() -> TFGBiomes.SHORE);
        TIDAL_FLATS = TFGLayers.register(() -> TFGBiomes.TIDAL_FLATS);
        SEA_STACKS = TFGLayers.register(() -> TFGBiomes.SEA_STACKS);
        TERRACE_UPPER = TFGLayers.register(() -> TFGBiomes.TERRACE_UPPER);
        TERRACE_LOWER = TFGLayers.register(() -> TFGBiomes.TERRACE_LOWER);
        SETBACK_CLIFFS = TFGLayers.register(() -> TFGBiomes.SETBACK_CLIFFS);
        COASTAL_DUNES = TFGLayers.register(() -> TFGBiomes.COASTAL_DUNES);
        ROCKY_SHORES = TFGLayers.register(() -> TFGBiomes.ROCKY_SHORES);
        EMBAYMENTS = TFGLayers.register(() -> TFGBiomes.EMBAYMENTS);

        LAKE = TFGLayers.register(() -> TFGBiomes.LAKE);
        RIVER = TFGLayers.register(() -> TFGBiomes.RIVER);

        MOUNTAIN_LAKE = TFGLayers.register(() -> TFGBiomes.MOUNTAIN_LAKE);
        OLD_MOUNTAIN_LAKE = TFGLayers.register(() -> TFGBiomes.OLD_MOUNTAIN_LAKE);
        OCEANIC_MOUNTAIN_LAKE = TFGLayers.register(() -> TFGBiomes.OCEANIC_MOUNTAIN_LAKE);
        VOLCANIC_MOUNTAIN_LAKE = TFGLayers.register(() -> TFGBiomes.VOLCANIC_MOUNTAIN_LAKE);
        VOLCANIC_OCEANIC_MOUNTAIN_LAKE = TFGLayers.register(() -> TFGBiomes.VOLCANIC_OCEANIC_MOUNTAIN_LAKE);
        PLATEAU_LAKE = TFGLayers.register(() -> TFGBiomes.PLATEAU_LAKE);

        MUD_FLATS = TFGLayers.register(() -> TFGBiomes.MUD_FLATS);
        SALT_FLATS = TFGLayers.register(() -> TFGBiomes.SALT_FLATS);
        DUNE_SEA = TFGLayers.register(() -> TFGBiomes.DUNE_SEA);
        GRASSY_DUNES = TFGLayers.register(() -> TFGBiomes.GRASSY_DUNES);
        WHORLED_CANYONS = TFGLayers.register(() -> TFGBiomes.WHORLED_CANYONS);
        STAIR_STEP_CANYONS = TFGLayers.register(() -> TFGBiomes.STAIR_STEP_CANYONS);
        MESAS = TFGLayers.register(() -> TFGBiomes.MESAS);
        BUTTES = TFGLayers.register(() -> TFGBiomes.BUTTES);
        HOODOOS = TFGLayers.register(() -> TFGBiomes.HOODOOS);
        ROCKY_PLATEAU = TFGLayers.register(() -> TFGBiomes.ROCKY_PLATEAU);

        TOWER_KARST_PLAINS = TFGLayers.register(() -> TFGBiomes.TOWER_KARST_PLAINS);
        TOWER_KARST_CANYONS = TFGLayers.register(() -> TFGBiomes.TOWER_KARST_CANYONS);
        TOWER_KARST_HILLS = TFGLayers.register(() -> TFGBiomes.TOWER_KARST_HILLS);
        TOWER_KARST_HIGHLANDS = TFGLayers.register(() -> TFGBiomes.TOWER_KARST_HIGHLANDS);
        TOWER_KARST_LAKE = TFGLayers.register(() -> TFGBiomes.TOWER_KARST_LAKE);
        TOWER_KARST_BAY = TFGLayers.register(() -> TFGBiomes.TOWER_KARST_BAY);

        BURREN_PLATEAU = TFGLayers.register(() -> TFGBiomes.BURREN_PLATEAU);
        BURREN_BADLANDS = TFGLayers.register(() -> TFGBiomes.BURREN_BADLANDS);
        BURREN_BADLANDS_TALL = TFGLayers.register(() -> TFGBiomes.BURREN_BADLANDS_TALL);
        BURREN_PLAINS = TFGLayers.register(() -> TFGBiomes.BURREN_PLAINS);
        BURREN_ROCHE_MOUTONEE = TFGLayers.register(() -> TFGBiomes.BURREN_ROCHE_MOUTONEE);

        SHILIN_PLAINS = TFGLayers.register(() -> TFGBiomes.SHILIN_PLAINS);
        SHILIN_CANYONS = TFGLayers.register(() -> TFGBiomes.SHILIN_CANYONS);
        SHILIN_HILLS = TFGLayers.register(() -> TFGBiomes.SHILIN_HILLS);
        SHILIN_HIGHLANDS = TFGLayers.register(() -> TFGBiomes.SHILIN_HIGHLANDS);
        SHILIN_PLATEAU = TFGLayers.register(() -> TFGBiomes.SHILIN_PLATEAU);

        DOLINE_PLAINS = TFGLayers.register(() -> TFGBiomes.DOLINE_PLAINS);
        DOLINE_HILLS = TFGLayers.register(() -> TFGBiomes.DOLINE_HILLS);
        DOLINE_ROLLING_HILLS = TFGLayers.register(() -> TFGBiomes.DOLINE_ROLLING_HILLS);
        DOLINE_HIGHLANDS = TFGLayers.register(() -> TFGBiomes.DOLINE_HIGHLANDS);
        DOLINE_PLATEAU = TFGLayers.register(() -> TFGBiomes.DOLINE_PLATEAU);
        DOLINE_CANYONS = TFGLayers.register(() -> TFGBiomes.DOLINE_CANYONS);

        CENOTE_PLAINS = TFGLayers.register(() -> TFGBiomes.CENOTE_PLAINS);
        CENOTE_HILLS = TFGLayers.register(() -> TFGBiomes.CENOTE_HILLS);
        CENOTE_ROLLING_HILLS = TFGLayers.register(() -> TFGBiomes.CENOTE_ROLLING_HILLS);
        CENOTE_CANYONS = TFGLayers.register(() -> TFGBiomes.CENOTE_CANYONS);
        CENOTE_HIGHLANDS = TFGLayers.register(() -> TFGBiomes.CENOTE_HIGHLANDS);
        CENOTE_PLATEAU = TFGLayers.register(() -> TFGBiomes.CENOTE_PLATEAU);

        EXTREME_DOLINE_PLATEAU = TFGLayers.register(() -> TFGBiomes.EXTREME_DOLINE_PLATEAU);
        EXTREME_DOLINE_MOUNTAINS = TFGLayers.register(() -> TFGBiomes.EXTREME_DOLINE_MOUNTAINS);
    }

    public static BiomeExtension getFromLayerId(int id) {
        BiomeExtension v = BIOME_LAYERS[id];
        if (v == null) {
            throw new NullPointerException("Layer id = " + id + " returned null!");
        } else {
            return v;
        }
    }

    public static int register(Supplier<BiomeExtension> variants) {
        int index = BIOME_LAYER_INDEX.getAndIncrement();
        if (index >= BIOME_LAYERS.length) {
            throw new IllegalStateException("Tried to register layer id " + index + " but only had space for " + BIOME_LAYERS.length + " layers");
        } else {
            BIOME_LAYERS[index] = Helpers.BOOTSTRAP_ENVIRONMENT ? null : variants.get();
            return index;
        }
    }

    public static boolean isOcean(int value) {
        return value == OCEAN || value == DEEP_OCEAN || value == DEEP_OCEAN_TRENCH || value == OCEAN_REEF;
    }

    public static boolean isFlats(int value) {
        return value == MUD_FLATS || value == SALT_FLATS;
    }

    public static boolean isFlatIceSheet(int value) {
        return false;
        //return value == ICE_SHEET || value == ICE_SHEET_TUYAS || value == SUBGLACIAL_LAKE;
    }

    public static boolean isMountains(int value) {
        return value == MOUNTAINS || value == OCEANIC_MOUNTAINS || value == OLD_MOUNTAINS || value == VOLCANIC_MOUNTAINS || value == VOLCANIC_OCEANIC_MOUNTAINS;
    }

    public static boolean isLow(int value) {
        return value == PLAINS || value == HILLS || value == LOW_CANYONS || value == LOWLANDS || value == SALT_MARSH || value == MUD_FLATS || value == SALT_FLATS || value == DUNE_SEA;
    }

    public static boolean hasShore(int value) {
        return value != LOW_CANYONS && value != CANYONS && value != OCEANIC_MOUNTAINS && value != VOLCANIC_OCEANIC_MOUNTAINS
                && value != TOWER_KARST_BAY //&& value != SUNKEN_SHIELD_VOLCANO && value != GLACIALLY_CARVED_OCEANIC_MOUNTAINS && value != GLACIATED_OCEANIC_MOUNTAINS && value != ICE_SHEET_OCEANIC_MOUNTAINS_EDGE
                //&& value != ICE_SHEET_SHIELD_VOLCANO && value != GLACIATED_SHIELD_VOLCANO
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
        //		if (value == ACTIVE_SHIELD_VOLCANO)
        //		{
        //			return SHIELD_VOLCANO_SHORE;
        //		}
        //		if (value == DORMANT_SHIELD_VOLCANO || value == EXTINCT_SHIELD_VOLCANO || value == ANCIENT_SHIELD_VOLCANO)
        //		{
        //			return OLD_SHIELD_VOLCANO_SHORE;
        //		}
        //		if (isFlatIceSheet(value) || value == ICE_SHEET_EDGE || value == ICE_SHEET_OCEANIC)
        //		{
        //			return ICE_SHEET_SHORE;
        //		}
        //		if (value == ICE_SHEET_OCEANIC_MOUNTAINS)
        //		{
        //			return ICE_SHEET_OCEANIC_MOUNTAINS_EDGE;
        //		}
        //		if (value == GLACIALLY_CARVED_OCEANIC_MOUNTAINS || value == GLACIALLY_CARVED_MOUNTAINS)
        //		{
        //			return GLACIATED_OCEANIC_MOUNTAINS;
        //		}
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
        return (!isOcean(value) && value != BADLANDS /*&& value != ACTIVE_SHIELD_VOLCANO && value != DORMANT_SHIELD_VOLCANO
                                                     && value != EXTINCT_SHIELD_VOLCANO && value != ANCIENT_SHIELD_VOLCANO && value != ICE_SHEET_MOUNTAINS
                                                     && value != ICE_SHEET_MOUNTAINS_EDGE && value != ICE_SHEET_OCEANIC_MOUNTAINS && value != ICE_SHEET_OCEANIC_MOUNTAINS_EDGE
                                                     && value != ICE_SHEET_SHIELD_VOLCANO && value != ICE_SHEET_SHORE && value != GLACIATED_SHIELD_VOLCANO
                                                     && value != GLACIATED_MOUNTAINS && value != GLACIATED_OCEANIC_MOUNTAINS && value != GLACIALLY_CARVED_MOUNTAINS
                                                     && value != GLACIALLY_CARVED_OCEANIC_MOUNTAINS*/);
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
        //    		if (isFlatIceSheet(value))
        //    		{
        //    			return SUBGLACIAL_LAKE;
        //    		}
        //    		if (value == ICE_SHEET_EDGE)
        //    		{
        //    			return MELTWATER_LAKE;
        //    		}
        return LAKE;
    }

    public static AreaFactory createRegionBiomeLayer(RegionGenerator generator, long worldSeed) {
        Seed seed = Seed.of(worldSeed);
        final TypedAreaFactory<Region.Point> regionLayer = new RegionLayer(generator).apply(seed.next());

        AreaFactory mainLayer;

        mainLayer = RegionBiomeLayer.INSTANCE.apply(regionLayer);

        // Grid scale

        mainLayer = TFGRegionEdgeBiomeLayer.INSTANCE.apply(seed.next(), mainLayer);
        mainLayer = ZoomLayer.NORMAL.apply(seed.next(), mainLayer);

        // 4x4 Chunk Scale
        mainLayer = TFGShoreLayer.INSTANCE.apply(seed.next(), mainLayer);
        mainLayer = TFGMoreShoresLayer.INSTANCE.apply(seed.next(), mainLayer);
        // TODO
        //mainLayer = IceSheetEdgeLayer.INSTANCE.apply(seed.next(), mainLayer);
        mainLayer = ZoomLayer.NORMAL.apply(seed.next(), mainLayer);
        mainLayer = ZoomLayer.NORMAL.apply(seed.next(), mainLayer);

        // Chunk scale

        mainLayer = ZoomLayer.NORMAL.apply(seed.next(), mainLayer);
        mainLayer = ZoomLayer.NORMAL.apply(seed.next(), mainLayer);

        // Quart scale

        mainLayer = SmoothLayer.INSTANCE.apply(seed.next(), mainLayer);

        return mainLayer;
    }
}
