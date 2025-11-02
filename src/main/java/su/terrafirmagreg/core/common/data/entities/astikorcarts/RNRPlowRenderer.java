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

/**
 * Renderer for the RNR Plow entity. This class handles rendering the plow's
 * components with their textures and ensures proper alignment
 * with the entity's model positions.
 */
public final class RNRPlowRenderer extends DrawnRenderer<RNRPlow, RNRPlowModel> {

    private static final ResourceLocation TEX_WHEEL = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_wheel.png");
    private static final ResourceLocation TEX_AXIS = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_axis.png");
    private static final ResourceLocation TEX_HOPPER_0 = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_hopper_0.png");
    private static final ResourceLocation TEX_HOPPER_1 = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_hopper_1.png");
    private static final ResourceLocation TEX_SHAFTS = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_shafts.png");
    private static final ResourceLocation TEX_BLADES = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/rnr_plow/rnr_plow_blades.png");

    /**
     * Constructs the RNR Plow renderer.
     *
     * @param renderManager EntityRendererProvider context.
     */
    public RNRPlowRenderer(final EntityRendererProvider.Context renderManager) {
        super(renderManager, new RNRPlowModel(renderManager.bakeLayer(RNRPlowModel.LAYER_LOCATION)));
        this.shadowRadius = 1.0F;
    }

    /**
     * Retrieves the texture location for the RNR Plow entity.
     *
     * @param entity The entity being rendered.
     * @return The texture resource location for the wheels.
     */
    @Override
    public @NotNull ResourceLocation getTextureLocation(final @NotNull RNRPlow entity) {
        return TEX_WHEEL;
    }

    /**
     * Renders the contents of the RNR Plow entity, applying textures to its components.
     *
     * @param entity      The RNR Plow entity being rendered.
     * @param delta       The partial tick time.
     * @param stack       The pose stack for transformations.
     * @param source      The buffer source for rendering.
     * @param packedLight The lightmap coordinates.
     */
    @Override
    protected void renderContents(final RNRPlow entity, final float delta, final PoseStack stack, final MultiBufferSource source, final int packedLight) {
        stack.pushPose();
        this.model.getBody().translateAndRotate(stack);

        // Render each part of the plow with its texture.
        renderPartForceVisible(this.model.getAxis(), TEX_AXIS, stack, source, packedLight);
        renderPartForceVisible(this.model.getTriangle0(), TEX_HOPPER_0, stack, source, packedLight);
        renderPartForceVisible(this.model.getTriangle1(), TEX_HOPPER_1, stack, source, packedLight);
        renderPartForceVisible(this.model.getShaftsGroup(), TEX_SHAFTS, stack, source, packedLight);
        renderPartForceVisible(this.model.getUpperShaft(0), TEX_BLADES, stack, source, packedLight);
        renderPartForceVisible(this.model.getUpperShaft(1), TEX_BLADES, stack, source, packedLight);
        renderPartForceVisible(this.model.getUpperShaft(2), TEX_BLADES, stack, source, packedLight);

        stack.popPose();
    }

    /**
     * Renders a specific part of the model, temporarily forcing it to be visible.
     *
     * @param part        The model part to render.
     * @param tex         The texture to apply to the part.
     * @param stack       The pose stack for transformations.
     * @param source      The buffer source for rendering.
     * @param light       The lightmap coordinates.
     */
    private static void renderPartForceVisible(ModelPart part, ResourceLocation tex, PoseStack stack, MultiBufferSource source, int light) {
        final boolean old = part.visible;
        part.visible = true;
        final VertexConsumer vc = source.getBuffer(RenderType.entityCutoutNoCull(tex));
        part.render(stack, vc, light, OverlayTexture.NO_OVERLAY);
        part.visible = old;
    }
}
