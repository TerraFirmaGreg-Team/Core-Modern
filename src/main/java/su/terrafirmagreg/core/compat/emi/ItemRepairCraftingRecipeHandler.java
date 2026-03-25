package su.terrafirmagreg.core.compat.emi;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.recipes.repair.ItemRepairRecipe;

/** Lets EMI fill the crafting grid from tfg:item_repair recipes (plus button). */
public class ItemRepairCraftingRecipeHandler implements StandardRecipeHandler<CraftingMenu> {

    private static final ResourceLocation ITEM_REPAIR_CATEGORY = TFGCore.id("item_repair");

    @Override
    public List<Slot> getInputSources(CraftingMenu handler) {
        List<Slot> list = Lists.newArrayList();
        for (int i = 1; i < 10; i++) {
            list.add(handler.getSlot(i));
        }
        int invStart = 10;
        for (int i = invStart; i < invStart + 36; i++) {
            list.add(handler.getSlot(i));
        }
        return list;
    }

    @Override
    public List<Slot> getCraftingSlots(CraftingMenu handler) {
        List<Slot> list = Lists.newArrayList();
        for (int i = 1; i < 10; i++) {
            list.add(handler.getSlot(i));
        }
        return list;
    }

    @Override
    public @Nullable Slot getOutputSlot(CraftingMenu handler) {
        return handler.slots.get(0);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        if (recipe == null || recipe.getCategory() == null || recipe.getCategory().getId() == null) {
            return false;
        }
        return ITEM_REPAIR_CATEGORY.equals(recipe.getCategory().getId());
    }

    /**
     * Dynamic repaired output using the live crafting grid, if current menu is a matching item_repair layout.
     */
    public static ItemStack getDynamicOutput(CraftingMenu menu, ItemRepairRecipe recipe) {
        ItemStack repairable = findRepairableInCraftingGrid(menu, recipe);
        return ItemRepairRecipe.computeRepairedResult(repairable, recipe.getRepairPercentage());
    }

    @Nullable
    private static ItemStack findRepairableInCraftingGrid(CraftingMenu menu, ItemRepairRecipe recipe) {
        Ingredient repairableIngredient = recipe.getKey().get(ItemRepairRecipe.REPAIRABLE_SYMBOL);
        if (repairableIngredient == null || repairableIngredient.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int i = 1; i <= 9; i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.isDamageableItem() && stack.isDamaged() && repairableIngredient.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
