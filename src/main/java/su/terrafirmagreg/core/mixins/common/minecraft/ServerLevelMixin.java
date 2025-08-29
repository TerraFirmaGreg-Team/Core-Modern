/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */
package su.terrafirmagreg.core.mixins.common.minecraft;

import earth.terrarium.adastra.api.planets.Planet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.utils.MarsEnvironmentalHelpers;

// higher priority to inject just before TFC does with its environmental helper
@Mixin(value = ServerLevel.class, priority = 900)
public abstract class ServerLevelMixin {
    /**
     * injects just before TFC's {@link net.dries007.tfc.mixin.ServerLevelMixin} inject, allowing for redirect of extraterrestrial weather events
     *
     */
    @Inject(method = "tickChunk", at = @At(value = "TAIL"))
    private void onEnvironmentTick(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci)
    {
        if (chunk.getLevel().dimension().equals(Planet.MARS))
        {
            final ServerLevel level = (ServerLevel) (Object) this;
            MarsEnvironmentalHelpers.tickChunk(level, chunk, level.getProfiler());
        }
    }
}
