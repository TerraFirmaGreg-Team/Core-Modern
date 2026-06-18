package su.terrafirmagreg.core.common.entity.fox;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;

public class TFGFoxCollarLayer extends RenderLayer<TFGFox, TFGFoxModel<TFGFox>> {
    private static final ResourceLocation FOX_COLLAR_LOCATION = TFGCore.id("textures/entity/fox/fox_collar.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "fox_collar"), "main");
    private final TFGFoxModel foxModel;

    public TFGFoxCollarLayer(RenderLayerParent<TFGFox, TFGFoxModel<TFGFox>> renderer, EntityModelSet ctx) {
        super(renderer);
        this.foxModel = new TFGFoxModel(ctx.bakeLayer(LAYER_LOCATION));
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TFGFox entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float yaw,
            float pitch) {
        if (entity.getOwnerUUID() != null && !entity.isInvisible()) {
            float[] colors = entity.getCollarColor().getTextureDiffuseColors();
            coloredCutoutModelCopyLayerRender(this.getParentModel(), this.foxModel, FOX_COLLAR_LOCATION, poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, ageInTicks, yaw, pitch,
                    partialTick, colors[0], colors[1], colors[2]);
        }
    }
}
