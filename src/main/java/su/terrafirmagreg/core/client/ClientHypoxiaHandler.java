package su.terrafirmagreg.core.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import earth.terrarium.adastra.api.systems.OxygenApi;

import su.terrafirmagreg.core.TFGCore;

/**
 * Client half of {@link su.terrafirmagreg.core.common.environment.HypoxiaHandler}.
 * <p>
 * It's required because the oxygen bar would go back to full when starting hypoxia thinking it can refill
 */
@Mod.EventBusSubscriber(modid = TFGCore.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public final class ClientHypoxiaHandler {

    private ClientHypoxiaHandler() {
    }

    @SubscribeEvent
    public static void onLivingBreathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player) || player.getAbilities().invulnerable) {
            return;
        }
        if (OxygenApi.API.hasOxygen(player.level())) {
            return;
        }
        event.setCanBreathe(true);
        event.setCanRefillAir(false);
    }
}
