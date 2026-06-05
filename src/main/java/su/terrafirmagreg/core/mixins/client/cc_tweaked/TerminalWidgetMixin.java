package su.terrafirmagreg.core.mixins.client.cc_tweaked;

import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_WIDTH;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import dan200.computercraft.client.gui.widgets.TerminalWidget;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.util.Colour;
import dan200.computercraft.core.util.StringUtil;
import dan200.computercraft.shared.computer.core.InputHandler;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.mixins.common.cc_tweaked.CcUtf8TextBufferAccess;

/**
 * Adds UTF-8 paste handling and Unicode rendering to CC:Tweaked's terminal widget.
 */

@Mixin(value = TerminalWidget.class, remap = false)
public class TerminalWidgetMixin {

    @Shadow
    @Final
    private InputHandler computer;

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

    @Inject(method = "paste", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$pasteUtf8(CallbackInfo ci) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        var clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
        var paste = tfg$encodePasteUtf8(clipboard);

        if (paste.remaining() > 0) {
            computer.paste(paste);
        }

        ci.cancel();
    }

    @Unique
    private static ByteBuffer tfg$encodePasteUtf8(String clipboard) {
        var output = ByteBuffer.allocate(StringUtil.MAX_PASTE_LENGTH);
        var iterator = clipboard.codePoints().iterator();

        while (iterator.hasNext()) {
            var codepoint = iterator.nextInt();

            if (codepoint == '\r' || codepoint == '\n' || codepoint == 0) {
                break;
            }

            var bytes = new String(Character.toChars(codepoint)).getBytes(StandardCharsets.UTF_8);

            if (bytes.length > output.remaining()) {
                break;
            }

            output.put(bytes);
        }

        output.flip();

        return output.asReadOnlyBuffer();
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$charTypedUtf8(char ch, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        if (ch == 0 || ch == '\r' || ch == '\n') {
            return;
        }

        if (ch <= 255) {
            return;
        }

        if (computer instanceof CcUtf8ClientInputAccess input) {
            input.tfg$charTypedCodepoint(ch);
            cir.setReturnValue(true);
        }
    }
}
