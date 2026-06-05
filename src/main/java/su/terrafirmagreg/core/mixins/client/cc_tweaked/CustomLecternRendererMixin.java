package su.terrafirmagreg.core.mixins.client.cc_tweaked;

import static dan200.computercraft.client.render.ComputerBorderRenderer.MARGIN;
import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_WIDTH;

import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;

import dan200.computercraft.client.model.LecternPocketModel;
import dan200.computercraft.client.render.CustomLecternRenderer;
import dan200.computercraft.client.render.RenderTypes;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.util.Colour;
import dan200.computercraft.shared.lectern.CustomLecternBlockEntity;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.mixins.common.cc_tweaked.CcUtf8TextBufferAccess;

/**
 * Renders Unicode text on pocket computers mounted on lecterns.
 */

@Mixin(value = CustomLecternRenderer.class, remap = false)
public class CustomLecternRendererMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ldan200/computercraft/client/render/CustomLecternRenderer;renderPocketTerminal(Lcom/mojang/blaze3d/vertex/PoseStack;Ldan200/computercraft/client/render/text/FixedWidthFontRenderer$QuadEmitter;Ldan200/computercraft/core/terminal/Terminal;)V"), remap = false)
    private void tfg$renderPocketTerminalWithUnicode(
            PoseStack poseStack,
            FixedWidthFontRenderer.QuadEmitter quadEmitter,
            Terminal terminal,
            CustomLecternBlockEntity lectern,
            float partialTick,
            PoseStack originalPoseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay) {
        var margin = tfg$applyScaledPocketComputerTerminalTransform(poseStack, terminal);

        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            FixedWidthFontRenderer.drawTerminal(
                    quadEmitter,
                    0,
                    0,
                    terminal,
                    margin.y,
                    margin.y,
                    margin.x,
                    margin.x);

            return;
        }

        FixedWidthFontRenderer.drawTerminalBackground(
                quadEmitter,
                0,
                0,
                terminal,
                margin.y,
                margin.y,
                margin.x,
                margin.x);

        FixedWidthFontRenderer.drawCursor(quadEmitter, 0, 0, terminal);

        tfg$renderUnicodeOverlay(poseStack, buffer, terminal);
    }

    @Unique
    private static Vector2f tfg$applyScaledPocketComputerTerminalTransform(PoseStack poseStack, Terminal terminal) {
        var width = terminal.getWidth() * FONT_WIDTH;
        var height = terminal.getHeight() * FONT_HEIGHT;

        var scaleX = LecternPocketModel.TERM_WIDTH / (width + MARGIN * 2.0f);
        var scaleY = LecternPocketModel.TERM_HEIGHT / (height + MARGIN * 2.0f);
        var scale = Math.min(scaleX, scaleY);

        poseStack.scale(scale, scale, -1.0f);

        var marginX = ((LecternPocketModel.TERM_WIDTH / scale) - width) / 2.0f;
        var marginY = ((LecternPocketModel.TERM_HEIGHT / scale) - height) / 2.0f;

        poseStack.translate(marginX, marginY, 0);

        return new Vector2f(marginX, marginY);
    }

    @Unique
    private static void tfg$renderUnicodeOverlay(PoseStack poseStack, MultiBufferSource buffer, Terminal terminal) {
        var font = Minecraft.getInstance().font;
        var palette = terminal.getPalette();

        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, 0.002f);

        var matrix = poseStack.last().pose();

        for (var y = 0; y < terminal.getHeight(); y++) {
            var textLine = terminal.getLine(y);
            var textColourLine = terminal.getTextColourLine(y);
            var textAccess = (CcUtf8TextBufferAccess) (Object) textLine;

            for (var x = 0; x < textLine.length(); x++) {
                var codepoint = textAccess.tfg$codePointAt(x);

                if (codepoint <= 0 || codepoint == ' ') {
                    continue;
                }

                var text = new String(Character.toChars(codepoint));
                var textColour = palette.getRenderColours(
                        FixedWidthFontRenderer.getColour(textColourLine.charAt(x), Colour.BLACK));

                var drawX = x * FONT_WIDTH + Math.max(0.0f, (FONT_WIDTH - font.width(text)) / 2.0f);
                var drawY = y * FONT_HEIGHT;

                font.drawInBatch(
                        text,
                        drawX,
                        drawY,
                        textColour,
                        false,
                        matrix,
                        buffer,
                        Font.DisplayMode.NORMAL,
                        0,
                        RenderTypes.FULL_BRIGHT_LIGHTMAP);
            }
        }

        poseStack.popPose();
    }
}
