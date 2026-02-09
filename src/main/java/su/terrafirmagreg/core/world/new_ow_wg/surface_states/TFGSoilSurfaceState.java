package su.terrafirmagreg.core.world.new_ow_wg.surface_states;

import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.surface.SoilSurfaceState;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg.SoilSurfaceStateAccessor;

public class TFGSoilSurfaceState {

    private static SurfaceState transition(SurfaceState first, SurfaceState second) {
        return context -> (Helpers.hash(729375982L, context.pos()) & 127) > 63 ? first.getState(context) : second.getState(context);
    }

    private static SurfaceState blobTransition(SurfaceState first, SurfaceState second) {
        return context -> {
            final BlockPos pos = context.pos();
            final double noise = SoilSurfaceState.PATCH_NOISE.noise(pos.getX(), pos.getZ());
            return noise > 0 ? first.getState(context) : second.getState(context);
        };
    }

    public static SurfaceState soil(SoilBlockType type, SoilBlockType.Variant variant) {
        final Supplier<Block> block = TFCBlocks.SOIL.get(type).get(variant);
        return context -> block.get().defaultBlockState();
    }

    public static SurfaceState transitioningSoil(SoilBlockType type) {
        // second argument is normally Oxisol
        return transitioningSoil(type, SoilBlockType.Variant.LOAM, SoilBlockType.Variant.SILTY_LOAM, 16f, 16.7f);
    }

    public static SurfaceState transitioningSoil(SoilBlockType blockType, SoilBlockType.Variant coldSoilType, SoilBlockType.Variant hotSoilType, float transitionStartTemp, float transitionEndTemp) {
        return context -> {
            // First, check if near a "flooding" river, and place silt if so
            if (context.rainfall() > 200) {
                return TFCBlocks.SOIL.get(blockType).get(SoilBlockType.Variant.SILT).get().defaultBlockState();
            }
            // Then run through the temperature calculations
            final float temp = context.averageTemperature();
            final BlockState coldBlock = TFCBlocks.SOIL.get(blockType).get(coldSoilType).get().defaultBlockState();
            if (temp < transitionStartTemp) {
                return coldBlock;
            }
            final BlockState hotBlock = TFCBlocks.SOIL.get(blockType).get(hotSoilType).get().defaultBlockState();
            if (temp > transitionEndTemp) {
                return hotBlock;
            }
            final BlockPos pos = context.pos();
            final double noise = SoilSurfaceState.PATCH_NOISE.noise(pos.getX(), pos.getZ());
            return noise > 0 ? hotBlock : coldBlock;
        };
    }

    public static SurfaceState buildSurfaceType(SoilBlockType type, SurfaceState dry) {
        var states = TFGSimpleSurfaceStates.INSTANCE();
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
                states.SNOW,
                states.SNOW,
                transition(states.SNOW, dry),
                dry,
                transition(dry, states.COARSE_ARIDISOL_BASE),
                states.COARSE_ARIDISOL_BASE,
                transition(states.COARSE_ARIDISOL_BASE, soil(type, SoilBlockType.Variant.SANDY_LOAM)),
                soil(type, SoilBlockType.Variant.SANDY_LOAM),
                blobTransition(soil(type, SoilBlockType.Variant.SANDY_LOAM), transitioningSoil(type)),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type));
        return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : SoilSurfaceStateAccessor.newSoilSurfaceState(regions);
    }

    public static SurfaceState buildVolcanicSurfaceType(SoilBlockType type, SurfaceState dry) {
        var states = TFGSimpleSurfaceStates.INSTANCE();
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
                states.SNOW,
                states.SNOW,
                transition(states.SNOW, dry),
                dry,
                transition(dry, states.COARSE_ANDISOL_BASE),
                states.COARSE_ANDISOL_BASE,
                transition(states.COARSE_ANDISOL_BASE, soil(type, SoilBlockType.Variant.SILTY_LOAM)),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM));
        return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : SoilSurfaceStateAccessor.newSoilSurfaceState(regions);
    }

    public static SurfaceState buildMidType(SoilBlockType type, SurfaceState dry) {
        var states = TFGSimpleSurfaceStates.INSTANCE();
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
                states.PACKED_ICE,
                blobTransition(states.PACKED_ICE, dry),
                dry,
                dry,
                transition(dry, states.COARSE_ARIDISOL_BASE),
                states.COARSE_ARIDISOL_BASE,
                transition(states.COARSE_ARIDISOL_BASE, soil(type, SoilBlockType.Variant.SANDY_LOAM)),
                soil(type, SoilBlockType.Variant.SANDY_LOAM),
                blobTransition(soil(type, SoilBlockType.Variant.SANDY_LOAM), transitioningSoil(type)),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type),
                transitioningSoil(type));
        return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : SoilSurfaceStateAccessor.newSoilSurfaceState(regions);
    }

    public static SurfaceState buildVolcanicMidType(SoilBlockType type, SurfaceState dry) {
        var states = TFGSimpleSurfaceStates.INSTANCE();
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
                states.PACKED_ICE,
                blobTransition(states.PACKED_ICE, dry),
                dry,
                dry,
                transition(dry, states.COARSE_ANDISOL_BASE),
                states.COARSE_ANDISOL_BASE,
                transition(states.COARSE_ANDISOL_BASE, soil(type, SoilBlockType.Variant.SILTY_LOAM)),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM),
                soil(type, SoilBlockType.Variant.SILTY_LOAM));
        return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : SoilSurfaceStateAccessor.newSoilSurfaceState(regions);
    }

    public static SurfaceState buildSnowableSurface(SurfaceState snow, SurfaceState typical) {
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
                snow,
                snow,
                transition(snow, typical),
                typical,
                typical,
                typical,
                typical,
                typical,
                typical,
                typical,
                typical,
                typical,
                typical,
                typical,
                typical,
                typical,
                typical,
                typical);
        return SoilSurfaceStateAccessor.newSoilSurfaceState(regions);
    }

    public static SurfaceState buildUnderType() {
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
                SurfaceStates.RAW,
                SurfaceStates.RAW,
                blobTransition(SurfaceStates.RAW, SurfaceStates.GRAVEL),
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL,
                SurfaceStates.GRAVEL);
        return SoilSurfaceStateAccessor.newSoilSurfaceState(regions);
    }
}
