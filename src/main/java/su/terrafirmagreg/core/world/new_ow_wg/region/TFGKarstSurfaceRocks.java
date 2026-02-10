package su.terrafirmagreg.core.world.new_ow_wg.region;

import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.RegionTask;
import net.dries007.tfc.world.settings.RockSettings;

import su.terrafirmagreg.core.world.new_ow_wg.RockSettingsHelpers;

public enum TFGKarstSurfaceRocks implements RegionTask {
    INSTANCE;

    @Override
    public void apply(RegionGenerator.Context context) {
        final Region region = context.region;
        final IRegionGenerator regionGenerator = (IRegionGenerator) context.generator();

        for (final var point : RegionHelpers.points(region)) {
            final RockSettings surfaceRock = regionGenerator.tfg$getSettings().rockLayerSettings().sampleAtLayer(point.rock, 0);
            ((IRegionPoint) point).tfg$setIsSurfaceRockKarst(RockSettingsHelpers.isKarst(surfaceRock));
        }
    }
}
