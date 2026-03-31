package su.terrafirmagreg.core.common.data.tfgt.machine.trait;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import lombok.Getter;
import lombok.Setter;

/**
 * RecipeLogic for environment machines.
 * Can handle recipes with no inputs and recipes with fractional fluid consumption.
 */
public class EnvironmentRecipeLogic extends RecipeLogic {

    @Getter
    @Setter
    private double fluidCostPerTick = 0.0;

    private double accumulator = 0.0;

    /** Cached recipe variants, invalidated when recipe identity changes */
    @Nullable
    private GTRecipe cachedSource;
    @Nullable
    private GTRecipe recipeNoFluids;
    @Nullable
    private GTRecipe recipeDrainFluids;
    /** Owned copy of the fluid ingredient in recipeDrainFluids — safe to mutate */
    @Nullable
    private FluidIngredient drainIngredient;

    public EnvironmentRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    // ========================= Recipe Search =========================

    @Override
    public @NotNull Iterator<GTRecipe> searchRecipe() {
        return machine.getRecipeType().getCategories().stream()
                .flatMap(cat -> machine.getRecipeType().getRecipesInCategory(cat).stream())
                .iterator();
    }

    // ======================== Recipe Matching ========================

    /** Reduce fluid tickInput amounts to 1 mB so matching only checks fluid type, not the full recipe amount. */
    @Override
    protected ActionResult matchRecipe(GTRecipe recipe) {
        if (!recipe.getTickInputContents(FluidRecipeCapability.CAP).isEmpty()) {
            return super.matchRecipe(minimizeFluidTickInputs(recipe));
        }
        return super.matchRecipe(recipe);
    }

    // ==================== Per-Tick Fluid Handling ====================

    @Override
    public ActionResult handleTickRecipe(GTRecipe recipe) {
        if (fluidCostPerTick <= 0) {
            // Energy-only machines (eg Space Heater)
            return super.handleTickRecipe(recipe);
        }

        ensureCached(recipe);
        accumulator += fluidCostPerTick;
        int toDrain = (int) accumulator;

        if (toDrain >= 1 && drainIngredient != null) {
            drainIngredient.setAmount(toDrain);
            var result = super.handleTickRecipe(recipeDrainFluids);
            if (result.isSuccess()) {
                accumulator -= toDrain;
            }
            return result;
        } else {
            return super.handleTickRecipe(recipeNoFluids);
        }
    }

    // ==================== Helpers ====================

    private void ensureCached(GTRecipe recipe) {
        if (cachedSource == recipe)
            return;
        cachedSource = recipe;

        recipeNoFluids = recipe.copy();
        recipeNoFluids.tickInputs.remove(FluidRecipeCapability.CAP);

        // Deep-copy tickInputs so mutations to drainIngredient don't alias back to recipe
        recipeDrainFluids = recipe.copy();
        List<Content> fluids = recipeDrainFluids.getTickInputContents(FluidRecipeCapability.CAP);
        if (!fluids.isEmpty() && fluids.get(0).getContent() instanceof FluidIngredient ingredient) {
            drainIngredient = ingredient.copy();
            List<Content> mutable = new java.util.ArrayList<>(fluids);
            mutable.set(0, new Content(drainIngredient, fluids.get(0).chance, fluids.get(0).maxChance, fluids.get(0).tierChanceBoost));
            recipeDrainFluids.tickInputs.put(FluidRecipeCapability.CAP, mutable);
        } else {
            drainIngredient = null;
        }
    }

    /** Set fluid amounts to 1 mB for recipe matching */
    private static GTRecipe minimizeFluidTickInputs(GTRecipe recipe) {
        GTRecipe copy = recipe.copy();
        List<Content> fluidInputs = copy.getTickInputContents(FluidRecipeCapability.CAP);
        if (!fluidInputs.isEmpty()) {
            List<Content> minimized = new java.util.ArrayList<>();
            for (Content content : fluidInputs) {
                if (content.getContent() instanceof FluidIngredient ingredient) {
                    FluidIngredient min = ingredient.copy();
                    min.setAmount(1);
                    minimized.add(new Content(min, content.chance, content.maxChance, content.tierChanceBoost));
                }
            }
            copy.tickInputs.put(FluidRecipeCapability.CAP, minimized);
        }
        return copy;
    }
}
