package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record FluidPoolConfig(
        BlockState fluidState,
        int minRadius,
        int maxRadius,
        int minDepth,
        int maxDepth,
        float spawnChance) implements FeatureConfiguration {

    public static final Codec<FluidPoolConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.BLOCK_STATE.fieldOf("fluid_state").forGetter(FluidPoolConfig::fluidState),
            Codec.intRange(1, 16).optionalFieldOf("min_radius", 6).forGetter(FluidPoolConfig::minRadius),
            Codec.intRange(1, 16).optionalFieldOf("max_radius", 8).forGetter(FluidPoolConfig::maxRadius),
            Codec.intRange(1, 16).optionalFieldOf("min_depth", 3).forGetter(FluidPoolConfig::minDepth),
            Codec.intRange(1, 16).optionalFieldOf("max_depth", 5).forGetter(FluidPoolConfig::maxDepth),
            Codec.floatRange(0, 1).optionalFieldOf("spawn_chance", 0.1f).forGetter(FluidPoolConfig::spawnChance)).apply(instance, FluidPoolConfig::new));
}
