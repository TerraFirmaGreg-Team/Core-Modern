package su.terrafirmagreg.core.compat.emi;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.LampFuel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import su.terrafirmagreg.core.TFGCore;

public class LampFuelEmiRecipe implements EmiRecipe {

    private final LampFuel fuel;
    private final EmiIngredient fluidInput;
    private final EmiIngredient lampInput;

    private static final int WIDTH = 140;
    private static final int HEIGHT = 30;

    public LampFuelEmiRecipe(LampFuel fuel) {
        this.fuel = fuel;
        this.fluidInput = EmiIngredient.of(fuel.getFluidIngredient().fluids().stream().map(EmiStack::of).toList());
        this.lampInput = EmiIngredient.of(fuel.getValidLamps().blocks().stream().map(EmiStack::of).toList());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.LAMP_FUEL;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        ResourceLocation fuelId = fuel.getId();
        return TFGCore.id("/lamp_fuel/" + fuelId.getNamespace() + "/" + fuelId.getPath());
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(fluidInput, lampInput);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        int x = 2;
        int y = 5;

        widgetHolder.addSlot(fluidInput, x, y);
        x += 20;

        x = TFGEmiPlugin.createLampWidget(widgetHolder, y, x, fuel.getBurnRate() * 5);

        widgetHolder.addSlot(lampInput, x, y);
        x += 24;

        int lampCapacity = TFCConfig.SERVER.lampCapacity.get();
        int secondsPerMb = fuel.getBurnRate() / 20;
        int daysPerLamp = fuel.getBurnRate() * lampCapacity / 24000;
        Object burnTime = secondsPerMb <= 0 ? "∞" : secondsPerMb;
        Object burnDays = daysPerLamp <= 0 ? "∞" : daysPerLamp;

        widgetHolder.add(new TextWidget(Component.translatable("tfg.emi.lamp_fuel.burn_rate", burnTime).getVisualOrderText(), x, y * 2, ChatFormatting.WHITE.getColor(), true) {
            @Override
            public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                return List.of(ClientTooltipComponent.create(Component.translatable("tfg.emi.lamp_fuel.days", burnDays, lampCapacity).getVisualOrderText()));
            }
        });

    }
}
