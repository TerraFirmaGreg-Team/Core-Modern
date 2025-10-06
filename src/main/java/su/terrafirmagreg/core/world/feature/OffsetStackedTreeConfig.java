package su.terrafirmagreg.core.world.feature;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.dries007.tfc.world.feature.tree.RootConfig;
import net.dries007.tfc.world.feature.tree.StackedTreeConfig;
import net.dries007.tfc.world.feature.tree.TreePlacementConfig;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

// A copy of TFC's StackedTreeConfig but with another number field

public record OffsetStackedTreeConfig(List<StackedTreeConfig.Layer> layers, TreePlacementConfig placement,
        Optional<RootConfig> rootSystem, int yOffset) implements FeatureConfiguration {

    public static final Codec<OffsetStackedTreeConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StackedTreeConfig.Layer.CODEC.listOf().fieldOf("layers").forGetter(c -> c.layers),
            TreePlacementConfig.CODEC.fieldOf("placement").forGetter(c -> c.placement),
            Codecs.optionalFieldOf(RootConfig.CODEC, "root_system").forGetter(c -> c.rootSystem),
            Codec.INT.fieldOf("y_offset").forGetter(c -> c.yOffset)).apply(instance, OffsetStackedTreeConfig::new));
}
