package su.terrafirmagreg.core.world.new_ow_wg.rivers;

import java.util.function.Function;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;

public enum TFGRiverBlendType {
    NONE(seed -> TFGRiverNoiseSampler.NONE),
    BANKED(TFGRiverNoise::banked), // Raised banks 1-2 blocks above water that may be higher than surrounding terrain in swampy biomes
    TALL_BANKED(TFGRiverNoise::tallBanked), // Sim to banked, but taller for use in mud flat/salt flat biomes
    FLOODPLAIN(TFGRiverNoise::floodplain), // Flat banks at water level with steep banks farther from river
    WIDE(TFGRiverNoise::wide), // Wide, smooth V-shaped valleys
    WIDE_DEEP(TFGRiverNoise::wideDeep), // Wide, smooth V-shaped valleys, but a few blocks deeper
    CANYON(TFGRiverNoise::canyon), // Tall, smooth V-shaped valleys
    TALL_CANYON(TFGRiverNoise::tallCanyon), // Slot canyons with undercut walls
    TALUS(TFGRiverNoise::talus), // Single line of cliffs with steep slopes above and below
    TERRACES(TFGRiverNoise::terraces), // Stair-step canyons, like the Grand Canyon
    CAVE(TFGRiverNoise::cave); // Underground river

    public static final TFGRiverBlendType[] ALL = values();
    public static final int SIZE = ALL.length;

    private final Function<Seed, TFGRiverNoiseSampler> factory;

    TFGRiverBlendType(Function<Seed, TFGRiverNoiseSampler> factory) {
        this.factory = factory;
    }

    public TFGRiverNoiseSampler createNoiseSampler(Seed seed) {
        return factory.apply(seed);
    }
}
