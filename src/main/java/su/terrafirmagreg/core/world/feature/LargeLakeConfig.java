package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record LargeLakeConfig(BlockStateProvider fluid, BlockStateProvider barrier) implements FeatureConfiguration {

    public static final Codec<LargeLakeConfig> CODEC = RecordCodecBuilder.create((config) -> config
            .group(
                    BlockStateProvider.CODEC.fieldOf("fluid").forGetter(LargeLakeConfig::fluid),
                    BlockStateProvider.CODEC.fieldOf("barrier").forGetter(LargeLakeConfig::barrier))
            .apply(config, LargeLakeConfig::new));
}
