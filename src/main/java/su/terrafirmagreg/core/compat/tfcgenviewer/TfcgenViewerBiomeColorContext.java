package su.terrafirmagreg.core.compat.tfcgenviewer;

/**
 * Set true only while TFCGenViewer is evaluating {@code BiomeColors#color} (map / HUD / legend), so
 * {@link net.dries007.tfc.util.DataManager#getOrThrow} can remap {@code tfg:earth/...} to
 * tfcgenviewer data keys without affecting other data managers.
 */
public final class TfcgenViewerBiomeColorContext {

    private static final ThreadLocal<Boolean> IN_BIOME_COLOR = ThreadLocal.withInitial(() -> false);

    private TfcgenViewerBiomeColorContext() {
    }

    public static void enter() {
        IN_BIOME_COLOR.set(true);
    }

    public static void leave() {
        IN_BIOME_COLOR.remove();
    }

    public static boolean isDrawingBiomePreview() {
        return Boolean.TRUE.equals(IN_BIOME_COLOR.get());
    }
}
