package su.terrafirmagreg.core.common.environment;

import com.spacegravity.spacegravity.api.SpaceGravityApi;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.utils.TFGModsResolver;

/**
 *  Toggles the "Player: real zero gravity" mod when in zerog.
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID)
public final class ZeroGravityHandler {

    private ZeroGravityHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (!TFGModsResolver.SPACE_GRAVITY.isLoaded()) {
            return;
        }

        boolean zeroGravity = !player.isPassenger() && !player.isSpectator() && EnvironmentSystem.getGravity(player.level(), player.blockPosition()) <= 0.0f;
        boolean zeroGravityEnabled = SpaceGravityApi.isZeroGravityEnabled(player);
        if (zeroGravity && !zeroGravityEnabled) {
            SpaceGravityApi.enableZeroGravity(player);
        } else if (!zeroGravity && zeroGravityEnabled) {
            SpaceGravityApi.disableZeroGravity(player);
        }
    }
}
