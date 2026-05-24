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

public record LargeDripstoneConfig(HolderSet<Block> replaceableBlocks, int floorToCeilingSearchRange, IntProvider columnRadius, FloatProvider heightScale,
        float maxColumnRadiusToCaveHeightRatio, FloatProvider stalactiteBluntness, FloatProvider stalagmiteBluntness,
        FloatProvider windSpeed, int minRadiusForWind, float minBluntnessForWind) implements FeatureConfiguration {

    public static final Codec<LargeDripstoneConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("replaceable_blocks").forGetter(LargeDripstoneConfig::replaceableBlocks),
            Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range").orElse(30).forGetter(LargeDripstoneConfig::floorToCeilingSearchRange),
            Codecs.optionalFieldOf(IntProvider.CODEC, "column_radius", UniformInt.of(1, 60)).forGetter(LargeDripstoneConfig::columnRadius),
            Codecs.optionalFieldOf(FloatProvider.CODEC, "height_scale", UniformFloat.of(0.0f, 20.0f)).forGetter(LargeDripstoneConfig::heightScale),
            Codec.floatRange(0.1f, 1.0f).fieldOf("max_column_radius_to_cave_height_ratio").forGetter(LargeDripstoneConfig::maxColumnRadiusToCaveHeightRatio),
            Codecs.optionalFieldOf(FloatProvider.CODEC, "stalactite_bluntness", UniformFloat.of(0.1f, 10.0f)).forGetter(LargeDripstoneConfig::stalactiteBluntness),
            Codecs.optionalFieldOf(FloatProvider.CODEC, "stalagmite_bluntness", UniformFloat.of(0.1f, 10.0f)).forGetter(LargeDripstoneConfig::stalagmiteBluntness),
            Codecs.optionalFieldOf(FloatProvider.CODEC, "wind_speed", UniformFloat.of(0.0f, 2.0f)).forGetter(LargeDripstoneConfig::windSpeed),
            Codec.intRange(0, 100).fieldOf("min_radius_for_wind").forGetter(LargeDripstoneConfig::minRadiusForWind),
            Codec.floatRange(0.0f, 5.0f).fieldOf("min_bluntness_for_wind").forGetter(LargeDripstoneConfig::minBluntnessForWind)).apply(instance, LargeDripstoneConfig::new));
}
