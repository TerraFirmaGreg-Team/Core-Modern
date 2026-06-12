package su.terrafirmagreg.tfcambiental;

import static su.terrafirmagreg.tfcambiental.TFCAmbientalConfig.CLIENT;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.common.ForgeMod;

import su.terrafirmagreg.tfcambiental.capability.TemperatureCapability;

@OnlyIn(Dist.CLIENT)
public class TFCAmbientalGuiRenderer {
    public static final ResourceLocation COLD_VIGNETTE = new ResourceLocation("tfcambiental:textures/gui/cold_vignette.png");
    public static final ResourceLocation HOT_VIGNETTE = new ResourceLocation("tfcambiental:textures/gui/hot_vignette.png");
    public static final ResourceLocation MINUS = new ResourceLocation("tfcambiental:textures/gui/lower.png");
    public static final ResourceLocation PLUS = new ResourceLocation("tfcambiental:textures/gui/higher.png");
    public static final ResourceLocation MINUSER = new ResourceLocation("tfcambiental:textures/gui/lowerer.png");
    public static final ResourceLocation PLUSER = new ResourceLocation("tfcambiental:textures/gui/higherer.png");
    public static final ResourceLocation WET = new ResourceLocation("tfcambiental:textures/gui/wet.png");

    public static void render(ForgeGui gui, GuiGraphics stack, float partialTicks, int widthh, int heightt) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.getCameraEntity() instanceof Player player) {
            if (player.isCreative() || !player.isAlive() || player.isSpectator()) {
                return;
            }
            TemperatureCapability tempSystem = player.getCapability(TemperatureCapability.CAPABILITY, null).orElse(TemperatureCapability.DEFAULT);
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            float redCol, greenCol, blueCol;

            drawTemperatureVignettes(width, height, player);

            int healthRowHeight = mc.getWindow().getGuiScaledHeight();
            int armorRowHeight = healthRowHeight - 51;
            int mid = mc.getWindow().getGuiScaledWidth() / 2;

            RenderSystem.enableBlend();
            float AVERAGE = TFCAmbientalConfig.COMMON.averageTemperature.get().floatValue();
            float HOT_THRESHOLD = TFCAmbientalConfig.COMMON.hotThreshold.get().floatValue();
            float COOL_THRESHOLD = TFCAmbientalConfig.COMMON.coolThreshold.get().floatValue();
            if (tempSystem.getTemperature() > AVERAGE) {
                float hotRange = HOT_THRESHOLD - AVERAGE + 2;
                float red = Math.max(0, Math.min(1, (tempSystem.getTemperature() - AVERAGE) / hotRange));
                redCol = 1F;
                greenCol = 1.0F - red / 2.4F;
                blueCol = 1.0F - red / 1.6F;
            } else {
                float coolRange = AVERAGE - COOL_THRESHOLD - 2;
                float blue = Math.max(0, Math.min(1, (AVERAGE - tempSystem.getTemperature()) / coolRange));
                redCol = 1.0F - blue / 1.6F;
                greenCol = 1.0F - blue / 2.4F;
                blueCol = 1.0F;
            }
            RenderSystem.setShaderColor(redCol, greenCol, blueCol, 0.9F);
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(redCol, greenCol, blueCol, 0.9F);

            float change = tempSystem.getTemperatureChange();

            if (change > 0) {
                if (change > TemperatureCapability.HIGH_CHANGE) {
                    drawTexturedModalRect(gui, stack, mid - 8, armorRowHeight - 4, 16, 16, PLUSER);
                } else {
                    drawTexturedModalRect(gui, stack, mid - 8, armorRowHeight - 4, 16, 16, PLUS);
                }
            } else {
                if (change < -TemperatureCapability.HIGH_CHANGE) {
                    drawTexturedModalRect(gui, stack, mid - 8, armorRowHeight - 4, 16, 16, MINUSER);
                } else {
                    drawTexturedModalRect(gui, stack, mid - 8, armorRowHeight - 4, 16, 16, MINUS);
                }
            }

            if (player.isCrouching()) {
                float shiftHeight = CLIENT.guiOffset.get().floatValue();
                int air = player.getAirSupply();
                if (player.isEyeInFluidType(ForgeMod.WATER_TYPE.get()) || air < 300 || player.getArmorValue() > 0) {
                    shiftHeight += 10.0f;
                }

                Font f = gui.getFont();
                String tempStr = String.format("%.1f\u00BA -> %.1f\u00BA", scaleTemperature(tempSystem.getTemperature()), scaleTemperature(tempSystem.getTargetTemperature()));
                stack.drawString(f, tempStr, mid + 50 - f.width(tempStr) / 2F, armorRowHeight + 1 - shiftHeight, TFCAmbientalGuiRenderer.getIntFromColor(redCol, greenCol, blueCol), false);

                String wetStr = String.format("%.1f -> %.1f", tempSystem.getWetness(), Math.max(0, tempSystem.getTargetWetness()));
                stack.drawString(f, wetStr, mid - 10 - f.width(tempStr), armorRowHeight + 1 - shiftHeight, TFCAmbientalGuiRenderer.getIntFromColor(redCol, greenCol, blueCol), false);
                drawTexturedModalRect(gui, stack, mid - 26 - f.width(tempStr), armorRowHeight - 4 - shiftHeight, 16, 16, WET);
            }

            RenderSystem.setShaderColor(1f, 1f, 1f, 0.9F);
            RenderSystem.disableBlend();
        }
    }

    private static void drawTexturedModalRect(ForgeGui gui, GuiGraphics stack, float x, float y, float width, float height, ResourceLocation loc) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindForSetup(loc);
        RenderSystem.setShaderTexture(0, loc);

        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(x, y + height, -90.0D).uv(0.0f, 1.0f).endVertex();
        buffer.vertex(x + width, y + height, -90.0D).uv(1.0F, 1.0F).endVertex();
        buffer.vertex(x + width, y, -90.0D).uv(1.0F, 0.0F).endVertex();
        buffer.vertex(x, y, -90.0D).uv(0.0F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawTemperatureVignettes(int width, int height, Player player) {
        ResourceLocation vignetteLocation = null;
        TemperatureCapability tempSystem = player.getCapability(TemperatureCapability.CAPABILITY, null).orElse(TemperatureCapability.DEFAULT);
        float temperature = tempSystem.getTemperature();

        float BURN_THRESHOLD = TFCAmbientalConfig.COMMON.burnThreshold.get().floatValue();
        float FREEZE_THRESHOLD = TFCAmbientalConfig.COMMON.freezeThreshold.get().floatValue();

        float opacity = 0f;
        if (temperature > BURN_THRESHOLD - 2.5f) {
            vignetteLocation = HOT_VIGNETTE;
            opacity = Math.min(0.80f, (temperature - (BURN_THRESHOLD - 2.5f)) / 18);
        } else if (temperature < FREEZE_THRESHOLD + 2.5f) {
            vignetteLocation = COLD_VIGNETTE;
            opacity = Math.min(0.80f, ((FREEZE_THRESHOLD + 2.5f) - temperature) / 18);
        }

        if (vignetteLocation != null) {
            RenderSystem.setShaderTexture(0, vignetteLocation);

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(0.0D, height, -90.0D).uv(0.0F, 1.0F).endVertex();
            buffer.vertex(width, height, -90.0D).uv(1.0F, 1.0F).endVertex();
            buffer.vertex(width, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
            buffer.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
            tessellator.end();

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static int getIntFromColor(float red, float green, float blue) {
        int R = Math.round(255 * red);
        int G = Math.round(255 * green);
        int B = Math.round(255 * blue);

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return 0xFF000000 | R | G | B;
    }

    private static float scaleTemperature(float celsius) {
        if (CLIENT.useFahrenheit.get()) {
            return celsius * (9f / 5f) + 32f;
        } else {
            return celsius;
        }
    }
}
