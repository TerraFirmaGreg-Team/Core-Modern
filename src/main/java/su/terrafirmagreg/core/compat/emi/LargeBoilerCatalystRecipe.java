package su.terrafirmagreg.core.compat.emi;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.api.pattern.TFGPredicates;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.steam.TFGLargeBoilerMachine;

public class LargeBoilerCatalystRecipe implements EmiRecipe {

    private final Item ITEM;
    private final int BURN_SECONDS;
    private final double DURATION_MULTIPLIER;
    private final int MIN_CATALYST_TIER;

    public LargeBoilerCatalystRecipe(TFGLargeBoilerMachine.CatalystItem catalyst) {
        this.ITEM = catalyst.item().get();
        this.BURN_SECONDS = catalyst.burnSeconds();
        this.DURATION_MULTIPLIER = catalyst.durationMultiplier();
        this.MIN_CATALYST_TIER = catalyst.minCatalystTier();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.LARGE_BOILER_CATALYST;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(ITEM);
        return TFGCore.id("/" + itemId.getNamespace() + "_" + itemId.getPath().replace('/', '_')
                + "_large_boiler_catalyst_emi");
    }

    @Override
    public int getDisplayWidth() {
        return 160;
    }

    @Override
    public int getDisplayHeight() {
        Font font = Minecraft.getInstance().font;
        int lines = 3;
        return 20 + 6 + (font.lineHeight * lines) + 6;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        int offsetY = 7;
        offsetY = createItemWidget(widgetHolder, offsetY);
        createCatalystStatsWidget(widgetHolder, offsetY);
    }

    private int createItemWidget(WidgetHolder holder, int offsetY) {
        int offsetX = 2;
        SlotWidget widget = new SlotWidget(EmiStack.of(ITEM), offsetX, offsetY);
        holder.add(widget);

        holder.addText(
                Component.translatable("tfg.emi.large_boiler_catalyst.burn_time", BURN_SECONDS),
                widget.getBounds().right() + 4,
                widget.getBounds().y() + (18 - Minecraft.getInstance().font.lineHeight) / 2,
                0xAAAAAA,
                true);

        return widget.getBounds().bottom() + 4;
    }

    private void createCatalystStatsWidget(WidgetHolder holder, int offsetY) {
        Font font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight + 1;

        holder.addText(
                Component.translatable("tfg.emi.large_boiler_catalyst.efficiency", "x" + DURATION_MULTIPLIER),
                2, offsetY, 0x55FF55, true);
        offsetY += lineHeight;

        holder.addText(
                Component.translatable("tfg.emi.large_boiler_catalyst.min_tier",
                        "T" + MIN_CATALYST_TIER,
                        TFGPredicates.getBoilerModuleCasingForTier(MIN_CATALYST_TIER).getName()),
                2, offsetY, 0xFFAA00, true);
        offsetY += lineHeight;

        holder.addText(ITEM.getDescription(), 2, offsetY, 0xFFFFFF, true);
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
