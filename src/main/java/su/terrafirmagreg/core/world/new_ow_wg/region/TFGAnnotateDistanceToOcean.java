package su.terrafirmagreg.core.world.new_ow_wg.region;

import java.util.BitSet;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.RegionTask;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;

public enum TFGAnnotateDistanceToOcean implements RegionTask {
    INSTANCE;

    @Override
    public void apply(RegionGenerator.Context context) {
        final Region region = context.region;
        final BitSet explored = new BitSet(RegionHelpers.size(region));
        final IntArrayFIFOQueue queue = new IntArrayFIFOQueue();

        for (final var point : RegionHelpers.points(region)) {
            if (point != null && !point.land()) {
                IRegionPoint pt = (IRegionPoint) point;
                point.distanceToOcean = -1;
                queue.enqueue(pt.tfg$getIndex());
                explored.set(pt.tfg$getIndex());
            }
        }

        while (!queue.isEmpty()) {
            final int last = queue.dequeueInt();
            final Region.Point lastPoint = region.data()[last];
            final int nextDistance = lastPoint.distanceToOcean + 1;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    final @Nullable Region.Point point = RegionHelpers.atOffset(region, last, dx, dz);
                    if (point != null && point.land() && point.distanceToOcean == 0) {
                        if (!lastPoint.land() && !point.island()) {
                            lastPoint.setShore(); // Mark as adjacent to land
                        }

                        IRegionPoint pt = (IRegionPoint) point;
                        if (!explored.get(pt.tfg$getIndex())) {
                            point.distanceToOcean = (byte) nextDistance;
                            queue.enqueue(pt.tfg$getIndex());
                        }
                        explored.set(pt.tfg$getIndex());
                    }
                }
            }
        }
    }
}
