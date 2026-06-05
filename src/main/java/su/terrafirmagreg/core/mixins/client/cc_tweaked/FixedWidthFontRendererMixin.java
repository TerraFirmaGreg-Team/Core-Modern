package su.terrafirmagreg.core.mixins.client.cc_tweaked;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Invoker;

import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Palette;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.core.util.Colour;

import su.terrafirmagreg.core.compat.cc_tweaked.CcUtf8TextBufferAccess;
import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Prevents CC:Tweaked's fixed-width renderer from drawing non-ASCII placeholder glyphs.
 */

@Mixin(value = FixedWidthFontRenderer.class, remap = false)
public class FixedWidthFontRendererMixin {

    @Overwrite(remap = false)
    public static void drawString(FixedWidthFontRenderer.QuadEmitter emitter, float x, float y, TextBuffer text, TextBuffer textColour, Palette palette, int light) {
        var enabled = TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get();
        var textAccess = (CcUtf8TextBufferAccess) (Object) text;

        for (var i = 0; i < text.length(); i++) {
            var colour = palette.getRenderColours(15 - dan200.computercraft.core.terminal.Terminal.getColour(textColour.charAt(i), Colour.BLACK));
            var codepoint = enabled ? textAccess.tfg$codePointAt(i) : text.charAt(i);

            if (enabled && (codepoint < 0 || codepoint > 255)) {
                continue;
            }

            tfg$drawChar(emitter, x + i * 6, y, codepoint, colour, light);
        }
    }

    @Invoker("drawChar")
    private static void tfg$drawChar(FixedWidthFontRenderer.QuadEmitter emitter, float x, float y, int index, int colour, int light) {
        throw new AssertionError();
    }
}
