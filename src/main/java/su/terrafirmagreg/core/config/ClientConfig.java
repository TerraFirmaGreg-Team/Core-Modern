package su.terrafirmagreg.core.config;

import net.minecraftforge.common.ForgeConfigSpec;

import su.terrafirmagreg.core.client.renderer.SkyboxRenderer;

/**
 * Client Config Not synced with server, only loaded on the client. Only use this for aesthetic and rendering options!
 */
@SuppressWarnings("ClassCanBeRecord")
public final class ClientConfig {
    public final ForgeConfigSpec.IntValue PRECISE_ORE_PROSPECTOR_PARTICLE_CHANCE;
    public final ForgeConfigSpec.EnumValue<SkyboxRenderer.Option> VENUS_SKYBOX;
    public final ForgeConfigSpec.BooleanValue VENUS_CLOUD_LAYERS;
    public final ForgeConfigSpec.BooleanValue VENUS_DIMMING;

    ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push("propick_vein_rendering");
        PRECISE_ORE_PROSPECTOR_PARTICLE_CHANCE = builder
                .comment(
                        "\n1 in N chance for the precise xray ore prospector particles to appear per block. Set to 0 to disable. Default: 5")
                .defineInRange("PreciseOreProspectorParticleChance", 5, 0, 1000);
        builder.pop();
        builder.push("venus_rendering");
        VENUS_SKYBOX = builder
                .comment("\nChanges the appearance of the skybox on Venus. Has no effect on gameplay, just pick whichever you like the look of the most!")
                .defineEnum("VenusSkybox", SkyboxRenderer.Option.PIXELATED);
        VENUS_CLOUD_LAYERS = builder
                .comment("\nEnables multiple layers of colored clouds on Venus. Disable if this is causing problems with other mods or shaders.")
                .define("VenusCloudLayers", true);
        VENUS_DIMMING = builder
                .comment("\nEnables the dimmer sky light level on Venus. Disable to have the same brightness as the Overworld.")
                .define("VenusDimming", true);
        builder.pop();
    }
}
