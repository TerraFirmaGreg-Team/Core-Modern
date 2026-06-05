package su.terrafirmagreg.core.compat.cc_tweaked;

/**
 * Exposes raw UTF-8 client input data used by keyboard handling.
 */

public interface CcUtf8TerminalStateAccess {

    int[] tfg$getUtf8Text();

    byte[] tfg$getUtf8Colours();

    byte[] tfg$getUtf8Palette();

    void tfg$setUtf8Data(int[] text, byte[] colours, byte[] palette);
}
