package su.terrafirmagreg.core.world.rock_getters;

import org.jetbrains.annotations.Nullable;

import com.notenoughmail.kubejs_tfc.event.CreateChunkDataProviderEventJS;
import com.notenoughmail.kubejs_tfc.util.implementation.custom.LayeredArea;

import net.dries007.tfc.world.chunkdata.ChunkRockDataCache;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.settings.RockSettings;

public record MoonRockGetter(LayeredArea rockLayer, OpenSimplex2D depthRockNoise) implements CreateChunkDataProviderEventJS.RocksGetter {

    // Doesn't do anything, but prevents rhino lag
    @Override
    public @Nullable RockSettings generate(int x, int y, int z, int surfaceY, @Nullable ChunkRockDataCache cache, RockLayerSettings rockSettings) {
        return rockSettings.sampleAtLayer(0, 0);
    }
}
