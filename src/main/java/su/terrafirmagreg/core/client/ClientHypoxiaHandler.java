package su.terrafirmagreg.core.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import earth.terrarium.adastra.api.systems.OxygenApi;
import lombok.Getter;
import lombok.Setter;

import su.terrafirmagreg.core.TFGCore;

/**
 * Client half of {@link su.terrafirmagreg.core.common.environment.HypoxiaHandler}.
 * <p>
 * It's required because the oxygen bar would go back to full when starting hypoxia thinking it can refill
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public final class ClientHypoxiaHandler {

    @Setter
    @Getter
    private static int oxygenDebt = 0;

    private ClientHypoxiaHandler() {
    }

    /** It runs lowest simply to be after Create diving blue suit */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player) || player.getAbilities().invulnerable) {
            return;
        }
        if (OxygenApi.API.hasOxygen(player.level()) && oxygenDebt == 0) {
            return;
        }
        event.setCanBreathe(true);
        event.setCanRefillAir(false);
    }

    /** The vanilla air bar is replaced by AirLevelOverlay that also draws the locked bubbles */
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.AIR_LEVEL.id())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        oxygenDebt = 0;
    }
}
