package su.terrafirmagreg.core.common.command;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.capabilities.food.NutritionData;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.food.nutrient.NutritionDataExtension;
import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Custom command for dumping TFC player data into chat.
 * Dumps Avg. Temp, Current Temp, Rainfall, and Nutrients.
 */
public class TFCDataDump {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> tfg) {
        tfg.then(literal("tfcDataDump")
                .executes(c -> execute(c.getSource())));
    }

    private static int execute(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be run by a player."));
            return 0;
        }

        Level level = player.level();
        BlockPos pos = player.blockPosition();

        float avgTemp = Climate.getAverageTemperature(level, pos);
        float currentTemp = Climate.getTemperature(level, pos);
        float rainfall = Climate.getRainfall(level, pos);

        MutableComponent msg = Component.literal("§6§l========== TFC Data Dump ==========\n\n")
                .append(Component.literal("~~------------~~ Climate ~~------------~~\n"))
                .append(Component.literal(String.format("     §e§oAverage Climate Temp:§r  . . . . . . %.1f °C\n", avgTemp)))
                .append(Component.literal(String.format("     §e§oCurrent Temp:§r . . . . . . . . . . . . . %.1f °C\n", currentTemp)))
                .append(Component.literal(String.format("     §e§oAverage Rainfall:§r  . . . . . . . . . . %.1f mm\n\n", rainfall)))
                .append(Component.literal("~~-----------~~ Nutrients ~~-----------~~"));

        if (player.getFoodData() instanceof TFCFoodData tfcFoodData) {
            NutritionData nutritionData = tfcFoodData.getNutrition();
            for (Nutrient nutrient : Nutrient.VALUES) {
                float value01;
                if (nutrient.ordinal() < TFGNutrients.ORIGINAL_COUNT) {
                    value01 = nutritionData.getNutrient(nutrient);
                } else {
                    value01 = NutritionDataExtension.getExtendedNutrient(nutritionData, nutrient);
                }
                float display = value01 * 100f;

                int color = getNutrientColor(nutrient);
                String name = nutrient.getSerializedName();
                String displayName = name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
                int displayNameLength = displayName.length();
                String padding = " .".repeat(Math.max(0, 23 - displayNameLength));
                MutableComponent line = Component.literal("\n        ")
                        .append(Component.literal(displayName)
                                .setStyle(Style.EMPTY.withItalic(true).withColor(TextColor.fromRgb(color))))
                        .append(Component.literal(String.format(": " + padding + " %.0f", display)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
                msg.append(line);
            }
        } else {
            msg.append(Component.literal("\n  (No TFC food data available)"));
        }

        source.sendSuccess(() -> msg, false);
        return 1;
    }

    private static int getNutrientColor(Nutrient nutrient) {
        Integer colorValue = nutrient.getColor().getColor();
        return colorValue != null ? (0xDD000000 | (colorValue & 0x00FFFFFF)) : 0xDDFFFFFF;
    }
}
