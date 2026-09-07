/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.chunkdata;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.region.Units;
import net.dries007.tfc.world.settings.RockSettings;
import net.dries007.tfc.TerraFirmaCraft;

public class RockData
{
    private final @Nullable ChunkDataGenerator generator;
    private int @Nullable [] surfaceHeight;
    private @Nullable ChunkRockDataCache cache;

    public RockData(@Nullable ChunkDataGenerator generator)
    {
        this.generator = generator;
        this.surfaceHeight = null;
        this.cache = null;
    }

    /**
     * Initializes the rock data's {@link ChunkRockDataCache}. This means that future queries into {@link #getRock(BlockPos)} or {@link #getRock(int, int, int)} will populate and re-use the cache for noise.
     * <strong>N.B.</strong> Only use if this is used to query many positions in the chunk, otherwise the cost of populating the cache on the first few queries will be worse than with no cache.
     * @param pos The current chunk position.
     */
    public void useCache(ChunkPos pos)
    {
        this.cache = new ChunkRockDataCache(pos);
    }

    /**
     * Return the rock at the position {@code (x, z)}, at the surface. Note that this method <strong>may be used</strong>
     * without populating the chunk data's surface height cache, because it queries not for a specific position, but a specific height.
     */
    public RockSettings getSurfaceRock(int x, int z)
    {
        assert generator != null;
        return generator.generateRock(x, 0, z, 0, cache); // Use y=0 because above y>125 we adjust actual height
    }

    public RockSettings getRock(BlockPos pos)
    {
        return getRock(pos.getX(), pos.getY(), pos.getZ());
    }

    public RockSettings getRock(int x, int y, int z)
    {
		assert generator != null;
		
		/*
		 * Fixes a crash where `RockData.getRock()` is called before surfaceHeight is initialized.
		 * This can happen during world generation when features like ErosionFeature or LooseRockFeature
		 * run on a chunk whose ChunkData hasn't had `generateFull()` called yet.
		 */
		if (this.surfaceHeight == null) {
			TerraFirmaCraft.LOGGER.warn("Queried rock data for position ({}, {}, {}) before surface height cache was populated", x, y, z);
			return this.generator.generateRock(x, y, z, 64, null);
		}
		else
        {
            return generator.generateRock(x, y, z, surfaceHeight[Units.index(x, z)], cache);
        }
    }

    public int[] getSurfaceHeight()
    {
        assert surfaceHeight != null;
        return surfaceHeight;
    }

    public void setSurfaceHeight(int[] surfaceHeight)
    {
        this.surfaceHeight = surfaceHeight;
    }
}
