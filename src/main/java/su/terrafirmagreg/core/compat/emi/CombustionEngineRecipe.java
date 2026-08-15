package su.terrafirmagreg.core.compat.emi;

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
    private final FuelType fuelType;
    private final Fluid fluid;
    private final float burnPerSec32;
    private final float burnPerSec256;

    public CombustionEngineRecipe(FuelType fuel) {
        this.fuelType = fuel;
        this.fluid = fuelType.fluid().get(0).get();

        burnPerSec32 = fuelType.getFuelBurnRate(CombustionEngineBlockEntity.DEFAULT_SPEED, GTValues.LV) * 20f;
        burnPerSec256 = fuelType.getFuelBurnRate(256, GTValues.LV) * 20f;
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
        return 47;
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

        widgets.addText(Component.literal("1mB"), widget.getBounds().right() + 2, widget.getBounds().bottom() - 12, 16777215, true);

        return widget.getBounds().bottom() + 2;
    }

    private int createStatsWidget(WidgetHolder widgets, int offsetY) {
        int lineHeight = Minecraft.getInstance().font.lineHeight;

        widgets.addText(
                Component.translatable("tfg.emi.combustion_engine_burn_time", 1f / burnPerSec32, CombustionEngineBlockEntity.DEFAULT_SPEED),
                2, offsetY, 16777215, true);
        offsetY += lineHeight;

        widgets.addText(
                Component.translatable("tfg.emi.combustion_engine_burn_time", 1f / burnPerSec256, 256),
                2, offsetY, 16777215, true);
        return offsetY + lineHeight;
    }
}
