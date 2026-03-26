package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import su.terrafirmagreg.core.common.recipe.repair.ItemRepairRecipe;

/** Shows a tfg:item_repair recipe in EMI from its pattern and key. */
public class ItemRepairEmiRecipe implements EmiRecipe {

    private final ItemRepairRecipe recipe;
    private final List<EmiIngredient> inputs;

    public ItemRepairEmiRecipe(ItemRepairRecipe recipe) {
        this.recipe = recipe;
        this.inputs = buildInputs(recipe);
    }

    private static List<EmiIngredient> buildInputs(ItemRepairRecipe recipe) {
        List<String> pattern = recipe.getPattern();
        Map<Character, Ingredient> key = recipe.getKey();
        List<EmiIngredient> result = new ArrayList<>();

        for (String row : pattern) {
            for (int x = 0; x < row.length(); x++) {
                char symbol = row.charAt(x);
                if (symbol == ' ') {
                    result.add(EmiStack.EMPTY);
                } else {
                    Ingredient ing = key.get(symbol);
                    if (ing != null && !ing.isEmpty()) {
                        result.add(EmiIngredient.of(ing));
                    } else {
                        result.add(EmiStack.EMPTY);
                    }
                }
            }
        }
        return result;
    }

    private static EmiStack buildOutput(ItemRepairRecipe recipe) {
        Ingredient repairableIngredient = recipe.getKey().get(ItemRepairRecipe.REPAIRABLE_SYMBOL);
        if (repairableIngredient == null || repairableIngredient.isEmpty()) {
            return EmiStack.EMPTY;
        }
        for (ItemStack stack : repairableIngredient.getItems()) {
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                ItemStack sampleDamaged = stack.copy();
                int maxDurability = sampleDamaged.getMaxDamage();
                if (!sampleDamaged.isDamaged() && maxDurability > 0) {
                    sampleDamaged.setDamageValue(maxDurability - 1);
                }

                ItemStack repaired = ItemRepairRecipe.computeRepairedResult(sampleDamaged, recipe.getRepairPercentage());
                return repaired.isEmpty() ? EmiStack.EMPTY : EmiStack.of(repaired);
            }
        }
        return EmiStack.EMPTY;
    }

    private EmiStack buildDynamicOrFallbackOutput() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && mc.player.containerMenu instanceof CraftingMenu craftingMenu) {
            ItemStack dynamic = ItemRepairCraftingRecipeHandler.getDynamicOutput(craftingMenu, recipe);
            if (!dynamic.isEmpty()) {
                return EmiStack.of(dynamic);
            }
        }
        // Fallback for recipe list/search screens where no live crafting grid context is available.
        return buildOutput(recipe);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.ITEM_REPAIR;
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
        return List.of(buildDynamicOrFallbackOutput());
    }

    @Override
    public int getDisplayWidth() {
        return 116;
    }

    @Override
    public int getDisplayHeight() {
        return 54;
    }

    @Override
    public void addWidgets(@NotNull WidgetHolder holder) {
        int slotSize = 18;
        int startX = 0;
        int startY = 0;
        int width = recipe.getWidth();
        int height = recipe.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                if (idx < inputs.size()) {
                    holder.addSlot(inputs.get(idx), startX + x * slotSize, startY + y * slotSize);
                }
            }
        }

        holder.addSlot(buildDynamicOrFallbackOutput(), startX + width * slotSize + 5, startY + (height * slotSize) / 2 - 9)
                .recipeContext(this);
    }
}
