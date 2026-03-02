package su.terrafirmagreg.core.world.new_ow_wg.placements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TuffRingNoise;

public class TuffRingPlacement extends CenterOrDistanceToPlacement<TuffRingNoise> {
    public static final Codec<TuffRingPlacement> PLACEMENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("center", false).forGetter(c -> c.center),
            Codecs.UNIT_FLOAT.optionalFieldOf("distance", 0f).forGetter(c -> c.distance)).apply(instance, TuffRingPlacement::new));

    public TuffRingPlacement(boolean center, float distance) {
        super(center, distance);
    }

    @Override
    public PlacementModifierType<?> type() {
        return TFGPlacements.TUFF_RING.get();
    }

    @Override
    protected TuffRingNoise createContext(Seed seed) {
        return new TuffRingNoise(seed);
    }
}
