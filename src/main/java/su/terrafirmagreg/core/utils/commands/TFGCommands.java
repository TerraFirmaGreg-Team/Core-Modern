package su.terrafirmagreg.core.utils.commands;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;

public class TFGCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> debug = literal("debug")
                .requires(c -> c.hasPermission(2));

        DebugFloodFillCommand.register(debug);
        DebugEnvironmentCommand.register(debug);
        DebugPassabilityCommand.register(debug);
        DebugSpaceSuitCommand.register(debug);
        DebugQuarktechCommand.register(debug);

        dispatcher.register(literal("tfg").then(debug));

        // Non-debug commands
        DustAndWindCommand.register(dispatcher);
    }
}
