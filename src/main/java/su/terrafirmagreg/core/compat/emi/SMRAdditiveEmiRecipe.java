package su.terrafirmagreg.core.compat.emi;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
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
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric.SMRGenerator2;

public class SMRAdditiveEmiRecipe implements EmiRecipe {

    public enum Type {

        LUBRICANT("lubricant", 0xFFFF55),
        BOOSTER("booster", 0x55FFFF);

        private final String name;
        private final int color;

        Type(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }

    private static final int TICKS_PER_HOUR = 72000;

    private final Fluid fluid;
    private final SMRGenerator2.FluidBonus bonus;
    private final Type type;

    public SMRAdditiveEmiRecipe(Fluid fluid, SMRGenerator2.FluidBonus bonus, Type type) {
        this.fluid = fluid;
        this.bonus = bonus;
        this.type = type;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.SMR_ADDITIVES;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
        return TFGCore.id("/smr_" + type.name + "_" + fluidId.getNamespace() + "_" + fluidId.getPath());
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
        return 160;
    }

    @Override
    public int getDisplayHeight() {
        int lines = type == Type.LUBRICANT ? 3 : 2;
        return 2 + 18 + 4 + lines * (Minecraft.getInstance().font.lineHeight + 1) + 2;
    }

    @Override
    public void addWidgets(WidgetHolder holder) {
        int lineHeight = Minecraft.getInstance().font.lineHeight + 1;

        SlotWidget slot = new SlotWidget(EmiStack.of(fluid), 2, 2);
        holder.add(slot);

        int textX = slot.getBounds().right() + 4;
        holder.addText(Component.translatable("tfg.emi.smr." + type.name), textX, 2, type.color, true);
        holder.addText(fluid.getFluidType().getDescription(), textX, 2 + lineHeight, 0xFFFFFF, true);

        int y = slot.getBounds().bottom() + 4;

        Component effect = type == Type.LUBRICANT ? Component.translatable("tfg.emi.smr.lubricant_effect", formatMultiplier(bonus.tier() / 2.0))
                : Component.translatable("tfg.emi.smr.booster_effect", formatMultiplier(bonus.tier() * 2.0));
        holder.addText(effect, 2, y, 0xFFFFFF, true);
        y += lineHeight;

        holder.addText(Component.translatable("tfg.emi.smr.consumption",
                String.format("%.1f", bonus.tickCycle() / 20.0),
                TICKS_PER_HOUR / bonus.tickCycle()), 2, y, 0xFFFFFF, true);
        y += lineHeight;

        if (type == Type.LUBRICANT) {
            holder.addText(Component.translatable("tfg.emi.smr.lubricant_required"), 2, y, 0xFF5555, true);
        }
    }

    private static String formatMultiplier(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
