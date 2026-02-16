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
import su.terrafirmagreg.core.common.environment.TemperatureProvider;

/**
 * /tfg debug environment: check oxygen + temperature at player position, list all providers
 */
public class DebugEnvironmentCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> debug) {
        debug.then(literal("environment")
                .executes(c -> {
                    BlockPos pos = BlockPos.containing(c.getSource().getPosition());
                    ServerLevel level = c.getSource().getLevel();
                    return checkEnvironment(c.getSource(), level, pos);
                }));
    }

    private static int checkEnvironment(CommandSourceStack source, ServerLevel level, BlockPos pos) {
        DimEnvManager manager = EnvironmentSystem.getManager(level);

        // Oxygen status
        boolean hasOxygen = EnvironmentSystem.hasOxygen(level, pos);
        source.sendSuccess(() -> Component.literal(String.format(
                "Oxygen at %s: %s", pos.toShortString(), hasOxygen ? "YES" : "NO")), false);

        // Temperature status
        boolean hasTemp = EnvironmentSystem.hasTemperature(level, pos);
        source.sendSuccess(() -> Component.literal(String.format(
                "Temperature at %s: %s", pos.toShortString(), hasTemp ? "COZY" : "COLD")), false);

        // Oxygen providers
        int oxygenCount = 0;
        source.sendSuccess(() -> Component.literal("--- Oxygen Providers ---"), false);
        for (OxygenProvider provider : manager.getProviders().values()) {
            oxygenCount++;
            BlockPos mPos = provider.getMachinePos();
            boolean loaded = provider.isMachineLoaded();
            boolean working = provider.isMachineWorking();
            String status = provider.getRoomScan().status().name();
            int intSize = provider.getRoomScan().interiorSize();
            int envSize = provider.getRoomScan().envelopeSize();

            source.sendSuccess(() -> Component.literal(String.format(
                    "  %s: status=%s, interior=%d, envelope=%d, loaded=%s, working=%s",
                    mPos.toShortString(), status, intSize, envSize, loaded, working)), false);
        }
        if (oxygenCount == 0) {
            source.sendSuccess(() -> Component.literal("  (none)"), false);
        }

        // Temperature providers
        int tempCount = 0;
        source.sendSuccess(() -> Component.literal("--- Temperature Providers ---"), false);
        for (TemperatureProvider provider : manager.getTempProviders().values()) {
            tempCount++;
            BlockPos mPos = provider.getMachinePos();
            int radius = provider.getRadius();
            boolean loaded = provider.isMachineLoaded();
            boolean working = provider.isMachineWorking();

            source.sendSuccess(() -> Component.literal(String.format(
                    "  %s: radius=%d, loaded=%s, working=%s",
                    mPos.toShortString(), radius, loaded, working)), false);
        }
        if (tempCount == 0) {
            source.sendSuccess(() -> Component.literal("  (none)"), false);
        }

        return 1;
    }
}
