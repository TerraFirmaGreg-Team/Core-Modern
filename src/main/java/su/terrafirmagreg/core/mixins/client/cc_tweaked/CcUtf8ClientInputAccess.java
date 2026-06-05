package su.terrafirmagreg.core.mixins.client.cc_tweaked;

/**
 * Exposes raw UTF-8 client input data used by keyboard handling.
 */

public interface CcUtf8ClientInputAccess {

    void tfg$charTypedCodepoint(int codepoint);
}
