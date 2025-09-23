/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */
package su.terrafirmagreg.core.mixins.client.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.client.ClientForgeEventHandler;
import net.dries007.tfc.client.ClientHelpers;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.client.TFGWindManager;

@Mixin(value = ClientForgeEventHandler.class, remap = false)
public abstract class ClientForgeEventHandlerMixin {

    /**
     * redirects tickWind() behavior depending on dimension
     */
    @Inject(method = "tickWind", at = @At("HEAD"), cancellable = true)
    private static void tfg$redirectTickWind(CallbackInfo ci) {
        final Level level = ClientHelpers.getLevel();
        if (level != null && level.dimension().equals(Planet.MARS)) {
            TFGWindManager.Mars.tickWind(level);
            ci.cancel();
        }
    }
}
