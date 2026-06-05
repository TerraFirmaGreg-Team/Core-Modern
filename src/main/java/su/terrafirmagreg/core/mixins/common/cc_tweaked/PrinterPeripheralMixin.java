package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dan200.computercraft.api.lua.Coerced;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.peripheral.printer.PrinterBlockEntity;
import dan200.computercraft.shared.peripheral.printer.PrinterPeripheral;

import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Decodes UTF-8 Lua strings before printer page text and titles are saved.
 */

@Mixin(value = PrinterPeripheral.class, remap = false)
public class PrinterPeripheralMixin {

    @Shadow
    @Final
    private PrinterBlockEntity printer;

    @Shadow
    private Terminal getCurrentPage() throws LuaException {
        throw new AssertionError();
    }

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

    @Unique
    private static String tfg$normaliseTitle(String title) {
        var out = new StringBuilder();
        var count = 0;

        var iterator = title.codePoints().iterator();

        while (iterator.hasNext() && count < 32) {
            var codepoint = iterator.nextInt();

            if (codepoint == 0 || codepoint == '\r' || codepoint == '\n') {
                break;
            }

            if (codepoint != 167) {
                out.appendCodePoint(codepoint);
                count++;
            }
        }

        return out.toString();
    }

    @Inject(method = "write", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$writeUtf8(Coerced<String> textA, CallbackInfo ci) throws LuaException {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        var text = tfg$decodeUtf8OrLegacy(textA.value());
        var page = getCurrentPage();

        page.write(text);
        page.setCursorPos(page.getCursorX() + text.codePointCount(0, text.length()), page.getCursorY());

        ci.cancel();
    }

    @Inject(method = "setPageTitle", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$setPageTitleUtf8(Optional<String> title, CallbackInfo ci) throws LuaException {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        getCurrentPage();

        var decoded = title
                .map(PrinterPeripheralMixin::tfg$decodeUtf8OrLegacy)
                .map(PrinterPeripheralMixin::tfg$normaliseTitle)
                .orElse("");

        ((PrinterBlockEntityAccessor) (Object) printer).tfg$setPageTitle(decoded);

        ci.cancel();
    }
}
