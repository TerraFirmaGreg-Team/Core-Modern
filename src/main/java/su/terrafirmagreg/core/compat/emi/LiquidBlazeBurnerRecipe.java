package su.terrafirmagreg.core.compat.emi;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.forsteri.createliquidfuel.util.Triplet;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import su.terrafirmagreg.core.TFGCore;

public class LiquidBlazeBurnerRecipe implements EmiRecipe {

    private final Fluid FLUID;
    private final String ID;
    private final Float BURN_TIME;
    private final Integer TICK_CONSUME;
    private final Boolean SUPERHEAT;

    public LiquidBlazeBurnerRecipe(Map.Entry<Fluid, Pair<ResourceLocation, Triplet<Integer, Boolean, Integer>>> liquidFuel) {
        FLUID = liquidFuel.getKey();
        ID = liquidFuel.getValue().getFirst().toString();
        int burn_tick = liquidFuel.getValue().getSecond().getFirst();
        BURN_TIME = (float) burn_tick / 20;
        TICK_CONSUME = liquidFuel.getValue().getSecond().getThird();
        SUPERHEAT = liquidFuel.getValue().getSecond().getSecond();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.BLAZE_BURNER;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return TFGCore.id(ID + "_emi");
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
        offsetY = createFluidWidget(widgetHolder, offsetY);
        createBurnerStatsWidget(widgetHolder, offsetY);
    }

    private int createFluidWidget(WidgetHolder holder, int offsetY) {
        int offsetX = 2;
        SlotWidget widget = new SlotWidget(EmiStack.of(FLUID, TICK_CONSUME), offsetX, offsetY);
        holder.add(widget);

        holder.addText(Component.literal(TICK_CONSUME + "mB"), widget.getBounds().right() + 2, widget.getBounds().bottom() - Minecraft.getInstance().font.lineHeight, 16777215, true);

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
        return List.of(EmiStack.of(FLUID));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

}
