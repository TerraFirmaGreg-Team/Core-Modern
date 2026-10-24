package su.terrafirmagreg.core.world.rock_getters;

import org.jetbrains.annotations.Nullable;

import com.notenoughmail.kubejs_tfc.event.CreateChunkDataProviderEventJS;
import com.notenoughmail.kubejs_tfc.util.implementation.custom.LayeredArea;

import net.dries007.tfc.world.chunkdata.ChunkRockDataCache;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.settings.RockSettings;

public record BeneathRockGetter(LayeredArea rockLayer, OpenSimplex2D depthRockNoise) implements CreateChunkDataProviderEventJS.RocksGetter {

    @Override
    public @Nullable RockSettings generate(int x, int y, int z, int surfaceY, @Nullable ChunkRockDataCache cache, RockLayerSettings rockSettings) {
        final int skew = y / 6;
        final int beneathRockLayerHeight = 38;
        final int netherHeight = 208;

        return rockSettings.sampleAtLayer(rockLayer.getAt(x + skew, z + skew),
                (int) ((netherHeight - y + depthRockNoise.noise(x + skew, z + skew)) / beneathRockLayerHeight));
    }
}
