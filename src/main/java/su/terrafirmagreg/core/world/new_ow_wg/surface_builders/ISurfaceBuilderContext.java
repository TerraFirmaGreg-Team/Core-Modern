package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.world.biome.BiomeExtension;

public interface ISurfaceBuilderContext {
    void tfg$init(BiomeExtension cinderConeBiome, BiomeExtension tuffRingBiome, BiomeExtension tuyaBiome);

    BiomeExtension tfg$getCinderConeBiome();

    BiomeExtension tfg$getTuffRingBiome();

    BiomeExtension tfg$getTuyaBiome();
}
