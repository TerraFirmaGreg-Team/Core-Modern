package su.terrafirmagreg.core.world.new_ow_wg;

import net.dries007.tfc.world.surface.SurfaceState;
import net.minecraft.world.level.block.Blocks;

import su.terrafirmagreg.core.common.data.TFGBlocks;

public class TFGSimpleSurfaceStates {

    private static TFGSimpleSurfaceStates instance = null;

    public static TFGSimpleSurfaceStates INSTANCE() {
        if (instance == null) {
            instance = new TFGSimpleSurfaceStates();
        }
        return instance;
    }

    public final SurfaceState SAND;

    public final SurfaceState TUFF;
    public final SurfaceState TUFF_GRAVEL;

    public final SurfaceState BLUE_ICE;
    public final SurfaceState PACKED_ICE;
    public final SurfaceState SNOW;

    public final SurfaceState COARSE_ARIDISOL_BASE;
    public final SurfaceState COARSE_ANDISOL_BASE;
    public final SurfaceState DRY_MUD;
    public final SurfaceState SALTED_EARTH;

    /**
     * Snowy surface builders - used when a SurfaceState should be replaced by snow blocks in the appropriate climate
     */

    public final SurfaceState SNOWY_SAND;

    private TFGSimpleSurfaceStates() {
        SAND = context -> context.getRock().sand().defaultBlockState();

        BLUE_ICE = context -> Blocks.BLUE_ICE.defaultBlockState();
        PACKED_ICE = context -> Blocks.PACKED_ICE.defaultBlockState();
        SNOW = context -> Blocks.SNOW_BLOCK.defaultBlockState();
        SNOWY_SAND = TFGSoilSurfaceState.buildSnowableSurface(SNOW, SAND);

        TUFF = context -> Blocks.TUFF.defaultBlockState();
        TUFF_GRAVEL = context -> TFGBlocks.TUFF_GRAVEL.get().defaultBlockState();

        COARSE_ARIDISOL_BASE = context -> TFGBlocks.COARSE_SANDY_LOAM_DIRT.get().defaultBlockState();
        COARSE_ANDISOL_BASE = context -> TFGBlocks.COARSE_SILTY_LOAM_DIRT.get().defaultBlockState();
        DRY_MUD = context -> TFGBlocks.HARDENED_CLAY.get().defaultBlockState();
        SALTED_EARTH = context -> TFGBlocks.HALITE.get().defaultBlockState();

    }
}
