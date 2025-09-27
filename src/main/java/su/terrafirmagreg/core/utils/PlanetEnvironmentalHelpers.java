package su.terrafirmagreg.core.utils;

import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGTags;

import java.util.Arrays;
import java.util.List;

import static su.terrafirmagreg.core.TFGCore.LOGGER;

public class PlanetEnvironmentalHelpers {
    private static final List<Pair<TagKey<Biome>, MarsSandBlockType>> marsBiomeTags =
            Arrays.asList(
                    new Pair<>(TFGTags.Biomes.HasDarkSandWind, MarsSandBlockType.DEEP),
                    new Pair<>(TFGTags.Biomes.HasMediumSandWind, MarsSandBlockType.MEDIUM),
                    new Pair<>(TFGTags.Biomes.HasLightSandWind, MarsSandBlockType.LIGHT));

    /**
     * Retrieves the correct sand layer block for a given block position.
     *
     * @param isPileBlock whether the layer block to be placed is a pile
     */
    public static Block getSandBlockForBiome(
            LevelReader level, BlockPos pos, boolean isPileBlock) {
        final Holder<Biome> currentBiome = level.getBiome(pos);

        Block layer = marsBiomeTags.stream()
                .findFirst()
                .filter(pair -> currentBiome.is(pair.getFirst()))
                .map(
                        pair ->
                                isPileBlock
                                        ? pair.getSecond().getPileBlock()
                                        : pair.getSecond().getLayerBlock())
                .orElse(null);

        if (layer == null) {
            layer = isPileBlock
                    ? MarsSandBlockType.MEDIUM.getPileBlock()
                    : MarsSandBlockType.MEDIUM.getLayerBlock();
            LOGGER.warn("{} is missing a sand wind biome tag! falling back to medium sand wind", currentBiome);
        }

        return layer;
    }

    public enum MarsSandBlockType {
        DEEP(TFGBlocks.RED_SAND_LAYER_BLOCK.get(), TFGBlocks.RED_SAND_PILE_BLOCK.get()),
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
