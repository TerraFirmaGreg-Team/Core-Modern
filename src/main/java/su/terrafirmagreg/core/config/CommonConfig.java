package su.terrafirmagreg.core.config;

import java.util.stream.Collectors;

import net.minecraftforge.common.ForgeConfigSpec;

import su.terrafirmagreg.core.utils.CustomSpawnHelper;

/**
 * Common Config Not synced with server, saved per Minecraft instance Use for config settings that don't make sense to
 * have per-world.
 */
public final class CommonConfig {
    public final ForgeConfigSpec.BooleanValue ENABLE_TFC_AMBIENTAL_COMPAT;
    public final ForgeConfigSpec.BooleanValue ENABLE_CREATE_COMPAT;
    public final ForgeConfigSpec.ConfigValue<String> NEW_WORLD_SPAWN;
    public final ForgeConfigSpec.BooleanValue ENABLE_CC_UTF8_COMPAT;

    CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push("general");
        ENABLE_CREATE_COMPAT = builder.comment("Should be create compat enabled?").define("createCompat", true);
        ENABLE_TFC_AMBIENTAL_COMPAT = builder.comment("Should be tfc ambiental compat enabled?")
                .define("tfcAmbientalCompat", true);
        ENABLE_CC_UTF8_COMPAT = builder.comment("Should CC:Tweaked UTF-8 compatibility patches be enabled?")
                .define("ccUtf8Compat", true);
        builder.pop();
        builder.push("worldSpawn");
        NEW_WORLD_SPAWN = builder.comment("ID of the custom spawn conditions for the next generated world.\nValid ids are: "
                + CustomSpawnHelper.CUSTOM_SPAWN_CONDITIONS.values().stream().map(CustomSpawnHelper.CustomSpawnCondition::id).collect(Collectors.joining(", "))).define("id", "default");
        builder.pop();

    }
}
