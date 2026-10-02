package su.terrafirmagreg.core.common.recipe;

import java.util.Optional;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import net.dries007.tfc.common.recipes.ISimpleRecipe;
import net.dries007.tfc.common.recipes.RecipeSerializerImpl;
import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.dries007.tfc.common.recipes.inventory.EmptyInventory;
import net.dries007.tfc.util.JsonHelpers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGRecipeSerializers;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;

public class GasBurnerFuelRecipe implements ISimpleRecipe<EmptyInventory> {

    private final ResourceLocation id;
    @Getter
    private final FluidStackIngredient fluid;
    @Getter
    private final int duration;
    @Getter
    private final int temperature;

    public GasBurnerFuelRecipe(ResourceLocation id, FluidStackIngredient fluid, int duration, int temperature) {
        this.id = id;
        this.fluid = fluid;
        this.duration = duration;
        this.temperature = temperature;
    }

    public static Optional<GasBurnerFuelRecipe> getRecipe(Level level, FluidStack stack) {
        if (stack.isEmpty() || level == null) {
            return Optional.empty();
        }
        return level.getRecipeManager().getAllRecipesFor(TFGRecipeTypes.GAS_BURNER_FUEL.get()).stream()
                .filter(r -> r.matches(stack))
                .findFirst();
    }

    public boolean matches(FluidStack stack) {
        return this.fluid.test(stack);
    }

    @Override
    public boolean matches(@NotNull EmptyInventory inventory, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull EmptyInventory inventory, @NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return TFGRecipeSerializers.GAS_BURNER_FUEL.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return TFGRecipeTypes.GAS_BURNER_FUEL.get();
    }

    public static class Serializer extends RecipeSerializerImpl<GasBurnerFuelRecipe> {

        @Override
        public @NotNull GasBurnerFuelRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            FluidStackIngredient fluid = FluidStackIngredient.fromJson(JsonHelpers.getAsJsonObject(json, "fluid"));
            int duration = JsonHelpers.getAsInt(json, "duration", 200);
            int temperature = JsonHelpers.getAsInt(json, "temperature", 1000);
            return new GasBurnerFuelRecipe(recipeId, fluid, duration, temperature);
        }

        @Override
        public @Nullable GasBurnerFuelRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            FluidStackIngredient fluid = FluidStackIngredient.fromNetwork(buffer);
            int duration = buffer.readVarInt();
            int temperature = buffer.readVarInt();
            return new GasBurnerFuelRecipe(recipeId, fluid, duration, temperature);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull GasBurnerFuelRecipe recipe) {
            recipe.fluid.toNetwork(buffer);
            buffer.writeVarInt(recipe.duration);
            buffer.writeVarInt(recipe.temperature);
        }
    }
}
