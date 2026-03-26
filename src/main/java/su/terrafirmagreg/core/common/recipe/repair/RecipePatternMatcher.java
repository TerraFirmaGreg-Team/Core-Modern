package su.terrafirmagreg.core.common.recipe.repair;

import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Checks shaped 3x3-style patterns in a crafting grid, including offsets and mirroring. */
public class RecipePatternMatcher {

    /** True if some placement of the pattern (maybe mirrored) fits the container. */
    public static boolean matchesPattern(CraftingContainer container,
            List<String> pattern,
            Map<Character, Ingredient> key,
            int width,
            int height) {
        if (container.getWidth() < width || container.getHeight() < height) {
            return false;
        }

        for (int xOffset = 0; xOffset <= container.getWidth() - width; xOffset++) {
            for (int yOffset = 0; yOffset <= container.getHeight() - height; yOffset++) {
                if (checkPatternAtOffset(container, pattern, key, width, height, xOffset, yOffset, true)) {
                    return true;
                }
                if (checkPatternAtOffset(container, pattern, key, width, height, xOffset, yOffset, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Match test anchored at offset; normal false mirrors the pattern horizontally. */
    public static boolean checkPatternAtOffset(CraftingContainer container,
            List<String> pattern,
            Map<Character, Ingredient> key,
            int width,
            int height,
            int xOffset,
            int yOffset,
            boolean normal) {
        for (int y = 0; y < container.getHeight(); y++) {
            for (int x = 0; x < container.getWidth(); x++) {
                int patternX = x - xOffset;
                int patternY = y - yOffset;

                ItemStack stackInSlot = container.getItem(x + y * container.getWidth());

                Ingredient expectedIngredient = Ingredient.EMPTY;

                if (patternX >= 0 && patternX < width && patternY >= 0 && patternY < height) {
                    String row = pattern.get(patternY);
                    char symbol = normal ? row.charAt(patternX) : row.charAt(width - 1 - patternX);

                    if (symbol != ' ' && key.containsKey(symbol)) {
                        expectedIngredient = key.get(symbol);
                    }
                }

                if (!expectedIngredient.isEmpty()) {
                    if (!expectedIngredient.test(stackInSlot)) {
                        return false;
                    }
                } else {
                    if (!stackInSlot.isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /** Throws if pattern or key breaks recipe rules; recipeId appears in the error text. */
    public static void validatePattern(ResourceLocation recipeId,
            List<String> pattern,
            Map<Character, Ingredient> key) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Recipe " + recipeId + ": pattern cannot be empty");
        }

        if (pattern.size() > 3) {
            throw new IllegalArgumentException("Recipe " + recipeId + ": pattern height cannot exceed 3, found: " + pattern.size());
        }

        int width = pattern.get(0).length();
        if (width > 3) {
            throw new IllegalArgumentException("Recipe " + recipeId + ": pattern width cannot exceed 3, found: " + width);
        }

        for (String row : pattern) {
            if (row.length() != width) {
                throw new IllegalArgumentException("Recipe " + recipeId + ": all pattern rows must have the same length");
            }
        }

        if (key != null && key.size() > 9) {
            throw new IllegalArgumentException("Recipe " + recipeId + ": key cannot contain more than 9 symbols, found: " + key.size());
        }

        boolean hasNonSpace = false;
        for (String row : pattern) {
            for (char c : row.toCharArray()) {
                if (c != ' ') {
                    hasNonSpace = true;
                    if (key != null && !key.containsKey(c)) {
                        throw new IllegalArgumentException("Recipe " + recipeId + ": pattern symbol '" + c + "' is not defined in key");
                    }
                }
            }
        }

        if (!hasNonSpace) {
            throw new IllegalArgumentException("Recipe " + recipeId + ": pattern must contain at least one non-space symbol");
        }
    }

    /** Same as validate with id, but uses a placeholder id (KubeJS). */
    public static void validatePattern(List<String> pattern, Map<Character, Ingredient> key) {
        validatePattern(ResourceLocation.fromNamespaceAndPath("unknown", "recipe"), pattern, key);
    }
}
