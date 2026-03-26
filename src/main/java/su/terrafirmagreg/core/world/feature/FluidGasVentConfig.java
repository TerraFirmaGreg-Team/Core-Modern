package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record FluidGasVentConfig(
        int baseRadius,
        float spawnChance,
        float geyseriteChance) implements FeatureConfiguration {

    public static final Codec<FluidGasVentConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 16).optionalFieldOf("base_radius", 4).forGetter(FluidGasVentConfig::baseRadius),
            Codec.floatRange(0, 1).optionalFieldOf("spawn_chance", 0.05f).forGetter(FluidGasVentConfig::spawnChance),
            Codec.floatRange(0, 1).optionalFieldOf("geyserite_chance", 0.7f).forGetter(FluidGasVentConfig::geyseriteChance)).apply(instance, FluidGasVentConfig::new));
}
