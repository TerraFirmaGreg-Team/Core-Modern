/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.common.data.entities.animals.tfcjerboa;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.dries007.tfc.client.render.entity.SimpleMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;

public class TFCJerboaRenderer extends SimpleMobRenderer<TFCJerboa, TFCJerboaModel> {
    public TFCJerboaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TFCJerboaModel(ctx.bakeLayer(TFCJerboaModel.LAYER_LOCATION)), "jerboa", 0.2f, false, 1f, false, true, null);
    }

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TFGCore.MOD_ID, "textures/entity/animal/jerboa.png");

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TFCJerboa entity) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(@NotNull TFCJerboa entity, @NotNull PoseStack poseStack, float ageInTicks, float yaw, float partialTick) {
        super.setupRotations(entity, poseStack, ageInTicks, yaw, partialTick);
        if (entity.isClimbing()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(90f));
            poseStack.popPose();
        }
        if (entity.draggingAnimation.isStarted()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            poseStack.popPose();
        }
    }
}
