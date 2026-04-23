package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record MineshaftOreConfig(boolean all_ores) implements FeatureConfiguration {
    public static final Codec<MineshaftOreConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("all_ores").forGetter(MineshaftOreConfig::all_ores))
                    .apply(instance, MineshaftOreConfig::new));
}
