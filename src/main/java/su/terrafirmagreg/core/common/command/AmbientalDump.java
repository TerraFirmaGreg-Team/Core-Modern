package su.terrafirmagreg.core.common.command;

import static net.minecraft.commands.Commands.literal;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.tfcambiental.api.BlockTemperatureProvider;
import su.terrafirmagreg.tfcambiental.api.EntityTemperatureProvider;
import su.terrafirmagreg.tfcambiental.api.EnvironmentalTemperatureProvider;
import su.terrafirmagreg.tfcambiental.api.EquipmentTemperatureProvider;
import su.terrafirmagreg.tfcambiental.api.ItemTemperatureProvider;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;
import su.terrafirmagreg.tfcambiental.modifier.TempModifierStorage;

/**
 * Custom command for dumping TFC-Ambiental data into chat.
 */
public class AmbientalDump {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> tfg) {
        tfg.then(literal("ambientalDump").executes(c -> execute(c.getSource())));
    }

    private static int execute(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }

        TempModifierStorage blockModifiers = new TempModifierStorage();
        BlockTemperatureProvider.evaluateAll(player, blockModifiers);
        TempModifierStorage equipModifiers = new TempModifierStorage();
        EquipmentTemperatureProvider.evaluateAll(player, equipModifiers);
        TempModifierStorage itemModifiers = new TempModifierStorage();
        ItemTemperatureProvider.evaluateAll(player, itemModifiers);

        MutableComponent msg = Component.literal("")
                .append(Component.literal("TFC Ambiental Modifiers\n\n")
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN))
                .append(formatTempModifier("handleGeneralTemperature",
                        EnvironmentalTemperatureProvider.handleGeneralTemperature(player)))
                .append(formatTempModifier("handleWater", EnvironmentalTemperatureProvider.handleWater(player)))
                .append(formatTempModifier("handleSprinting", EnvironmentalTemperatureProvider.handleSprinting(player)))
                .append(formatTempModifier("handleWetness", EnvironmentalTemperatureProvider.handleWetness(player)))
                .append(formatTempModifier("handleThirst", EnvironmentalTemperatureProvider.handleThirst(player)))
                .append(formatTempModifier("handleFood", EnvironmentalTemperatureProvider.handleFood(player)))
                .append(formatTempModifier("handleFire", EnvironmentalTemperatureProvider.handleFire(player)));
        if (player.level().dimension() != Level.NETHER) {
            msg
                    .append(formatTempModifier("handleTimeOfDay", EnvironmentalTemperatureProvider.handleTimeOfDay(player)))
                    .append(formatTempModifier("handleShade", EnvironmentalTemperatureProvider.handleShade(player)))
                    .append(formatTempModifier("handleCozy", EnvironmentalTemperatureProvider.handleCozy(player)))
                    .append(formatTempModifier("handleRain", EnvironmentalTemperatureProvider.handleRain(player)))
                    .append(formatTempModifier("handleWind", EnvironmentalTemperatureProvider.handleWind(player)))
                    .append(formatTempModifier("handleUnderground", EnvironmentalTemperatureProvider.handleUnderground(player)));
        }
        msg.append(formatTempModifierStorage("BlockTemperatureProvider", blockModifiers))
                .append(formatTempModifierStorage("EquipmentTemperatureProvider", equipModifiers))
                .append(formatTempModifierStorage("ItemTemperatureProvider", itemModifiers))
                .append(formatTempModifier("EntityTemperatureProvider", EntityTemperatureProvider.getEntityTempModifier(player)));

        source.sendSuccess(() -> msg, false);
        return 1;
    }

    private static @Nonnull MutableComponent formatTempModifier(String text, Optional<TempModifier> omod) {
        MutableComponent line = Component.literal(text);
        if (omod.isEmpty()) {
            return line.append(" ").append(Component.literal("null").withStyle(ChatFormatting.DARK_GRAY)).append("\n");
        }

        TempModifier mod = omod.get();
        if (mod.getChange() != 0f) {
            line.append(" ");
            line.append(Component.literal("%.1f".formatted(mod.getChange()))
                    .withStyle(mod.getChange() > 0f ? ChatFormatting.RED : ChatFormatting.AQUA));
            line.append("°C");
        }
        if (mod.getPotency() != 1f) {
            line.append(" potency=");
            line.append(Component.literal("%.1f".formatted(mod.getPotency())).withStyle(ChatFormatting.GOLD));
        }
        if (mod.getWetness() != 0f) {
            line.append(" wetness=");
            line.append(Component.literal("%.1f".formatted(mod.getWetness())).withStyle(ChatFormatting.BLUE));
        }
        return line.append("\n");
    }

    private static @Nonnull MutableComponent formatTempModifierStorage(String text, TempModifierStorage storage) {
        MutableComponent line = Component.literal(text).append("\n");
        for (TempModifier mod : storage) {
            line.append(formatTempModifier("-", Optional.of(mod))).append("\n");
        }
        return line;
    }
}
