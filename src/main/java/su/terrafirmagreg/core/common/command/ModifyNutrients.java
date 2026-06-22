package su.terrafirmagreg.core.common.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.NutritionData;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import su.terrafirmagreg.core.common.food.nutrient.INutritionDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.NutritionDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Custom command for modifying a player's TFC nutrition data for dev testing.
 * Doesn't fully work for the original nutrients. I will fix that later.
 */
public class ModifyNutrients {

    private static final SuggestionProvider<CommandSourceStack> NUTRIENT_SUGGESTIONS = (ctx, builder) -> {
        List<String> names = new ArrayList<>();
        names.add("all");
        names.add("allNutrients");
        names.add("allContaminants");
        names.add("allTransient");
        for (Nutrient nutrient : Nutrient.VALUES) {
            names.add(nutrient.getSerializedName());
        }
        return SharedSuggestionProvider.suggest(names, builder);
    };

    public static void register(LiteralArgumentBuilder<CommandSourceStack> tfg) {
        tfg.then(literal("modifyNutrients")
                .requires(c -> c.hasPermission(2))
                .then(argument("player", EntityArgument.player())
                        .then(argument("nutrient", StringArgumentType.word())
                                .suggests(NUTRIENT_SUGGESTIONS)
                                .then(literal("reset")
                                        .executes(c -> execute(
                                                c.getSource(),
                                                EntityArgument.getPlayer(c, "player"),
                                                StringArgumentType.getString(c, "nutrient"),
                                                "reset",
                                                0f)))
                                .then(literal("set")
                                        .then(argument("value", FloatArgumentType.floatArg(0f))
                                                .executes(c -> execute(
                                                        c.getSource(),
                                                        EntityArgument.getPlayer(c, "player"),
                                                        StringArgumentType.getString(c, "nutrient"),
                                                        "set",
                                                        FloatArgumentType.getFloat(c, "value")))))
                                .then(literal("add")
                                        .then(argument("value", FloatArgumentType.floatArg(0f))
                                                .executes(c -> execute(
                                                        c.getSource(),
                                                        EntityArgument.getPlayer(c, "player"),
                                                        StringArgumentType.getString(c, "nutrient"),
                                                        "add",
                                                        FloatArgumentType.getFloat(c, "value")))))
                                .then(literal("subtract")
                                        .then(argument("value", FloatArgumentType.floatArg(0f))
                                                .executes(c -> execute(
                                                        c.getSource(),
                                                        EntityArgument.getPlayer(c, "player"),
                                                        StringArgumentType.getString(c, "nutrient"),
                                                        "subtract",
                                                        FloatArgumentType.getFloat(c, "value"))))))));
    }

    private static int execute(CommandSourceStack source, ServerPlayer player, String nutrientName, String operation, float value) {
        if (!(player.getFoodData() instanceof TFCFoodData tfcFoodData)) {
            source.sendFailure(Component.literal("Player does not have TFC food data."));
            return 0;
        }

        NutritionData nutritionData = tfcFoodData.getNutrition();

        // Handle "all", "allNutrients", "allContaminants", "allTransient".
        if (nutrientName.equalsIgnoreCase("all") || nutrientName.equalsIgnoreCase("allNutrients") || nutrientName.equalsIgnoreCase("allContaminants")
                || nutrientName.equalsIgnoreCase("allTransient")) {
            boolean doPositive = nutrientName.equalsIgnoreCase("all") || nutrientName.equalsIgnoreCase("allNutrients");
            boolean doNegative = nutrientName.equalsIgnoreCase("all") || nutrientName.equalsIgnoreCase("allContaminants");
            boolean doTransient = nutrientName.equalsIgnoreCase("all") || nutrientName.equalsIgnoreCase("allTransient");
            INutritionDataExtension ext = INutritionDataExtension.of(nutritionData);
            if (operation.equals("reset")) {
                // Clear records so TFC recalculates from defaults.
                if (doPositive) {
                    nutritionData.reset();
                }
                if (doNegative || doTransient) {
                    NutritionDataExtension.reset((NutritionData) nutritionData);
                }
            } else {
                // Only handle extended nutrients. Positive nutrients are not supported.
                for (Nutrient nutrient : Nutrient.VALUES) {
                    if (TFGNutrients.isOriginal(nutrient))
                        continue;

                    if ((doNegative && TFGNutrients.isNegative(nutrient)) || (doTransient && TFGNutrients.isTransient(nutrient))) {
                        applyToNutrient(nutritionData, ext, nutrient, operation, value);
                    }
                }
            }
            final String finalNutrientName = nutrientName;
            source.sendSuccess(() -> Component.literal(
                    "Applied " + operation + " to " + finalNutrientName + " nutrients for " + player.getName().getString()), true);
            return 1;
        }

        // Find the nutrient by name.
        Nutrient target = null;
        for (Nutrient nutrient : Nutrient.VALUES) {
            if (nutrient.getSerializedName().equalsIgnoreCase(nutrientName)) {
                target = nutrient;
                break;
            }
        }

        if (target == null) {
            StringBuilder names = new StringBuilder("all, allPositive, allNegative, allTransient");
            for (Nutrient nutrient : Nutrient.VALUES) {
                names.append(", ").append(nutrient.getSerializedName());
            }
            source.sendFailure(Component.literal("Unknown nutrient '" + nutrientName + "'. Valid options: " + names));
            return 0;
        }

        boolean isOriginalTarget = target.ordinal() < TFGNutrients.ORIGINAL_COUNT;
        if (isOriginalTarget && !operation.equals("reset")) {
            source.sendFailure(
                    Component.literal("Modifying original TFC nutrients (grain, fruit, vegetables, protein, dairy) is not supported. Use 'reset' to reset them, or modify custom nutrients instead."));
            return 0;
        }

        float current01 = isOriginalTarget
                ? nutritionData.getNutrient(target)
                : NutritionDataExtension.getExtendedNutrient(nutritionData, target);
        float currentDisplay = current01 * 100f;

        INutritionDataExtension ext = INutritionDataExtension.of(nutritionData);
        applyToNutrient(nutritionData, ext, target, operation, value);

        float result01 = isOriginalTarget
                ? nutritionData.getNutrient(target)
                : NutritionDataExtension.getExtendedNutrient(nutritionData, target);
        float resultDisplay = result01 * 100f;

        final Nutrient finalTarget = target;
        source.sendSuccess(() -> Component.literal(
                "Set " + player.getName().getString() + "'s " + finalTarget.getSerializedName()
                        + " nutrient to " + String.format("%.2f", resultDisplay) + " (was " + String.format("%.2f", currentDisplay) + ")"),
                true);
        return 1;
    }

    private static void applyToNutrient(NutritionData nutritionData, INutritionDataExtension ext, Nutrient nutrient, String operation, float value) {
        // Only handle Extended nutrients.
        if (nutrient.ordinal() < TFGNutrients.ORIGINAL_COUNT)
            return;

        float current01 = NutritionDataExtension.getExtendedNutrient(nutritionData, nutrient);

        float value01 = value / 100f;
        float newValue01 = switch (operation) {
            case "reset" -> 0f;
            case "set" -> value01;
            case "add" -> current01 + value01;
            case "subtract" -> current01 - value01;
            default -> current01;
        };

        if (TFGNutrients.isTransient(nutrient)) {
            newValue01 = Math.max(0f, newValue01);
        } else {
            newValue01 = Math.min(1f, Math.max(0f, newValue01));
        }

        NutritionDataExtension.setExtendedNutrient(nutritionData, nutrient, newValue01);
    }
}
