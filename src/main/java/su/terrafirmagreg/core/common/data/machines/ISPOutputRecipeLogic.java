package su.terrafirmagreg.core.common.data.machines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import su.terrafirmagreg.core.TFGCore;

public class ISPOutputRecipeLogic extends RecipeLogic {
    
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ISPOutputRecipeLogic.class, RecipeLogic.MANAGED_FIELD_HOLDER);
    
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // There is probably a better way to expose the TFC recipe data
    record TFCRecipeData(List<Ingredient> inputs, ItemStackProvider outputISP) {}
    private static Map<String, TFCRecipeData> TFCRecipes = new HashMap<>();
    public static void RegisterRecipeData(String id, List<Ingredient> inputs, ItemStackProvider output) {
        TFCRecipes.put(id, new TFCRecipeData(inputs, output));
    }

    @Persisted
    List<ItemStack> currentItems = new ArrayList<>();

    List<ItemStack> currentItemsSimulated = new ArrayList<>();


    public ISPOutputRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    private IRecipeCapabilityHolder getCapHolder() {
        return (IRecipeCapabilityHolder) getMachine();
    }

    // Custom recipe match function
    private GTRecipe.ActionResult matchRecipe(GTRecipe recipe, IRecipeCapabilityHolder holder) {
        ActionResult result = recipe.matchRecipe(holder);
        
        TFCRecipeData recipeData = TFCRecipes.get(recipe.id.getPath());
        if (result.isSuccess() && recipeData != null) {
            if (!consumeRecipeInputItems(recipeData, true)) {
                result = ActionResult.fail(() -> Component.translatable("gtceu.recipe_logic.insufficient_in").append(": ").append(ItemRecipeCapability.CAP.getName()), 0.0F);
            }

            if (currentItemsSimulated.isEmpty()) return ActionResult.FAIL_NO_REASON;

            var itemStack = recipeData.outputISP.getStack(currentItemsSimulated.get(0));
            result = recipe.matchRecipeContents(IO.OUT, getCapHolder(), 
            Map.of(ItemRecipeCapability.CAP, List.of(new Content(Ingredient.of(itemStack), ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)))
            , false);
        }
        return result;
    }

    // Overrides to use custom match function matchRecipe instead of lastRecipe.matchRecipe
    //#region Recipe search overrides

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
            if (lastOriginRecipe != null) {
                var modified = machine.fullModifyRecipe(lastOriginRecipe.copy());
                if (modified == null)
                    markLastRecipeDirty();
                else
                    lastRecipe = modified;
            } else {
                markLastRecipeDirty();
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

    //#endregion

    // Custom recipe IO logic
    @Override
    protected boolean handleRecipeIO(GTRecipe recipe, IO io) {
        TFCRecipeData currentRecipe = TFCRecipes.get(recipe.id.getPath());

        if (currentRecipe == null) return super.handleRecipeIO(recipe, io);

        if (currentItems.isEmpty()) return false;

        // Handle fluid IO
        var fluids = (io == IO.IN) ? recipe.getInputContents(FluidRecipeCapability.CAP): recipe.getOutputContents(FluidRecipeCapability.CAP);
        recipe.handleRecipe(io, getCapHolder(), false, Map.of(FluidRecipeCapability.CAP, fluids), chanceCaches);

        if (io == IO.IN) return consumeRecipeInputItems(currentRecipe, false);
        else {
            var itemStack = currentRecipe.outputISP.getStack(currentItems.get(0));
            return recipe.handleRecipe(IO.OUT, getCapHolder(), false,
            Map.of(ItemRecipeCapability.CAP, List.of(new Content(Ingredient.of(itemStack), ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)))
            , chanceCaches);
        }
    }

    private boolean consumeRecipeInputItems(TFCRecipeData currentRecipe, boolean simulate) {
        List<IRecipeHandler<?>> inputHandlers = getCapHolder().getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP);
        if (inputHandlers == null) return false;
        inputHandlers.sort(IRecipeHandler.ENTRY_COMPARATOR);

        List<Ingredient> inputsToConsume = new ArrayList<>(currentRecipe.inputs);
        List<ItemStack> extracted = new ArrayList<>();

        var success = false;
        for (IRecipeHandler<?> inputHandler : inputHandlers) {
            if (inputHandler instanceof NotifiableItemStackHandler stackHandler) { 
                for (int index = 0; index < stackHandler.getSlots(); index++) {
                    if (inputsToConsume.isEmpty()) break;
                    ItemStack iStack = stackHandler.getStackInSlot(index);
                    for (Ingredient ingredient : inputsToConsume) {
                        if (ingredient.test(iStack)) {
                            extracted.add(stackHandler.extractItemInternal(index, 1, simulate));
                            inputsToConsume.remove(ingredient);
                            if (inputsToConsume.isEmpty()) {
                                success = true;
                                break;
                            }
                        }
                    }
                }

            } else {
                TFGCore.LOGGER.warn("Unexpected input capability proxy: Expected NotifiableItemStackHandler, actual: " + inputHandler.getClass());
            }
        }

        if (simulate) currentItemsSimulated = extracted;
        else currentItems = extracted;

        return success;
    }

}
