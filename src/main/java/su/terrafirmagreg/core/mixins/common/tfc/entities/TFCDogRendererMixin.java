package su.terrafirmagreg.core.mixins.common.tfc.entities;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.google.common.collect.Maps;

import net.dries007.tfc.client.model.entity.DogCollarLayer;
import net.dries007.tfc.client.model.entity.DogModel;
import net.dries007.tfc.client.render.entity.DogRenderer;
import net.dries007.tfc.common.entities.livestock.pet.Dog;
import net.minecraft.Util;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfInterface;
import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfVariant;

@Mixin(value = DogRenderer.class, remap = false)
public class TFCDogRendererMixin extends MobRenderer<Dog, DogModel> {
    @Unique
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

    public TFCDogRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx, new DogModel(ctx.bakeLayer(ModelLayers.WOLF)), 0.5F);
        this.addLayer(new DogCollarLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Dog entity) {
        TFCWolfVariant variant = ((TFCWolfInterface) entity).tfg$getVariant();
        return LOCATION_BY_VARIANT.get(variant);
    }
}
