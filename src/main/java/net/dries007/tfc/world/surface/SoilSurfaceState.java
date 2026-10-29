/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.surface;

import java.util.List;

import com.google.common.collect.ImmutableList;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import org.jetbrains.annotations.NotNull;

public class SoilSurfaceState implements SurfaceState
{
    public static final Noise2D PATCH_NOISE = new OpenSimplex2D(18273952837592L).octaves(2).spread(0.04f);

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
			if (context.biome() == TFCBiomes.RIVER) {
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
		final ImmutableList<SurfaceState> regions = ImmutableList.of(
			SurfaceStates.SNOW,
			SurfaceStates.SNOW,
			transition(SurfaceStates.SNOW, dry),
			dry,
			transition(dry, SurfaceStates.COARSE_ARIDISOL),
			SurfaceStates.COARSE_ARIDISOL,
			transition(SurfaceStates.COARSE_ARIDISOL, soil(type, TFGSoilVariant.ARIDISOL)),
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
		return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : new SoilSurfaceState(regions);
	}

	public static SurfaceState buildSandOrGravel()
	{
		return new SoilSurfaceState(ImmutableList.of(
			SurfaceStates.SAND,
			transition(SurfaceStates.SAND, SurfaceStates.GRAVEL),
			SurfaceStates.GRAVEL,
			SurfaceStates.GRAVEL,
			SurfaceStates.GRAVEL,
			SurfaceStates.GRAVEL,
			SurfaceStates.GRAVEL,
			SurfaceStates.GRAVEL,
			SurfaceStates.GRAVEL
		));
	}

	public static SurfaceState buildVolcanicSurfaceType(SoilBlockType type, SurfaceState dry) {
		final ImmutableList<SurfaceState> regions = ImmutableList.of(
			SurfaceStates.SNOW,
			SurfaceStates.SNOW,
			transition(SurfaceStates.SNOW, dry),
			dry,
			transition(dry, SurfaceStates.COARSE_MOLLISOL),
			SurfaceStates.COARSE_MOLLISOL,
			// Intentionally aridisol here because the color against the grass looks better
			transition(SurfaceStates.COARSE_ARIDISOL, soil(type, TFGSoilVariant.MOLLISOL)),
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
		return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : new SoilSurfaceState(regions);
	}

	public static SurfaceState buildSiltySurfaceType(SoilBlockType type, SurfaceState dry)
	{
		final ImmutableList<SurfaceState> regions = ImmutableList.of(
			SurfaceStates.SNOW,
			SurfaceStates.SNOW,
			transition(SurfaceStates.SNOW, dry),
			dry,
			transition(dry, SurfaceStates.COARSE_FLUVISOL),
			SurfaceStates.COARSE_FLUVISOL,
			transition(SurfaceStates.COARSE_FLUVISOL, soil(type, TFGSoilVariant.FLUVISOL)),
			soil(type, TFGSoilVariant.FLUVISOL),
			soil(type, TFGSoilVariant.FLUVISOL),
			soil(type, TFGSoilVariant.FLUVISOL),
			soil(type, TFGSoilVariant.FLUVISOL),
			soil(type, TFGSoilVariant.FLUVISOL),
			soil(type, TFGSoilVariant.FLUVISOL),
			soil(type, TFGSoilVariant.FLUVISOL),
			soil(type, TFGSoilVariant.FLUVISOL),
			soil(type, TFGSoilVariant.FLUVISOL),
			soil(type, TFGSoilVariant.FLUVISOL),
			soil(type, TFGSoilVariant.FLUVISOL)
		);
		return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : new SoilSurfaceState(regions);
	}

	public static SurfaceState buildMidType(SoilBlockType type, SurfaceState dry) {
		final ImmutableList<SurfaceState> regions = ImmutableList.of(
			SurfaceStates.PACKED_ICE,
			blobTransition(SurfaceStates.PACKED_ICE, dry),
			dry,
			dry,
			transition(dry, SurfaceStates.COARSE_ARIDISOL),
			SurfaceStates.COARSE_ARIDISOL,
			transition(SurfaceStates.COARSE_ARIDISOL, soil(type, TFGSoilVariant.ARIDISOL)),
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
		return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : new SoilSurfaceState(regions);
	}

	public static SurfaceState buildVolcanicMidType(SoilBlockType type, SurfaceState dry) {
		final ImmutableList<SurfaceState> regions = ImmutableList.of(
			SurfaceStates.PACKED_ICE,
			blobTransition(SurfaceStates.PACKED_ICE, dry),
			dry,
			dry,
			transition(dry, SurfaceStates.COARSE_MOLLISOL),
			SurfaceStates.COARSE_MOLLISOL,
			// Intentionally aridisol here because the color against the grass looks better
			transition(SurfaceStates.COARSE_ARIDISOL, soil(type, TFGSoilVariant.MOLLISOL)),
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
		return type == SoilBlockType.GRASS ? new NeedsPostProcessingSoilSurfaceState(regions) : new SoilSurfaceState(regions);
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
		return new SoilSurfaceState(regions);
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
		return new SoilSurfaceState(regions);
	}

	private final List<SurfaceState> regions;

	private SoilSurfaceState(List<SurfaceState> regions) {
		this.regions = regions;
	}

	@Override
	public @NotNull BlockState getState(SurfaceBuilderContext context) {
		final float rainfall = context.rainfall();
		final float temperature = Helpers.adjustAverageTemperatureByElevation(context.pos().getY(), context.averageTemperature(), context.getSeaLevel());

		final BlockPos pos = context.pos();
		final float noise = (float) PATCH_NOISE.noise(pos.getX(), pos.getZ());
		final float dither = (Helpers.hash(729375982L, context.pos()) & 127) * (1f / 64) - 1;

		// Rain-controlled surface: <64 pure gravel, <91 mixed gravel/dirt, <118 dirt, <145 mixed dirt/grass, otherwise grass
		final int rainIndex = (int) Mth.clampedMap(rainfall + 15 * noise + 10 * dither, 35, 450, 3, regions.size() - 0.01f);

		// Temperature-controlled surface: <-17.4 pure snow, <-16.6 mixed gravel/snow <-15.7 pure gravel, <-15 mixed gravel/dirt, <14.1, <-13.2 mixed dirt/grass, otherwise grass
		// -17c = Koppen EF/ET Border
		// -12c = Koppen ET Border
		final int tempIndex = (int) Mth.clampedMap(temperature, -19 + 0.5 * noise + 0.5 * dither, -4, 0, regions.size() - 0.01f);

		return regions.get(Math.min(rainIndex, tempIndex)).getState(context);
	}

	static class NeedsPostProcessingSoilSurfaceState extends SoilSurfaceState {

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
