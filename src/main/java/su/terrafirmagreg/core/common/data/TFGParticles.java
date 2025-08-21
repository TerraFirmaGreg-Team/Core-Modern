package su.terrafirmagreg.core.common.data;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import su.terrafirmagreg.core.TFGCore;

public class TFGParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TFGCore.MOD_ID);

    //Register Particles v
    public static final RegistryObject<SimpleParticleType> RAILGUN_BOOM =
            PARTICLES.register("railgun_boom", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> RAILGUN_AMMO =
            PARTICLES.register("railgun_ammo", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> ORE_PROSPECTOR =
            PARTICLES.register("ore_prospector", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> ORE_PROSPECTOR_VEIN =
            PARTICLES.register("ore_prospector_vein", () -> new SimpleParticleType(true));

    // Martian wind dust
    public static final RegistryObject<SimpleParticleType> DEEP_MARS_WIND =
            PARTICLES.register("deep_mars_wind", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> MEDIUM_MARS_WIND =
            PARTICLES.register("medium_mars_wind", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LIGHT_MARS_WIND =
            PARTICLES.register("light_mars_wind", () -> new SimpleParticleType(false));


    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }
}
