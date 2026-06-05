package su.terrafirmagreg.core.mixins.common.cc_tweaked;

/**
 * Exposes UTF-8 terminal state data used by network synchronization.
 */

public interface CcUtf8TextBufferAccess {

    int tfg$codePointAt(int index);

    void tfg$setCodePoint(int index, int codepoint);
}
