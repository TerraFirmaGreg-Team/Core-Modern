package su.terrafirmagreg.core.world.new_ow_wg.region;

import net.dries007.tfc.world.region.*;

public enum TFGRegionTask {
    INIT(TFGInitTask.INSTANCE), // OK
    ADD_CONTINENTS(TFGAddContinents.INSTANCE), // OK
    ANNOTATE_DISTANCE_TO_CELL_EDGE(AnnotateDistanceToCellEdge.INSTANCE), // OK
    FLOOD_FILL_SMALL_OCEANS(FloodFillSmallOceans.INSTANCE), // OK
    ADD_ISLANDS(TFGAddIslands.INSTANCE), // OK
    //ADD_HOTSPOTS(TFGAddHotspots.INSTANCE), // LOOPS
    //ANNOTATE_DISTANCE_TO_OCEAN(TFGAnnotateDistanceToOcean.INSTANCE), // ERRORS
    ANNOTATE_BASE_LAND_HEIGHT(AnnotateBaseLandHeight.INSTANCE), // OK
    ANNOTATE_DISTANCE_TO_WEST_COAST(TFGAnnotateDistanceToWestCoast.INSTANCE), // OK
    ADD_MOUNTAINS(AddMountains.INSTANCE), // OK
    ANNOTATE_BIOME_ALTITUDE(AnnotateBiomeAltitude.INSTANCE), // OK
    ANNOTATE_CLIMATE(TFGAnnotateClimate.INSTANCE), // OK
    CHOOSE_ROCKS(ChooseRocks.INSTANCE), // OK
    ANNOTATE_KARST_SURFACE(TFGKarstSurfaceRocks.INSTANCE), // OK
    // TODO: Change to our own biomes task
    CHOOSE_BIOMES(ChooseBiomes.INSTANCE), // OK
    ADD_RIVERS_AND_LAKES(TFGAddRiversAndLakes.INSTANCE);

    public static final TFGRegionTask[] VALUES = values();

    public final RegionTask task;

    TFGRegionTask(RegionTask task) {
        this.task = task;
    }
}
