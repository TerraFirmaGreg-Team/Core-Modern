package su.terrafirmagreg.tfcambiental.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import su.terrafirmagreg.tfcambiental.TFCAmbiental;
import su.terrafirmagreg.tfcambiental.TFCAmbientalConfig;
import su.terrafirmagreg.tfcambiental.capability.TemperatureCapability;

@Mod.EventBusSubscriber(modid = TFCAmbiental.MOD_ID)
public class ForgeEventHandler {
    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            TemperatureCapability capability = new TemperatureCapability();
            capability.setPlayer(player);
            event.addCapability(TemperatureCapability.KEY, capability);
        }
    }

    @SubscribeEvent
    public static void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
        var level = event.player.level();
        if (!level.isClientSide()) {
            event.player.getCapability(TemperatureCapability.CAPABILITY).ifPresent(TemperatureCapability::update);
        } else {
            // Drip when wet
            if (level.getRandom().nextInt() <= TFCAmbientalConfig.CLIENT.drippiness.get() && !event.player.isUnderWater()) {
                event.player.getCapability(TemperatureCapability.CAPABILITY).ifPresent(temperatureCapability -> {
                    if (temperatureCapability.getWetness() > 0.5f && level.getRandom().nextDouble() < (0.005D * temperatureCapability.getWetness())) {
                        var pos = event.player.position();
                        level.addParticle(ParticleTypes.FALLING_WATER, Mth.lerp(level.random.nextDouble(), pos.x - 0.3D, pos.x + 0.3D), pos.y + 1.7D,
                                Mth.lerp(level.random.nextDouble(), pos.z - 0.3D, pos.z + 0.3D), 0.0D, 0.0D, 0.0D);
                    }
                });
            }
        }
    }
}
