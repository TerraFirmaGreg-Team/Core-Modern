package su.terrafirmagreg.core.utils.commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

import su.terrafirmagreg.core.utils.MarsEnvironmentalHelpers;

public class DustAndWindCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("tfg").then(
                        literal("set").requires(c -> c.hasPermission(2)).then(
                                literal("dust_intensity").then(
                                        argument("intensity", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                .executes(c -> {
                                                    float target_intensity = FloatArgumentType.getFloat(c, "intensity");
                                                    MarsEnvironmentalHelpers.setDustIntensity(target_intensity);
                                                    c.getSource().sendSuccess(() ->
                                                    //TODO: add translation keys
                                                    //Component.translatable("tfg.commands.dust_intensity.success", target_intensity)
                                                    Component.literal(String.format("Dust intensity set to %f", target_intensity)),
                                                            true);
                                                    return 1;
                                                })))));

        dispatcher.register(
                literal("tfg").then(
                        literal("set").requires(c -> c.hasPermission(2)).then(
                                literal("wind_intensity").then(
                                        argument("intensity", FloatArgumentType.floatArg(0.0f))
                                                .executes(c -> {
                                                    float target_intensity = FloatArgumentType.getFloat(c, "intensity");
                                                    MarsEnvironmentalHelpers.setWind(target_intensity);
                                                    c.getSource().sendSuccess(() ->
                                                    //Component.translatable("tfg.commands.wind_intensity.success", target_intensity)
                                                    Component.literal(String.format("wind intensity set to %f", target_intensity)),
                                                            true);
                                                    return 1;
                                                })))));

        dispatcher.register(
                literal("tfg").then(
                        literal("set").requires(c -> c.hasPermission(2)).then(
                                literal("wind").then(
                                        argument("vector", Vec2Argument.vec2())
                                                .executes(c -> {
                                                    Vec2 target_vector = Vec2Argument.getVec2(c, "vector");
                                                    MarsEnvironmentalHelpers.setWind(target_vector);
                                                    c.getSource().sendSuccess(() ->
                                                    //Component.translatable("tfg.commands.wind.success", target_vector)
                                                    Component.literal(String.format("Wind vector set to <%.2f, %.2f>", target_vector.x, target_vector.y)),
                                                            true);
                                                    return 1;
                                                })))));
    }

}
