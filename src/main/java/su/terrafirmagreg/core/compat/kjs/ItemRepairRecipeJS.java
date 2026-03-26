package su.terrafirmagreg.core.compat.kjs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.world.item.crafting.Ingredient;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.MapJS;

import su.terrafirmagreg.core.common.recipe.repair.RecipePatternMatcher;

/**
 * KubeJS recipe builder for tfg:item_repair: call pattern(...) with up to three rows of up to three characters, key(...) with
 * one-letter symbols, and repairPercentage. Spaces are empty slots. H marks the damaged item to fix, R the material that is
 * eaten, T the hammer-style tool that wears down, C an optional extra consumed slot. Tag paths should use the same lowercase
 * metal names as TFGMetalType (HSSS becomes hsss in ids).
 */
public class ItemRepairRecipeJS extends RecipeJS {

    private List<String> pattern = null;
    private Map<String, Object> key = null;

    public ItemRepairRecipeJS repairPercentage(float percentage) {
        setValue(ItemRepairRecipeSchema.REPAIR_PERCENTAGE, percentage);
        return this;
    }

    public ItemRepairRecipeJS pattern(String... rows) {
        if (rows == null || rows.length == 0) {
            throw new IllegalArgumentException("Pattern cannot be empty");
        }
        if (rows.length > 3) {
            throw new IllegalArgumentException("Pattern height cannot exceed 3, found: " + rows.length);
        }

        this.pattern = new ArrayList<>();
        int width = rows[0].length();

        if (width > 3) {
            throw new IllegalArgumentException("Pattern width cannot exceed 3, found: " + width);
        }

        for (String row : rows) {
            if (row.length() != width) {
                throw new IllegalArgumentException("All pattern rows must have the same length");
            }
            this.pattern.add(row);
        }

        addPatternAndKeyToJson();
        return this;
    }

    public ItemRepairRecipeJS key(Map<String, Object> keyMap) {
        if (keyMap == null || keyMap.isEmpty()) {
            throw new IllegalArgumentException("Key map cannot be empty");
        }
        if (keyMap.size() > 9) {
            throw new IllegalArgumentException("Key cannot contain more than 9 symbols, found: " + keyMap.size());
        }

        for (String k : keyMap.keySet()) {
            if (k.length() != 1) {
                throw new IllegalArgumentException("Key symbols must be exactly 1 character long, found: " + k);
            }
        }

        this.key = keyMap;
        addPatternAndKeyToJson();
        return this;
    }

    private void addPatternAndKeyToJson() {
        if (this.pattern != null && !this.pattern.isEmpty() && this.key != null && !this.key.isEmpty()) {
            validatePatternAndKey();

            JsonArray patternArray = new JsonArray();
            for (String row : this.pattern) {
                patternArray.add(row);
            }
            json.add("pattern", patternArray);

            JsonObject keyObject = new JsonObject();
            for (Map.Entry<String, Object> entry : this.key.entrySet()) {
                String symbol = entry.getKey();
                Object ingredient = entry.getValue();

                JsonElement ingredientJson;
                if (ingredient instanceof String) {
                    JsonObject itemObj = new JsonObject();
                    itemObj.addProperty("item", (String) ingredient);
                    ingredientJson = itemObj;
                } else if (ingredient instanceof Map) {
                    ingredientJson = MapJS.json((Map<?, ?>) ingredient);
                } else if (ingredient instanceof JsonElement) {
                    ingredientJson = (JsonElement) ingredient;
                } else {
                    JsonObject itemObj = new JsonObject();
                    itemObj.addProperty("item", ingredient.toString());
                    ingredientJson = itemObj;
                }

                keyObject.add(symbol, ingredientJson);
            }
            json.add("key", keyObject);
        }
    }

    private void validatePatternAndKey() {
        Map<Character, Ingredient> ingredientKey = new HashMap<>();
        for (Map.Entry<String, Object> entry : this.key.entrySet()) {
            char symbol = entry.getKey().charAt(0);
            ingredientKey.put(symbol, Ingredient.EMPTY);
        }

        try {
            RecipePatternMatcher.validatePattern(this.pattern, ingredientKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Pattern validation failed: " + e.getMessage(), e);
        }
    }

    //TODO
}
