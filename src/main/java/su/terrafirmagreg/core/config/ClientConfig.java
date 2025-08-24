package su.terrafirmagreg.core.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Client Config
 * Not synced with server, only loaded on the client.
 * Only use this for aesthetic and rendering options!
 */
@SuppressWarnings("ClassCanBeRecord")
public final class ClientConfig {
    public final ForgeConfigSpec.IntValue PRECISE_ORE_PROSPECTOR_PARTICLE_CHANCE;

    ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push("propick_vein_rendering");
        PRECISE_ORE_PROSPECTOR_PARTICLE_CHANCE = builder
                .comment("\n\n1 in N chance for the precise xray ore prospector particles to appear per block. Set to 0 to disable. Default: 5")
                .defineInRange("PreciseOreProspectorParticleChance", 5, 0, 1000);
        builder.pop();
    }
}
