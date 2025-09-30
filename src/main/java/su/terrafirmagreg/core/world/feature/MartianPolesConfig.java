package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record MartianPolesConfig(int snowStartTemp, int snowFinishTemp, int maximumTemp) implements FeatureConfiguration {

    public static final Codec<MartianPolesConfig> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codec.INT.fieldOf("snowStartTemp").forGetter(c -> c.snowStartTemp),
                    Codec.INT.fieldOf("snowFinishTemp").forGetter(c -> c.snowFinishTemp),
                    Codec.INT.fieldOf("maximumTemp").forGetter(c -> c.maximumTemp))
            .apply(instance, MartianPolesConfig::new));
}
