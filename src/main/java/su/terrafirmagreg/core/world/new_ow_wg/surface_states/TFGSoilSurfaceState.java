/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.surface_states;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.surface.SoilSurfaceState;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.world.new_ow_wg.DirtHelpers;
import su.terrafirmagreg.core.world.new_ow_wg.TFGSoilVariant;
import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGNoiseHelpers;

public class TFGSoilSurfaceState implements SurfaceState {

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

    public static SurfaceState soil(SoilBlockType type, TFGSoilVariant variant) {
        final Block block = DirtHelpers.getBlock(type, variant);
        return context -> block.defaultBlockState();
    }

    public static SurfaceState transitioningSoil(SoilBlockType type, TFGSoilVariant soil) {
        return transitioningSoil(type, soil, TFGSoilVariant.OXISOL, 16f, 16.7f);
    }

    public static SurfaceState transitioningSoil(SoilBlockType blockType, TFGSoilVariant coldSoilType, TFGSoilVariant hotSoilType, float transitionStartTemp, float transitionEndTemp) {
        return context -> {
            // First, check if near a "flooding" river, and place silt if so
            if (context.biome() == TFGBiomes.RIVER) {
                return TFCBlocks.SOIL.get(blockType).get(SoilBlockType.Variant.SILT).get().defaultBlockState();
            }
            // Then run through the temperature calculations
            final float temp = context.averageTemperature();
            final BlockState coldBlock = DirtHelpers.getBlock(blockType, coldSoilType).defaultBlockState();
            if (temp < transitionStartTemp) {
                return coldBlock;
            }
            final BlockState hotBlock = DirtHelpers.getBlock(blockType, hotSoilType).defaultBlockState();
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
                transition(dry, states.COARSE_ARIDISOL),
                states.COARSE_ARIDISOL,
                transition(states.COARSE_ARIDISOL, soil(type, TFGSoilVariant.ARIDISOL)),
                soil(type, TFGSoilVariant.ARIDISOL),
                blobTransition(soil(type, TFGSoilVariant.ARIDISOL), soil(type, TFGSoilVariant.ENTISOL)),
                soil(type, TFGSoilVariant.ENTISOL),
                soil(type, TFGSoilVariant.ENTISOL),
                blobTransition(soil(type, TFGSoilVariant.ENTISOL), transitioningSoil(type, TFGSoilVariant.ANDISOL)),
                transitioningSoil(type, TFGSoilVariant.ANDISOL),
                transitioningSoil(type, TFGSoilVariant.ANDISOL),
                blobTransition(soil(type, TFGSoilVariant.ANDISOL), transitioningSoil(type, TFGSoilVariant.FLUVISOL)),
                transitioningSoil(type, TFGSoilVariant.FLUVISOL),
                transitioningSoil(type, TFGSoilVariant.FLUVISOL),
                transitioningSoil(type, TFGSoilVariant.FLUVISOL));
        return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : new TFGSoilSurfaceState(regions);
    }

    public static SurfaceState buildVolcanicSurfaceType(SoilBlockType type, SurfaceState dry) {
        var states = TFGSimpleSurfaceStates.INSTANCE();
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
                states.SNOW,
                states.SNOW,
                transition(states.SNOW, dry),
                dry,
                transition(dry, states.COARSE_MOLLISOL),
                states.COARSE_MOLLISOL,
                // Intentionally aridisol here because the color against the grass looks better
                transition(states.COARSE_ARIDISOL, soil(type, TFGSoilVariant.MOLLISOL)),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL));
        return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : new TFGSoilSurfaceState(regions);
    }

    public static SurfaceState buildMidType(SoilBlockType type, SurfaceState dry) {
        var states = TFGSimpleSurfaceStates.INSTANCE();
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
                states.PACKED_ICE,
                blobTransition(states.PACKED_ICE, dry),
                dry,
                dry,
                transition(dry, states.COARSE_ARIDISOL),
                states.COARSE_ARIDISOL,
                transition(states.COARSE_ARIDISOL, soil(type, TFGSoilVariant.ARIDISOL)),
                soil(type, TFGSoilVariant.ARIDISOL),
                blobTransition(soil(type, TFGSoilVariant.ARIDISOL), soil(type, TFGSoilVariant.ENTISOL)),
                soil(type, TFGSoilVariant.ENTISOL),
                soil(type, TFGSoilVariant.ENTISOL),
                blobTransition(soil(type, TFGSoilVariant.ENTISOL), transitioningSoil(type, TFGSoilVariant.ANDISOL)),
                transitioningSoil(type, TFGSoilVariant.ANDISOL),
                transitioningSoil(type, TFGSoilVariant.ANDISOL),
                blobTransition(soil(type, TFGSoilVariant.ANDISOL), transitioningSoil(type, TFGSoilVariant.FLUVISOL)),
                transitioningSoil(type, TFGSoilVariant.FLUVISOL),
                transitioningSoil(type, TFGSoilVariant.FLUVISOL),
                transitioningSoil(type, TFGSoilVariant.FLUVISOL));
        return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : new TFGSoilSurfaceState(regions);
    }

    public static SurfaceState buildVolcanicMidType(SoilBlockType type, SurfaceState dry) {
        var states = TFGSimpleSurfaceStates.INSTANCE();
        final ImmutableList<SurfaceState> regions = ImmutableList.of(
                states.PACKED_ICE,
                blobTransition(states.PACKED_ICE, dry),
                dry,
                dry,
                transition(dry, states.COARSE_MOLLISOL),
                states.COARSE_MOLLISOL,
                // Intentionally aridisol here because the color against the grass looks better
                transition(states.COARSE_ARIDISOL, soil(type, TFGSoilVariant.MOLLISOL)),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL),
                soil(type, TFGSoilVariant.MOLLISOL));
        return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : new TFGSoilSurfaceState(regions);
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
        return new TFGSoilSurfaceState(regions);
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
        return new TFGSoilSurfaceState(regions);
    }

    private final List<SurfaceState> m_regions;

    private TFGSoilSurfaceState(List<SurfaceState> regions) {
        this.m_regions = regions;
    }

    @Override
    public @NotNull BlockState getState(SurfaceBuilderContext context) {
        final float rainfall = context.rainfall();
        final float temperature = TFGNoiseHelpers.adjustAverageTemperatureByElevation(context.pos().getY(), context.averageTemperature(), context.getSeaLevel());

        // Rain-controlled surface: <64 pure gravel, <91 mixed gravel/dirt, <118 dirt, <145 mixed dirt/grass, otherwise grass
        final int rainIndex = (int) Mth.clampedMap(rainfall, 35, 450, 3, m_regions.size() - 0.01f);

        // Temperature-controlled surface: <-17.4 pure snow, <-16.6 mixed gravel/snow <-15.7 pure gravel, <-15 mixed gravel/dirt, <14.1, <-13.2 mixed dirt/grass, otherwise grass
        // -17c = Koppen EF/ET Border
        // -12c = Koppen ET Border
        final int tempIndex = (int) Mth.clampedMap(temperature, -19, -4, 0, m_regions.size() - 0.01f);

        return m_regions.get(Math.min(rainIndex, tempIndex)).getState(context);
    }

    static class NeedsPostProcessingSoilSurfaceState extends TFGSoilSurfaceState {

        private NeedsPostProcessingSoilSurfaceState(List<SurfaceState> regions) {
            super(regions);
        }

        @Override
        public void setState(SurfaceBuilderContext context) {
            context.chunk().setBlockState(context.pos(), getState(context), false);
            context.chunk().markPosForPostprocessing(context.pos());
        }
    }

}
