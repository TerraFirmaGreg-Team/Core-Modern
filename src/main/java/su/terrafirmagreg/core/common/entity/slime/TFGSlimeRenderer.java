package su.terrafirmagreg.core.common.entity.slime;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TFGSlimeRenderer extends MobRenderer<TFGSlime, TFGSlimeModel<TFGSlime>> {
    private static final ResourceLocation SLIME_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/slime/slime.png");

    public TFGSlimeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TFGSlimeModel<>(ctx.bakeLayer(TFGSlimeModel.INNER_LAYER_LOCATION)), 0.4F);
        this.addLayer(new TFGSlimeOuterLayer(this, ctx.getModelSet()));
    }

    protected void setupRotations(TFGSlime entityLiving, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entityLiving, poseStack, ageInTicks, rotationYaw, partialTicks);
    }

    protected void scale(TFGSlime entity, PoseStack poseStack, float scale) {
        float amount = entity.getGeneticSize();
        poseStack.scale(amount, amount, amount);
        super.scale(entity, poseStack, scale);
    }

    public ResourceLocation getTextureLocation(TFGSlime entity) {
        return SLIME_LOCATION;
    }
}
