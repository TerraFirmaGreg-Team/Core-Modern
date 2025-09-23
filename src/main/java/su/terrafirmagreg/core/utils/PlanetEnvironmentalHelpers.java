package su.terrafirmagreg.core.utils;

import java.util.Arrays;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGTags;

public class PlanetEnvironmentalHelpers {
    private static final List<Pair<TagKey<Biome>, MarsSandBlockType>> marsBiomeTags = Arrays.asList(
            new Pair<>(TFGTags.Biomes.HasDarkSandWind, MarsSandBlockType.DEEP),
            new Pair<>(TFGTags.Biomes.HasMediumSandWind, MarsSandBlockType.MEDIUM),
            new Pair<>(TFGTags.Biomes.HasLightSandWind, MarsSandBlockType.LIGHT));

    public static Block getSandBlockForBiome(
            LevelAccessor level, BlockPos pos, boolean isPileBlock) {
        final Holder<Biome> currentBiome = level.getBiome(pos);

        return marsBiomeTags.stream()
                .findFirst()
                .filter(pair -> currentBiome.is(pair.getFirst()))
                .map(
                        pair -> isPileBlock
                                ? pair.getSecond().getPileBlock()
                                : pair.getSecond().getLayerBlock())
                // TODO: replace with orElseThrow before modpack release
                .orElse(
                        isPileBlock
                                ? MarsSandBlockType.MEDIUM.getPileBlock()
                                : MarsSandBlockType.MEDIUM.getLayerBlock());
    }

    public enum MarsSandBlockType {
        DEEP(TFGBlocks.HEMATITIC_SAND_LAYER_BLOCK.get(), TFGBlocks.HEMATITIC_SAND_PILE_BLOCK.get()),
        MEDIUM(TFGBlocks.MARS_SAND_LAYER_BLOCK.get(), TFGBlocks.MARS_SAND_PILE_BLOCK.get()),
        LIGHT(TFGBlocks.VENUS_SAND_LAYER_BLOCK.get(), TFGBlocks.VENUS_SAND_PILE_BLOCK.get());

        private static final MarsSandBlockType[] VALUES = values();

        @Getter
        private final Block layerBlock;
        @Getter
        private final Block pileBlock;

        MarsSandBlockType(Block layerBlock, Block pileBlock) {
            this.layerBlock = layerBlock;
            this.pileBlock = pileBlock;
        }
    }
}
