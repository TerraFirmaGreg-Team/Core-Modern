package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record FluidloggedVegetationPatchConfig(
        TagKey<Block> replaceable,
        BlockStateProvider groundState,
        BlockState fluidState,
        Holder<PlacedFeature> vegetationFeature,
        CaveSurface surface,
        IntProvider depth,
        float extraBottomBlockChance,
        int verticalRange,
        float vegetationChance,
        IntProvider xzRadius,
        float extraEdgeColumnChance) implements FeatureConfiguration {

    public static final Codec<FluidloggedVegetationPatchConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("replaceable").forGetter(FluidloggedVegetationPatchConfig::replaceable),
            BlockStateProvider.CODEC.fieldOf("ground_state").forGetter(FluidloggedVegetationPatchConfig::groundState),
            Codecs.BLOCK_STATE.fieldOf("fluid_state").forGetter(c -> c.fluidState),
            PlacedFeature.CODEC.fieldOf("vegetation_feature").forGetter(FluidloggedVegetationPatchConfig::vegetationFeature),
            CaveSurface.CODEC.fieldOf("surface").forGetter(FluidloggedVegetationPatchConfig::surface),
            IntProvider.codec(1, 128).fieldOf("depth").forGetter(FluidloggedVegetationPatchConfig::depth),
            Codec.floatRange(0f, 1f).fieldOf("extra_bottom_block_chance").forGetter(FluidloggedVegetationPatchConfig::extraBottomBlockChance),
            Codec.intRange(1, 256).fieldOf("vertical_range").forGetter(FluidloggedVegetationPatchConfig::verticalRange),
            Codec.floatRange(0f, 1f).fieldOf("vegetation_chance").forGetter(FluidloggedVegetationPatchConfig::vegetationChance),
            IntProvider.CODEC.fieldOf("xz_radius").forGetter(FluidloggedVegetationPatchConfig::xzRadius),
            Codec.floatRange(0f, 1f).fieldOf("extra_edge_column_chance").forGetter(FluidloggedVegetationPatchConfig::extraEdgeColumnChance)).apply(instance, FluidloggedVegetationPatchConfig::new));
}
