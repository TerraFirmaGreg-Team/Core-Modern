package su.terrafirmagreg.core.mixins.client.cc_tweaked;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dan200.computercraft.client.render.monitor.MonitorTextureBufferShader;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.util.Colour;

import su.terrafirmagreg.core.compat.cc_tweaked.CcUtf8TextBufferAccess;
import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Adds UTF-8 support to CC:Tweaked's text buffer.
 */

@Mixin(value = MonitorTextureBufferShader.class, remap = false)
public class MonitorTextureBufferShaderMixin {

    @Overwrite(remap = false)
    public static void setTerminalData(ByteBuffer buffer, Terminal terminal) {
        var width = terminal.getWidth();
        var height = terminal.getHeight();
        var enabled = TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get();

        var pos = 0;

        for (var y = 0; y < height; y++) {
            var text = terminal.getLine(y);
            var textColour = terminal.getTextColourLine(y);
            var background = terminal.getBackgroundColourLine(y);
            var textAccess = (CcUtf8TextBufferAccess) (Object) text;

            for (var x = 0; x < width; x++) {
                var codepoint = enabled ? textAccess.tfg$codePointAt(x) : text.charAt(x);

                if (enabled && (codepoint < 0 || codepoint > 255)) {
                    codepoint = 0;
                }

                buffer.put(pos, (byte) codepoint);
                buffer.put(pos + 1, (byte) FixedWidthFontRenderer.getColour(textColour.charAt(x), Colour.WHITE));
                buffer.put(pos + 2, (byte) FixedWidthFontRenderer.getColour(background.charAt(x), Colour.BLACK));

                pos += 3;
            }
        }

        buffer.limit(pos);
    }
}
