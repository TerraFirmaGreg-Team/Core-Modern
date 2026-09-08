/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.placements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.dries007.tfc.world.Seed;
import net.dries007.tfc.world.volcano.CenteredFeatureNoise;
import net.dries007.tfc.world.volcano.CenteredFeatureNoiseSampler;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import su.terrafirmagreg.core.world.TFGPlacements;

public class TuyaPlacement extends CenterOrDistanceToPlacement<CenteredFeatureNoiseSampler> {
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
    protected CenteredFeatureNoiseSampler createContext(Seed seed) {
        return CenteredFeatureNoise.tuya(seed);
    }
}
