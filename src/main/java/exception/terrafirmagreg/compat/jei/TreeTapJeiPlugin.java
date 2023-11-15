package exception.terrafirmagreg.compat.jei;

import exception.terrafirmagreg.TerraFirmaGreg;
import exception.terrafirmagreg.objects.recipe.TapExtractRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

@JeiPlugin
public class TreeTapJeiPlugin implements IModPlugin {
    public static final RecipeType<TapExtractRecipe> TAP_EXTRACT_TYPE = RecipeType.create(TerraFirmaGreg.MOD_ID, "tap_extract", TapExtractRecipe.class);
    private static final ResourceLocation pluginId = new ResourceLocation(TerraFirmaGreg.MOD_ID, "treetap");

    public TreeTapJeiPlugin() {
    }

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return pluginId;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
//        registration.addRecipeCatalyst(new ItemStack(TreeTap.TAP.get()), TAP_EXTRACT_TYPE);
        Arrays.stream(Ingredient.of(TerraFirmaGreg.TAPS).getItems()).forEach(itemStack -> {
            registration.addRecipeCatalyst(itemStack, TAP_EXTRACT_TYPE);
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

        registration.addRecipeCategories(new TapExtractRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<TapExtractRecipe> bottlerRecipeMap = recipeManager.getAllRecipesFor(TerraFirmaGreg.TAP_RECIPE_TYPE.get());
        registration.addRecipes(TAP_EXTRACT_TYPE, bottlerRecipeMap);
    }
}
