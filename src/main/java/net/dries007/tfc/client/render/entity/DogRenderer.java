/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.render.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.model.entity.DogCollarLayer;
import net.dries007.tfc.client.model.entity.DogModel;
import net.dries007.tfc.common.entities.livestock.pet.Dog;
import net.dries007.tfc.util.Helpers;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfInterface;
import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfVariant;

import java.util.Map;

public class DogRenderer extends MobRenderer<Dog, DogModel>
{
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

    public DogRenderer(EntityRendererProvider.Context ctx)
    {
		super(ctx, new DogModel(ctx.bakeLayer(ModelLayers.WOLF)), 0.5F);
		this.addLayer(new DogCollarLayer(this));
    }

    @Override
    protected void setupRotations(Dog entity, PoseStack stack, float age, float yaw, float partialTicks)
    {
        super.setupRotations(entity, stack, age, yaw, partialTicks);
        if (entity.isSleeping())
        {
            stack.translate(0.2F, 0.1F, 0.0D);
            stack.mulPose(Axis.ZP.rotationDegrees(90f));
        }
    }

	@Override
	public ResourceLocation getTextureLocation(Dog entity) {
		TFCWolfVariant variant = ((TFCWolfInterface) entity).tfg$getVariant();
		return LOCATION_BY_VARIANT.get(variant);
	}
}
