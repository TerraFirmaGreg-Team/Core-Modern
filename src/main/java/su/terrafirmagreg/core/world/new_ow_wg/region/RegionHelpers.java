package su.terrafirmagreg.core.world.new_ow_wg.region;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.AbstractIterator;

import net.dries007.tfc.world.region.Region;
import net.minecraft.util.RandomSource;

public final class RegionHelpers {
    /**
     * @param index An index obtained from {@link Region.Point#index} representing a point within this region.
     * @return The point at a given {@code index}, offset by {@code (dx, dz)}, or {@code null} if the point is out of the
     * region's bounding box.
     */
    @Nullable
    public static Region.Point atOffset(Region region, int index, int dx, int dz) {
        final int localX = dx + (index % region.sizeX());
        final int localZ = dz + (index / region.sizeX());
        return localX >= 0 && localX < region.sizeX() && localZ >= 0 && localZ < region.sizeZ()
                ? region.data()[localX + region.sizeX() * localZ]
                : null;
    }

    /**
     * @return An iterator through all points present within this region.
     */
    public static Iterable<Region.Point> points(Region region) {
        return () -> new AbstractIterator<>() {
            int index = -1;

            @Override
            protected Region.Point computeNext() {
                do {
                    index++;
                } while (index < region.data().length && region.data()[index] == null);
                return index < region.data().length ? region.data()[index] : endOfData();
            }
        };
    }

    /**
     * @return An estimate for the region's size, useful for pre-allocating bitsets to the correct capacity.
     */
    public static int size(Region region) {
        return region.sizeX() * region.sizeZ();
    }

    /**
     * @return A randomly chosen point within the region, possibly null.
     */
    @Nullable
    public static Region.Point random(Region region, RandomSource random) {
        return region.data()[random.nextInt(region.data().length)];
    }
}
