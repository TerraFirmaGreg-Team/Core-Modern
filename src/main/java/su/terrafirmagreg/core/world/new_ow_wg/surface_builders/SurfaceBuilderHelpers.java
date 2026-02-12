package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import java.util.Collections;

import net.minecraft.util.RandomSource;

public class SurfaceBuilderHelpers {
    /**
     * Shuffles the contents of an array. Borrowed from {@link Collections#shuffle} but modified to work with both an array,
     * and with {@link RandomSource}.
     */
    public static <T> void shuffleArray(T[] array, RandomSource r) {
        for (int i = array.length; i > 1; i--)
            swap(array, i - 1, r.nextInt(i));
    }

    private static void swap(Object[] arr, int i, int j) {
        final Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}
