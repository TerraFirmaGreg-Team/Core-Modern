package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dan200.computercraft.core.terminal.TextBuffer;

import su.terrafirmagreg.core.compat.cc_tweaked.CcUtf8TextBufferAccess;
import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Stores Unicode codepoints alongside CC:Tweaked's legacy text buffer so terminal cells can keep UTF-8 text.
 */

@Mixin(value = TextBuffer.class, remap = false)
public class TextBufferMixin implements CcUtf8TextBufferAccess {

    @Shadow
    @Final
    private char[] text;

    @Unique
    private int[] tfg$codepoints;

    @Inject(method = "<init>(CI)V", at = @At("RETURN"), remap = false)
    private void tfg$initFromChar(char c, int length, CallbackInfo ci) {
        tfg$codepoints = new int[text.length];

        for (var i = 0; i < text.length; i++) {
            tfg$codepoints[i] = c;
        }
    }

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"), remap = false)
    private void tfg$initFromString(String value, CallbackInfo ci) {
        tfg$codepoints = new int[text.length];

        for (var i = 0; i < text.length; i++) {
            tfg$codepoints[i] = text[i];
        }
    }

    @Unique
    private int[] tfg$getCodepoints() {
        if (tfg$codepoints == null || tfg$codepoints.length != text.length) {
            tfg$codepoints = new int[text.length];

            for (var i = 0; i < text.length; i++) {
                tfg$codepoints[i] = text[i];
            }
        }

        return tfg$codepoints;
    }

    @Unique
    private void tfg$setFallbackChar(int index, int codepoint) {
        text[index] = codepoint >= 0 && codepoint <= Character.MAX_VALUE ? (char) codepoint : '?';
    }

    @Unique
    private void tfg$writeCodepoints(String value, int start) {
        var codepoints = tfg$getCodepoints();
        var source = value.codePoints().toArray();

        var pos = start;
        start = Math.max(start, 0);

        var end = Math.min(start + source.length, pos + source.length);
        end = Math.min(end, codepoints.length);

        for (var i = start; i < end; i++) {
            var codepoint = source[i - pos];

            codepoints[i] = codepoint;
            tfg$setFallbackChar(i, codepoint);
        }
    }

    @Overwrite(remap = false)
    public int length() {
        return text.length;
    }

    @Overwrite(remap = false)
    public void write(String value) {
        write(value, 0);
    }

    @Overwrite(remap = false)
    public void write(String value, int start) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            var pos = start;
            start = Math.max(start, 0);

            var end = Math.min(start + value.length(), pos + value.length());
            end = Math.min(end, text.length);

            for (var i = start; i < end; i++) {
                text[i] = value.charAt(i - pos);
                tfg$getCodepoints()[i] = text[i];
            }

            return;
        }

        tfg$writeCodepoints(value, start);
    }

    @Overwrite(remap = false)
    public void write(ByteBuffer value, int start) {
        var codepoints = tfg$getCodepoints();

        var pos = start;
        var bufferPos = value.position();

        start = Math.max(start, 0);

        var length = value.remaining();
        var end = Math.min(start + length, pos + length);
        end = Math.min(end, codepoints.length);

        for (var i = start; i < end; i++) {
            var codepoint = value.get(bufferPos + i - pos) & 0xFF;

            codepoints[i] = codepoint;
            text[i] = (char) codepoint;
        }
    }

    @Overwrite(remap = false)
    public void write(TextBuffer value) {
        var codepoints = tfg$getCodepoints();
        var access = (CcUtf8TextBufferAccess) (Object) value;

        var end = Math.min(value.length(), codepoints.length);

        for (var i = 0; i < end; i++) {
            var codepoint = access.tfg$codePointAt(i);

            codepoints[i] = codepoint;
            tfg$setFallbackChar(i, codepoint);
        }
    }

    @Overwrite(remap = false)
    public void fill(char c) {
        fill(c, 0, text.length);
    }

    @Overwrite(remap = false)
    public void fill(char c, int start, int end) {
        var codepoints = tfg$getCodepoints();

        start = Math.max(start, 0);
        end = Math.min(end, codepoints.length);

        for (var i = start; i < end; i++) {
            codepoints[i] = c;
            text[i] = c;
        }
    }

    @Overwrite(remap = false)
    public char charAt(int index) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return text[index];
        }

        var codepoint = tfg$getCodepoints()[index];

        return codepoint >= 0 && codepoint <= Character.MAX_VALUE ? (char) codepoint : '?';
    }

    @Overwrite(remap = false)
    public void setChar(int index, char c) {
        if (index >= 0 && index < text.length) {
            text[index] = c;
            tfg$getCodepoints()[index] = c;
        }
    }

    @Override
    public int tfg$codePointAt(int index) {
        return tfg$getCodepoints()[index];
    }

    @Override
    public void tfg$setCodePoint(int index, int codepoint) {
        if (index >= 0 && index < text.length) {
            tfg$getCodepoints()[index] = codepoint;
            tfg$setFallbackChar(index, codepoint);
        }
    }

    @Overwrite(remap = false)
    public String toString() {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return new String(text);
        }

        var codepoints = tfg$getCodepoints();

        return new String(codepoints, 0, codepoints.length);
    }
}
