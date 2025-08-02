package su.terrafirmagreg.core.common.data.tfgt.machine.trait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.recipes.RecipeHelpers;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.TFGCore;

import static com.gregtechceu.gtceu.api.recipe.RecipeHelper.addToRecipeHandlerMap;

public class ISPOutputRecipeLogic extends RecipeLogic {
    
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ISPOutputRecipeLogic.class, RecipeLogic.MANAGED_FIELD_HOLDER);
    
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // There is probably a better way to expose the TFC recipe data
    record TFCRecipeData(List<SizedIngredient> inputs, ItemStackProvider outputISP, List<ItemStack> secondaryOutputs) {}
    private static final Map<String, TFCRecipeData> TFCRecipes = new HashMap<>();
    public static void RegisterRecipeData(String id, List<Ingredient> inputs, ItemStackProvider output, List<ItemStack> secondaryOutputs) {
        List<SizedIngredient> sizedIngredients = new ArrayList<>();

        for (Ingredient in : inputs) {
            if (in instanceof SizedIngredient sized) sizedIngredients.add(sized);
            else sizedIngredients.add(SizedIngredient.create(in, 1));
        }

        TFCRecipes.put(id, new TFCRecipeData(sizedIngredients, output, secondaryOutputs));
    }

    @Persisted
    List<ItemStack> currentItems = new ArrayList<>();

    List<ItemStack> currentItemsSimulated = new ArrayList<>();

    static class SimulatedCraftingContainer implements CraftingContainer {

        private final List<ItemStack> _items = new ArrayList<>();
        public SimulatedCraftingContainer(List<ItemStack> items) {
            // TFC expects each ItemStack to only have one item
            for (ItemStack itemStack : items) {
                for (int i = 0; i < itemStack.getCount(); i++) {
                    _items.add(itemStack.copyWithCount(1));
                }
            }
        }
        @Override
        public int getContainerSize() {
            return _items.size();
        }

        @Override
        public boolean isEmpty() {
            return _items.isEmpty();
        }

        @Override
        public @NotNull ItemStack getItem(int pSlot) {
            return pSlot >= _items.size() ? ItemStack.EMPTY : _items.get(pSlot);
        }

        @Override
        public @NotNull ItemStack removeItem(int pSlot, int pAmount) {
            return pSlot >= _items.size() ? ItemStack.EMPTY : _items.get(pSlot);
        }

        @Override
        public @NotNull ItemStack removeItemNoUpdate(int pSlot) {return ItemStack.EMPTY;}

        @Override
        public void setItem(int pSlot, @NotNull ItemStack pStack) {}

        @Override
        public void setChanged() {}

        @Override
        public boolean stillValid(@NotNull Player pPlayer) {return false;}

        @Override
        public void clearContent() {}

        @Override
        public void fillStackedContents(@NotNull StackedContents pContents) {}

        @Override
        public int getWidth() { return 1;
        }

        @Override
        public int getHeight() {return 1;}

        @Override
        public @NotNull List<ItemStack> getItems() {return _items;}

    }


    public ISPOutputRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    private IRecipeCapabilityHolder getCapHolder() {
        return (IRecipeCapabilityHolder) getMachine();
    }

    @Override
    protected ActionResult checkRecipe(GTRecipe recipe) {
        var result = super.checkRecipe(recipe);

        TFCRecipeData recipeData = TFCRecipes.get(recipe.id.getPath());
        if (result.isSuccess() && recipeData != null) {
            if (!consumeRecipeInputItems(recipeData, true)) {
                return ActionResult.fail(Component.translatable("gtceu.recipe_logic.insufficient_in")
                        .append(": ").append(ItemRecipeCapability.CAP.getName()));
            }

            if (!handleOutput(recipeData, true)) {
                return ActionResult.fail(Component.translatable("gtceu.recipe_logic.insufficient_out")
                        .append(": ").append(ItemRecipeCapability.CAP.getName()));
            }
        }
        return result;
    }

    // Custom recipe IO logic
    @Override
    protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
        TFCRecipeData currentRecipe = TFCRecipes.get(recipe.id.getPath());

        if (currentRecipe == null) return super.handleRecipeIO(recipe, io);


        // Handle fluid IO
        var fluids = (io == IO.IN) ? recipe.getInputContents(FluidRecipeCapability.CAP): recipe.getOutputContents(FluidRecipeCapability.CAP);
        RecipeHelper.handleRecipe(getCapHolder(), recipe, io, Map.of(FluidRecipeCapability.CAP, fluids), chanceCaches, false, false);

        if (io == IO.IN) return consumeRecipeInputItems(currentRecipe, false) ? ActionResult.SUCCESS :
                ActionResult.fail(Component.translatable("gtceu.recipe_logic.insufficient_in")
                .append(": ").append(ItemRecipeCapability.CAP.getName()));

        else return handleOutput(currentRecipe, false) ? ActionResult.SUCCESS :
                ActionResult.fail(Component.translatable("gtceu.recipe_logic.insufficient_out")
                .append(": ").append(ItemRecipeCapability.CAP.getName()));
    }

    private boolean consumeRecipeInputItems(TFCRecipeData currentRecipe, boolean simulate) {

        if (currentRecipe.inputs.isEmpty()) return true;

        List<IRecipeHandler<?>> inputHandlers = new ArrayList<>();
        getCapHolder().getCapabilitiesForIO(IO.IN).forEach(v -> inputHandlers.addAll(v.getCapability(ItemRecipeCapability.CAP)));
        inputHandlers.sort(IRecipeHandler.ENTRY_COMPARATOR);

        List<SizedIngredient> inputsToConsume = new ArrayList<>(currentRecipe.inputs);
        List<ItemStack> extracted = new ArrayList<>();
        
        for (IRecipeHandler<?> inputHandler : inputHandlers) {
            if (inputHandler instanceof NotifiableItemStackHandler stackHandler) { 
                var iter = inputsToConsume.iterator();
                while (iter.hasNext()) {
                    var sized = iter.next();
                    var amount = sized.getAmount();

                    for (int index = 0; index < stackHandler.getSlots(); index++) {
                        ItemStack iStack = stackHandler.getStackInSlot(index);
                        IFood food = FoodCapability.get(iStack);
                        if (sized.getInner().test(iStack) && (food == null || !food.isRotten())) {
                            ItemStack result = stackHandler.extractItemInternal(index, amount, simulate);
                            if (result.getCount() < amount) {
                                amount = amount - result.getCount();
                                extracted.add(result);
                            } else {
                                iter.remove();
                                extracted.add(result);
                                break;
                            }
                        }
                    }
                }
            } else {
                TFGCore.LOGGER.warn("Unexpected input capability proxy: Expected NotifiableItemStackHandler, actual: {}", inputHandler.getClass());
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

        List<IRecipeHandler<?>> outputHandlers = new ArrayList<>();
        getCapHolder().getCapabilitiesForIO(IO.OUT).forEach(v -> outputHandlers.addAll(v.getCapability(ItemRecipeCapability.CAP)));
        outputHandlers.sort(IRecipeHandler.ENTRY_COMPARATOR);

        RecipeHelpers.setCraftingInput(new SimulatedCraftingContainer(simulate ? currentItemsSimulated : currentItems));
        var ispResult = currentRecipe.outputISP.getStack(simulate ? currentItemsSimulated.get(0) : currentItems.get(0));
        List<ItemStack> allOutputs = new ArrayList<>(currentRecipe.secondaryOutputs);
        allOutputs.add(0, ispResult);
        // Logic to allow food items with similar creation dates to stack properly
        for (IRecipeHandler<?> outputHandler : outputHandlers) {
            if (outputHandler instanceof NotifiableItemStackHandler stackHandler) {
                for (int index = 0; index < stackHandler.getSlots(); index++) {
                    var iter = allOutputs.iterator();
                    while (iter.hasNext()) {
                        var itemStack = iter.next();
                        if (!stackHandler.isItemValid(index, itemStack)) continue;
                        ItemStack inSlot = stackHandler.getStackInSlot(index);
                        if (inSlot.isEmpty()) {
                            itemStack = stackHandler.insertItemInternal(index, itemStack, simulate);
                        } else if (FoodCapability.has(itemStack) && FoodCapability.has(inSlot) && FoodCapability.areStacksStackableExceptCreationDate(itemStack, inSlot)) {
                            var date1 = FoodCapability.get(inSlot).getCreationDate();
                            var date2 = FoodCapability.get(itemStack).getCreationDate();
                            if (!simulate || (FoodCapability.getRoundedCreationDate(date1) == FoodCapability.getRoundedCreationDate(date2))) {
                                FoodCapability.get(itemStack).setCreationDate(date1);
                                itemStack = stackHandler.insertItemInternal(index, itemStack, simulate);
                            }
                        }
                        if (itemStack.isEmpty()) iter.remove();
                    }
                    if (allOutputs.isEmpty()) return true;
                }
            } else {
                TFGCore.LOGGER.warn("Unexpected output capability proxy: Expected NotifiableItemStackHandler, actual: {}", outputHandler.getClass());
            }
        }
        return false;
    }
}
