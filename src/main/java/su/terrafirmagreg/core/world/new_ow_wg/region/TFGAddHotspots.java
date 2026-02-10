package su.terrafirmagreg.core.world.new_ow_wg.region;

import net.dries007.tfc.world.noise.Cellular2D;
import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.RegionTask;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;

public enum TFGAddHotspots implements RegionTask {
    INSTANCE;

    @Override
    public void apply(RegionGenerator.Context context) {
        final Region region = context.region;
        final double threshold = 0.65;
        final double expansionThreshold = 0.15;
        final IRegionGenerator generator = (IRegionGenerator) context.generator();

        final IntArrayFIFOQueue queue = new IntArrayFIFOQueue();

        // If a location reaches a value of at least exceeding a threshold value, a hot spot is placed in the region
        for (final var point : RegionHelpers.points(region)) {
            final IRegionPoint pt = (IRegionPoint) point;

            final Cellular2D.Cell cell = generator.tfg$getPlateRegionNoise().cell(pt.tfg$getX(), pt.tfg$getZ());
            final double edgeDist = Math.abs(cell.f1() - cell.f2());

            double val = generator.tfg$getHotSpotIntensityNoise().noise(shift(pt.tfg$getX()), shift(pt.tfg$getZ()));
            if (val > threshold && edgeDist > 0.05) {
                final byte age = (byte) (int) generator.tfg$getHotSpotAgeNoise().noise(shift(pt.tfg$getX()), shift(pt.tfg$getZ()));
                pt.tfg$setHotSpotAge(age);
                if (age != 4)
                    point.setLand();
                queue.enqueue(pt.tfg$getIndex());
            }
        }

        // From the above crater locations, the hotspots are extended outwards
        while (!queue.isEmpty()) {
            final int index = queue.dequeueInt();
            final IRegionPoint pt = (IRegionPoint) region.data()[index];
            final byte lastAge = pt.tfg$getHotSpotAge();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    final Region.Point next = RegionHelpers.atOffset(region, index, dx, dz);
                    final IRegionPoint nextPt = (IRegionPoint) next;
                    if (next != null && nextPt.tfg$getHotSpotAge() == 0) {
                        if (generator.tfg$getHotSpotIntensityNoise().noise(shift(nextPt.tfg$getX()), shift(nextPt.tfg$getZ())) > expansionThreshold) {
                            queue.enqueue(nextPt.tfg$getIndex());
                            nextPt.tfg$setHotSpotAge(lastAge);
                            if (lastAge != 4)
                                next.setLand();
                        }
                        // This adds an extra layer outside where the hotspot exceeds the threshold as a buffer against oceans
                        else if (!next.land() && generator.tfg$getHotSpotIntensityNoise().noise(shift(nextPt.tfg$getX()) - dx, shift(nextPt.tfg$getZ()) - dz) > expansionThreshold) {
                            // Do not set land on the outer layer
                            nextPt.tfg$setHotSpotAge(lastAge);
                            if (lastAge != 4)
                                next.setLand();
                        }
                    }
                }
            }
        }
    }

    // Use this when sampling noise from regional coordinates to sample the center of the region point
    public double shift(int point) {
        return point + 0.5;
    }
}
