/*
 * This file includes code from Grappling Hook - Reforged (https://www.curseforge.com/minecraft/mc-mods/grappling-hook-mod-reforged)
 * Copyright (c) 2024 Chummycho
 * Licensed under the GPLv3 License
 */
package su.terrafirmagreg.core.mixins.common.grappling_hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.yyon.grapplinghook.controllers.ForcefieldController;
import com.yyon.grapplinghook.controllers.GrappleController;

import net.minecraft.world.entity.Entity;

import su.terrafirmagreg.core.common.environment.DimensionEnvironment;
import su.terrafirmagreg.core.common.environment.EnvironmentSystem;

@Mixin(value = ForcefieldController.class, remap = false)
public class ForcefieldControllerMixin {
    @ModifyConstant(method = "updatePlayerPos", constant = @Constant(doubleValue = -0.05))
    private double tfg$gravity(double original) {
        Entity entity = ((GrappleController) (Object) this).entity;
        return original * EnvironmentSystem.getGravity(entity.level(), entity.blockPosition()) / DimensionEnvironment.EARTH_GRAVITY;
    }
}
