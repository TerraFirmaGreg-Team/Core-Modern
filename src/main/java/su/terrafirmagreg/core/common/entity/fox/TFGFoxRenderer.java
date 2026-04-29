package su.terrafirmagreg.core.common.entity.fox;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TFGFoxRenderer extends MobRenderer<TFGFox, TFGFoxModel<TFGFox>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox.png");
    private static final ResourceLocation SLEEP_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox_sleep.png");

    public TFGFoxRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TFGFoxModel<>(ctx.bakeLayer(ModelLayers.FOX)), 0.4F);
    }

    protected void setupRotations(TFGFox entityLiving, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entityLiving, poseStack, ageInTicks, rotationYaw, partialTicks);
    }

    public ResourceLocation getTextureLocation(TFGFox entity) {
        return entity.isSleeping() ? SLEEP_TEXTURE : TEXTURE;
    }
}
