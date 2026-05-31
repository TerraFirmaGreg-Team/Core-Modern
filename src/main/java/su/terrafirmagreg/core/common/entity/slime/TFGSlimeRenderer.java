package su.terrafirmagreg.core.common.entity.slime;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TFGSlimeRenderer extends MobRenderer<TFGSlime, TFGSlimeModel<TFGSlime>> {

    public TFGSlimeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TFGSlimeModel<>(ctx.bakeLayer(TFGSlimeModel.LAYER_LOCATION)), 0.4F);
        this.addLayer(new TFGSlimeOuterLayer(this, ctx.getModelSet()));
        this.addLayer(new TFGSlimeFaceLayer(this, ctx.getModelSet()));
    }

    protected void setupRotations(TFGSlime entityLiving, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entityLiving, poseStack, ageInTicks, rotationYaw, partialTicks);
    }

    public ResourceLocation getTextureLocation(TFGSlime entity) {
        return entity.getTextureLocation();
    }
}
