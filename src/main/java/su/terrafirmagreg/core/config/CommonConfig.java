package su.terrafirmagreg.core.config;

import java.util.stream.Collectors;

import net.minecraftforge.common.ForgeConfigSpec;

import su.terrafirmagreg.core.utils.CustomSpawnHelper;

/**
 * Common Config Not synced with server, saved per Minecraft instance Use for config settings that don't make sense to
 * have per-world.
 */
public final class CommonConfig {
    public final ForgeConfigSpec.ConfigValue<String> NEW_WORLD_SPAWN;

    CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push("worldSpawn");
        NEW_WORLD_SPAWN = builder.comment("ID of the custom spawn conditions for the next generated world.\nValid ids are: "
                + CustomSpawnHelper.CUSTOM_SPAWN_CONDITIONS.values().stream().map(CustomSpawnHelper.CustomSpawnCondition::id).collect(Collectors.joining(", "))).define("id", "default");
        builder.pop();

    }
}
