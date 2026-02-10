package su.terrafirmagreg.core.world.new_ow_wg.region;

import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.RegionTask;
import net.minecraft.util.RandomSource;

public enum TFGAddIslands implements RegionTask {
    INSTANCE;

    @Override
    public void apply(RegionGenerator.Context context) {
        final Region region = context.region;
        final RandomSource random = context.random;

        for (int attempt = 0, placed = 0; attempt < 130 && placed < 15; attempt++) {
            Region.Point point = RegionHelpers.random(region, random);
            IRegionPoint pt = (IRegionPoint) point;
            if (point == null) {
                continue;
            }

            if (!point.land() && !point.shore() && point.distanceToEdge > 2) {
                // Place a small island chain
                for (int island = 0; island < 12; island++) {
                    point.setLand();
                    point.setIsland();
                    point = region.at(
                            pt.tfg$getX() + random.nextInt(4) - random.nextInt(4),
                            pt.tfg$getZ() + random.nextInt(4) - random.nextInt(4));
                    if (point == null || (point.land() && !point.island()) || point.distanceToEdge <= 2) {
                        break;
                    }
                }
                placed += 1;
            }
        }
    }
}
