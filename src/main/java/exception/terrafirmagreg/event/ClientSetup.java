package exception.terrafirmagreg.event;

import exception.terrafirmagreg.TerraFirmaGreg;
import exception.terrafirmagreg.client.particle.ColoredDripParticle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TerraFirmaGreg.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerParticles(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(TerraFirmaGreg.COLORED_DRIP_PARTICLE.get(), ColoredDripParticle.ColoredDripParticleFactory::new);
    }
}
