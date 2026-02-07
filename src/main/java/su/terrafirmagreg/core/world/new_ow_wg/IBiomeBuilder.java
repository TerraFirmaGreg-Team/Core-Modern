package su.terrafirmagreg.core.world.new_ow_wg;

import net.dries007.tfc.world.biome.BiomeBuilder;

public interface IBiomeBuilder {

    BiomeBuilder tfg$type(ShoreBlendType type);

    BiomeBuilder tfg$tuyas(int frequency, int baseHeight, int scaleHeight, int volcanoBasaltHeight, boolean icy);

    BiomeBuilder tfg$tuffRings(int frequency, int baseHeight, int scaleHeight);

    BiomeBuilder tfg$volcanoes(int frequency, int baseHeight, int scaleHeight, int volcanoBasaltHeight);

    BiomeBuilder tfg$volcanoes(int frequency, int baseHeight, int scaleHeight, int volcanoBasaltHeight, boolean additive);
}
