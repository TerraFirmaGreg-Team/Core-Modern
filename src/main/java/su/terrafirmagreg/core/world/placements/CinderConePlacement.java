/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.placements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.Codecs;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import su.terrafirmagreg.core.world.TFGPlacements;
import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.noise.CenteredFeatureNoise;
import su.terrafirmagreg.core.world.new_ow_wg.noise.CenteredFeatureNoiseSampler;

public class CinderConePlacement extends CenterOrDistanceToPlacement<CenteredFeatureNoiseSampler> {
    public static final Codec<CinderConePlacement> PLACEMENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("center", false).forGetter(c -> c.center),
            Codecs.UNIT_FLOAT.optionalFieldOf("distance", 0f).forGetter(c -> c.distance))
            .apply(instance, CinderConePlacement::new));

    public CinderConePlacement(boolean center, float distance) {
        super(center, distance);
    }

    @Override
    public PlacementModifierType<?> type() {
        return TFGPlacements.CINDER_CONE.get();
    }

    @Override
    protected CenteredFeatureNoiseSampler createContext(Seed seed) {
        return CenteredFeatureNoise.cinder(seed);
    }
}
