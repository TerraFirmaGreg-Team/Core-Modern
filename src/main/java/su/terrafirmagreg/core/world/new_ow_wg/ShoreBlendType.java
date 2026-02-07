package su.terrafirmagreg.core.world.new_ow_wg;

import java.util.function.Function;

import su.terrafirmagreg.core.world.new_ow_wg.noise.ShoreNoise;

public enum ShoreBlendType {
    NONE(seed -> ShoreNoiseSampler.NONE),
    CLASSIC(ShoreNoise::classic), // Matches 1.20 beaches, simple cliffs with noisy sand below
    SANDY(ShoreNoise::sandyBeach), // Typical monoslope
    EMBAYMENTS(ShoreNoise::embayments),
    UPPER_TERRACE(ShoreNoise::upperTerrace),
    LOWER_TERRACE(ShoreNoise::lowerTerrace),
    SETBACK_CLIFFS(ShoreNoise::setbackCliffs),
    DUNES(ShoreNoise::dunes),
    ROCKY_SHORES(ShoreNoise::rockyShores),
    SEA_STACKS(ShoreNoise::seaStacks); // Monoslopes mixed with protruding rocks

    public static final ShoreBlendType[] ALL = values();
    public static final int SIZE = ALL.length;

    private final Function<Seed, ShoreNoiseSampler> factory;

    ShoreBlendType(Function<Seed, ShoreNoiseSampler> factory) {
        this.factory = factory;
    }

    public ShoreNoiseSampler createNoiseSampler(Seed seed) {
        return factory.apply(seed);
    }
}
