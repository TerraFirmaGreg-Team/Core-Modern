/*
 * This file includes code from FTB (https://github.com/FTBTeam/FTB-Teams)
 * Copyright © 2015-2021 the original authors.
 * Licensed under the Apache License
 */
package su.terrafirmagreg.core.mixins.client.ftb;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import dev.ftb.mods.ftbteams.FTBTeams;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import dev.ftb.mods.ftbteams.api.client.KnownClientPlayer;
import dev.ftb.mods.ftbteams.data.ClientTeam;
import dev.ftb.mods.ftbteams.data.ClientTeamManagerImpl;

@OnlyIn(Dist.CLIENT)
@Mixin(value = ClientTeamManagerImpl.class, remap = false)
public abstract class ClientTeamManagerImplMixin implements ClientTeamManager {

    @Shadow
    @Final
    private Map<UUID, ClientTeam> teamMap;

    @Shadow
    @Final
    private Map<UUID, KnownClientPlayer> knownPlayers;

    @Shadow
    private ClientTeam selfTeam;

    @Shadow
    private KnownClientPlayer selfKnownPlayer;

    /**
     * Позволяет людям без лицухи нормально играть с модом FTB-Teams. Конвертируя пиратский ник в UUID, который
     * FTB-Teams берет из лиц. профиля.
     */
    @Inject(method = "initSelfDetails", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void tfg$initSelfDetails(UUID selfTeamID, CallbackInfo ci) {
        selfTeam = teamMap.get(selfTeamID);
        UUID userId = UUIDTypeAdapter.fromString(Minecraft.getInstance().player.getStringUUID());
        selfKnownPlayer = knownPlayers.get(userId);
        if (selfKnownPlayer == null) {
            FTBTeams.LOGGER.error(
                    "Local player id {} was not found in the known players list [{}]! FTB Teams will not be able to function correctly!",
                    userId, String.join(",", knownPlayers.keySet().stream().map(UUID::toString).toList()));
        }

        ci.cancel();
    }

}
