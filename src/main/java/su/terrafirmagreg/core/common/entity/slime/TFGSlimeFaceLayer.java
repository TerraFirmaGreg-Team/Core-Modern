package su.terrafirmagreg.core.common.entity.slime;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

import su.terrafirmagreg.core.TFGCore;

@SuppressWarnings({ "unchecked" })
public class TFGSlimeFaceLayer extends RenderLayer<TFGSlime, TFGSlimeModel<TFGSlime>> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            TFGCore.id("slime_face"), "main");

    private final TFGSlimeModel model;

    public TFGSlimeFaceLayer(RenderLayerParent<TFGSlime, TFGSlimeModel<TFGSlime>> renderer, EntityModelSet ctx) {
        super(renderer);
        this.model = new TFGSlimeModel(ctx.bakeLayer(LAYER_LOCATION));
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TFGSlime livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        if (!livingEntity.isInvisible()) {
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(TFGCore.id("textures/entity/slime/face/" + livingEntity.getVariant().getSerializedName() + "_smile.png")));

            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(livingEntity, limbSwing, limbSwingAmount, partialTicks);
            this.model.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
