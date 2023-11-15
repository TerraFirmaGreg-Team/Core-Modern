package exception.terrafirmagreg.compat.tfc;

import exception.terrafirmagreg.objects.block.TapBlock;
import exception.terrafirmagreg.objects.recipe.TapExtractRecipe;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Month;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;

import java.util.Arrays;
import java.util.List;

public class TFCCompat {
    public static boolean canProcess(TapExtractRecipe recipe) {
        Month month = Calendars.SERVER.getCalendarMonthOfYear();

        return recipe.lifeCycles.length > month.ordinal() && recipe.lifeCycles[month.ordinal()] > 0;
    }

    public static void showRecipeText(GuiGraphics guiGraphics, TapExtractRecipe recipe) {
        if (ModList.get().isLoaded("tfc")) {
            Minecraft minecraft = Minecraft.getInstance();

            var months = Arrays.stream(Month.values()).filter(month -> recipe.lifeCycles[month.ordinal()] > 0).map(e -> capitalize(e.name()))
                    .toList();

            Component text = Component.translatable("jei.treetap.life_cycle.all_year");
            if (months.size() < 12) {
                text = Component.literal(String.join(", ", months));
            }

            guiGraphics.drawString(minecraft.font, Language.getInstance()
                    .getVisualOrder(Component.translatable("jei.treetap.life_cycle.months", text)), 0, 60, 0xFF000000, false);
        }
    }

    private static String capitalize(String name) {
        String[] nameParts = name.toLowerCase().split("_");

        for (int i = 0; i < nameParts.length; i++) {
            nameParts[i] = nameParts[i].substring(0, 1).toUpperCase() + nameParts[i].substring(1);
        }

        return String.join(" ", nameParts);
    }

    public static void appendHoverText(List<Component> pTooltip, Block block) {
        float mod = 1.0f;
        if (block instanceof TapBlock tapBlock) {
            mod = tapBlock.getModifier();
        }
        pTooltip.add(Component.translatable("item.treetap.tap.modifier", mod).withStyle(ChatFormatting.DARK_GREEN));
    }
}
