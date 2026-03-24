/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
import su.terrafirmagreg.core.world.new_ow_wg.layers.TFGIceSheetEdgeLayer;
import su.terrafirmagreg.core.world.new_ow_wg.layers.TFGMoreShoresLayer;
import su.terrafirmagreg.core.world.new_ow_wg.layers.TFGRegionEdgeBiomeLayer;
import su.terrafirmagreg.core.world.new_ow_wg.layers.TFGShoreLayer;

public class TFGLayers {

    public static void init() {
    }

    private static final List<Supplier<BiomeExtension>> BIOME_LAYERS;

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

    static {
        BIOME_LAYERS = new ArrayList<>(128);

        DEEP_OCEAN_TRENCH = register(() -> TFGBiomes.DEEP_OCEAN_TRENCH);
        DEEP_OCEAN = register(() -> TFGBiomes.DEEP_OCEAN);
        OCEAN = register(() -> TFGBiomes.OCEAN);
        OCEAN_REEF = register(() -> TFGBiomes.OCEAN_REEF);

        PLAINS = register(() -> TFGBiomes.PLAINS);
        HILLS = register(() -> TFGBiomes.HILLS);
        LOWLANDS = register(() -> TFGBiomes.LOWLANDS);
        SALT_MARSH = register(() -> TFGBiomes.SALT_MARSH);
        LOW_CANYONS = register(() -> TFGBiomes.LOW_CANYONS);

        ROLLING_HILLS = register(() -> TFGBiomes.ROLLING_HILLS);
        HIGHLANDS = register(() -> TFGBiomes.HIGHLANDS);
        BADLANDS = register(() -> TFGBiomes.BADLANDS);
        PLATEAU = register(() -> TFGBiomes.PLATEAU);
        PLATEAU_WIDE = register(() -> TFGBiomes.PLATEAU_WIDE);
        CANYONS = register(() -> TFGBiomes.CANYONS);

        MOUNTAINS = register(() -> TFGBiomes.MOUNTAINS);
        OLD_MOUNTAINS = register(() -> TFGBiomes.OLD_MOUNTAINS);
        OCEANIC_MOUNTAINS = register(() -> TFGBiomes.OCEANIC_MOUNTAINS);
        VOLCANIC_MOUNTAINS = register(() -> TFGBiomes.VOLCANIC_MOUNTAINS);
        VOLCANIC_OCEANIC_MOUNTAINS = register(() -> TFGBiomes.VOLCANIC_OCEANIC_MOUNTAINS);

        GUANO_ISLAND = register(() -> TFGBiomes.GUANO_ISLAND);
        SHORE = register(() -> TFGBiomes.SHORE);
        TIDAL_FLATS = register(() -> TFGBiomes.TIDAL_FLATS);
        SEA_STACKS = register(() -> TFGBiomes.SEA_STACKS);
        TERRACE_UPPER = register(() -> TFGBiomes.TERRACE_UPPER);
        TERRACE_LOWER = register(() -> TFGBiomes.TERRACE_LOWER);
        SETBACK_CLIFFS = register(() -> TFGBiomes.SETBACK_CLIFFS);
        COASTAL_DUNES = register(() -> TFGBiomes.COASTAL_DUNES);
        ROCKY_SHORES = register(() -> TFGBiomes.ROCKY_SHORES);
        EMBAYMENTS = register(() -> TFGBiomes.EMBAYMENTS);

        LAKE = register(() -> TFGBiomes.LAKE);
        RIVER = register(() -> TFGBiomes.RIVER);

        MOUNTAIN_LAKE = register(() -> TFGBiomes.MOUNTAIN_LAKE);
        OLD_MOUNTAIN_LAKE = register(() -> TFGBiomes.OLD_MOUNTAIN_LAKE);
        OCEANIC_MOUNTAIN_LAKE = register(() -> TFGBiomes.OCEANIC_MOUNTAIN_LAKE);
        VOLCANIC_MOUNTAIN_LAKE = register(() -> TFGBiomes.VOLCANIC_MOUNTAIN_LAKE);
        VOLCANIC_OCEANIC_MOUNTAIN_LAKE = register(() -> TFGBiomes.VOLCANIC_OCEANIC_MOUNTAIN_LAKE);
        PLATEAU_LAKE = register(() -> TFGBiomes.PLATEAU_LAKE);

        MUD_FLATS = register(() -> TFGBiomes.MUD_FLATS);
        SALT_FLATS = register(() -> TFGBiomes.SALT_FLATS);
        DUNE_SEA = register(() -> TFGBiomes.DUNE_SEA);
        GRASSY_DUNES = register(() -> TFGBiomes.GRASSY_DUNES);
        WHORLED_CANYONS = register(() -> TFGBiomes.WHORLED_CANYONS);
        STAIR_STEP_CANYONS = register(() -> TFGBiomes.STAIR_STEP_CANYONS);
        MESAS = register(() -> TFGBiomes.MESAS);
        BUTTES = register(() -> TFGBiomes.BUTTES);
        HOODOOS = register(() -> TFGBiomes.HOODOOS);
        ROCKY_PLATEAU = register(() -> TFGBiomes.ROCKY_PLATEAU);

        TOWER_KARST_PLAINS = register(() -> TFGBiomes.TOWER_KARST_PLAINS);
        TOWER_KARST_CANYONS = register(() -> TFGBiomes.TOWER_KARST_CANYONS);
        TOWER_KARST_HILLS = register(() -> TFGBiomes.TOWER_KARST_HILLS);
        TOWER_KARST_HIGHLANDS = register(() -> TFGBiomes.TOWER_KARST_HIGHLANDS);
        TOWER_KARST_LAKE = register(() -> TFGBiomes.TOWER_KARST_LAKE);
        TOWER_KARST_BAY = register(() -> TFGBiomes.TOWER_KARST_BAY);

        BURREN_PLATEAU = register(() -> TFGBiomes.BURREN_PLATEAU);
        BURREN_BADLANDS = register(() -> TFGBiomes.BURREN_BADLANDS);
        BURREN_BADLANDS_TALL = register(() -> TFGBiomes.BURREN_BADLANDS_TALL);
        BURREN_PLAINS = register(() -> TFGBiomes.BURREN_PLAINS);
        BURREN_ROCHE_MOUTONEE = register(() -> TFGBiomes.BURREN_ROCHE_MOUTONEE);

        SHILIN_PLAINS = register(() -> TFGBiomes.SHILIN_PLAINS);
        SHILIN_CANYONS = register(() -> TFGBiomes.SHILIN_CANYONS);
        SHILIN_HILLS = register(() -> TFGBiomes.SHILIN_HILLS);
        SHILIN_HIGHLANDS = register(() -> TFGBiomes.SHILIN_HIGHLANDS);
        SHILIN_PLATEAU = register(() -> TFGBiomes.SHILIN_PLATEAU);

        DOLINE_PLAINS = register(() -> TFGBiomes.DOLINE_PLAINS);
        DOLINE_HILLS = register(() -> TFGBiomes.DOLINE_HILLS);
        DOLINE_ROLLING_HILLS = register(() -> TFGBiomes.DOLINE_ROLLING_HILLS);
        DOLINE_HIGHLANDS = register(() -> TFGBiomes.DOLINE_HIGHLANDS);
        DOLINE_PLATEAU = register(() -> TFGBiomes.DOLINE_PLATEAU);
        DOLINE_CANYONS = register(() -> TFGBiomes.DOLINE_CANYONS);

        CENOTE_PLAINS = register(() -> TFGBiomes.CENOTE_PLAINS);
        CENOTE_HILLS = register(() -> TFGBiomes.CENOTE_HILLS);
        CENOTE_ROLLING_HILLS = register(() -> TFGBiomes.CENOTE_ROLLING_HILLS);
        CENOTE_CANYONS = register(() -> TFGBiomes.CENOTE_CANYONS);
        CENOTE_HIGHLANDS = register(() -> TFGBiomes.CENOTE_HIGHLANDS);
        CENOTE_PLATEAU = register(() -> TFGBiomes.CENOTE_PLATEAU);

        EXTREME_DOLINE_PLATEAU = register(() -> TFGBiomes.EXTREME_DOLINE_PLATEAU);
        EXTREME_DOLINE_MOUNTAINS = register(() -> TFGBiomes.EXTREME_DOLINE_MOUNTAINS);

        ACTIVE_SHIELD_VOLCANO = register(() -> TFGBiomes.ACTIVE_SHIELD_VOLCANO);
        DORMANT_SHIELD_VOLCANO = register(() -> TFGBiomes.DORMANT_SHIELD_VOLCANO);
        EXTINCT_SHIELD_VOLCANO = register(() -> TFGBiomes.EXTINCT_SHIELD_VOLCANO);
        ANCIENT_SHIELD_VOLCANO = register(() -> TFGBiomes.ANCIENT_SHIELD_VOLCANO);
        SUNKEN_SHIELD_VOLCANO = register(() -> TFGBiomes.SUNKEN_SHIELD_VOLCANO);

        SHIELD_VOLCANO_SHORE = register(() -> TFGBiomes.SHIELD_VOLCANO_SHORE);
        OLD_SHIELD_VOLCANO_SHORE = register(() -> TFGBiomes.OLD_SHIELD_VOLCANO_SHORE);

        ICE_SHEET = register(() -> TFGBiomes.ICE_SHEET);
        ICE_SHEET_MOUNTAINS = register(() -> TFGBiomes.ICE_SHEET_MOUNTAINS);
        ICE_SHEET_OCEANIC_MOUNTAINS = register(() -> TFGBiomes.ICE_SHEET_OCEANIC_MOUNTAINS);
        ICE_SHEET_SHIELD_VOLCANO = register(() -> TFGBiomes.ICE_SHEET_SHIELD_VOLCANO);
        ICE_SHEET_TUYAS = register(() -> TFGBiomes.ICE_SHEET_TUYAS);
        SUBGLACIAL_LAKE = register(() -> TFGBiomes.SUBGLACIAL_LAKE);

        ICE_SHEET_EDGE = register(() -> TFGBiomes.ICE_SHEET_EDGE);
        ICE_SHEET_TUYAS_EDGE = register(() -> TFGBiomes.ICE_SHEET_TUYAS_EDGE);
        ICE_SHEET_MOUNTAINS_EDGE = register(() -> TFGBiomes.ICE_SHEET_MOUNTAINS_EDGE);
        ICE_SHEET_OCEANIC_MOUNTAINS_EDGE = register(() -> TFGBiomes.ICE_SHEET_OCEANIC_MOUNTAINS_EDGE);
        MELTWATER_LAKE = register(() -> TFGBiomes.MELTWATER_LAKE);
        ICE_SHEET_OCEANIC = register(() -> TFGBiomes.ICE_SHEET_OCEANIC);
        ICE_SHEET_SHORE = register(() -> TFGBiomes.ICE_SHEET_SHORE);

        GLACIATED_MOUNTAINS = register(() -> TFGBiomes.GLACIATED_MOUNTAINS);
        GLACIATED_OCEANIC_MOUNTAINS = register(() -> TFGBiomes.GLACIATED_OCEANIC_MOUNTAINS);
        GLACIATED_SHIELD_VOLCANO = register(() -> TFGBiomes.GLACIATED_SHIELD_VOLCANO);

        GLACIALLY_CARVED_MOUNTAINS = register(() -> TFGBiomes.GLACIALLY_CARVED_MOUNTAINS);
        GLACIALLY_CARVED_OCEANIC_MOUNTAINS = register(() -> TFGBiomes.GLACIALLY_CARVED_OCEANIC_MOUNTAINS);

        DRUMLINS = register(() -> TFGBiomes.DRUMLINS);
        TUYAS = register(() -> TFGBiomes.TUYAS);

        KNOB_AND_KETTLE = register(() -> TFGBiomes.KNOB_AND_KETTLE);
        PATTERNED_GROUND = register(() -> TFGBiomes.PATTERNED_GROUND);
        INVERTED_PATTERNED_GROUND = register(() -> TFGBiomes.INVERTED_PATTERNED_GROUND);
        STONE_CIRCLES = register(() -> TFGBiomes.STONE_CIRCLES);
    }

    public static BiomeExtension getFromLayerId(int id) {
        BiomeExtension v = BIOME_LAYERS.get(id).get();
        if (v == null) {
            throw new NullPointerException("Layer id = " + id + " returned null!");
        } else {
            return v;
        }
    }

    public static int register(Supplier<BiomeExtension> biome) {
        BIOME_LAYERS.add(Helpers.BOOTSTRAP_ENVIRONMENT ? null : biome);
        return BIOME_LAYERS.size() - 1;
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
        mainLayer = TFGIceSheetEdgeLayer.INSTANCE.apply(seed.next(), mainLayer);
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
