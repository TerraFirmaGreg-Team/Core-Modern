package su.terrafirmagreg.core.compat.emi;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.steam.TFGLargeBoilerMachine;

public class LargeBoilerBoosterRecipe implements EmiRecipe {

    private final Fluid FLUID;
    private final int FLUID_AMOUNT_MB;
    private final int TEMPERATURE_BONUS;
    private final int MIN_BOILER_TEMPERATURE;
    private final String TRANSLATION_KEY;

    public LargeBoilerBoosterRecipe(TFGLargeBoilerMachine.BoosterFluid booster) {
        this.FLUID = booster.fluid().get();
        this.FLUID_AMOUNT_MB = booster.fluidAmountMb();
        this.TEMPERATURE_BONUS = booster.temperatureBonus();
        this.MIN_BOILER_TEMPERATURE = booster.minBoilerTemperature();
        this.TRANSLATION_KEY = booster.translationKey();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.LARGE_BOILER_BOOSTER;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(FLUID);
        return TFGCore.id("/" + fluidId.getNamespace() + "_" + fluidId.getPath() + "_large_boiler_booster_emi");
    }

    @Override
    public int getDisplayWidth() {
        return 160;
    }

    @Override
    public int getDisplayHeight() {
        Font font = Minecraft.getInstance().font;
        int lines = 2; // Bonus Temp + mB/s
        if (MIN_BOILER_TEMPERATURE > 0)
            lines++; // Temp Boiler required
        return 20 + 6 + (font.lineHeight * lines) + 6;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        int offsetY = 7;
        offsetY = createFluidWidget(widgetHolder, offsetY);
        createBoosterStatsWidget(widgetHolder, offsetY);
    }

    private int createFluidWidget(WidgetHolder holder, int offsetY) {
        int offsetX = 2;
        SlotWidget widget = new SlotWidget(EmiStack.of(FLUID, FLUID_AMOUNT_MB), offsetX, offsetY);
        holder.add(widget);

        holder.addText(
                Component.literal(FLUID_AMOUNT_MB + " mB/s"),
                widget.getBounds().right() + 4,
                widget.getBounds().y() + (18 - Minecraft.getInstance().font.lineHeight) / 2,
                0xAAAAAA,
                true);

        return widget.getBounds().bottom() + 4;
    }

    private void createBoosterStatsWidget(WidgetHolder holder, int offsetY) {
        Font font = Minecraft.getInstance().font;
        int lineHeight = font.lineHeight + 1;

        // Bonus Temp
        holder.addText(
                Component.translatable("tfg.emi.large_boiler_booster.temperature_bonus",
                        "+" + TEMPERATURE_BONUS + "°C"),
                2, offsetY, 0xFF5555, true);
        offsetY += lineHeight;

        // Min Temp required if > 0
        if (MIN_BOILER_TEMPERATURE > 0) {
            holder.addText(
                    Component.translatable("tfg.emi.large_boiler_booster.min_boiler",
                            (MIN_BOILER_TEMPERATURE + 274) + "K"),
                    2, offsetY, 0xFFAA00, true);
            offsetY += lineHeight;
        }

        // Fluid name
        holder.addText(
                Component.translatable(TRANSLATION_KEY),
                2, offsetY, 0xFFFFFF, true);
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
