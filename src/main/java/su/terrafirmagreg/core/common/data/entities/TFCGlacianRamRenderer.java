package su.terrafirmagreg.core.common.data.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.adastra.AdAstra;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TFCGlacianRamRenderer extends MobRenderer<TFCGlacianRam, TFCGlacianRamModel<TFCGlacianRam>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(AdAstra.MOD_ID, "textures/entity/mob/glacian_ram/glacian_ram.png");
    private static final ResourceLocation SHEARED_TEXTURE = new ResourceLocation(AdAstra.MOD_ID, "textures/entity/mob/glacian_ram/sheared_glacian_ram.png");

    public TFCGlacianRamRenderer(EntityRendererProvider.Context context) {
        super(context, new TFCGlacianRamModel(context.bakeLayer(TFCGlacianRamModel.LAYER_LOCATION)), 0.7F);
    }


    @Override
    protected void scale(TFCGlacianRam pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
        final float amount = pLivingEntity.getAgeScale();
        pPoseStack.scale(amount, amount, amount);
        super.scale(pLivingEntity, pPoseStack, pPartialTickTime);
    }

    public @NotNull ResourceLocation getTextureLocation(TFCGlacianRam entity) {
        return entity.hasWool() ?   TEXTURE : SHEARED_TEXTURE;
    }



}
