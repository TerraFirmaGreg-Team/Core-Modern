package su.terrafirmagreg.core.common.data;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;

import su.terrafirmagreg.core.common.command.DebugRecipeDump;
import su.terrafirmagreg.core.common.command.DebugWorldgenVersions;
import su.terrafirmagreg.core.common.command.ModifyNutrients;
import su.terrafirmagreg.core.common.command.TFCDataDump;

public class TFGCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        LiteralArgumentBuilder<CommandSourceStack> debug = literal("debug")
                .requires(c -> c.hasPermission(2));
        DebugRecipeDump.register(debug);
        DebugWorldgenVersions.register(debug);

        LiteralArgumentBuilder<CommandSourceStack> tfg = literal("tfg").then(debug);
        ModifyNutrients.register(tfg);
        TFCDataDump.register(tfg);
        dispatcher.register(tfg);

        //DustAndWindCommand.register(dispatcher);
    }
}
