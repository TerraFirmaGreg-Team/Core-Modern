package su.terrafirmagreg.core.common.entity.slime;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

import su.terrafirmagreg.core.TFGCore;

public class TFGSlimeOuterLayer extends RenderLayer<TFGSlime, TFGSlimeModel<TFGSlime>> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            TFGCore.id("slime_outer"), "main");

    private final TFGSlimeModel model;

    public TFGSlimeOuterLayer(RenderLayerParent<TFGSlime, TFGSlimeModel<TFGSlime>> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new TFGSlimeModel(modelSet.bakeLayer(LAYER_LOCATION));
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TFGSlime livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = minecraft.shouldEntityAppearGlowing(livingEntity) && livingEntity.isInvisible();
        if (!livingEntity.isInvisible() || flag) {
            VertexConsumer vertexconsumer;
            if (flag) {
                vertexconsumer = buffer.getBuffer(RenderType.outline(this.getTextureLocation(livingEntity)));
            } else {
                vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(livingEntity)));
            }

            this.getParentModel().copyPropertiesTo(this.model);
            this.model.prepareMobModel(livingEntity, limbSwing, limbSwingAmount, partialTicks);
            this.model.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
        }

    }
}
