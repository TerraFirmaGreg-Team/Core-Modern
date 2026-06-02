package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dan200.computercraft.api.lua.Coerced;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.apis.TermMethods;
import dan200.computercraft.core.terminal.Terminal;

import su.terrafirmagreg.core.compat.cc_tweaked.CcUtf8TextBufferAccess;
import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = TermMethods.class, remap = false)
public abstract class TermMethodsMixin {

    @Shadow
    protected abstract Terminal getTerminal();

    @Unique
    private static String tfg$decodeUtf8OrLegacy(String text) {
        var bytes = new byte[text.length()];

        for (var i = 0; i < text.length(); i++) {
            var c = text.charAt(i);

            if (c > 255) {
                return text;
            }

            bytes[i] = (byte) c;
        }

        var decoder = StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try {
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException ignored) {
            return text;
        }
    }

    @Inject(method = "write", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$writeUtf8(Coerced<String> textA, CallbackInfo ci) throws LuaException {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        var text = tfg$decodeUtf8OrLegacy(textA.value());
        var width = text.codePointCount(0, text.length());
        var terminal = getTerminal();

        synchronized (terminal) {
            terminal.write(text);
            terminal.setCursorPos(terminal.getCursorX() + width, terminal.getCursorY());
        }

        ci.cancel();
    }

    @Unique
    private static byte[] tfg$copyBytes(ByteBuffer buffer) {
        var copy = buffer.slice();
        var bytes = new byte[copy.remaining()];

        copy.get(bytes);

        return bytes;
    }

    @Unique
    private static String tfg$decodeUtf8Bytes(byte[] bytes) {
        var decoder = StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try {
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException ignored) {
            return null;
        }
    }

    @Unique
    private static int[] tfg$getUtf8Offsets(int[] codepoints) {
        var offsets = new int[codepoints.length];
        var offset = 0;

        for (var i = 0; i < codepoints.length; i++) {
            offsets[i] = offset;
            offset += new String(Character.toChars(codepoints[i])).getBytes(StandardCharsets.UTF_8).length;
        }

        return offsets;
    }

    @Unique
    private static char tfg$getColour(ByteBuffer buffer, int byteIndex, int charIndex, int byteLength, int charLength) {
        var position = buffer.position();

        if (buffer.remaining() == charLength) {
            return (char) (buffer.get(position + charIndex) & 0xFF);
        }

        return (char) (buffer.get(position + byteIndex) & 0xFF);
    }

    @Inject(method = "blit", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$blitUtf8(ByteBuffer text, ByteBuffer textColour, ByteBuffer backgroundColour, CallbackInfo ci) throws LuaException {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        var textBytes = tfg$copyBytes(text);
        var decoded = tfg$decodeUtf8Bytes(textBytes);

        if (decoded == null) {
            return;
        }

        var codepoints = decoded.codePoints().toArray();
        var offsets = tfg$getUtf8Offsets(codepoints);

        var textColourLength = textColour.remaining();
        var backgroundColourLength = backgroundColour.remaining();

        var validTextColourLength = textColourLength == textBytes.length || textColourLength == codepoints.length;
        var validBackgroundColourLength = backgroundColourLength == textBytes.length || backgroundColourLength == codepoints.length;

        if (!validTextColourLength || !validBackgroundColourLength) {
            throw new LuaException("Arguments must be the same length");
        }

        var terminal = getTerminal();

        synchronized (terminal) {
            var cursorX = terminal.getCursorX();
            var cursorY = terminal.getCursorY();

            if (cursorY >= 0 && cursorY < terminal.getHeight()) {
                var textLine = terminal.getLine(cursorY);
                var textColourLine = terminal.getTextColourLine(cursorY);
                var backgroundColourLine = terminal.getBackgroundColourLine(cursorY);
                var textAccess = (CcUtf8TextBufferAccess) (Object) textLine;

                for (var i = 0; i < codepoints.length; i++) {
                    var x = cursorX + i;

                    if (x < 0 || x >= terminal.getWidth()) {
                        continue;
                    }

                    var byteIndex = offsets[i];

                    textAccess.tfg$setCodePoint(x, codepoints[i]);
                    textColourLine.setChar(x, tfg$getColour(textColour, byteIndex, i, textBytes.length, codepoints.length));
                    backgroundColourLine.setChar(x, tfg$getColour(backgroundColour, byteIndex, i, textBytes.length, codepoints.length));
                }
            }

            terminal.setCursorPos(cursorX + codepoints.length, cursorY);
            terminal.setChanged();
        }

        ci.cancel();
    }
}
