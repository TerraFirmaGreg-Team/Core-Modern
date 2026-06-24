package su.terrafirmagreg.core.common.data;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;

import su.terrafirmagreg.core.common.command.*;

public class TFGCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> debug = literal("debug")
                .requires(c -> c.hasPermission(2));

        DebugFloodFillCommand.register(debug);
        DebugEnvironmentCommand.register(debug);
        DebugPassabilityCommand.register(debug);
        DebugSpaceSuitCommand.register(debug);
        DebugQuarktechCommand.register(debug);
        DebugRecipeDump.register(debug);
        DebugWorldgenVersions.register(debug);

        LiteralArgumentBuilder<CommandSourceStack> tfg = literal("tfg").then(debug);
        ModifyNutrients.register(tfg);
        TFCDataDump.register(tfg);
        AmbientalDump.register(tfg);
        dispatcher.register(tfg);

        //DustAndWindCommand.register(dispatcher);
    }
}
