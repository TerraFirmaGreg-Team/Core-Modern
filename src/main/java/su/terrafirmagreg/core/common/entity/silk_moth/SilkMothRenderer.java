package su.terrafirmagreg.core.common.entity.silk_moth;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;

// Taken from Better End, MIT license

public class SilkMothRenderer extends MobRenderer<SilkMoth, SilkMothModel> {
    private static final ResourceLocation TEXTURE = TFGCore.id("textures/entity/animal/silk_moth.png");

    public SilkMothRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SilkMothModel(ctx.bakeLayer(SilkMothModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SilkMoth entity) {
        return TEXTURE;
    }
}
