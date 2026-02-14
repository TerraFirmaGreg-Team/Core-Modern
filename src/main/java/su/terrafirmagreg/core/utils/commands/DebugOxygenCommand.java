package su.terrafirmagreg.core.utils.commands;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import su.terrafirmagreg.core.common.environment.DimEnvManager;
import su.terrafirmagreg.core.common.environment.EnvironmentSystem;
import su.terrafirmagreg.core.common.environment.OxygenProvider;

/**
 * /tfg debug oxygen: check oxygen at player position + list all providers
 */
public class DebugOxygenCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> debug) {
        debug.then(literal("oxygen")
                .executes(c -> {
                    BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                    ServerLevel level = c.getSource().getLevel();
                    return checkOxygen(c.getSource(), level, pos);
                }));
    }

    private static int checkOxygen(CommandSourceStack source, ServerLevel level, BlockPos pos) {
        boolean hasOxygen = EnvironmentSystem.hasOxygen(level, pos);

        source.sendSuccess(() -> Component.literal(String.format(
                "Oxygen at %s: %s", pos.toShortString(), hasOxygen ? "YES" : "NO")), false);

        DimEnvManager manager = EnvironmentSystem.getManager(level);
        int providerCount = 0;
        for (OxygenProvider provider : manager.getProviders().values()) {
            providerCount++;
            BlockPos mPos = provider.getMachinePos();
            boolean loaded = provider.isMachineLoaded();
            boolean working = provider.isMachineWorking();
            String status = provider.getRoomScan().status().name();
            int intSize = provider.getRoomScan().interiorSize();
            int envSize = provider.getRoomScan().envelopeSize();

            source.sendSuccess(() -> Component.literal(String.format(
                    "  Provider at %s: status=%s, interior=%d, envelope=%d, loaded=%s, working=%s",
                    mPos.toShortString(), status, intSize, envSize, loaded, working)), false);
        }

        if (providerCount == 0) {
            source.sendSuccess(() -> Component.literal("  No oxygen providers in this dimension"), false);
        }

        return 1;
    }
}
