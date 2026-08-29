package su.terrafirmagreg.core.compat.emi;

import java.text.DecimalFormat;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.client.Minecraft;
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
import su.terrafirmagreg.core.common.block.create.CombustionEngineBlockEntity;
import su.terrafirmagreg.core.common.data.fuel_type.FuelType;

public class CombustionEngineRecipe implements EmiRecipe {
    private final Fluid fluid;
    private final String burnPerSec32;

    public CombustionEngineRecipe(FuelType fuel) {
        this.fluid = fuel.fluid().get(0).get();

        var formatter = new DecimalFormat("0.###");
        burnPerSec32 = formatter.format(1.0 / (fuel.getFuelBurnRate(CombustionEngineBlockEntity.DEFAULT_SPEED, GTValues.LV) * 20.0));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.COMBUSTION_ENGINE;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return TFGCore.id("/combustion_engine_emi/" + fluid.getFluidType().toString().split(":")[1]);
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(EmiStack.of(fluid));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return 140;
    }

    @Override
    public int getDisplayHeight() {
        return 38;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int offsetY = 5;
        offsetY = createFluidWidget(widgets, offsetY);
        offsetY = createStatsWidget(widgets, offsetY);
    }

    private int createFluidWidget(WidgetHolder widgets, int offsetY) {
        SlotWidget widget = new SlotWidget(EmiStack.of(fluid), 2, offsetY);
        widgets.add(widget);

        widgets.addText(
                Component.translatable("tfg.emi.combustion_engine_amount"),
                widget.getBounds().right() + 2, widget.getBounds().bottom() - 12, 16777215, true);

        return widget.getBounds().bottom() + 2;
    }

    private int createStatsWidget(WidgetHolder widgets, int offsetY) {
        int lineHeight = Minecraft.getInstance().font.lineHeight;

        widgets.addText(
                Component.translatable("tfg.emi.combustion_engine_burn_time", burnPerSec32, CombustionEngineBlockEntity.DEFAULT_SPEED),
                2, offsetY, 16777215, true);
        return offsetY + lineHeight;
    }
}
