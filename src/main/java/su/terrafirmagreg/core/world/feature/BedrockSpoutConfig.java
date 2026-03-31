package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record BedrockSpoutConfig(IntProvider size, IntProvider surfaceOffset) implements FeatureConfiguration {
    public static final Codec<BedrockSpoutConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    IntProvider.codec(1, 64).fieldOf("size").forGetter(BedrockSpoutConfig::size),
                    IntProvider.codec(0, 24).fieldOf("surface_offset").forGetter(BedrockSpoutConfig::surfaceOffset))
                    .apply(instance, BedrockSpoutConfig::new));
}
