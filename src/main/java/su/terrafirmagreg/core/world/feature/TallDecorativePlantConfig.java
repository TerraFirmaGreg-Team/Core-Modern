package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dries007.tfc.world.Codecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record TallDecorativePlantConfig(Block block, int plantHeight, int minHeight, int maxHeight, int middle) implements FeatureConfiguration {

	public static final Codec<TallDecorativePlantConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codecs.BLOCK.fieldOf("block").forGetter(c -> c.block),
		Codec.INT.fieldOf("plantHeight").forGetter(c -> c.plantHeight),
		Codec.INT.fieldOf("minHeight").forGetter(c -> c.minHeight),
		Codec.INT.fieldOf("maxHeight").forGetter(c -> c.maxHeight),
		Codec.INT.fieldOf("middle").forGetter(c -> c.middle)
	).apply(instance, TallDecorativePlantConfig::new));
}
