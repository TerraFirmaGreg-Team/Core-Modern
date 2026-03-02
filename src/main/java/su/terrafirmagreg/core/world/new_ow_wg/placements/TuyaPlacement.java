package su.terrafirmagreg.core.world.new_ow_wg.placements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TuyaNoise;

public class TuyaPlacement extends CenterOrDistanceToPlacement<TuyaNoise> {
    public static final Codec<TuyaPlacement> PLACEMENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("center", false).forGetter(c -> c.center),
            Codecs.UNIT_FLOAT.optionalFieldOf("distance", 0f).forGetter(c -> c.distance)).apply(instance, TuyaPlacement::new));

    public TuyaPlacement(boolean center, float distance) {
        super(center, distance);
    }

    @Override
    public PlacementModifierType<?> type() {
        return TFGPlacements.TUYA.get();
    }

    @Override
    protected TuyaNoise createContext(Seed seed) {
        return new TuyaNoise(seed);
    }
}
