package su.terrafirmagreg.core.common.data.machines;

import java.util.List;
import java.util.Map;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.world.item.crafting.Ingredient;

public class ISPOutputRecipeLogic extends RecipeLogic {

    // There is probably a better way to expose the TFC recipe data
    record TFCRecipeData(Map<String, Object> inputs, ItemStackProvider output) {}
    private static Map<String, TFCRecipeData> TFCRecipes;

    public static void RegisterRecipeData(String id, List<Ingredient> inputs, ItemStackProvider output) {
        GTCEu.LOGGER.debug("Registering data: " + id);
        for (Ingredient ingredient : inputs) {
            GTCEu.LOGGER.debug("Ingredient: " + ingredient.toString());
        }
    }

    public ISPOutputRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    private IRecipeCapabilityHolder getCapHolder() {
        return (IRecipeCapabilityHolder) getMachine();
    }

    // Custom recipe match function
    private GTRecipe.ActionResult matchRecipe(GTRecipe recipe, IRecipeCapabilityHolder holder) {
        var recipeInputsGeneral = recipe.inputs;
        var recipeOutputsGeneral = recipe.outputs;
        return recipe.matchRecipe(holder);
    }

    // Overrides to use custom match function matchRecipe instead of lastRecipe.matchRecipe
    @Override
    public void findAndHandleRecipe() {
        lastFailedMatches = null;
        if (!recipeDirty && lastRecipe != null && matchRecipe(lastRecipe, this.machine).isSuccess()
                && lastRecipe.matchTickRecipe(this.machine).isSuccess()
                && lastRecipe.checkConditions(this).isSuccess()) {
            var recipe = lastRecipe;
            lastRecipe = null;
            lastOriginRecipe = null;
            setupRecipe(recipe);
        } else {
            lastRecipe = null;
            lastOriginRecipe = null;
            handleSearchingRecipes(searchRecipe());
        }
    }

    @Override
    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            lastRecipe.postWorking(machine);
            handleRecipeIO(lastRecipe, IO.OUT);
            if (machine.alwaysTryModifyRecipe()) {
                if (lastOriginRecipe != null) {
                    var modified = machine.fullModifyRecipe(lastOriginRecipe.copy());
                    if (modified == null)
                        markLastRecipeDirty();
                    else
                        lastRecipe = modified;
                } else {
                    markLastRecipeDirty();
                }
            }
            if (!recipeDirty && !suspendAfterFinish && matchRecipe(lastRecipe, this.machine).isSuccess()
                    && lastRecipe.matchTickRecipe(this.machine).isSuccess()
                    && lastRecipe.checkConditions(this).isSuccess()) {
                setupRecipe(lastRecipe);
                if (isActive)
                    consecutiveRecipes++;
            } else {
                if (suspendAfterFinish) {
                    setStatus(Status.SUSPEND);
                    suspendAfterFinish = false;
                } else {
                    setStatus(Status.IDLE);
                }
                consecutiveRecipes = 0;
                progress = 0;
                duration = 0;
                isActive = false;
            }
        }

    }

    @Override
    public boolean checkMatchedRecipeAvailable(GTRecipe match) {
        var matchCopy = match.copy();
        var modified = machine.fullModifyRecipe(matchCopy);
        if (modified != null) {
            if (modified.checkConditions(this).isSuccess() && matchRecipe(modified, machine).isSuccess()
                    && modified.matchTickRecipe(machine).isSuccess()) {
                setupRecipe(modified);
            }
            if (lastRecipe != null && getStatus() == Status.WORKING) {
                lastOriginRecipe = match;
                lastFailedMatches = null;
                return true;
            }
        }
        return false;
    }

    // Custom recipe IO logic
    @Override
    protected boolean handleRecipeIO(GTRecipe recipe, IO io) {
        return super.handleRecipeIO(recipe, io);
    }

    private boolean runItemIO() {
        return false;
    }

}
