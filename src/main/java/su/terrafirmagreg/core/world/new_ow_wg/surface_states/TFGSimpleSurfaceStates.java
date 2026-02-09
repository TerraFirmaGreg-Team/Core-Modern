package su.terrafirmagreg.core.world.new_ow_wg.surface_states;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
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
    public final SurfaceState SANDSTONE;

    public final SurfaceState BASALT;
    public final SurfaceState BASALT_COBBLE;
    public final SurfaceState BASALT_GRAVEL;
    public final SurfaceState BASALT_MORAINE;

    public final SurfaceState TUFF;
    public final SurfaceState TUFF_GRAVEL;

    public final SurfaceState MORAINE;
    public final SurfaceState SAND_AND_GRAVEL;

    public final SurfaceState BLUE_ICE;
    public final SurfaceState PACKED_ICE;
    public final SurfaceState SNOW;

    public final SurfaceState COARSE_ARIDISOL_BASE;
    public final SurfaceState COARSE_ANDISOL_BASE;
    public final SurfaceState DRY_MUD;
    public final SurfaceState SALTED_EARTH;

    public final SurfaceState OCEAN_MUD;

    // Snowy surface builders - used when a SurfaceState should be replaced by snow blocks in the appropriate climate
    public final SurfaceState SNOWY_RAW;
    public final SurfaceState SNOWY_COBBLE;
    public final SurfaceState SNOWY_GRAVEL;
    public final SurfaceState SNOWY_SAND;
    public final SurfaceState SNOWY_SANDSTONE;

    public final SurfaceState SNOWY_MORAINE;
    public final SurfaceState SNOWY_BASALT;
    public final SurfaceState SNOWY_BASALT_COBBLE;
    public final SurfaceState SNOWY_BASALT_GRAVEL;
    public final SurfaceState SNOWY_BASALT_MORAINE;
    public final SurfaceState SNOWY_SAND_AND_GRAVEL;

    private TFGSimpleSurfaceStates() {
        SAND = context -> context.getRock().sand().defaultBlockState();
        SANDSTONE = context -> context.getRock().sandstone().defaultBlockState();

        BASALT = context -> TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT).get(Rock.BlockType.RAW).get().defaultBlockState();
        BASALT_COBBLE = context -> TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT).get(Rock.BlockType.COBBLE).get().defaultBlockState();
        BASALT_GRAVEL = context -> TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT).get(Rock.BlockType.GRAVEL).get().defaultBlockState();
        BASALT_MORAINE = context -> (Helpers.hash(729375982L, context.pos()) & 127) > 96 ? TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT).get(Rock.BlockType.COBBLE).get().defaultBlockState()
                : TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT).get(Rock.BlockType.GRAVEL).get().defaultBlockState();

        TUFF = context -> Blocks.TUFF.defaultBlockState();
        TUFF_GRAVEL = context -> TFGBlocks.TUFF_GRAVEL.get().defaultBlockState();

        MORAINE = context -> (Helpers.hash(729375982L, context.pos()) & 127) > 96 ? context.getRock().cobble().defaultBlockState() : context.getRock().gravel().defaultBlockState();
        SAND_AND_GRAVEL = context -> (Helpers.hash(728275914L, context.pos()) & 127) > 48 ? context.getRock().sand().defaultBlockState() : context.getRock().gravel().defaultBlockState();

        BLUE_ICE = context -> Blocks.BLUE_ICE.defaultBlockState();
        PACKED_ICE = context -> Blocks.PACKED_ICE.defaultBlockState();
        SNOW = context -> Blocks.SNOW_BLOCK.defaultBlockState();

        SNOWY_RAW = TFGSoilSurfaceState.buildSnowableSurface(SNOW, SurfaceStates.RAW);
        SNOWY_COBBLE = TFGSoilSurfaceState.buildSnowableSurface(SNOW, SurfaceStates.COBBLE);
        SNOWY_GRAVEL = TFGSoilSurfaceState.buildSnowableSurface(SNOW, SurfaceStates.GRAVEL);
        SNOWY_SAND = TFGSoilSurfaceState.buildSnowableSurface(SNOW, SAND);
        SNOWY_SANDSTONE = TFGSoilSurfaceState.buildSnowableSurface(SNOW, SANDSTONE);

        SNOWY_MORAINE = TFGSoilSurfaceState.buildSnowableSurface(SNOW, MORAINE);
        SNOWY_BASALT = TFGSoilSurfaceState.buildSnowableSurface(SNOW, BASALT);
        SNOWY_BASALT_COBBLE = TFGSoilSurfaceState.buildSnowableSurface(SNOW, BASALT_COBBLE);
        SNOWY_BASALT_GRAVEL = TFGSoilSurfaceState.buildSnowableSurface(SNOW, BASALT_GRAVEL);
        SNOWY_BASALT_MORAINE = TFGSoilSurfaceState.buildSnowableSurface(SNOW, BASALT_MORAINE);
        SNOWY_SAND_AND_GRAVEL = TFGSoilSurfaceState.buildSnowableSurface(SNOW, SAND_AND_GRAVEL);

        COARSE_ARIDISOL_BASE = context -> TFGBlocks.COARSE_SANDY_LOAM_DIRT.get().defaultBlockState();
        COARSE_ANDISOL_BASE = context -> TFGBlocks.COARSE_SILTY_LOAM_DIRT.get().defaultBlockState();
        DRY_MUD = context -> TFGBlocks.HARDENED_CLAY.get().defaultBlockState();
        SALTED_EARTH = context -> TFGBlocks.HALITE.get().defaultBlockState();

        OCEAN_MUD = context -> TFCBlocks.SOIL.get(SoilBlockType.MUD).get(SoilBlockType.Variant.SILT).get().defaultBlockState();

    }
}
