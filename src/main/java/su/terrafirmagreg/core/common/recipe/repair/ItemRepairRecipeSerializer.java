package su.terrafirmagreg.core.common.recipe.repair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Loads and saves tfg:item_repair. JSON must have pattern and key; repairPercentage is optional and defaults to 0.25.
 */
public class ItemRepairRecipeSerializer implements RecipeSerializer<ItemRepairRecipe> {
    public static final float DEFAULT_REPAIR_PERCENT = 0.25f;

    @Override
    public ItemRepairRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        @SuppressWarnings("deprecation")
        CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                json.has("category") ? json.get("category").getAsString() : "misc",
                CraftingBookCategory.MISC);

        float repairPercentage = json.has("repairPercentage")
                ? json.get("repairPercentage").getAsFloat()
                : DEFAULT_REPAIR_PERCENT;

        if (!json.has("pattern") || !json.has("key")) {
            throw new IllegalArgumentException("Recipe " + recipeId + ": item_repair requires pattern and key");
        }

        List<String> pattern = readPattern(json.getAsJsonArray("pattern"));
        Map<Character, Ingredient> key = readKey(json.getAsJsonObject("key"));

        RecipePatternMatcher.validatePattern(recipeId, pattern, key);

        return new ItemRepairRecipe(recipeId, category, pattern, key, repairPercentage);
    }

    private List<String> readPattern(JsonArray patternArray) {
        List<String> pattern = new ArrayList<>();
        for (JsonElement element : patternArray) {
            pattern.add(element.getAsString());
        }
        return pattern;
    }

    private Map<Character, Ingredient> readKey(JsonObject keyObject) {
        Map<Character, Ingredient> key = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : keyObject.entrySet()) {
            if (entry.getKey().length() != 1) {
                throw new IllegalArgumentException("Key symbols must be exactly 1 character long, found: " + entry.getKey());
            }
            char symbol = entry.getKey().charAt(0);
            Ingredient ingredient = Ingredient.fromJson(entry.getValue());
            key.put(symbol, ingredient);
        }
        return key;
    }

    @Override
    public ItemRepairRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
        List<String> pattern = readPatternFromNetwork(buffer);
        Map<Character, Ingredient> key = readKeyFromNetwork(buffer);
        float repairPercentage = buffer.readFloat();
        return new ItemRepairRecipe(recipeId, category, pattern, key, repairPercentage);
    }

    private List<String> readPatternFromNetwork(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<String> pattern = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            pattern.add(buffer.readUtf());
        }
        return pattern;
    }

    private Map<Character, Ingredient> readKeyFromNetwork(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Map<Character, Ingredient> key = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            char symbol = buffer.readChar();
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            key.put(symbol, ingredient);
        }
        return key;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ItemRepairRecipe recipe) {
        buffer.writeEnum(recipe.category());
        writePatternToNetwork(buffer, recipe.getPattern());
        writeKeyToNetwork(buffer, recipe.getKey());
        buffer.writeFloat(recipe.getRepairPercentage());
    }

    private void writePatternToNetwork(FriendlyByteBuf buffer, List<String> pattern) {
        buffer.writeVarInt(pattern.size());
        for (String row : pattern) {
            buffer.writeUtf(row);
        }
    }

    private void writeKeyToNetwork(FriendlyByteBuf buffer, Map<Character, Ingredient> key) {
        buffer.writeVarInt(key.size());
        for (Map.Entry<Character, Ingredient> entry : key.entrySet()) {
            buffer.writeChar(entry.getKey());
            entry.getValue().toNetwork(buffer);
        }
    }
}
