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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.OSAPI;

import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Converts computer labels between Lua UTF-8 byte strings and Java Unicode strings.
 */

@Mixin(value = OSAPI.class, remap = false)
public class OSAPIMixin {

    @Shadow
    @Final
    private IAPIEnvironment apiEnvironment;

    @Inject(method = "getComputerLabel", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$getComputerLabelUtf8(CallbackInfoReturnable<Object[]> cir) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        var label = apiEnvironment.getLabel();

        if (label == null) {
            cir.setReturnValue(null);
            return;
        }

        cir.setReturnValue(new Object[] { tfg$encodeUtf8ForLua(label) });
    }

    @Inject(method = "setComputerLabel", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$setComputerLabelUtf8(Optional<String> label, CallbackInfo ci) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        apiEnvironment.setLabel(label
                .map(OSAPIMixin::tfg$decodeUtf8OrLegacy)
                .map(OSAPIMixin::tfg$normaliseLabelUtf8)
                .orElse(null));

        ci.cancel();
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
    private static String tfg$normaliseLabelUtf8(String label) {
        var out = new StringBuilder();
        var count = 0;

        var iterator = label.codePoints().iterator();

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

    @Unique
    private static String tfg$encodeUtf8ForLua(String text) {
        var bytes = text.getBytes(StandardCharsets.UTF_8);
        var out = new StringBuilder(bytes.length);

        for (var b : bytes) {
            out.append((char) (b & 0xFF));
        }

        return out.toString();
    }
}
