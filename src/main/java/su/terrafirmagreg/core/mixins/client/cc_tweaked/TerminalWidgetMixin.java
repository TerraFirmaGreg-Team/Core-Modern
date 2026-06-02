package su.terrafirmagreg.core.mixins.client.cc_tweaked;

import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_WIDTH;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import dan200.computercraft.client.gui.widgets.TerminalWidget;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.util.Colour;

import su.terrafirmagreg.core.compat.cc_tweaked.CcUtf8TextBufferAccess;
import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = TerminalWidget.class, remap = false)
public class TerminalWidgetMixin {

    @Shadow
    @Final
    private Terminal terminal;

    @Shadow
    @Final
    private int innerX;

    @Shadow
    @Final
    private int innerY;

    @Inject(method = "renderWidget", at = @At("TAIL"), remap = false)
    private void tfg$renderUnicodeOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        tfg$renderUnicodeOverlay(graphics);
    }

    @Unique
    private void tfg$renderUnicodeOverlay(GuiGraphics graphics) {
        var font = Minecraft.getInstance().font;
        var palette = terminal.getPalette();

        for (var y = 0; y < terminal.getHeight(); y++) {
            var textLine = terminal.getLine(y);
            var textColourLine = terminal.getTextColourLine(y);
            var backColourLine = terminal.getBackgroundColourLine(y);
            var textAccess = (CcUtf8TextBufferAccess) (Object) textLine;

            for (var x = 0; x < textLine.length(); x++) {
                var codepoint = textAccess.tfg$codePointAt(x);

                if (codepoint >= 0 && codepoint <= 255) {
                    continue;
                }

                var text = new String(Character.toChars(codepoint));
                var drawX = innerX + x * FONT_WIDTH;
                var drawY = innerY + y * FONT_HEIGHT;

                var backgroundColour = palette.getRenderColours(
                        FixedWidthFontRenderer.getColour(backColourLine.charAt(x), Colour.BLACK));

                var textColour = palette.getRenderColours(
                        FixedWidthFontRenderer.getColour(textColourLine.charAt(x), Colour.WHITE));

                graphics.fill(drawX, drawY, drawX + FONT_WIDTH, drawY + FONT_HEIGHT, backgroundColour);

                var glyphWidth = font.width(text);
                var xOffset = Math.max(0, (FONT_WIDTH - glyphWidth) / 2);

                graphics.drawString(font, text, drawX + xOffset, drawY, textColour, false);
            }
        }
    }
}
