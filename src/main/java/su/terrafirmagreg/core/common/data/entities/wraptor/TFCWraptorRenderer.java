package su.terrafirmagreg.core.common.data.entities.wraptor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ninni.species.client.model.mob.update_1.WraptorModel;
import com.ninni.species.registry.SpeciesEntityModelLayers;
import com.ninni.species.server.entity.mob.update_1.Wraptor;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import su.terrafirmagreg.core.common.data.entities.sniffer.TFCSniffer;
import su.terrafirmagreg.core.common.data.entities.sniffer.TFCSnifferModel;

public class TFCWraptorRenderer extends MobRenderer<TFCWraptor, TFCWraptorModel<TFCWraptor>> {
    public static final ResourceLocation TEXTURE = new ResourceLocation("species", "textures/entity/wraptor/wraptor.png");
    public static final ResourceLocation TEXTURE_GOTH = new ResourceLocation("species", "textures/entity/wraptor/wraptor_goth.png");
    public static final ResourceLocation TEXTURE_TRANS = new ResourceLocation("species", "textures/entity/wraptor/wraptor_trans.png");

    public TFCWraptorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TFCWraptorModel(ctx.bakeLayer(SpeciesEntityModelLayers.WRAPTOR)), 0.5F);
    }

    public ResourceLocation getTextureLocation(TFCWraptor entity) {
        if (!entity.getName().getString().equalsIgnoreCase("goth") && !entity.getName().getString().equalsIgnoreCase("susie")) {
            return entity.getName().getString().equalsIgnoreCase("trans") ? TEXTURE_TRANS : TEXTURE;
        } else {
            return TEXTURE_GOTH;
        }
    }

    @Override
    protected void scale(TFCWraptor pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
        final float amount = pLivingEntity.getAgeScale();
        pPoseStack.scale(amount, amount, amount);
        super.scale(pLivingEntity, pPoseStack, pPartialTickTime);
    }

}
