package su.terrafirmagreg.core.common.data.entities.astikorcarts;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import de.mennomax.astikorcarts.client.renderer.entity.DrawnRenderer;

import su.terrafirmagreg.core.TFGCore;

public final class RNRPlowRenderer extends DrawnRenderer<RNRPlow, RNRPlowModel> {

    private static final ResourceLocation TEX_WHEEL = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_wheel.png");
    private static final ResourceLocation TEX_AXIS = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_axis.png");
    private static final ResourceLocation TEX_HOPPER_0 = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_hopper_0.png");
    private static final ResourceLocation TEX_HOPPER_1 = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_hopper_1.png");
    private static final ResourceLocation TEX_SHAFTS = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_shafts.png");
    private static final ResourceLocation TEX_BLADES = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_blades.png");

    public RNRPlowRenderer(final EntityRendererProvider.Context renderManager) {
        super(renderManager, new RNRPlowModel(renderManager.bakeLayer(RNRPlowModel.LAYER_LOCATION)));
        this.shadowRadius = 1.0F;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(final @NotNull RNRPlow entity) {
        // Default texture which gets applied to the second render pass (wheels).
        return TEX_WHEEL;
    }

    @Override
    protected void renderContents(final RNRPlow entity, final float delta, final PoseStack stack, final MultiBufferSource source, final int packedLight) {
        stack.pushPose();
        this.model.getBody().translateAndRotate(stack);

        renderPartForceVisible(this.model.getAxis(), TEX_AXIS, stack, source, packedLight);
        renderPartForceVisible(this.model.getTriangle0(), TEX_HOPPER_0, stack, source, packedLight);
        renderPartForceVisible(this.model.getTriangle1(), TEX_HOPPER_1, stack, source, packedLight);
        renderPartForceVisible(this.model.getShaftsGroup(), TEX_SHAFTS, stack, source, packedLight);
        renderPartForceVisible(this.model.getUpperShaft(0), TEX_BLADES, stack, source, packedLight);
        renderPartForceVisible(this.model.getUpperShaft(1), TEX_BLADES, stack, source, packedLight);
        renderPartForceVisible(this.model.getUpperShaft(2), TEX_BLADES, stack, source, packedLight);

        stack.popPose();
    }

    private static void renderPartForceVisible(ModelPart part, ResourceLocation tex, PoseStack stack, MultiBufferSource source, int light) {
        final boolean old = part.visible;
        part.visible = true;
        final VertexConsumer vc = source.getBuffer(RenderType.entityCutoutNoCull(tex));
        part.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        part.visible = old;
    }
}
