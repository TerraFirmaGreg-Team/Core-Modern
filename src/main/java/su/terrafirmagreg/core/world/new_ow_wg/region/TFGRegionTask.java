/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.region;

import net.dries007.tfc.world.region.*;

public enum TFGRegionTask {
    INIT(TFGInitTask.INSTANCE),
    ADD_CONTINENTS(TFGAddContinents.INSTANCE),
    ANNOTATE_DISTANCE_TO_CELL_EDGE(AnnotateDistanceToCellEdge.INSTANCE),
    FLOOD_FILL_SMALL_OCEANS(FloodFillSmallOceans.INSTANCE),
    ADD_ISLANDS(TFGAddIslands.INSTANCE),
    ADD_HOTSPOTS(TFGAddHotspots.INSTANCE),
    ANNOTATE_DISTANCE_TO_OCEAN(TFGAnnotateDistanceToOcean.INSTANCE),
    ANNOTATE_BASE_LAND_HEIGHT(AnnotateBaseLandHeight.INSTANCE),
    ANNOTATE_DISTANCE_TO_WEST_COAST(TFGAnnotateDistanceToWestCoast.INSTANCE),
    ADD_MOUNTAINS(AddMountains.INSTANCE),
    ANNOTATE_BIOME_ALTITUDE(AnnotateBiomeAltitude.INSTANCE),
    ANNOTATE_CLIMATE(TFGAnnotateClimate.INSTANCE),
    CHOOSE_ROCKS(TFGChooseRocks.INSTANCE),
    ANNOTATE_KARST_SURFACE(TFGKarstSurfaceRocks.INSTANCE),
    CHOOSE_BIOMES(TFGChooseBiomesTask.INSTANCE),
    ADD_RIVERS_AND_LAKES(TFGAddRiversAndLakes.INSTANCE);

    public static final TFGRegionTask[] VALUES = values();

    public final RegionTask task;

    TFGRegionTask(RegionTask task) {
        this.task = task;
    }
}
