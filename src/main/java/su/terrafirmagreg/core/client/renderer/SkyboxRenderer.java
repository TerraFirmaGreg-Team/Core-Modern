package su.terrafirmagreg.core.client.renderer;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.util.UVRange;
import su.terrafirmagreg.core.config.TFGConfig;

// Taken from the mod Nether Skyboxes
// https://www.curseforge.com/minecraft/mc-mods/nether-skyboxes

public class SkyboxRenderer {

    public enum Option {
        REALISTIC,
        PIXELATED,
        SOLID_COLOR
    }

    @Nullable
    private static final ResourceLocation VENUS_SKY;
    private static final TextureManager textureMgr;

    public static final UVRange[] TEXTURE_FACES = new UVRange[] {
            new UVRange(0, 0, 1.0F / 3.0F, 1.0F / 2.0F), // bottom
            new UVRange(1.0F / 3.0F, 1.0F / 2.0F, 2.0F / 3.0F, 1), // north
            new UVRange(2.0F / 3.0F, 0, 1, 1.0F / 2.0F), // south
            new UVRange(1.0F / 3.0F, 0, 2.0F / 3.0F, 1.0F / 2.0F), // top
            new UVRange(2.0F / 3.0F, 1.0F / 2.0F, 1, 1), // east
            new UVRange(0, 1.0F / 2.0F, 1.0F / 3.0F, 1) // west
    };

    static {
        textureMgr = Minecraft.getInstance().getTextureManager();

        VENUS_SKY = switch (TFGConfig.CLIENT.VENUS_SKYBOX.get()) {
            case PIXELATED -> TFGCore.id("textures/environment/venus_sky_16px.png");
            case REALISTIC -> TFGCore.id("textures/environment/venus_sky_realistic.png");
            default -> null;
        };
    }

    public static void renderVenusSky(RenderLevelStageEvent event) {
        if (VENUS_SKY == null)
            return;

        Level level = Minecraft.getInstance().level;
        if (level == null || level.dimension() != Planet.VENUS)
            return;

        PoseStack poseStack = event.getPoseStack();

        RenderSystem.depthMask(false);

        var timeOfDay = level.dimensionType().timeOfDay(level.dayTime());
        float brightness = Mth.clamp(Mth.cos(timeOfDay * (float) (Math.PI * 2)) * 2.0F + 0.5F, 0.0f, 1.0f);

        RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0f);

        textureMgr.bindForSetup(VENUS_SKY);
        RenderSystem.setShaderTexture(0, VENUS_SKY);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        for (int face = 0; face < 6; face++) {
            poseStack.pushPose();
            if (face == 1) {
                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(90.0F)));
            } else if (face == 2) {
                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-90.0F)));
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(180.0F)));
            } else if (face == 3) {
                poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(180.0F)));
            } else if (face == 4) {
                poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(90.0F)));
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(-90.0F)));
            } else if (face == 5) {
                poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(-90.0F)));
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(90.0F)));
            }

            Matrix4f matrix4f = poseStack.last().pose();
            UVRange tex = TEXTURE_FACES[face];
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, -100.0F).uv(tex.minU(), tex.minV()).endVertex();
            bufferBuilder.vertex(matrix4f, -100.0F, -100.0F, 100.0F).uv(tex.minU(), tex.maxV()).endVertex();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, 100.0F).uv(tex.maxU(), tex.maxV()).endVertex();
            bufferBuilder.vertex(matrix4f, 100.0F, -100.0F, -100.0F).uv(tex.maxU(), tex.minV()).endVertex();

            poseStack.popPose();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }
}
