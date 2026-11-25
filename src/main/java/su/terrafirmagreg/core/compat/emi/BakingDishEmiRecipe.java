package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.config.TFCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.recipe.BakingDishRecipe;

@SuppressWarnings({ "ConstantValue", "UnnecessaryLocalVariable" })
public class BakingDishEmiRecipe implements EmiRecipe {

    private static final ResourceLocation PB = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/baking_progress_bar.png");
    private static final ResourceLocation BAKING_DISH = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/emi/baking_dish_emi.png");
    private static final int BAKING_DISH_WIDTH = 50;
    private static final int BAKING_DISH_HEIGHT = 96;
    private static final int PB_SIZE = 20;

    private static final EmiTexture BAKING_DISH_BG = new EmiTexture(
            BAKING_DISH,
            0, 0,
            BAKING_DISH_WIDTH, BAKING_DISH_HEIGHT,
            BAKING_DISH_WIDTH, BAKING_DISH_HEIGHT,
            128, 128);

    private static final EmiTexture PB_BACKGROUND = new EmiTexture(
            PB,
            0, 0,
            PB_SIZE, PB_SIZE,
            PB_SIZE, PB_SIZE,
            PB_SIZE * 2,
            PB_SIZE);
    private static final EmiTexture PB_FOREGROUND = new EmiTexture(
            PB,
            PB_SIZE, 0,
            PB_SIZE, PB_SIZE,
            PB_SIZE, PB_SIZE,
            PB_SIZE * 2, PB_SIZE);

    private static final int WIDTH = (BAKING_DISH_WIDTH * 2) + 10;
    private static final int HEIGHT = BAKING_DISH_HEIGHT;
    private static final int SLOT_SIZE = 18;

    @Getter
    private final BakingDishRecipe recipe;
    private final List<EmiIngredient> itemInputs;
    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> catalysts;
    private final List<EmiStack> outputs;

    public BakingDishEmiRecipe(BakingDishRecipe recipe) {
        this.recipe = recipe;

        this.itemInputs = new ArrayList<>();
        // Ingredients
        for (Ingredient ing : recipe.getItemIngredients()) {
            if (ing.isEmpty()) {
                continue;
            }
            EmiIngredient emiIng = EmiIngredient.of(ing);
            if (emiIng.isEmpty()) {
                continue;
            }
            boolean merged = false;
            for (EmiIngredient existing : this.itemInputs) {
                if (EmiIngredient.areEqual(existing, emiIng)) {
                    existing.setAmount(existing.getAmount() + emiIng.getAmount());
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                this.itemInputs.add(emiIng.copy());
            }
        }

        this.inputs = new ArrayList<>(this.itemInputs);

        this.catalysts = List.of(
                EmiStack.of(TFGBlocks.FIRE_BOX_TOP_OVEN.get()),
                EmiIngredient.of(TFGTags.Items.BAKING_DISHES));

        // Item Outputs
        this.outputs = new ArrayList<>();
        for (var provider : recipe.getItemOutputs()) {
            ItemStack out = provider.getStack(ItemStack.EMPTY);
            if (!out.isEmpty()) {
                this.outputs.add(EmiStack.of(out));
            }
        }
        // Fluid Output
        for (var fluid : recipe.getFluidOutputs()) {
            if (!fluid.isEmpty()) {
                this.outputs.add(EmiStack.of(fluid.getFluid(), fluid.getTag(), fluid.getAmount()));
            }
        }
    }

    private Component getFormattedTemp() {
        float temp = recipe.getMinTemp();
        MutableComponent formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temp);
        if (formattedTemp == null) {
            formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatRange(temp);
        }
        if (formattedTemp == null) {
            formattedTemp = Component.literal(String.format("%.0f°C", temp));
        }

        return Component.literal("🔥 ").append(formattedTemp);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.BAKING_DISH;
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
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(this.getFormattedTemp());
        return WIDTH + textWidth;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder holder) {

        // Item Inputs
        int inX = 7;
        int inY = 12;
        holder.addTexture(BAKING_DISH_BG, inX - 7, inY - 12);

        int inIdx = 0;
        for (EmiIngredient ing : this.itemInputs) {
            int col = inIdx % 2;
            int row = inIdx / 2;
            holder.addSlot(ing, inX + (col * SLOT_SIZE), inY + (row * SLOT_SIZE));
            inIdx++;
        }

        // Fluid Input
        if (!recipe.getFluidIngredients().isEmpty()) {
            var fsi = recipe.getFluidIngredients().get(0);
            var fluidStacks = fsi.ingredient().fluids().stream()
                    .map(fluid -> (EmiIngredient) EmiStack.of(fluid, fsi.amount()))
                    .toList();
            if (!fluidStacks.isEmpty()) {
                holder.addSlot(EmiIngredient.of(fluidStacks), inX, inY + (3 * SLOT_SIZE));
            }
        }

        // Text Width
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(this.getFormattedTemp());

        // Progress Bar
        int pbX = inX + BAKING_DISH_WIDTH + (textWidth / 2) - (PB_SIZE / 2);
        int pbY = (HEIGHT / 2) - (PB_SIZE / 2) - 8;

        holder.addFillingArrow(pbX + 12, pbY + 2, Integer.MAX_VALUE);
        holder.addFillingArrow(pbX - 12, pbY + 2, Integer.MAX_VALUE);
        holder.addTexture(PB_BACKGROUND, pbX, pbY);
        holder.addAnimatedTexture(PB_FOREGROUND, pbX, pbY, 8000, false, true, true);

        int ovenX = pbX + (SLOT_SIZE / 2);
        int ovenY = pbY + PB_SIZE - 5;
        holder.addSlot(EmiStack.of(TFGBlocks.FIRE_BOX_TOP_OVEN.get()), ovenX, ovenY).drawBack(false).catalyst(true);

        // Item Outputs
        int outX = this.getDisplayWidth() - BAKING_DISH_WIDTH + 7;
        int outY = inY;
        holder.addTexture(BAKING_DISH_BG, outX - 7, outY - 12);

        int outIdx = 0;
        for (var provider : recipe.getItemOutputs()) {
            ItemStack out = provider.getStack(ItemStack.EMPTY);
            int col = outIdx % 2;
            int row = outIdx / 2;
            holder.addSlot(EmiStack.of(out), outX + (col * SLOT_SIZE), outY + (row * SLOT_SIZE)).recipeContext(this);
            outIdx++;
        }

        // Fluid Output Slot
        if (!recipe.getFluidOutputs().isEmpty()) {
            var fluid = recipe.getFluidOutputs().get(0);
            if (!fluid.isEmpty()) {
                holder.addSlot(EmiStack.of(fluid.getFluid(), fluid.getTag(), fluid.getAmount()), outX, outY + (3 * SLOT_SIZE)).recipeContext(this);
            }
        }

        // Temperature Text
        int textX = Math.max(0, pbX - (textWidth / 2) + (PB_SIZE / 2));
        int textY = this.getDisplayHeight() - 16;
        holder.add(new TextWidget(this.getFormattedTemp().getVisualOrderText(), textX, textY, 0xFFFFFF, true));
    }
}
