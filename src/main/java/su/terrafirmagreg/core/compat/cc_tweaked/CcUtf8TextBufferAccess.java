package su.terrafirmagreg.core.compat.cc_tweaked;

public interface CcUtf8TextBufferAccess {

    int tfg$codePointAt(int index);

    void tfg$setCodePoint(int index, int codepoint);
}
