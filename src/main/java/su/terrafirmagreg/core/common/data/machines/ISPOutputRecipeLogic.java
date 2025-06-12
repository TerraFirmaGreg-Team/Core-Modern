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
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
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
        ActionResult result = recipe.matchRecipeContents(IO.IN, holder, recipe.inputs, false);
        
        TFCRecipeData recipeData = TFCRecipes.get(recipe.id.getPath());
        if (result.isSuccess() && recipeData != null) {
            if (!consumeRecipeInputItems(recipeData, true)) {
                result = ActionResult.fail(() -> Component.translatable("gtceu.recipe_logic.insufficient_in").append(": ").append(ItemRecipeCapability.CAP.getName()), 0.0F);
            }

            if (!handleOutput(recipeData, true)) {
                result = ActionResult.fail(() -> Component.translatable("gtceu.recipe_logic.insufficient_out").append(": ").append(ItemRecipeCapability.CAP.getName()), 0.0F);
            }
        }

        //TFGCore.LOGGER.info("Testing: {} - {} {}", recipe.id.toString(), result.isSuccess(), result.isSuccess() ? "" : result.reason().get().getString());
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


        // Handle fluid IO
        var fluids = (io == IO.IN) ? recipe.getInputContents(FluidRecipeCapability.CAP): recipe.getOutputContents(FluidRecipeCapability.CAP);
        recipe.handleRecipe(io, getCapHolder(), false, Map.of(FluidRecipeCapability.CAP, fluids), chanceCaches);

        if (io == IO.IN) return consumeRecipeInputItems(currentRecipe, false);
        else return handleOutput(currentRecipe, false);
    }

    private boolean consumeRecipeInputItems(TFCRecipeData currentRecipe, boolean simulate) {

        if (currentRecipe.inputs.isEmpty()) return true;

        List<IRecipeHandler<?>> inputHandlers = getCapHolder().getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP);
        if (inputHandlers == null) return false;
        inputHandlers.sort(IRecipeHandler.ENTRY_COMPARATOR);

        List<Ingredient> inputsToConsume = new ArrayList<>(currentRecipe.inputs);
        List<ItemStack> extracted = new ArrayList<>();

        for (IRecipeHandler<?> inputHandler : inputHandlers) {
            if (inputHandler instanceof NotifiableItemStackHandler stackHandler) { 
                var iter = inputsToConsume.iterator();
                while (iter.hasNext()) {
                    var ingredient = iter.next();
                    for (int index = 0; index < stackHandler.getSlots(); index++) {
                        ItemStack iStack = stackHandler.getStackInSlot(index);
                        if (ingredient instanceof SizedIngredient sized) {
                            if (sized.getInner().test(iStack)) {
                                var amount = sized.getAmount();
                                ItemStack result = stackHandler.extractItemInternal(index, amount, simulate);
                                if (result.getCount() < amount) {
                                    sized.setAmount(amount - result.getCount());
                                    extracted.add(result);
                                } else {
                                    iter.remove();
                                    extracted.add(result);
                                    break;
                                }
                                extracted.add(result);
                            }
                        } else {
                            if (ingredient.test(iStack)) {
                                extracted.add(stackHandler.extractItemInternal(index, 1, simulate));
                                iter.remove();
                                break;
                            }
                        }
                    }
                }
            } else {
                TFGCore.LOGGER.warn("Unexpected input capability proxy: Expected NotifiableItemStackHandler, actual: " + inputHandler.getClass());
            }
        }
        if (!inputsToConsume.isEmpty()) return false;
        if (simulate) currentItemsSimulated = extracted;
        else currentItems = extracted;

        return true;
    }

    private boolean handleOutput(TFCRecipeData currentRecipe, boolean simulate) {

        if (currentRecipe.outputISP == null) return true;

        if ((simulate && currentItemsSimulated.isEmpty()) || (!simulate && currentItems.isEmpty())) return false;

        List<IRecipeHandler<?>> outputHandlers = getCapHolder().getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP);
        if (outputHandlers == null) return false;
        outputHandlers.sort(IRecipeHandler.ENTRY_COMPARATOR);

        var itemStack = currentRecipe.outputISP.getStack(simulate ? currentItemsSimulated.get(0) : currentItems.get(0));
        
        //TFGCore.LOGGER.info("Handling: {}", simulate);
        //TFGCore.LOGGER.info("Testing stack: {} tick: ({}) {}", itemStack.toString(), 
        //FoodCapability.has(itemStack) ? FoodCapability.getRoundedCreationDate(FoodCapability.get(itemStack).getCreationDate()) : -1, 
        //FoodCapability.has(itemStack) ? FoodCapability.get(itemStack).getCreationDate() : -1);

        // Logic to allow food items with similar creation dates to stack properly
        for (IRecipeHandler<?> outputHandler : outputHandlers) {
            if (outputHandler instanceof NotifiableItemStackHandler stackHandler) {
                for (int index = 0; index < stackHandler.getSlots(); index++) {
                    if (!stackHandler.isItemValid(index, itemStack)) continue;
                    ItemStack inSlot = stackHandler.getStackInSlot(index);
                    //TFGCore.LOGGER.info("Slot {} stack: {} tick: ({}) {}", index, inSlot.toString(), 
                    //FoodCapability.has(inSlot) ? FoodCapability.getRoundedCreationDate(FoodCapability.get(inSlot).getCreationDate()) : -1, 
                    //FoodCapability.has(inSlot) ? FoodCapability.get(inSlot).getCreationDate() : -1);
                    if (inSlot.isEmpty()) {
                        itemStack = stackHandler.insertItemInternal(index, itemStack, simulate);
                    } else if (FoodCapability.has(itemStack) && FoodCapability.has(inSlot) && FoodCapability.areStacksStackableExceptCreationDate(itemStack, inSlot)) {
                        //TFGCore.LOGGER.info("Stackable");
                        var date1 = FoodCapability.get(inSlot).getCreationDate();
                        var date2 = FoodCapability.get(itemStack).getCreationDate();
                        if (FoodCapability.getRoundedCreationDate(date1) == FoodCapability.getRoundedCreationDate(date2)) {
                            //TFGCore.LOGGER.info("Merging");
                            FoodCapability.get(itemStack).setCreationDate(date1);
                            itemStack = stackHandler.insertItemInternal(index, itemStack, simulate);
                        }
                    }
                    if (itemStack.isEmpty()) return true;
                }
            } else {
                TFGCore.LOGGER.warn("Unexpected output capability proxy: Expected NotifiableItemStackHandler, actual: " + outputHandler.getClass());
            }
        }
        return false;
    }
}
