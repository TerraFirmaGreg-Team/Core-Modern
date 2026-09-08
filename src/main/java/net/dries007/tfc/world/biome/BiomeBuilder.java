/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.biome;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongFunction;

import net.dries007.tfc.world.surface.builder.CinderConeSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.dries007.tfc.world.surface.builder.TuffRingsSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.TuyaSurfaceBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.BiomeNoiseSampler;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.river.RiverBlendType;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.shores.ShoreBlendType;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

public class BiomeBuilder
{
	public static BiomeBuilder builder()
	{
		return new BiomeBuilder();
	}

	@Nullable private Function<Seed, Noise2D> heightNoiseFactory;
	@Nullable private Function<Seed, BiomeNoiseSampler> noiseFactory;
	@Nullable private SurfaceBuilderFactory surfaceBuilderFactory;

	private AquiferLookahead aquiferSurfaceHeight;
	private BiomeBlendType biomeBlendType;
	private RiverBlendType riverBlendType;
	private ShoreBlendType shoreBlendType;
	private boolean salty;
	private boolean hasCinderCones;
	private boolean hasTuyas;
	private boolean hasTuffRings;
	private int centeredFeatureFrequency;
	private int centeredFeatureRockHeight;
	private int centeredFeatureBaseHeight;
	private int centeredFeatureScaleHeight;
	private boolean centeredFeatureIce;
	private boolean spawnable;
	private boolean rivers;
	private boolean shore;
	private int shoreBaseHeight;
	private boolean sandyRiverShores;

	private BiomeBuilder()
	{
		aquiferSurfaceHeight = (sampler, x, z) -> {
			sampler.setColumn(x, z);
			return sampler.height();
		};
		biomeBlendType = BiomeBlendType.LAND;
		riverBlendType = RiverBlendType.NONE;
		shoreBlendType = ShoreBlendType.NONE;
		salty = false;
		hasCinderCones = false;
		hasTuffRings = false;
		hasTuyas = false;
		centeredFeatureIce = false;
		centeredFeatureFrequency = 0;
		centeredFeatureRockHeight = 0;
		centeredFeatureBaseHeight = 0;
		centeredFeatureScaleHeight = 0;
		spawnable = false;
		rivers = true;
		shore = false;
		shoreBaseHeight = SEA_LEVEL_Y;
		sandyRiverShores = true;
	}

	public BiomeBuilder heightmap(LongFunction<Noise2D> heightNoiseFactory)
	{
		this.heightNoiseFactory = seed -> heightNoiseFactory.apply(seed.seed());
		this.noiseFactory = seed -> BiomeNoiseSampler.fromHeightNoise(heightNoiseFactory.apply(seed.seed()));
		return this;
	}

	public BiomeBuilder surface(SurfaceBuilderFactory surfaceBuilderFactory)
	{
		this.surfaceBuilderFactory = CinderConeSurfaceBuilder.create(surfaceBuilderFactory);
		this.surfaceBuilderFactory = TuffRingsSurfaceBuilder.create(this.surfaceBuilderFactory);
		this.surfaceBuilderFactory = TuyaSurfaceBuilder.create(this.surfaceBuilderFactory);
		return this;
	}

	public BiomeBuilder carving(BiFunction<Long, Noise2D, BiomeNoiseSampler> carvingNoiseFactory)
	{
		Objects.requireNonNull(heightNoiseFactory, "Height noise must not be null");

		final Function<Seed, Noise2D> baseHeightNoiseFactory = heightNoiseFactory;
		this.noiseFactory = seed -> carvingNoiseFactory.apply(seed.seed(), baseHeightNoiseFactory.apply(seed));
		this.aquiferSurfaceHeight = (sampler, x, z) -> TFCChunkGenerator.SEA_LEVEL_Y - 16; // Expect sea level carving to restrict aquifers

		return this;
	}

	public BiomeBuilder noise(LongFunction<BiomeNoiseSampler> noiseFactory)
	{
		this.noiseFactory = seed -> noiseFactory.apply(seed.seed());
		return this;
	}

	public BiomeBuilder aquiferHeightOffset(final double delta)
	{
		return aquiferHeight((sampler, x, z) -> {
			sampler.setColumn(x, z);
			return sampler.height() + delta;
		});
	}

	public BiomeBuilder aquiferHeight(AquiferLookahead aquiferSurfaceHeight)
	{
		this.aquiferSurfaceHeight = aquiferSurfaceHeight;
		return this;
	}

	public BiomeBuilder type(BiomeBlendType type)
	{
		this.biomeBlendType = type;
		return this;
	}

	public BiomeBuilder type(RiverBlendType type)
	{
		this.riverBlendType = type;
		if (type == RiverBlendType.CAVE)
			this.sandyRiverShores = false;
		return this;
	}

	public BiomeBuilder type(ShoreBlendType type)
	{
		this.shoreBlendType = type;
		return this;
	}

	public BiomeBuilder salty()
	{
		this.salty = true;
		return this;
	}

	public BiomeBuilder spawnable()
	{
		this.spawnable = true;
		return this;
	}

	public BiomeBuilder noRivers()
	{
		this.rivers = false;
		this.sandyRiverShores = false;
		return this;
	}

	public BiomeBuilder noSandyRiverShores()
	{
		this.sandyRiverShores = false;
		return this;
	}

	public BiomeBuilder setShoreBaseHeight(int shoreBaseHeight)
	{
		this.shoreBaseHeight = SEA_LEVEL_Y + shoreBaseHeight;
		return this;
	}

	public BiomeBuilder shore()
	{
		this.shore = true;
		return this;
	}

	public BiomeBuilder cinderCones(int frequency, int baseHeight, int scaleHeight, int cinderConeBasaltHeight)
	{
		return cinderCones(frequency, baseHeight, scaleHeight, cinderConeBasaltHeight, false);
	}

	public BiomeBuilder cinderCones(int frequency, int baseHeight, int scaleHeight, int cinderConeBasaltHeight, boolean additive)
	{
		this.hasCinderCones = true;
		this.centeredFeatureFrequency = frequency;
		this.centeredFeatureRockHeight = SEA_LEVEL_Y + cinderConeBasaltHeight;
		this.centeredFeatureBaseHeight = baseHeight;
		this.centeredFeatureScaleHeight = scaleHeight;

		return this;
	}

	public BiomeBuilder tuffRings(int frequency, int baseHeight, int scaleHeight)
	{
		this.hasTuffRings = true;
		this.centeredFeatureFrequency = frequency;
		this.centeredFeatureBaseHeight = baseHeight;
		this.centeredFeatureScaleHeight = scaleHeight;

		return this;
	}

	public BiomeBuilder tuyas(int frequency, int baseHeight, int scaleHeight, int tuyaBasaltHeight, boolean icy)
	{
		this.hasTuyas = true;
		this.centeredFeatureFrequency = frequency;
		this.centeredFeatureRockHeight = SEA_LEVEL_Y + tuyaBasaltHeight;
		this.centeredFeatureBaseHeight = baseHeight;
		this.centeredFeatureScaleHeight = scaleHeight;
		this.centeredFeatureIce = icy;

		return this;
	}

	public BiomeExtension build(ResourceKey<Biome> key)
	{
		assert surfaceBuilderFactory != null : "missing surface builder";

		return new BiomeExtension(key, noiseFactory, surfaceBuilderFactory, aquiferSurfaceHeight, biomeBlendType, riverBlendType, shoreBlendType, salty, hasCinderCones, hasTuffRings, hasTuyas, centeredFeatureFrequency, centeredFeatureRockHeight, centeredFeatureBaseHeight, centeredFeatureScaleHeight, centeredFeatureIce, spawnable, rivers, shore, shoreBaseHeight, sandyRiverShores);
	}
}
