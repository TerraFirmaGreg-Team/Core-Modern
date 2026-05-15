package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record FluidGasVentConfig(int baseRadius, float chance, BlockState ventState) implements FeatureConfiguration {

    public static final Codec<FluidGasVentConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 16).optionalFieldOf("base_radius", 4).forGetter(FluidGasVentConfig::baseRadius),
            Codec.floatRange(0, 1).optionalFieldOf("chance", 0.7f).forGetter(FluidGasVentConfig::chance),
            Codecs.BLOCK_STATE.fieldOf("vent").forGetter(FluidGasVentConfig::ventState))
            .apply(instance, FluidGasVentConfig::new));
}
