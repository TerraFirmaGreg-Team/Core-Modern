/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.surface;

import java.util.Set;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.RockData;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.settings.RockSettings;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.world.WorldgenData;

public class SurfaceBuilderContext
{
	private final LevelAccessor level;
	private final ChunkAccess chunk;
	@Getter
	private final ChunkData chunkData;
	private final RockData rockData;
	private final RandomSource random;
	@Getter
	private final int seaLevel;
	private final int minY;

	private final Set<BlockState> defaultBlockStates;
	private final Set<BlockState> defaultFluidStates;

	private final BlockPos.MutableBlockPos cursor;

	@Nullable private BiomeExtension biome;
	@Nullable private BiomeExtension originalBiome;
	private final BiomeExtension cinderConeBiome;
	private final BiomeExtension tuffRingBiome;
	private final BiomeExtension tuyaBiome;
	private final BiomeExtension atollBiome;
	private final BiomeExtension stratovolcanoBiome;
	private double biomeWeight;
	@Setter @Getter
	private double slope;
	@Setter @Getter
	private int preVolcanicHeight;
	private float temperature;
	private float rainfall;
	private boolean salty;

	// Mantle mountain stuff
	private static final int DITHER_AMPLITUDE = 5;
	private int mountainColumnSurfaceY = Integer.MIN_VALUE;
	private int mountainColumnX = Integer.MIN_VALUE;
	private int mountainColumnZ = Integer.MIN_VALUE;

	public SurfaceBuilderContext(LevelAccessor level, ChunkAccess chunk, ChunkData chunkData, RandomSource random, RockLayerSettings rockLayerSettings, int seaLevel, int minY, BiomeExtension cinderConeBiome, BiomeExtension tuffRingBiome, BiomeExtension tuyaBiome, BiomeExtension atollBiome, BiomeExtension stratovolcanoBiome)
	{
		this.level = level;
		this.chunk = chunk;
		this.chunkData = chunkData;
		this.rockData = chunkData.getRockData();
		this.random = random;
		this.seaLevel = seaLevel;
		this.minY = minY;
		this.cinderConeBiome = cinderConeBiome;
		this.tuffRingBiome = tuffRingBiome;
		this.tuyaBiome = tuyaBiome;
		this.atollBiome = atollBiome;
		this.stratovolcanoBiome = stratovolcanoBiome;

		this.defaultBlockStates = new ObjectOpenHashSet<>();
		this.defaultFluidStates = new ObjectOpenHashSet<>();

		this.cursor = new BlockPos.MutableBlockPos();

		for (RockSettings rock : rockLayerSettings.getRocks())
		{
			defaultBlockStates.add(rock.raw().defaultBlockState());
		}
		defaultFluidStates.add(Blocks.WATER.defaultBlockState());
	}

	public void buildSurface(BiomeExtension biome, BiomeExtension originalBiome, double biomeWeight, boolean salty, SurfaceBuilder builder, int x, int y, int z, double slope, int preVolcanicHeight)
	{
		this.biome = biome;
		this.originalBiome = originalBiome;
		this.biomeWeight = biomeWeight;
		this.slope = slope;
		this.preVolcanicHeight = preVolcanicHeight;
		this.temperature = chunkData.getAverageTemp(x, z);
		this.rainfall = chunkData.getRainfall(x, z);
		this.salty = salty;
		this.mountainColumnX = x;
		this.mountainColumnZ = z;
		this.mountainColumnSurfaceY = y;

		// We iterate down based on the actual surface height (since our capability for overhangs is much more limited than vanilla)
		final int oceanFloor = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
		final int actualMinSurfaceHeight = Math.max(minY, Math.min(y, oceanFloor) - 20); // Iterate down to at least the ocean floor and below

		cursor.set(x, 0, z);
		builder.buildSurface(this, y, actualMinSurfaceHeight);
	}

	public BiomeExtension biome()
	{
		assert biome != null;
		return biome;
	}

	/**
	 * Will never be a river
	 */
	public BiomeExtension originalBiome()
	{
		assert originalBiome != null;
		return originalBiome;
	}

	public BiomeExtension cinderConeBiome()
	{
		return cinderConeBiome;
	}

	public BiomeExtension tuffRingBiome()
	{
		return tuffRingBiome;
	}

	public BiomeExtension tuyaBiome()
	{
		return tuyaBiome;
	}

	public BiomeExtension atollBiome()
	{
		return atollBiome;
	}

	public BiomeExtension stratovolcanoBiome()
	{
		return stratovolcanoBiome;
	}

	public double weight()
	{
		return biomeWeight;
	}

	public BlockPos pos()
	{
		return cursor;
	}

	public RockSettings getRock()
	{
		return rockData.getRock(cursor.getX(), cursor.getY(), cursor.getZ());
	}

	public RockSettings getSeaLevelRock()
	{
		return rockData.getRock(cursor.getX(), seaLevel, cursor.getZ());
	}

	public RockSettings getApproxSecondRock()
	{
		return rockData.getRock(cursor.getX(), Math.max(cursor.getY() - 64, -64), cursor.getZ());
	}

	public RockSettings getBottomRock()
	{
		return rockData.getRock(cursor.getX(), -64, cursor.getZ());
	}

	public float averageTemperature()
	{
		if (mountainColumnSurfaceY <= seaLevel)
			return temperature;

		final var mountainScaling = WorldgenData.MOUNTAIN_SCALING;
		if (mountainScaling == null || mountainScaling == WorldgenData.MOUNTAIN_SCALING_NONE)
			return temperature;

		final int currentX = cursor.getX();
		final int currentZ = cursor.getZ();

		if (mountainColumnSurfaceY == Integer.MIN_VALUE || currentX != mountainColumnX || currentZ != mountainColumnZ)
			return temperature;

		final boolean snowCaps = TFGConfig.SERVER.snowCaps.get();
		if (!snowCaps)
			return temperature;

		long h = 0x534E4F57L
					 ^ ((long) currentX * 1610612741L)
					 ^ ((long) currentZ * 805306457L);
		h ^= (h >>> 33);
		h *= 0xff51afd7ed558ccdL;
		h ^= (h >>> 33);
		h *= 0xc4ceb9fe1a85ec53L;
		h ^= (h >>> 33);

		final double hashDither = ((double) (h & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL) * 2.0 - 1.0;
		final double snowLine = mountainScaling.snowLineY() + hashDither * DITHER_AMPLITUDE;

		if (mountainColumnSurfaceY >= snowLine) {
			return -25.0f;
		}
		else {
			return temperature;
		}
	}

	public float rainfall()
	{
		return rainfall;
	}

	public boolean salty()
	{
		return salty;
	}

	public BlockState getBlockState(int y)
	{
		return chunk.getBlockState(cursor.setY(y));
	}

	public void setBlockState(int y, SurfaceState state)
	{
		cursor.setY(y);
		state.setState(this);
	}

	public void setBlockState(int y, BlockState state)
	{
		chunk.setBlockState(cursor.setY(y), state, false);
	}

	public LevelAccessor level()
	{
		return level;
	}

	public ChunkAccess chunk()
	{
		return chunk;
	}

	public RandomSource random()
	{
		return random;
	}

	public boolean isDefaultBlock(BlockState state)
	{
		return defaultBlockStates.contains(state);
	}

	public boolean isDefaultFluid(BlockState state)
	{
		return defaultFluidStates.contains(state);
	}

	/**
	 * Calculates a surface depth value, taking into account altitude and slope
	 * Slope is applied as a multiplier, altitude limits the maximum depth
	 *
	 * @param y                  The y value. Values over sea level (63) are treated as lower depth
	 * @param minimumReturnValue The minimum possible slope. Typically 0, -1 is used as a flag value for not placing the top surface layer on occasion.
	 * @return a surface depth in the range [minimumReturnValue, maxSlope]
	 */
	public int calculateAltitudeSlopeSurfaceDepth(int y, int minimumReturnValue, int maxDepth)
	{
		final double slopeFactor = 1 - Mth.clamp(slope / 15d, 0, 1); // Large = low slope
		final double seaLevelFactor = y < seaLevel ?
										  Mth.clampedMap((seaLevel - y) / 15d, 0, 0.4, 1, 1.4) : 1; // Altitudes below sea level have larger depth
		final int maxElevationDepth = y < seaLevel + 7 ? maxDepth :
										  (int) Mth.clampedMap(y, seaLevel + 7, seaLevel + 67, maxDepth, 2);

		return Mth.clamp((int) Mth.lerp(slopeFactor * seaLevelFactor, minimumReturnValue, maxElevationDepth), minimumReturnValue, maxElevationDepth);
	}

	public int calculateAltitudeSlopeSurfaceDepth(int y, int minimumReturnValue)
	{
		return calculateAltitudeSlopeSurfaceDepth(y, minimumReturnValue, 5);
	}
}
