package su.terrafirmagreg.core.common.data.recipes.repair;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.data.TFGRecipeSerializers;

/**
 * Crafting recipe that restores durability on damaged items. Uses a shaped 3x3 pattern. The item under key H must have
 * durability and already be damaged. R is repair material and is consumed; T is the main tool and loses one durability per
 * craft; C is an optional extra ingredient that is consumed. Repair amount is max durability times repairPercentage; tool wear
 * amount is not configurable per recipe.
 */
public class ItemRepairRecipe extends CustomRecipe {

    private final List<String> pattern;
    private final Map<Character, Ingredient> key;
    private final int width;
    private final int height;
    private final float repairPercentage;

    public ItemRepairRecipe(ResourceLocation id, CraftingBookCategory category,
            List<String> pattern, Map<Character, Ingredient> key,
            float repairPercentage) {
        super(id, category);
        this.pattern = pattern;
        this.key = key;
        this.height = pattern.size();
        this.width = pattern.get(0).length();
        this.repairPercentage = repairPercentage;
    }

    @Override
    public boolean matches(@Nonnull CraftingContainer container, @Nonnull Level level) {
        if (!RecipePatternMatcher.matchesPattern(container, pattern, key, width, height)) {
            return false;
        }
        return findRepairableStack(container) != null;
    }

    /** Pattern and JSON key letter for the stack that gets repaired (always H). */
    public static final char REPAIRABLE_SYMBOL = 'H';

    @Nullable
    private ItemStack findRepairableStack(CraftingContainer container) {
        Ingredient repairableIngredient = key.get(REPAIRABLE_SYMBOL);
        if (repairableIngredient == null || repairableIngredient.isEmpty()) {
            return null;
        }
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty())
                continue;
            if (stack.isDamageableItem() && stack.isDamaged() && repairableIngredient.test(stack)) {
                return stack;
            }
        }
        return null;
    }

    @Override
    public ItemStack assemble(@Nonnull CraftingContainer container, @Nonnull RegistryAccess registryAccess) {
        ItemStack toRepair = findRepairableStack(container);
        return computeRepairedResult(toRepair, repairPercentage);
    }

    /**
     * Returns repaired copy of input using the same formula as assemble(). Empty if input cannot be repaired.
     */
    public static ItemStack computeRepairedResult(@Nullable ItemStack input, float repairPercentage) {
        if (input == null || input.isEmpty() || !input.isDamageableItem() || !input.isDamaged()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = input.copy();
        int maxDurability = result.getMaxDamage();
        int repairAmount = (int) (maxDurability * repairPercentage);
        int newDamage = Math.max(0, result.getDamageValue() - repairAmount);
        result.setDamageValue(newDamage);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TFGRecipeSerializers.ITEM_REPAIR.get();
    }

    /**
     * Clears the repaired item slot (result takes its place), consumes R and C where present, and applies one durability loss
     * to stacks counted as tools, including the T slot and anything RepairRecipeHelper treats as a tool (so a file in C may wear
     * down instead of vanishing).
     */
    @Override
    public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        Ingredient toolCheck = key.get('T');
        Ingredient repairMaterialCheck = key.get('R');
        Ingredient consumableCheck = key.get('C');
        Ingredient repairableCheck = key.get(REPAIRABLE_SYMBOL);

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty())
                continue;

            if (repairableCheck != null && repairableCheck.test(stack)) {
                remaining.set(i, ItemStack.EMPTY);
                continue;
            }

            boolean isTool = (toolCheck != null && toolCheck.test(stack)) || RepairRecipeHelper.isToolItem(stack);
            if (isTool) {
                remaining.set(i, RepairRecipeHelper.damageToolItem(stack, 1));
                continue;
            }

            if (consumableCheck != null && consumableCheck.test(stack)) {
                remaining.set(i, ItemStack.EMPTY);
                continue;
            }

            if (repairMaterialCheck != null && RepairRecipeHelper.isRepairMaterial(stack, repairMaterialCheck)) {
                remaining.set(i, ItemStack.EMPTY);
                continue;
            }
        }

        return remaining;
    }

    public List<String> getPattern() {
        return pattern;
    }

    public Map<Character, Ingredient> getKey() {
        return key;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getRepairPercentage() {
        return repairPercentage;
    }
}
