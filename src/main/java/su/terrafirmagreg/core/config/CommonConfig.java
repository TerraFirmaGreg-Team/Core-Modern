package su.terrafirmagreg.core.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class CommonConfig {
    public final ForgeConfigSpec.BooleanValue ENABLE_TFC_AMBIENTAL_COMPAT;
    public final ForgeConfigSpec.BooleanValue ENABLE_CREATE_COMPAT;
    public final ForgeConfigSpec.IntValue PRECISE_ORE_PROSPECTOR_PARTICLE_CHANCE;

    CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push("general");
        ENABLE_CREATE_COMPAT = builder.comment("Should be create compat enabled?").define("createCompat", true);
        ENABLE_TFC_AMBIENTAL_COMPAT = builder.comment("Should be tfc ambiental compat enabled?").define("tfcAmbientalCompat", true);
        PRECISE_ORE_PROSPECTOR_PARTICLE_CHANCE = builder
                .comment("\n\n1 in N chance for the precise xray ore prospector particles to appear per block. Set to 0 to disable. Default: 5")
                .defineInRange("PreciseOreProspectorParticleChance", 5, 0, 1000);
        builder.pop();
    }
}
