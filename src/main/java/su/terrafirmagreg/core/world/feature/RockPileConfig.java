package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record RockPileConfig(int radius, int height, IntProvider size) implements FeatureConfiguration {
    public static final Codec<RockPileConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("radius").forGetter(RockPileConfig::radius),
                    Codec.INT.fieldOf("height").forGetter(RockPileConfig::height),
                    IntProvider.codec(1, 128).fieldOf("size").forGetter(RockPileConfig::size))
                    .apply(instance, RockPileConfig::new));
}
