package su.terrafirmagreg.core.common.entity.fox;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Fox;

public class TFGFoxRenderer extends MobRenderer<TFGFox, TFGFoxModel<TFGFox>> {
    private static final Map<Fox.Type, ResourceLocation> MAIN_TEXTURE = Util.make(Maps.newEnumMap(Fox.Type.class), (map) -> {
        map.put(Fox.Type.SNOW, ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox.png"));
        map.put(Fox.Type.RED, ResourceLocation.withDefaultNamespace("textures/entity/fox/fox.png"));
    });
    private static final Map<Fox.Type, ResourceLocation> SLEEP_TEXTURE = Util.make(Maps.newEnumMap(Fox.Type.class), (map) -> {
        map.put(Fox.Type.SNOW, ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox_sleep.png"));
        map.put(Fox.Type.RED, ResourceLocation.withDefaultNamespace("textures/entity/fox/fox_sleep.png"));
    });

    public TFGFoxRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TFGFoxModel<>(ctx.bakeLayer(TFGFoxModel.LAYER_LOCATION)), 0.4F);
        this.addLayer(new TFGFoxCollarLayer(this, ctx.getModelSet()));
    }

    protected void setupRotations(TFGFox entityLiving, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entityLiving, poseStack, ageInTicks, rotationYaw, partialTicks);
    }

    public ResourceLocation getTextureLocation(TFGFox entity) {
        Fox.Type variant = entity.getVariant();
        return entity.isSleeping() ? SLEEP_TEXTURE.get(variant) : MAIN_TEXTURE.get(variant);
    }
}
