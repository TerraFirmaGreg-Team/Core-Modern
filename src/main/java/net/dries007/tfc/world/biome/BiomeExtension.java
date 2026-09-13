/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.biome;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.BiomeNoiseSampler;
import net.dries007.tfc.world.river.RiverBlendType;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.shore.ShoreBlendType;

/**
 * Represents all extra data TFC attaches to biomes, which is not present in the {@link Biome} class, nor is it data driven.
 */
public class BiomeExtension
{
	private final ResourceKey<Biome> key;

	@Nullable private final Function<Seed, BiomeNoiseSampler> noiseFactory;
	private final AquiferLookahead aquiferSurfaceHeight;
	private final SurfaceBuilderFactory surfaceBuilderFactory;

	private final BiomeBlendType biomeBlendType;
	private final RiverBlendType riverBlendType;
	private final ShoreBlendType shoreBlendType;
	@Getter
	private final boolean salty;
	private final boolean hasCinderCones;
	private final boolean hasTuffCones;
	private final boolean hasTuyas;
	private final boolean hasStratovolcanoes;
	private final boolean hasAtolls;
	@Getter
	private final float centeredFeatureFrequency;
	@Getter
	private final int centeredFeatureBaseHeight;
	@Getter
	private final int centeredFeatureScaleHeight;
	private final boolean centeredFeatureIce;
	@Getter
	private final boolean spawnable;
	@Getter
	private final boolean rivers;
	@Getter
	private final boolean shore;
	@Getter
	private final int shoreBaseHeight;
	private final boolean sandyRiverShores;

	@Nullable private List<HolderSet<PlacedFeature>> flattenedFeatures;
	@Nullable private Set<PlacedFeature> flattenedFeatureSet;
	@Nullable private Biome prevBiome;

	BiomeExtension(ResourceKey<Biome> key, @Nullable Function<Seed, BiomeNoiseSampler> noiseFactory,
				   SurfaceBuilderFactory surfaceBuilderFactory, AquiferLookahead aquiferSurfaceHeight,
				   BiomeBlendType biomeBlendType, RiverBlendType riverBlendType, ShoreBlendType shoreBlendType,
				   boolean salty, boolean hasCinderCones, boolean hasTuffCones, boolean hasTuyas, boolean hasAtolls,
				   boolean hasStratovolcanoes, float centeredFeatureFrequency, int centeredFeatureBaseHeight,
				   int centeredFeatureScaleHeight, boolean centeredFeatureIce, boolean spawnable, boolean rivers,
				   boolean shore, int shoreBaseHeight, boolean sandyRiverShores)
	{
		this.key = key;
		this.noiseFactory = noiseFactory;
		this.surfaceBuilderFactory = surfaceBuilderFactory;
		this.aquiferSurfaceHeight = aquiferSurfaceHeight;
		this.biomeBlendType = biomeBlendType;
		this.riverBlendType = riverBlendType;
		this.shoreBlendType = shoreBlendType;
		this.salty = salty;
		this.hasCinderCones = hasCinderCones;
		this.hasTuffCones = hasTuffCones;
		this.hasTuyas = hasTuyas;
		this.hasAtolls = hasAtolls;
		this.hasStratovolcanoes = hasStratovolcanoes;
		this.centeredFeatureFrequency = centeredFeatureFrequency;
		this.centeredFeatureBaseHeight = centeredFeatureBaseHeight;
		this.centeredFeatureScaleHeight = centeredFeatureScaleHeight;
		this.centeredFeatureIce = centeredFeatureIce;
		this.spawnable = spawnable;
		this.rivers = rivers;
		this.shore = shore;
		this.shoreBaseHeight = shoreBaseHeight;
		this.sandyRiverShores = sandyRiverShores;
	}

	public ResourceKey<Biome> key()
	{
		return key;
	}

	public BiomeBlendType biomeBlendType()
	{
		return biomeBlendType;
	}

	public RiverBlendType riverBlendType()
	{
		return riverBlendType;
	}

	public ShoreBlendType shoreBlendType()
	{
		return shoreBlendType;
	}

	public boolean hasSandyRiverShores()
	{
		return sandyRiverShores;
	}

	public boolean hasRivers()
	{
		return rivers;
	}

	public boolean hasCinderCones()
	{
		return hasCinderCones;
	}

	public boolean hasTuffRings()
	{
		return hasTuffCones;
	}

	public boolean hasTuyas()
	{
		return hasTuyas;
	}

	public boolean hasAtolls()
	{
		return hasAtolls;
	}

	public boolean hasStratovolcanoes()
	{
		return hasStratovolcanoes;
	}

	public boolean getCenteredFeatureIce()
	{
		return centeredFeatureIce;
	}

	public double getAquiferSurfaceHeight(BiomeNoiseSampler sampler, int x, int z)
	{
		return aquiferSurfaceHeight.getHeight(sampler, x, z);
	}

	@Nullable
	public BiomeNoiseSampler createNoiseSampler(Seed seed)
	{
		return noiseFactory != null ? noiseFactory.apply(seed) : null;
	}

	public SurfaceBuilder createSurfaceBuilder(Seed seed)
	{
		return surfaceBuilderFactory.apply(seed);
	}

	public List<HolderSet<PlacedFeature>> getFlattenedFeatures(Biome biome)
	{
		if (biome != prevBiome)
		{
			// These fields are cached on a per-biome-instance (aka per registry) basis
			// But extensions are globally instanced.
			// So we need to invalidate when we're querying from a different biome instance.
			prevBiome = biome;
			flattenedFeatures = null;
		}
		if (flattenedFeatures == null)
		{
			flattenedFeatures = biome.getGenerationSettings().features();
			flattenedFeatureSet = flattenedFeatures.stream().flatMap(HolderSet::stream).map(Holder::value).collect(Collectors.toSet());
		}
		return flattenedFeatures;
	}

	public Set<PlacedFeature> getFlattenedFeatureSet(Biome biome)
	{
		getFlattenedFeatures(biome);
		return Objects.requireNonNull(flattenedFeatureSet);
	}
}