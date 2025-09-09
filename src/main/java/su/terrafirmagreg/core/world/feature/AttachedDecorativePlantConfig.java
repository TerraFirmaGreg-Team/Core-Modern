package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record AttachedDecorativePlantConfig(Block block, int heightRange) implements FeatureConfiguration {

    public static final Codec<AttachedDecorativePlantConfig> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    Codecs.BLOCK.fieldOf("block").forGetter(c -> c.block),
                    Codec.INT.fieldOf("heightRange").forGetter(c -> c.heightRange))
            .apply(instance, AttachedDecorativePlantConfig::new));
}
