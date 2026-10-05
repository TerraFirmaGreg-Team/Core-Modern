package su.terrafirmagreg.core.compat.emi;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.config.TemperatureDisplayStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.blockentity.GasBurnerBlockEntity;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.recipe.GasBurnerFuelRecipe;

@SuppressWarnings("unused")
public class GasBurnerFuelEmiRecipe implements EmiRecipe {

    public static final ResourceLocation PROGRESS_BAR = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/gas_burner_progress_bar.png");
    public static final int PROGRESS_BAR_WIDTH = 21;
    public static final int PROGRESS_BAR_HEIGHT = 32;
    public static final EmiTexture FLAME_BACKGROUND = new EmiTexture(
            PROGRESS_BAR,
            0, 0,
            PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT,
            PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT,
            PROGRESS_BAR_WIDTH * 2,
            PROGRESS_BAR_HEIGHT);
    public static final EmiTexture FLAME_FOREGROUND = new EmiTexture(
            PROGRESS_BAR,
            PROGRESS_BAR_WIDTH, 0,
            PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT,
            PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT,
            PROGRESS_BAR_WIDTH * 2, PROGRESS_BAR_HEIGHT);

    private final GasBurnerFuelRecipe recipe;
    private final List<EmiIngredient> inputs;

    public GasBurnerFuelEmiRecipe(GasBurnerFuelRecipe recipe) {
        this.recipe = recipe;
        var fluidStacks = recipe.getFluid().ingredient().fluids().stream()
                .map(fluid -> (EmiIngredient) EmiStack.of(fluid, recipe.getFluid().amount()))
                .toList();
        this.inputs = fluidStacks.isEmpty() ? List.of() : List.of(EmiIngredient.of(fluidStacks));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.GAS_BURNER;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return 200;
    }

    @Override
    public int getDisplayHeight() {
        return 40;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {

        float temp = recipe.getTemperature();
        float seconds = (float) recipe.getDuration() / 20;
        MutableComponent formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temp);
        if (formattedTemp == null) {
            formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatRange(temp);
        }
        Component tempText = Component.literal("\uD83D\uDD25 ").append(formattedTemp != null ? formattedTemp : Component.empty());
        final TemperatureDisplayStyle heatUnit = TFCConfig.CLIENT.climateTooltipStyle.get();
        final MutableComponent unitHeatValue = heatUnit.formatRange(temp);
        Font font = Minecraft.getInstance().font;
        int burnerCapacity = GasBurnerBlockEntity.CAPACITY;
        String daysPerTank = String.valueOf(recipe.getDuration() * burnerCapacity / 24000);

        int offset = 6;
        int slotSize = 18;
        int catalystX = 4;
        int flameX = catalystX + slotSize + offset;
        int flameY = (this.getDisplayHeight() / 2) - (PROGRESS_BAR_HEIGHT / 2);
        int inputX = flameX + PROGRESS_BAR_WIDTH + offset;
        int slotY = (this.getDisplayHeight() / 2) - (slotSize / 2);
        int textHeight = font.lineHeight;
        int textX = inputX + slotSize + (offset * 2);
        int burnTextY = (this.getDisplayHeight() / 2) - (textHeight + (offset / 2));
        int tempTextY = burnTextY + textHeight + offset;

        // Fluid Slot.
        if (!inputs.isEmpty()) {
            SlotWidget slot = new SlotWidget(inputs.get(0), inputX, slotY);
            widgets.add(slot);
        }

        // Flame Progress Bar.
        widgets.addTexture(FLAME_BACKGROUND, flameX, flameY);
        if (seconds <= 0) {
            widgets.addTexture(FLAME_FOREGROUND, flameX, flameY);
        } else {
            widgets.addAnimatedTexture(FLAME_FOREGROUND, flameX, flameY, (int) (seconds * 1000), false, true, true);
        }

        // Gas Burner Item.
        widgets.addSlot(EmiStack.of(TFGBlocks.GAS_BURNER.get()), catalystX, slotY).catalyst(true);

        // Burn mB and time, with tooltip for full tank.
        Component burnAmount = Component.literal("⌛ ")
                .append(Component.translatable("tfg.tooltip.unit.mB", recipe.getFluid().amount()))
                .append(Component.literal(" / "))
                .append(Component.translatable("tfg.tooltip.unit.seconds", String.format("%.1f", seconds)));

        widgets.add(new TextWidget(burnAmount.getVisualOrderText(), textX, burnTextY, 0xFFFFFF, true) {
            @Override
            public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                return List.of(ClientTooltipComponent.create(Component.translatable("tfg.tooltip.fuel_burning.days", daysPerTank, burnerCapacity).getVisualOrderText()));
            }
        });

        // Burn temp, with tooltip for Celsius unit amount.
        widgets.add(new TextWidget(tempText.getVisualOrderText(), textX, tempTextY, 0xFFFFFF, true) {
            @Override
            public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
                if (unitHeatValue != null) {
                    return List.of(ClientTooltipComponent.create(unitHeatValue.getVisualOrderText()));
                }
                return List.of();
            }
        });
    }
}
