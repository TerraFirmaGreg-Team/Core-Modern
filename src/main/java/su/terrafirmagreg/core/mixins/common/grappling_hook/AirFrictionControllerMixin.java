/*
 * This file includes code from Grappling Hook - Reforged (https://www.curseforge.com/minecraft/mc-mods/grappling-hook-mod-reforged)
 * Copyright (c) 2024 Chummycho
 * Licensed under the GPLv3 License
 */
package su.terrafirmagreg.core.mixins.common.grappling_hook;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.yyon.grapplinghook.controllers.AirfrictionController;
import com.yyon.grapplinghook.controllers.GrappleController;
import com.yyon.grapplinghook.utils.GrappleCustomization;
import com.yyon.grapplinghook.utils.Vec;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(value = AirfrictionController.class, remap = false)
public class AirFrictionControllerMixin extends GrappleController {

    public AirFrictionControllerMixin(int grapplehookEntityId, int entityId, Level world, Vec pos, int controllerid,
            GrappleCustomization custom) {
        super(grapplehookEntityId, entityId, world, pos, controllerid, custom);
    }

    @Inject(method = "updatePlayerPos", at = @At(value = "NEW", target = "(DDD)Lcom/yyon/grapplinghook/utils/Vec;", ordinal = 0), cancellable = true)
    private void tfg$injectLadderFix(CallbackInfo ci, @Local Entity entity) {
        if (entity instanceof LivingEntity living && living.onClimbable()) {
            this.unattach();
            ci.cancel();
        }
    }
}
