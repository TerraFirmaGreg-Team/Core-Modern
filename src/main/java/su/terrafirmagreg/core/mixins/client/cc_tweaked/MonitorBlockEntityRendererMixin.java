package su.terrafirmagreg.core.mixins.client.cc_tweaked;

import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.render.text.FixedWidthFontRenderer.FONT_WIDTH;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;

import dan200.computercraft.client.render.RenderTypes;
import dan200.computercraft.client.render.monitor.MonitorBlockEntityRenderer;
import dan200.computercraft.client.render.monitor.MonitorRenderState;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.util.Colour;
import dan200.computercraft.shared.peripheral.monitor.ClientMonitor;
import dan200.computercraft.shared.peripheral.monitor.MonitorBlockEntity;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.mixins.common.cc_tweaked.CcUtf8TextBufferAccess;

/**
 * Renders Unicode text on CC:Tweaked monitors with Minecraft's font.
 */

@Mixin(value = MonitorBlockEntityRenderer.class, remap = false)
public class MonitorBlockEntityRendererMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Ldan200/computercraft/client/render/monitor/MonitorBlockEntityRenderer;renderTerminal(Lorg/joml/Matrix4f;Ldan200/computercraft/shared/peripheral/monitor/ClientMonitor;Ldan200/computercraft/client/render/monitor/MonitorRenderState;Ldan200/computercraft/core/terminal/Terminal;FF)V"), remap = false)
    private void tfg$renderTerminalWithUnicode(
            Matrix4f matrix,
            ClientMonitor monitor,
            MonitorRenderState renderState,
            Terminal terminal,
            float xMargin,
            float yMargin,
            MonitorBlockEntity blockEntity,
            float partialTicks,
            PoseStack transform,
            MultiBufferSource bufferSource,
            int lightmapCoord,
            int overlayLight) {
        tfg$renderTerminal(matrix, monitor, renderState, terminal, xMargin, yMargin);

        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        tfg$renderUnicodeOverlay(transform, bufferSource, terminal);
    }

    @Unique
    private static void tfg$renderUnicodeOverlay(PoseStack transform, MultiBufferSource bufferSource, Terminal terminal) {
        var font = Minecraft.getInstance().font;
        var palette = terminal.getPalette();

        transform.pushPose();
        transform.translate(0.0f, 0.0f, 0.001f);

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

                var drawX = x * FONT_WIDTH + Math.max(0.0f, (FONT_WIDTH - font.width(text)) / 2.0f);
                var drawY = y * FONT_HEIGHT;

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

    @Invoker("renderTerminal")
    private static void tfg$renderTerminal(
            Matrix4f matrix,
            ClientMonitor monitor,
            MonitorRenderState renderState,
            Terminal terminal,
            float xMargin,
            float yMargin) {
        throw new AssertionError();
    }
}
