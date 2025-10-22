package su.terrafirmagreg.core.utils;

import static su.terrafirmagreg.core.TFGCore.LOGGER;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGTags;

public class PlanetEnvironmentalHelpers {
    private static final Map<ResourceKey<Biome>, MarsSandBlockType> marsBiomeTags = new HashMap<>();

    /**
     * Retrieves the correct sand layer block for a given block position.
     */
    public static Block getSandBlockForBiome(LevelReader level, BlockPos pos) {
        final Holder<Biome> currentBiome = level.getBiome(pos);
        final ResourceKey<Biome> biomeKey = currentBiome.unwrapKey().orElse(null);
        if (biomeKey == null) {
            LOGGER.warn("{} is missing a sand wind biome tag! falling back to medium sand wind", biomeKey.location());
            return MarsSandBlockType.MEDIUM.getPileBlock();
        }

        Block pile;
        if (!marsBiomeTags.containsKey(biomeKey)) {
            if (currentBiome.is(TFGTags.Biomes.HasDarkSandWind)) {
                marsBiomeTags.put(currentBiome.unwrapKey().orElseThrow(), MarsSandBlockType.DEEP);
            } else if (currentBiome.is(TFGTags.Biomes.HasLightSandWind)) {
                marsBiomeTags.put(currentBiome.unwrapKey().orElseThrow(), MarsSandBlockType.LIGHT);
            } else {
                marsBiomeTags.put(currentBiome.unwrapKey().orElseThrow(), MarsSandBlockType.MEDIUM);
            }
        }

        pile = marsBiomeTags.get(biomeKey).getPileBlock();

        return pile;
    }

    @Getter
    public enum MarsSandBlockType {
        DEEP(TFGBlocks.HEMATITIC_SAND_PILE_BLOCK.get()),
        MEDIUM(TFGBlocks.MARS_SAND_PILE_BLOCK.get()),
        LIGHT(TFGBlocks.VENUS_SAND_PILE_BLOCK.get());

        private static final MarsSandBlockType[] VALUES = values();

        private final Block pileBlock;

        MarsSandBlockType(Block pileBlock) {
            this.pileBlock = pileBlock;
        }
    }
}
