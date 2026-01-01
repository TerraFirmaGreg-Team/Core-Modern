package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record AttachedDecorativePlantConfig(BlockState blockState, int heightRange) implements FeatureConfiguration {

    public static final Codec<AttachedDecorativePlantConfig> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codecs.BLOCK_STATE.fieldOf("block").forGetter(c -> c.blockState),
                    Codec.INT.fieldOf("heightRange").forGetter(c -> c.heightRange))
            .apply(instance, AttachedDecorativePlantConfig::new));
}
