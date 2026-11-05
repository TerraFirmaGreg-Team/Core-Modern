package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Mostly copied from here, which itself is mostly copied from vanilla:
 * https://github.com/Apollounknowndev/lithostitched/blob/1.20.1/common/src/main/java/dev/worldgen/lithostitched/worldgen/feature/config/LargeDripstoneConfig.java
 */

public record SurfaceSpikeConfig(HolderSet<Block> replaceableBlocks, IntProvider columnRadius, FloatProvider heightScale,
        float maxColumnRadiusToCaveHeightRatio, FloatProvider bluntness,
        FloatProvider windSpeed, int minRadiusForWind, float minBluntnessForWind) implements FeatureConfiguration {

    public static final Codec<SurfaceSpikeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("replaceable_blocks").forGetter(SurfaceSpikeConfig::replaceableBlocks),
            Codecs.optionalFieldOf(IntProvider.CODEC, "column_radius", UniformInt.of(1, 60)).forGetter(SurfaceSpikeConfig::columnRadius),
            Codecs.optionalFieldOf(FloatProvider.CODEC, "height_scale", UniformFloat.of(0.0f, 20.0f)).forGetter(SurfaceSpikeConfig::heightScale),
            Codec.floatRange(0.1f, 1.0f).optionalFieldOf("max_column_radius_to_cave_height_ratio", 0.1f).forGetter(SurfaceSpikeConfig::maxColumnRadiusToCaveHeightRatio),
            Codecs.optionalFieldOf(FloatProvider.CODEC, "bluntness", UniformFloat.of(0.1f, 10.0f)).forGetter(SurfaceSpikeConfig::bluntness),
            Codecs.optionalFieldOf(FloatProvider.CODEC, "wind_speed", UniformFloat.of(0.0f, 2.0f)).forGetter(SurfaceSpikeConfig::windSpeed),
            Codec.intRange(0, 100).fieldOf("min_radius_for_wind").forGetter(SurfaceSpikeConfig::minRadiusForWind),
            Codec.floatRange(0.0f, 5.0f).fieldOf("min_bluntness_for_wind").forGetter(SurfaceSpikeConfig::minBluntnessForWind))
            .apply(instance, SurfaceSpikeConfig::new));
}
