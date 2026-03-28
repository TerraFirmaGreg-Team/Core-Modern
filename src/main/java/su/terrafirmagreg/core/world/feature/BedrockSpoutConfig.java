package su.terrafirmagreg.core.world.feature;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.material.Fluid;

public record BedrockSpoutConfig(IntProvider size, IntProvider surfaceOffset, List<Fluid> allowedFluids) implements FeatureConfiguration {
    public static final Codec<BedrockSpoutConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    IntProvider.codec(1, 64).fieldOf("size").forGetter(BedrockSpoutConfig::size),
                    IntProvider.codec(0, 24).fieldOf("surface_offset").forGetter(BedrockSpoutConfig::surfaceOffset),
                    Codecs.FLUID.listOf().fieldOf("allowed_fluids").forGetter(BedrockSpoutConfig::allowedFluids))
                    .apply(instance, BedrockSpoutConfig::new));
}
