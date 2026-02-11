package su.terrafirmagreg.core.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common Config Not synced with server, saved per Minecraft instance Use for config settings that don't make sense to
 * have per-world.
 */
public final class CommonConfig {
    public final ForgeConfigSpec.BooleanValue ENABLE_TFC_AMBIENTAL_COMPAT;
    public final ForgeConfigSpec.BooleanValue ENABLE_CREATE_COMPAT;
    public final ForgeConfigSpec.ConfigValue<String> NEW_WORLD_SPAWN;

    CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push("general");
        ENABLE_CREATE_COMPAT = builder.comment("Should be create compat enabled?").define("createCompat", true);
        ENABLE_TFC_AMBIENTAL_COMPAT = builder.comment("Should be tfc ambiental compat enabled?")
                .define("tfcAmbientalCompat", true);
        builder.pop();
        builder.push("worldSpawn");
        NEW_WORLD_SPAWN = builder.comment("ID of the custom spawn conditions for the next generated world").define("id", "default");
        builder.pop();

    }
}
