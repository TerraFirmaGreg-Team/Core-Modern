package su.terrafirmagreg.core.common.entity.dragonfly;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;

// Taken from Better End, MIT license

public class DragonflyRenderer extends MobRenderer<Dragonfly, DragonflyModel> {
    private static final ResourceLocation TEXTURE = TFGCore.id("textures/entity/animal/dragonfly.png");
    private static final RenderType GLOW = RenderType.eyes(TFGCore.id("textures/entity/animal/dragonfly_glow.png"));

    public DragonflyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DragonflyModel(ctx.bakeLayer(DragonflyModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new EyesLayer<Dragonfly, DragonflyModel>(this) {
            @Override
            public RenderType renderType() {
                return GLOW;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(Dragonfly entity) {
        return TEXTURE;
    }
}
