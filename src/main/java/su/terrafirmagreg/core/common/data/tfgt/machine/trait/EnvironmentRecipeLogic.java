package su.terrafirmagreg.core.common.data.tfgt.machine.trait;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

/**
 * RecipeLogic that bypasses GT's recipe DB indexing.
 * <p>
 * GT's DB can't index energy-only recipes (no item/fluid inputs to key on).
 * This iterates all recipes in the type instead — fine for types with 1-2 recipes.
 */
public class EnvironmentRecipeLogic extends RecipeLogic {

    public EnvironmentRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Override
    public @NotNull Iterator<GTRecipe> searchRecipe() {
        return machine.getRecipeType().getCategories().stream()
                .flatMap(cat -> machine.getRecipeType().getRecipesInCategory(cat).stream())
                .iterator();
    }
}
