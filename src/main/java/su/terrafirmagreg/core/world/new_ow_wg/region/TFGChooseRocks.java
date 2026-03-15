/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.region;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.layer.framework.Area;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.RegionTask;

public enum TFGChooseRocks implements RegionTask {
    INSTANCE;

    public static final int OCEAN = 0;
    public static final int VOLCANIC = 1;
    public static final int LAND = 2;
    public static final int UPLIFT = 3;

    public static final int TYPE_BITS = 2;
    public static final int TYPE_MASK = (1 << TYPE_BITS) - 1; // 0b11

    @Override
    public void apply(RegionGenerator.Context context) {
        final Region region = context.region;
        final Area rockArea = context.generator().rockArea.get();

        for (final var point : RegionHelpers.points(region)) {
            var pt = (IRegionPoint) point;
            // Lower two bits are the supertype, upper bits are seed
            point.rock = (rockArea.get(pt.tfg$getX(), pt.tfg$getZ()) << TYPE_BITS) | findClosestType(region, point, pt.tfg$getIndex());
        }
    }

    private int findClosestType(Region region, Region.Point center, int index) {
        int type = center.land() ? LAND : OCEAN, minDist = Integer.MAX_VALUE;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                final @Nullable Region.Point point = RegionHelpers.atOffset(region, index, dx, dz);
                final IRegionPoint pt = (IRegionPoint) point;
                final int dist = Math.abs(dx) + Math.abs(dz);
                if (point != null && dist < minDist) {
                    if (point.island() && dist < 4 || pt.tfg$getHotSpotAge() > 0) {
                        type = VOLCANIC;
                        minDist = dist;
                    } else if ((point.mountain() || point.coastalMountain()) && dist < 3) {
                        type = UPLIFT;
                        minDist = dist;
                    }
                }
            }
        }
        return type;
    }
}
