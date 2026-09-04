/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */
package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.utils.CalendarSleepHelper;
import su.terrafirmagreg.core.utils.MarsEnvironmentalHelpers;
import su.terrafirmagreg.core.utils.SnowCorrection;

// higher priority to inject just before TFC does with its environmental helper
@Mixin(value = ServerLevel.class, priority = 900)
public abstract class ServerLevelMixin {
    /**
     * injects just before TFC's {@link net.dries007.tfc.mixin.ServerLevelMixin} inject, allowing for redirect of extraterrestrial weather events
     * <p>
     *     NOTE: this works in conjunction with {@link su.terrafirmagreg.core.mixins.common.tfc.EnvironmentHelpersMixin} to override planetary weather behavior. This first triggers mars-specific weather, and {@code EnvironmentalHelpersMixin} then cancels overworld weather.
     * </p>
     */
    @Inject(method = "tickChunk", at = @At(value = "TAIL"))
    private void onEnvironmentTick(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        final ServerLevel level = (ServerLevel) (Object) this;

        if (level.dimension().equals(Planet.MARS)) {
            MarsEnvironmentalHelpers.tickChunk(level, chunk, level.getProfiler());
        }

        if (TFGConfig.SERVER.enableSnowCorrection.get() && chunk.getLevel().dimension().equals(Level.OVERWORLD)) {
            SnowCorrection.onTickChunk(level, chunk);
        }

        // Ad Astra's ServerLevelMixin also injects at TAIL with a popPush, which pops a section
        // it doesn't own (vanilla has already popped everything inside tickChunk by TAIL).
        // Push a sacrificial section so Ad Astra's popPush consumes it instead of corrupting the stack.
        level.getProfiler().push("adastra$tickChunk_compat");
    }

    /**
     * Backport of TFC 4 {@code ServerLevelMixin#onWakeUpAllPlayers}: advance the TFC calendar when sleeping
     * outside the Overworld (Nether/Beneath, planets, etc.).
     */
    @Inject(method = "wakeUpAllPlayers", at = @At("TAIL"))
    private void tfg$onWakeUpAllPlayers(CallbackInfo ci) {
        CalendarSleepHelper.onPlayersFinishedSleeping((ServerLevel) (Object) this);
    }
}
