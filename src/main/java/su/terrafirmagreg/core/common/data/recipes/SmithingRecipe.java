package su.terrafirmagreg.core.common.data.recipes;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.dries007.tfc.common.recipes.ISimpleRecipe;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeRecipeSerializer;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGRecipeSerializers;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;
import su.terrafirmagreg.core.common.data.container.SmithingTableContainer;

public class SmithingRecipe implements ISimpleRecipe<SmithingTableContainer.RecipeHandler> {

    private final ResourceLocation id;
    @Getter
    private final SmithingPattern pattern;
    private final ItemStack result;
    private final @Nullable Ingredient ingredient;

    public SmithingRecipe(ResourceLocation id, SmithingPattern pattern, ItemStack result, @Nullable Ingredient ingredient) {
        this.id = id;
        this.pattern = pattern;
        this.result = result;
        this.ingredient = ingredient;
    }

    @Override
    public boolean matches(SmithingTableContainer.RecipeHandler recipeHandler, Level level) {
        //recipeHandler.container().getPattern().matches(pattern) &&
        return matchesItem(recipeHandler.container().getInputItem())
                && matchesTool(recipeHandler.container().getToolItem());
    }

    public boolean matchesItem(ItemStack stack) {
        if (ingredient == null || ingredient.test(stack)) {
            System.out.println("Ingredient Matches");
            return true;
        }
        System.out.println("No Ingredient Match");
        return false;
    }

    public boolean matchesTool(ItemStack stack) {
        if (stack.is(CustomTags.HAMMERS)) {
            System.out.println("Tool Match");
            return true;
        }
        System.out.println("No Tool Match");
        return false;
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
        return TFGRecipeSerializers.SMITHING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return TFGRecipeTypes.SMITHING.get();
    }

    public static class Serializer implements RecipeSerializer<SmithingRecipe>, IForgeRecipeSerializer<SmithingRecipe> {

        @Override
        public SmithingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            final ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            final @Nullable Ingredient ingredient = json.has("ingredient") ? Ingredient.fromJson(json.get("ingredient")) : null;
            final SmithingPattern pattern = SmithingPattern.fromJson(json);
            return new SmithingRecipe(recipeId, pattern, result, ingredient);
        }

        @Nullable
        @Override
        public SmithingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            final SmithingPattern pattern = SmithingPattern.fromNetwork(buffer);
            final ItemStack stack = buffer.readItem();
            final @Nullable Ingredient ingredient = Helpers.decodeNullable(buffer, Ingredient::fromNetwork);
            return new SmithingRecipe(recipeId, pattern, stack, ingredient);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SmithingRecipe recipe) {
            recipe.getPattern().toNetwork(buffer);
            buffer.writeItem(recipe.result);
            Helpers.encodeNullable(recipe.ingredient, buffer, Ingredient::toNetwork);
        }
    }
}
