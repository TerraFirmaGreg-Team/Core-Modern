package su.terrafirmagreg.core.mixins.common.minecraft;

import java.awt.*;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;

import net.dries007.tfc.client.ClimateRenderCache;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import earth.terrarium.adastra.api.planets.Planet;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    // Disable fog blending of clouds on venus

    @Shadow
    @Final
    private static ResourceLocation CLOUDS_LOCATION;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Nullable
    private ClientLevel level;
    @Shadow
    @Nullable
    private CloudStatus prevCloudsType;
    @Shadow
    @Nullable
    private VertexBuffer cloudBuffer;
    @Shadow
    private int prevCloudX;
    @Shadow
    private int prevCloudY;
    @Shadow
    private int prevCloudZ;
    @Shadow
    private Vec3 prevCloudColor;
    @Shadow
    private boolean generateClouds;
    @Shadow
    private int ticks;

    @Shadow
    protected abstract BufferBuilder.RenderedBuffer buildClouds(BufferBuilder builder, double x, double y, double z, Vec3 cloudColor);

    @Unique
    private final Vec3 tfg$bottomVenusCloudColor = new Vec3(133f / 255f, 127f / 255f, 31f / 255f);
    @Unique
    private final Vec3 tfg$middleVenusCloudColor = new Vec3(165f / 255f, 162f / 255f, 32f / 255f);
    @Unique
    private final Vec3 tfg$topVenusCloudColor = new Vec3(190f / 255f, 191f / 255f, 48f / 255f);

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void tfg$renderClouds(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        if (level != null && level.dimension() == Planet.VENUS) {
            final float height = level.effects().getCloudHeight();

            // The tint for things like going dark at night or during rain/thunder
            Vec3 cloudTint = this.level.getCloudColor(partialTick);

            Vec2 wind = ClimateRenderCache.INSTANCE.getWind();

            // Overworld clouds have a speed of 0.03f
            tfg$renderCloudLayer(height + 60, 0.3f, tfg$tintClouds(tfg$topVenusCloudColor, cloudTint), wind, poseStack, projectionMatrix, partialTick, camX, camY, camZ);
            tfg$renderCloudLayer(height + 30, 0.2f, tfg$tintClouds(tfg$middleVenusCloudColor, cloudTint), wind, poseStack, projectionMatrix, partialTick, camX, camY, camZ);
            tfg$renderCloudLayer(height, 0.1f, tfg$tintClouds(tfg$bottomVenusCloudColor, cloudTint), wind, poseStack, projectionMatrix, partialTick, camX, camY, camZ);
            ci.cancel();
        }
    }

    @Unique
    private Vec3 tfg$tintClouds(Vec3 cloudColor, Vec3 cloudTint) {
        return new Vec3(cloudColor.x * cloudTint.x, cloudColor.y * cloudTint.y, cloudColor.z * cloudTint.z);
    }

    // A copy and paste of the vanilla renderCloudLayer but with some bits changed that were too complicated to do as a mixin

    @Unique
    private void tfg$renderCloudLayer(float height, float speed, Vec3 cloudColor, Vec2 wind, PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ) {

        if (!Float.isNaN(height)) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.depthMask(true);

			// TODO: The wind velocity seems to work correctly, but the direction does not.
			// I have no idea what's going on with the math here, but I suspect there's some matrix/quaternion
			// bullshit going on that's way beyond me.
            double velocityX = (this.ticks + partialTick) * wind.x;
            double velocityZ = (this.ticks + partialTick) * wind.y;
			// Here below is vanilla
            double cloudX = ((camX + velocityX) / 12.0) * speed;
            double cloudY = height - (float) camY + 0.33F;
            double cloudZ = ((camZ + velocityZ) / 12.0 + 0.33F) * speed;
            cloudX -= Mth.floor(cloudX / 2048.0) * 2048;
            cloudZ -= Mth.floor(cloudZ / 2048.0) * 2048;
            float xInCell = (float) (cloudX - Mth.floor(cloudX));
            float yInCell = (float) (cloudY / 4.0 - Mth.floor(cloudY / 4.0)) * 4.0F;
            float zInCell = (float) (cloudZ - Mth.floor(cloudZ));
            int cellX = (int) Math.floor(cloudX);
            int cellY = (int) Math.floor(cloudY / 4.0);
            int cellZ = (int) Math.floor(cloudZ);
            if (cellX != this.prevCloudX
                    || cellY != this.prevCloudY
                    || cellZ != this.prevCloudZ
                    || this.minecraft.options.getCloudsType() != this.prevCloudsType
                    || this.prevCloudColor.distanceToSqr(cloudColor) > 2.0E-4) {
                this.prevCloudX = cellX;
                this.prevCloudY = cellY;
                this.prevCloudZ = cellZ;
                this.prevCloudColor = cloudColor;
                this.prevCloudsType = this.minecraft.options.getCloudsType();
                this.generateClouds = true;
            }

            if (this.generateClouds) {
                this.generateClouds = false;
                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                if (this.cloudBuffer != null) {
                    this.cloudBuffer.close();
                }

                this.cloudBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                BufferBuilder.RenderedBuffer renderedBuffer = this.buildClouds(bufferBuilder, cloudX, cloudY, cloudZ, cloudColor);
                this.cloudBuffer.bind();
                this.cloudBuffer.upload(renderedBuffer);
                VertexBuffer.unbind();
            }

            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);

            // Custom fog color goes here
            RenderSystem.setShaderFogColor((float) cloudColor.x, (float) cloudColor.y, (float) cloudColor.z);

            poseStack.pushPose();
            poseStack.scale(12.0F, 1.0F, 12.0F);
            poseStack.translate(-xInCell, yInCell, -zInCell);
            if (this.cloudBuffer != null) {
                this.cloudBuffer.bind();
                int v = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

                for (int w = v; w < 2; w++) {
                    if (w == 0) {
                        RenderSystem.colorMask(false, false, false, false);
                    } else {
                        RenderSystem.colorMask(true, true, true, true);
                    }

                    ShaderInstance shaderInstance = RenderSystem.getShader();
                    this.cloudBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shaderInstance);
                }

                VertexBuffer.unbind();
            }

            poseStack.popPose();
            RenderSystem.enableCull();
            FogRenderer.levelFogColor();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }
}
