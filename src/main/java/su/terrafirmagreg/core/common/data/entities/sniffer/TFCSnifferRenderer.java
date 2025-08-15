package su.terrafirmagreg.core.common.data.entities.sniffer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.TFGCore;

public class TFCSnifferRenderer extends MobRenderer<TFCSniffer, TFCSnifferModel<TFCSniffer>> {
    private static final ResourceLocation SNIFFER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/sniffer/sniffer.png");
    private static final ResourceLocation SHEARED_SNIFFER = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/entity/sniffer/sheared_sniffer.png");


    public TFCSnifferRenderer(EntityRendererProvider.Context context) {
        super(context, new TFCSnifferModel<>(context.bakeLayer(ModelLayers.SNIFFER)), 1.1F);
    }

    @Override
    protected void scale(TFCSniffer pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
        final float amount = 1.2f * pLivingEntity.getAgeScale();
        pPoseStack.scale(amount, amount, amount);
        super.scale(pLivingEntity, pPoseStack, pPartialTickTime);
    }

    public @NotNull ResourceLocation getTextureLocation(TFCSniffer sniffer) {
        return  sniffer.hasWool() ?   SNIFFER_LOCATION : SHEARED_SNIFFER;
    }

}
