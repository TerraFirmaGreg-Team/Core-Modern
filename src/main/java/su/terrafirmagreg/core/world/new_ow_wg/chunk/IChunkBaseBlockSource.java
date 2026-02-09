package su.terrafirmagreg.core.world.new_ow_wg.chunk;

import net.dries007.tfc.world.biome.BiomeExtension;

public interface IChunkBaseBlockSource {
    void tfg$useAccurateBiome(int localX, int localZ, BiomeExtension biome, double weight, boolean couldBeSalty);
}
