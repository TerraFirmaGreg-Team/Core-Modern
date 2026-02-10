package su.terrafirmagreg.core.world.new_ow_wg.region;

import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.RegionTask;

public enum TFGAddContinents implements RegionTask {
    INSTANCE;

    @Override
    public void apply(RegionGenerator.Context context) {
        for (final var point : RegionHelpers.points(context.region)) {
            IRegionPoint pt = (IRegionPoint) point;
            final double continent = context.generator().continentNoise.noise(pt.tfg$getX(), pt.tfg$getZ());
            if (continent > 4.4) {
                point.setLand();
            }
        }
    }
}
