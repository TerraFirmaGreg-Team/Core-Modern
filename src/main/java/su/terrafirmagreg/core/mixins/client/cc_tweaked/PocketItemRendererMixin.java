package su.terrafirmagreg.core.mixins.client.cc_tweaked;

import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_WIDTH;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

import dan200.computercraft.client.render.PocketItemRenderer;
import dan200.computercraft.client.render.RenderTypes;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.util.Colour;

import su.terrafirmagreg.core.compat.cc_tweaked.CcUtf8TextBufferAccess;
import su.terrafirmagreg.core.config.TFGConfig;

/**
 * Renders Unicode text on pocket computers displayed as items.
 */

@Mixin(value = PocketItemRenderer.class, remap = false)
public class PocketItemRendererMixin {

    @Redirect(method = "renderItem", at = @At(value = "INVOKE", target = "Ldan200/computercraft/client/render/text/FixedWidthFontRenderer;drawTerminal(Ldan200/computercraft/client/render/text/FixedWidthFontRenderer$QuadEmitter;FFLdan200/computercraft/core/terminal/Terminal;FFFF)V"), remap = false)
    private void tfg$drawTerminalWithUnicode(
            FixedWidthFontRenderer.QuadEmitter emitter,
            float x,
            float y,
            Terminal terminal,
            float topMarginSize,
            float bottomMarginSize,
            float leftMarginSize,
            float rightMarginSize,
            PoseStack transform,
            MultiBufferSource bufferSource,
            ItemStack stack,
            int light) {
        FixedWidthFontRenderer.drawTerminal(
                emitter,
                x,
                y,
                terminal,
                topMarginSize,
                bottomMarginSize,
                leftMarginSize,
                rightMarginSize);

        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        tfg$renderUnicodeOverlay(transform, bufferSource, terminal, x, y);
    }

    @Unique
    private static void tfg$renderUnicodeOverlay(
            PoseStack transform,
            MultiBufferSource bufferSource,
            Terminal terminal,
            float startX,
            float startY) {
        var font = Minecraft.getInstance().font;
        var palette = terminal.getPalette();

        transform.pushPose();
        transform.translate(0.0f, 0.0f, 0.002f);

        var matrix = transform.last().pose();

        for (var y = 0; y < terminal.getHeight(); y++) {
            var textLine = terminal.getLine(y);
            var textColourLine = terminal.getTextColourLine(y);
            var textAccess = (CcUtf8TextBufferAccess) (Object) textLine;

            for (var x = 0; x < textLine.length(); x++) {
                var codepoint = textAccess.tfg$codePointAt(x);

                if (codepoint >= 0 && codepoint <= 255) {
                    continue;
                }

                var text = new String(Character.toChars(codepoint));
                var textColour = palette.getRenderColours(
                        FixedWidthFontRenderer.getColour(textColourLine.charAt(x), Colour.WHITE));

                var drawX = startX + x * FONT_WIDTH + Math.max(0.0f, (FONT_WIDTH - font.width(text)) / 2.0f);
                var drawY = startY + y * FONT_HEIGHT;

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
                        RenderTypes.FULL_BRIGHT_LIGHTMAP);
            }
        }

        transform.popPose();
    }
}
