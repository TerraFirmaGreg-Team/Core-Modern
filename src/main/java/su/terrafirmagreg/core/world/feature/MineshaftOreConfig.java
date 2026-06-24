package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record MineshaftOreConfig(IntProvider horizontal_range, IntProvider vertical_range, boolean all_ores) implements FeatureConfiguration {
    public static final Codec<MineshaftOreConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    IntProvider.codec(1, 64).fieldOf("horizontal_range").forGetter(MineshaftOreConfig::horizontal_range),
                    IntProvider.codec(1, 64).fieldOf("vertical_range").forGetter(MineshaftOreConfig::vertical_range),
                    Codec.BOOL.fieldOf("all_ores").forGetter(MineshaftOreConfig::all_ores))
                    .apply(instance, MineshaftOreConfig::new));
}
