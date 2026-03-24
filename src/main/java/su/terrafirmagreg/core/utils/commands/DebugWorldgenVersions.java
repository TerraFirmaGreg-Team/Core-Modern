package su.terrafirmagreg.core.utils.commands;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData;

public class DebugWorldgenVersions {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> debug) {
        debug.then(literal("worldgen_versions")
                .executes(c -> {
                    final var source = c.getSource();
                    final WorldgenVersionData data = WorldgenVersionData.get(source.getServer());

                    source.sendSuccess(() -> Component.literal(
                            "Active overworld worldgen version: " + WorldgenVersionData.OVERWORLD_VERSION
                                    + " (TFC 1.21 backport = " + WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT + ")"),
                            false);

                    final var generated = data.generatedVersions;
                    if (generated.isEmpty()) {
                        source.sendSuccess(() -> Component.literal("No generated versions recorded."), false);
                    } else {
                        source.sendSuccess(() -> Component.literal("Recorded generation versions:"), false);
                        generated.forEach((dim, version) -> source.sendSuccess(() -> Component.literal("  " + dim + " = " + version), false));
                    }

                    return 1;
                }));
    }
}
