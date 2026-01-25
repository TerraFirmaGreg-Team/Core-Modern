package su.terrafirmagreg.core.common.data.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;

import net.dries007.tfc.common.recipes.ISimpleRecipe;
import net.dries007.tfc.common.recipes.RecipeSerializerImpl;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGRecipeSerializers;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;
import su.terrafirmagreg.core.common.data.container.ArtisanTableContainer;

/**
 * Represents the recipes for the Artisan Table.
 */
@SuppressWarnings("java:S125")
public class ArtisanRecipe implements ISimpleRecipe<ArtisanTableContainer.RecipeHandler> {

    private final ResourceLocation id;
    @Getter
    private final ArtisanPattern pattern;
    @Getter
    private final ItemStack result;
    @Getter
    private final @Nullable Ingredient ingredient;
    @Getter
    private final ArrayList<TagKey<Item>> tools;
    @Getter
    private final ArtisanType artisanType;

    /**
     * Constructs a new ArtisanRecipe.
     * @param id         The recipe ID.
     * @param pattern    The artisan pattern required.
     * @param result     The resulting ItemStack.
     * @param ingredient The ingredient required (nullable).
     * @param tools      The required tool tags.
     * @param type       The artisan type.
     */
    public ArtisanRecipe(ResourceLocation id, ArtisanPattern pattern, ItemStack result, @Nullable Ingredient ingredient, ArrayList<TagKey<Item>> tools, ArtisanType type) {
        this.id = id;
        this.pattern = pattern;
        this.result = result;
        this.ingredient = ingredient;
        this.tools = tools;
        this.artisanType = type;
    }

    /**
     * Checks if the recipe matches the given handler and level.
     * @param recipeHandler The recipe handler.
     * @param level         The world level.
     * @return True if the recipe matches.
     */
    @Override
    public boolean matches(ArtisanTableContainer.RecipeHandler recipeHandler, @NotNull Level level) {
        boolean patternMatch = recipeHandler.container().getPattern().matches(pattern);
        boolean inputsMatch = matchesItems(recipeHandler.container().getInputItems());
        boolean toolsMatch = matchesTools(recipeHandler.container().getToolItems());

        return patternMatch && inputsMatch && toolsMatch;
    }

    /**
     * Checks if the provided item stacks match the recipe's ingredient requirements.
     * @param stacks The input item stacks.
     * @return True if the items match.
     */
    public boolean matchesItems(ArrayList<ItemStack> stacks) {
        var stdStacks = stacks.stream().filter(itemStack -> !itemStack.isEmpty()).toList();

        if (stdStacks.size() == 1) {
            assert ingredient != null;
            return ingredient.test(stdStacks.get(0));
        } else if (stdStacks.size() == 2) {
            assert ingredient != null;
            return ingredient.test(stdStacks.get(0)) && ingredient.test(stdStacks.get(1));
        }
        return false;
    }

    /**
     * Checks if the provided tool stacks match the recipe's tool requirements.
     * @param toolStacks The tool item stacks.
     * @return True if the tools match.
     */
    public boolean matchesTools(ArrayList<ItemStack> toolStacks) {
        for (TagKey<Item> toolTag : tools) {
            boolean matchToolA = toolStacks.get(0).is(toolTag);
            boolean matchToolB = toolStacks.get(1).is(toolTag);

            if (!matchToolA && !matchToolB) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the result item for this recipe.
     * @param registryAccess The registry access.
     * @return The result ItemStack.
     */
    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return result;
    }

    /**
     * @return The recipe ID.
     */
    @Override
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    /**
     * @return The recipe serializer.
     */
    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return TFGRecipeSerializers.ARTISAN.get();
    }

    /**
     * @return The recipe type.
     */
    @Override
    public @NotNull RecipeType<?> getType() {
        return TFGRecipeTypes.ARTISAN.get();
    }

    /**
     * Serializer for ArtisanRecipe.
     */
    public static class Serializer extends RecipeSerializerImpl<ArtisanRecipe> {

        /**
         * Reads an ArtisanRecipe from JSON.
         * @param recipeId The recipe ID.
         * @param json     The JSON object.
         * @return The ArtisanRecipe.
         */
        @Override
        public @NotNull ArtisanRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            final ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            final ArtisanPattern pattern = ArtisanPattern.fromJson(json);
            final ArtisanType type = ArtisanType.ARTISAN_TYPES.get(ResourceLocation.parse(GsonHelper.getAsString(json, "artisanType")));
            return new ArtisanRecipe(recipeId, pattern, result, Ingredient.of(type.getInputItems().stream()), type.getToolTags(), type);
        }

        /**
         * Reads an ArtisanRecipe from the network buffer.
         * @param recipeId The recipe ID.
         * @param buffer   The network buffer.
         * @return The ArtisanRecipe.
         */
        @Nullable
        @Override
        public ArtisanRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            final ArtisanPattern pattern = ArtisanPattern.fromNetwork(buffer);
            final ItemStack stack = buffer.readItem();
            final @Nullable Ingredient ingredient = Helpers.decodeNullable(buffer, Ingredient::fromNetwork);
            final ArrayList<TagKey<Item>> tools = pathsToTags(buffer.readCollection(c -> new ArrayList<>(), FriendlyByteBuf::readUtf));

            return new ArtisanRecipe(recipeId, pattern, stack, ingredient, tools, null);
        }

        /**
         * Writes an ArtisanRecipe to the network buffer.
         * @param buffer The network buffer.
         * @param recipe The ArtisanRecipe.
         */
        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, ArtisanRecipe recipe) {
            recipe.getPattern().toNetwork(buffer);
            buffer.writeItem(recipe.result);
            Helpers.encodeNullable(recipe.ingredient, buffer, Ingredient::toNetwork);
            buffer.writeCollection(tagsToPaths(recipe.tools), FriendlyByteBuf::writeUtf);
        }

        /**
         * Converts tool tags to their path strings.
         * @param tools The tool tags.
         * @return A collection of tag paths.
         */
        private Collection<String> tagsToPaths(ArrayList<TagKey<Item>> tools) {
            return tools.stream().map(TagKey::location).map(ResourceLocation::getPath).toList();
        }

        /**
         * Converts a list of tag paths to TagKey<Item> objects.
         * @param paths The tag paths.
         * @return The list of TagKey<Item>.
         */
        private ArrayList<TagKey<Item>> pathsToTags(ArrayList<String> paths) {
            return new ArrayList<>(Arrays.asList(TagUtil.createItemTag(paths.get(0)), TagUtil.createItemTag(paths.get(1))));
        }

        /**
         * Parses tool tags from a JSON object.
         * @param json The JSON object.
         * @return The list of TagKey<Item>.
         */
        private ArrayList<TagKey<Item>> jsonToTags(JsonObject json) {
            final JsonArray baseArray = GsonHelper.getAsJsonArray(json, "tools");
            ArrayList<TagKey<Item>> tags = new ArrayList<>();
            for (JsonElement element : baseArray) {
                String path = element.getAsString().split(":")[1];
                tags.add(TagUtil.createItemTag(path));
            }
            return tags;
        }
    }
}
