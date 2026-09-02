package su.terrafirmagreg.core.common.environment;

/**
 * Shared energy-per-volume curve for enclosed-room machines (oxygen distributor, space heater,
 * higgs emitter). A room 10x larger costs only 4x as much.
 */
public final class EnclosedRoomEnergyCurve {

    /** Reference volume (in blocks) used to normalize energy consumption. */
    public static final int BASE_VOLUME = 10_000;

    /** Base EU/t for the reference volume. */
    public static final double BASE_ENERGY = 128;

    /** Exponent so that 10x volume = 4x energy (log10(4)). */
    public static final double ENERGY_VOLUME_EXPONENT = Math.log10(4);

    private EnclosedRoomEnergyCurve() {
    }

    /** EU/t for an enclosed region of the given volume (blocks), clamped to at least one block. */
    public static double eutForVolume(int volume) {
        double normalized = Math.max(1, volume) / (double) BASE_VOLUME;
        return BASE_ENERGY * Math.pow(normalized, ENERGY_VOLUME_EXPONENT);
    }

    /** Volume (in blocks) the curve maps to the given EU/t, i.e. the inverse of eutForVolume. */
    public static double volumeForEut(double eut) {
        if (eut <= 0) {
            return 0;
        }
        return BASE_VOLUME * Math.pow(eut / BASE_ENERGY, 1.0 / ENERGY_VOLUME_EXPONENT);
    }
}
