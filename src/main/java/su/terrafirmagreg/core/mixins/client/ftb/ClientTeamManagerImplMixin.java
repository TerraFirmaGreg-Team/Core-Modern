package su.terrafirmagreg.core.mixins.client.ftb;

import com.mojang.util.UUIDTypeAdapter;
import dev.ftb.mods.ftbteams.FTBTeams;
import dev.ftb.mods.ftbteams.api.client.ClientTeamManager;
import dev.ftb.mods.ftbteams.api.client.KnownClientPlayer;
import dev.ftb.mods.ftbteams.data.ClientTeam;
import dev.ftb.mods.ftbteams.data.ClientTeamManagerImpl;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

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

    @Inject(method = "initSelfDetails", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void tfg$initSelfDetails(UUID selfTeamID, CallbackInfo ci) {
        selfTeam = teamMap.get(selfTeamID);
        UUID userId = UUIDTypeAdapter.fromString(Minecraft.getInstance().player.getStringUUID());
        selfKnownPlayer = knownPlayers.get(userId);
        if (selfKnownPlayer == null) {
            FTBTeams.LOGGER.error("Local player id {} was not found in the known players list [{}]! FTB Teams will not be able to function correctly!",
                    userId, String.join(",", knownPlayers.keySet().stream().map(UUID::toString).toList()));
        }

        ci.cancel();
    }

}