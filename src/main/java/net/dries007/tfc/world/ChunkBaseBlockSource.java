/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world;

import net.dries007.tfc.world.biome.TFCBiomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.chunkdata.RockData;
import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;

public class ChunkBaseBlockSource
{
    private static int index(int x, int z)
    {
        return (x & 15) | ((z & 15) << 4);
    }

    private final RockData rockData;
    private final Sampler<BiomeExtension> biomeSampler;
    private final BlockState[] cachedFluidStates;

    private final BlockState freshWater = Blocks.WATER.defaultBlockState();
	private final BlockState saltWater = TFCBlocks.SALT_WATER.get().defaultBlockState();
	private final BlockState muddyWater = TFGBlocks.MUDDY_WATER.get().defaultBlockState();

    public ChunkBaseBlockSource(RockData rockData, Sampler<BiomeExtension> biomeSampler)
    {
        this.rockData = rockData;
        this.biomeSampler = biomeSampler;
        this.cachedFluidStates = new BlockState[16 * 16];
    }

	/**
	 * Getting saltwater from only the biome has issues at shores, where sometimes the original land biome will influence the water
	 * on the edge of the ocean biome, but the shore biome will influence the water at the shore proper, leading to freshwater "rings"
	 * around the shore. This can be addressed by only placing freshwater when the biome weight is sufficiently high, but that causes
	 * rivers to be salty as they do not ever have high biome weights, so that is then special cased. With both of those checks, there is
	 * still an edge case where if we only check the primary biome's weight, at intersections between 3 non-salty biomes saltwater will
	 * generate because the highest weight is still low. Thus, before running those checks we see if there are any nearby biomes that are salty
	 */
	public void useAccurateBiome(int localX, int localZ, BiomeExtension biome, double weight, boolean couldBeSalty, boolean forceSaltWater)
	{
		cachedFluidStates[index(localX, localZ)] = forceSaltWater
													   || (couldBeSalty
															   && (biome.isSalty()
																	   || (weight <= 0.5 && biome != TFCBiomes.RIVER)))
													   ? saltWater
													   : (biome.isMuddy() ? muddyWater : freshWater);
	}

    public BlockState getBaseBlock(int blockX, int blockY, int blockZ)
    {
        return rockData.getRock(blockX, blockY, blockZ).raw().defaultBlockState();
    }

    public BlockState modifyFluid(BlockState fluidOrAir, int x, int z)
    {
        if (fluidOrAir == freshWater)
        {
            final int index = index(x, z);
            BlockState state = cachedFluidStates[index];
            if (state == null)
            {
				var sample = biomeSampler.get(x, z);
				if (sample.isSalty())
                	state = saltWater;
				else if (sample.isMuddy())
					state = muddyWater;
				else
					state = freshWater;

                cachedFluidStates[index] = state;
            }
            return state;
        }
        return fluidOrAir;
    }
}
