package su.terrafirmagreg.core.common.data.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nullable;

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

    public ArtisanRecipe(ResourceLocation id, ArtisanPattern pattern, ItemStack result, @Nullable Ingredient ingredient, ArrayList<TagKey<Item>> tools, ArtisanType type) {
        this.id = id;
        this.pattern = pattern;
        this.result = result;
        this.ingredient = ingredient;
        this.tools = tools;
        this.artisanType = type;
    }

    @Override
    public boolean matches(ArtisanTableContainer.RecipeHandler recipeHandler, Level level) {
        boolean patternMatch = recipeHandler.container().getPattern().matches(pattern);
        boolean inputsMatch = matchesItems(recipeHandler.container().getInputItems());
        boolean toolsMatch = matchesTools(recipeHandler.container().getToolItems());

        /*System.out.println(patternMatch);
        System.out.println(inputMatch);
        System.out.println(toolsMatch);*/

        return patternMatch && inputsMatch && toolsMatch;
    }

    public boolean matchesItems(ArrayList<ItemStack> stacks) {
        var stdStacks = stacks.stream().filter(itemStack -> !itemStack.isEmpty()).toList();

        if (stdStacks.size() == 1) {
            if (ingredient.test(stdStacks.get(0))) {
                //System.out.println("Ingredient Matches");
                return true;
            }
        } else if (stdStacks.size() == 2) {
            if (ingredient.test(stdStacks.get(0)) && ingredient.test(stdStacks.get(1))) {
                //System.out.println("Ingredients Match");
                return true;
            }
        }
        //System.out.println("No Ingredient Match");
        return false;
    }

    public boolean matchesTools(ArrayList<ItemStack> toolStacks) {
        for (TagKey<Item> toolTag : tools) {
            boolean matchToolA = toolStacks.get(0).is(toolTag);
            boolean matchToolB = toolStacks.get(1).is(toolTag);

            if (!matchToolA && !matchToolB) {
                System.out.println("No Tool Match");
                return false;
            }
        }
        System.out.println("Tool Match");
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TFGRecipeSerializers.ARTISAN.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TFGRecipeTypes.ARTISAN.get();
    }

    public static class Serializer extends RecipeSerializerImpl<ArtisanRecipe> {

        @Override
        public ArtisanRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            final ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            final ArtisanPattern pattern = ArtisanPattern.fromJson(json);
            final ArtisanType type = ArtisanType.ARTISAN_TYPES.get(ResourceLocation.parse(GsonHelper.getAsString(json, "artisanType")));
            return new ArtisanRecipe(recipeId, pattern, result, Ingredient.of(type.getInputItems().stream()), type.getToolTags(), type);
        }

        @Nullable
        @Override
        public ArtisanRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            final ArtisanPattern pattern = ArtisanPattern.fromNetwork(buffer);
            final ItemStack stack = buffer.readItem();
            final @Nullable Ingredient ingredient = Helpers.decodeNullable(buffer, Ingredient::fromNetwork);
            final ArrayList<TagKey<Item>> tools = pathsToTags(buffer.readCollection(c -> new ArrayList<>(), FriendlyByteBuf::readUtf));

            return new ArtisanRecipe(recipeId, pattern, stack, ingredient, tools, null);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ArtisanRecipe recipe) {
            recipe.getPattern().toNetwork(buffer);
            buffer.writeItem(recipe.result);
            Helpers.encodeNullable(recipe.ingredient, buffer, Ingredient::toNetwork);
            buffer.writeCollection(tagsToPaths(recipe.tools), FriendlyByteBuf::writeUtf);
        }

        private Collection<String> tagsToPaths(ArrayList<TagKey<Item>> tools) {
            return tools.stream().map(TagKey::location).map(ResourceLocation::getPath).toList();
        }

        private ArrayList<TagKey<Item>> pathsToTags(ArrayList<String> paths) {
            return new ArrayList<>(Arrays.asList(TagUtil.createItemTag(paths.get(0)), TagUtil.createItemTag(paths.get(1))));
        }

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
