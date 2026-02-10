package su.terrafirmagreg.core.world.new_ow_wg.region;

import java.util.BitSet;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;
import net.dries007.tfc.world.region.RegionTask;
import net.dries007.tfc.world.region.Units;
import net.minecraft.util.Mth;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;

public enum TFGAnnotateDistanceToWestCoast implements RegionTask {
    INSTANCE;

    @Override
    public void apply(RegionGenerator.Context context) {
        final Region region = context.region;

        for (int dx = 0; dx < region.sizeX(); dx++) {
            for (int dz = 0; dz < region.sizeZ(); dz++) {
                final int index = dx + region.sizeX() * dz;
                final Region.Point point = region.data()[index];
                final IRegionPoint pt = (IRegionPoint) point;
                if (point != null) {
                    if (dx == 0) {
                        pt.tfg$setDistanceToWestCoast((byte) 0);
                    } else {
                        final Region.Point lastCenterPoint = region.data()[index - 1];
                        final IRegionPoint lastCenterPt = (IRegionPoint) lastCenterPoint;
                        if (lastCenterPoint == null) {
                            //Sets a little starting bonus when a point in on the eastern border of a cell. The cusp will be smoothed out in the actual rain map
                            pt.tfg$setDistanceToWestCoast(point.land() ? (byte) (25 + point.distanceToOcean) : 0);
                        } else {
                            final int lastCenterVal = lastCenterPt.tfg$getDistanceToWestCoast();
                            if (!point.land()) {
                                //Ocean east of a shore will decrease in value faster than the land gains in value, and never below zero
                                pt.tfg$setDistanceToWestCoast((byte) Math.max(lastCenterVal - 2, 0));
                            } else {
                                int sum = 0;
                                final IRegionGenerator generator = (IRegionGenerator) context.generator();
                                final float scale = generator.tfg$getSettings().temperatureScale();
                                final float frequency = Units.GRID_WIDTH_IN_BLOCK / (2f * scale);
                                final float function = Math.abs(4f * frequency * pt.tfg$getZ() - 4f * Mth.floor(frequency * pt.tfg$getZ() + 0.75f)) - 1f;
                                final int start = -2 + (function > 0.1 ? 1 : 0);
                                final int end = 2 - (function < -0.1 ? 1 : 0);
                                //Can adjust the start and end point of this loop arbitrarily, just change the denominator of the average function too
                                for (int dz2 = start; dz2 <= end; dz2++) {
                                    final Region.Point lastPoint = RegionHelpers.atOffset(region, pt.tfg$getIndex(), -1, dz2);
                                    final IRegionPoint lastPt = (IRegionPoint) lastPoint;
                                    if (lastPoint != null) {
                                        sum = sum + lastPt.tfg$getDistanceToWestCoast();
                                    } else {
                                        sum = sum + lastCenterVal;
                                    }
                                }
                                pt.tfg$setDistanceToWestCoast((byte) (Mth.ceil(sum / (1f + end - start)) + 1));
                            }
                        }
                    }
                }
            }
        }

        // For ocean tiles, we set values based on the value at the nearest coast
        final BitSet explored = new BitSet(RegionHelpers.size(region));
        final IntArrayFIFOQueue queue = new IntArrayFIFOQueue();

        for (int dx = 0; dx < region.sizeX(); dx++) {
            for (int dz = 0; dz < region.sizeZ(); dz++) {
                final int index = dx + region.sizeX() * dz;
                final Region.Point point = region.maybeAt(dx + region.minX(), dz + region.minZ());
                if (point != null && point.land()) {
                    explored.set(index);
                    queue.enqueue(index);
                }
            }
        }

        while (!queue.isEmpty()) {
            final int last = queue.dequeueInt();
            final Region.Point lastPoint = region.data()[last];
            if (lastPoint != null) {
                final IRegionPoint lastPt = (IRegionPoint) lastPoint;
                final int lastDistance = lastPt.tfg$getDistanceToWestCoast();
                final int nextDistance = lastDistance + (lastDistance > 40 ? -1 : 1);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        final @Nullable Region.Point point = RegionHelpers.atOffset(region, last, dx, dz);
                        if (point != null) {
                            final IRegionPoint pt = (IRegionPoint) point;
                            if (pt.tfg$getDistanceToWestCoast() == 0) {
                                if (!explored.get(pt.tfg$getIndex())) {
                                    pt.tfg$setDistanceToWestCoast((byte) nextDistance);
                                    queue.enqueue(pt.tfg$getIndex());
                                }
                                explored.set(pt.tfg$getIndex());
                            }
                        }
                    }
                }
            }
        }
    }
}
