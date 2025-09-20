package su.terrafirmagreg.core.compat.emi;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeHooks;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import su.terrafirmagreg.core.TFGCore;

public class SolidBlazeBurnerRecipe implements EmiRecipe {

    private final Item ITEM;
    private final Integer BURN_TIME;
    private final Boolean SUPERHEAT;

    public SolidBlazeBurnerRecipe(Item ITEM, Boolean SUPERHEAT) {
        this.ITEM = ITEM;
        this.SUPERHEAT = SUPERHEAT;

        int burn_tick = ForgeHooks.getBurnTime(ITEM.getDefaultInstance(), null);
        if (burn_tick == 0)
            burn_tick = 1600;

        if (SUPERHEAT)
            burn_tick = 3600;

        BURN_TIME = burn_tick / 20;

    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.BLAZE_BURNER;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return TFGCore.id(ITEM.getDefaultInstance().getDisplayName() + "_blaze_burner_emi");
    }

    @Override
    public int getDisplayWidth() {
        return 140;
    }

    @Override
    public int getDisplayHeight() {
        int defaultHeight = 38;
        if (SUPERHEAT) {
            return defaultHeight + Minecraft.getInstance().font.lineHeight;
        } else {
            return defaultHeight;
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        int offsetY = 5;
        offsetY = createItemWidget(widgetHolder, offsetY);
        offsetY = createBurnerStatsWidget(widgetHolder, offsetY);
    }

    private int createItemWidget(WidgetHolder holder, int offsetY) {
        int offsetX = 2;
        SlotWidget widget = new SlotWidget(EmiStack.of(ITEM), offsetX, offsetY);
        holder.add(widget);

        return widget.getBounds().bottom() + 2;
    }

    private int createBurnerStatsWidget(WidgetHolder holder, int offsetY) {
        Font font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight;

        holder.addText(Component.translatable("tfg.emi.liquid_bb_burn_time", BURN_TIME), 2, offsetY, 16777215, true);
        offsetY += lineHeight;
        if (SUPERHEAT) {
            holder.addText(Component.translatable("tfg.emi.liquid_bb_superheat"), 2, offsetY, 16777215, true);
        }
        return offsetY;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(EmiStack.of(ITEM));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }
}
