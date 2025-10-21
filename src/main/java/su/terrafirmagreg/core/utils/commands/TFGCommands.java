package su.terrafirmagreg.core.utils.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;

public class TFGCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        DustAndWindCommand.register(dispatcher);
    }
}
