package su.terrafirmagreg.core.world.new_ow_wg;

import java.util.function.Supplier;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TFGComplexSurfaceStates {
    private static TFGComplexSurfaceStates instance = null;

    public static TFGComplexSurfaceStates INSTANCE() {
        if (instance == null) {
            instance = new TFGComplexSurfaceStates();
        }
        return instance;
    }

    public final SurfaceState TOP_GRASS_TO_SAND;
    public final SurfaceState MID_DIRT_TO_SAND;
    public final SurfaceState VOLCANIC_TOP_GRASS_TO_LOCAL_GRAVEL;
    public final SurfaceState VOLCANIC_MID_DIRT_TO_LOCAL_GRAVEL;

    public final SurfaceState UNDER_GRAVEL;

    /**
     * Similar to rare shore sand, but forces volcanic types and green sand is rarer
     */
    public final SurfaceState VOLCANIC_SHORE_SAND;

    private TFGComplexSurfaceStates() {

        TFGSimpleSurfaceStates simpleStates = TFGSimpleSurfaceStates.INSTANCE();

        TOP_GRASS_TO_SAND = TFGSoilSurfaceState.buildSurfaceType(SoilBlockType.GRASS, simpleStates.SAND);
        MID_DIRT_TO_SAND = TFGSoilSurfaceState.buildMidType(SoilBlockType.DIRT, simpleStates.SAND);
        VOLCANIC_TOP_GRASS_TO_LOCAL_GRAVEL = TFGSoilSurfaceState.buildVolcanicSurfaceType(SoilBlockType.GRASS, SurfaceStates.GRAVEL);
        VOLCANIC_MID_DIRT_TO_LOCAL_GRAVEL = TFGSoilSurfaceState.buildVolcanicMidType(SoilBlockType.DIRT, SurfaceStates.GRAVEL);

        UNDER_GRAVEL = TFGSoilSurfaceState.buildUnderType();

        VOLCANIC_SHORE_SAND = new SurfaceState() {
            private final Supplier<Block> greenSand = TFCBlocks.SAND.get(SandBlockType.GREEN);
            private final Supplier<Block> blackSand = TFCBlocks.SAND.get(SandBlockType.BLACK);

            @Override
            public BlockState getState(SurfaceBuilderContext context) {
                if (context.rainfall() > 420f) {
                    return greenSand.get().defaultBlockState();
                }
                return blackSand.get().defaultBlockState();
            }
        };
    }
}
