package su.terrafirmagreg.core.mixins.client.cc_tweaked;

import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_WIDTH;
import static dan200.computercraft.shared.media.items.PrintoutItem.LINES_PER_PAGE;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;

import dan200.computercraft.client.render.PrintoutRenderer;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Palette;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.core.util.Colour;

import su.terrafirmagreg.core.compat.cc_tweaked.CcUtf8TextBufferAccess;
import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = PrintoutRenderer.class, remap = false)
public class PrintoutRendererMixin {

    @Inject(method = "drawText(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IIII[Ldan200/computercraft/core/terminal/TextBuffer;[Ldan200/computercraft/core/terminal/TextBuffer;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$drawTextUtf8(
            PoseStack transform,
            MultiBufferSource bufferSource,
            int x,
            int y,
            int start,
            int light,
            TextBuffer[] text,
            TextBuffer[] colours,
            CallbackInfo ci) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        for (var line = 0; line < LINES_PER_PAGE; line++) {
            var index = start + line;

            if (index >= text.length || index >= colours.length) {
                break;
            }

            tfg$drawLine(transform, bufferSource, x, y + line * FONT_HEIGHT, light, text[index], colours[index]);
        }

        ci.cancel();
    }

    @Inject(method = "drawText(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IIII[Ljava/lang/String;[Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$drawStringTextUtf8(
            PoseStack transform,
            MultiBufferSource bufferSource,
            int x,
            int y,
            int start,
            int light,
            String[] text,
            String[] colours,
            CallbackInfo ci) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        for (var line = 0; line < LINES_PER_PAGE; line++) {
            var index = start + line;

            if (index >= text.length || index >= colours.length) {
                break;
            }

            tfg$drawLine(
                    transform,
                    bufferSource,
                    x,
                    y + line * FONT_HEIGHT,
                    light,
                    new TextBuffer(text[index]),
                    new TextBuffer(colours[index]));
        }

        ci.cancel();
    }

    @Unique
    private static void tfg$drawLine(
            PoseStack transform,
            MultiBufferSource bufferSource,
            int startX,
            int startY,
            int light,
            TextBuffer textLine,
            TextBuffer colourLine) {
        var font = Minecraft.getInstance().font;
        var matrix = transform.last().pose();
        var textAccess = (CcUtf8TextBufferAccess) (Object) textLine;

        for (var x = 0; x < textLine.length(); x++) {
            var codepoint = textAccess.tfg$codePointAt(x);

            if (codepoint <= 0 || codepoint == ' ') {
                continue;
            }

            var text = new String(Character.toChars(codepoint));
            var colourChar = x < colourLine.length() ? colourLine.charAt(x) : '0';
            var textColour = Palette.DEFAULT.getRenderColours(
                    FixedWidthFontRenderer.getColour(colourChar, Colour.BLACK));

            var drawX = startX + x * FONT_WIDTH + Math.max(0.0f, (FONT_WIDTH - font.width(text)) / 2.0f);
            var drawY = startY;

            font.drawInBatch(
                    text,
                    drawX,
                    drawY,
                    textColour,
                    false,
                    matrix,
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0,
                    light);
        }
    }
}
