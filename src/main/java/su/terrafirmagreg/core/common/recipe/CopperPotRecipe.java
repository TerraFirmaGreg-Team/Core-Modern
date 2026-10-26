package su.terrafirmagreg.core.common.recipe;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

import su.terrafirmagreg.core.common.data.TFGRecipeSerializers;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;
import su.terrafirmagreg.core.common.recipe.attachment.RecipeAttachment;

@SuppressWarnings("unused")
public class CopperPotRecipe extends CookingApplianceRecipe {

    public CopperPotRecipe(
            ResourceLocation id,
            NonNullList<Ingredient> itemIngredients,
            List<FluidStackIngredient> fluidIngredients,
            Ingredient catalyst,
            Ingredient tool,
            int toolDamage,
            List<ItemStackProvider> itemOutputs,
            List<FluidStack> fluidOutputs,
            int duration,
            float minTemp) {
        super(id, itemIngredients, fluidIngredients, catalyst, tool, toolDamage, itemOutputs, fluidOutputs, duration, minTemp, List.of());
    }

    public CopperPotRecipe(
            ResourceLocation id,
            NonNullList<Ingredient> itemIngredients,
            List<FluidStackIngredient> fluidIngredients,
            Ingredient catalyst,
            Ingredient tool,
            int toolDamage,
            List<ItemStackProvider> itemOutputs,
            List<FluidStack> fluidOutputs,
            int duration,
            float minTemp,
            List<RecipeAttachment> attachments) {
        super(id, itemIngredients, fluidIngredients, catalyst, tool, toolDamage, itemOutputs, fluidOutputs, duration, minTemp, attachments);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return TFGRecipeSerializers.COPPER_POT.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return TFGRecipeTypes.COPPER_POT.get();
    }
}
