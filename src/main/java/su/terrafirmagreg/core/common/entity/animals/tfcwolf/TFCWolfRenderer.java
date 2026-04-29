package su.terrafirmagreg.core.common.entity.animals.tfcwolf;

import java.util.Map;

import com.google.common.collect.Maps;

import net.dries007.tfc.common.entities.ai.predator.PackPredator;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;

public class TFCWolfRenderer extends MobRenderer<PackPredator, TFGWolfModel> {
    private static final Map<TFCWolfVariant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(TFCWolfVariant.class), (map) -> {
        map.put(TFCWolfVariant.DEFAULT, TFGCore.id("textures/entity/animal/dog/default.png"));
        map.put(TFCWolfVariant.ASHEN, TFGCore.id("textures/entity/animal/dog/ashen.png"));
        map.put(TFCWolfVariant.BLACK, TFGCore.id("textures/entity/animal/dog/black.png"));
        map.put(TFCWolfVariant.CHESTNUT, TFGCore.id("textures/entity/animal/dog/chestnut.png"));
        map.put(TFCWolfVariant.RUSTY, TFGCore.id("textures/entity/animal/dog/rusty.png"));
        map.put(TFCWolfVariant.SNOWY, TFGCore.id("textures/entity/animal/dog/snowy.png"));
        map.put(TFCWolfVariant.SPOTTED, TFGCore.id("textures/entity/animal/dog/spotted.png"));
        map.put(TFCWolfVariant.STRIPED, TFGCore.id("textures/entity/animal/dog/striped.png"));
        map.put(TFCWolfVariant.WOODS, TFGCore.id("textures/entity/animal/dog/woods.png"));
    });

    public TFCWolfRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TFGWolfModel(ctx.bakeLayer(TFGWolfModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(PackPredator entity) {
        TFCWolfVariant variant = ((TFCWolfInterface) entity).tfg$getVariant();
        return LOCATION_BY_VARIANT.get(variant);
    }

}
