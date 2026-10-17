package su.terrafirmagreg.core.mixins.common.minecraft;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.client.renderer.CloudRendererHelper;
import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow
    @Nullable
    private ClientLevel level;
    @Shadow
    private int ticks;

    @Unique
    private float tfg$partialTick;

    @Inject(method = "renderClouds", at = @At("HEAD"))
    private void tfg$renderClouds(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        tfg$partialTick = partialTick;
    }

    @ModifyVariable(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;getCloudHeight()F"), argsOnly = true, ordinal = 0)
    private double tfg$shiftX(double camX) {
        final double vanillaDrift = ((float) ticks + tfg$partialTick) * 0.03f; // Subtract original movement
        return camX - vanillaDrift - CloudRendererHelper.offsetX(tfg$partialTick) * CloudRendererHelper.currentSpeedMult; // Add our offset
    }

    @ModifyVariable(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;getCloudHeight()F"), argsOnly = true, ordinal = 2)
    private double tfg$shiftZ(double camZ) {
        return camZ - CloudRendererHelper.offsetZ(tfg$partialTick) * CloudRendererHelper.currentSpeedMult; // Add our offset
    }

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FDDD)V"))
    private void tfg$renderLevel(LevelRenderer instance, PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ) {
        assert level != null;
        if (level.dimension() == Level.OVERWORLD) {
            CloudRendererHelper.render(0, 0.1f, CloudRendererHelper.earthCloudColor,
                    () -> instance.renderClouds(poseStack, projectionMatrix, partialTick, camX, camY, camZ));
        } else if (level.dimension() == Planet.VENUS) {
            CloudRendererHelper.render(0, 0.5f, CloudRendererHelper.bottomVenusCloudColor,
                    () -> instance.renderClouds(poseStack, projectionMatrix, partialTick, camX, camY, camZ));

			if (TFGConfig.CLIENT.VENUS_CLOUD_LAYERS.get()) {
				CloudRendererHelper.render(30, 1f, CloudRendererHelper.middleVenusCloudColor,
					() -> instance.renderClouds(poseStack, projectionMatrix, partialTick, camX, camY, camZ));

				CloudRendererHelper.render(60, 1.5f, CloudRendererHelper.topVenusCloudColor,
					() -> instance.renderClouds(poseStack, projectionMatrix, partialTick, camX, camY, camZ));
			}
        }
        // TODO: jupiter
    }
}
