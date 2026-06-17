package su.terrafirmagreg.core.common.tfgt.machine.trait;

import java.util.*;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.dries007.tfc.common.capabilities.forge.*;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeTypes;

public class TFCAnvilRecipeLogic extends RecipeLogic {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(TFCAnvilRecipeLogic.class, RecipeLogic.MANAGED_FIELD_HOLDER);

    // Maps item output name -> anvil recipe data
    private static final Map<String, TFCAnvilRecipeData> tfcAnvilRecipes = new HashMap<>();

    @Persisted
    private ItemStack currentStack;

    private ItemStack simulatedStack;

    public TFCAnvilRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    @SuppressWarnings("unused")
    public static void RegisterRecipeData(String id, Ingredient input, int minTier, ForgeRule[] rules, boolean applyForgingBonus, ItemStackProvider output) {
        SizedIngredient sizedIngredient;
        if (input instanceof SizedIngredient sized)
            sizedIngredient = sized;
        else
            sizedIngredient = SizedIngredient.create(input, 1);

        // Get output item name from id
        List<String> idParts = Arrays.stream(id.split("/")).toList();
        String itemName = idParts.get(idParts.size() - 1);
        ResourceLocation resourceLocation = ResourceLocation.parse(id);

        tfcAnvilRecipes.put(itemName, new TFCAnvilRecipeData(resourceLocation, sizedIngredient, minTier, rules, applyForgingBonus, output));
    }

    @Override
    protected ActionResult checkRecipe(GTRecipe recipe) {
        var result = super.checkRecipe(recipe);

        List<String> idParts = Arrays.stream(recipe.id.getPath().split("/")).toList();
        TFCAnvilRecipeData recipeData = tfcAnvilRecipes.get(idParts.get(idParts.size() - 1));
        if (result.isSuccess() && recipeData != null) {
            if (!consumeRecipeInputItems(recipeData, true)) {
                return ActionResult.fail(Component.translatable("gtceu.recipe_logic.insufficient_in")
                        .append(": ").append(ItemRecipeCapability.CAP.getName()), ItemRecipeCapability.CAP, IO.IN);
            }

            if (!handleOutput(recipeData, getForgeStepByRecipeType(recipe.getType()), true)) {
                return ActionResult.fail(Component.translatable("gtceu.recipe_logic.insufficient_out")
                        .append(": ").append(ItemRecipeCapability.CAP.getName()), ItemRecipeCapability.CAP, IO.OUT);
            }
        }
        return result;
    }

    @Override
    protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
        List<String> idParts = Arrays.stream(recipe.id.getPath().split("/")).toList();
        TFCAnvilRecipeData currentRecipe = tfcAnvilRecipes.get(idParts.get(idParts.size() - 1));

        if (currentRecipe == null) {
            return super.handleRecipeIO(recipe, io);
        }

        if (io == IO.IN)
            return consumeRecipeInputItems(
                    currentRecipe, false)
                            ? ActionResult.SUCCESS
                            : ActionResult.fail(Component.translatable("gtceu.recipe_logic.insufficient_in")
                                    .append(": ").append(ItemRecipeCapability.CAP.getName()), ItemRecipeCapability.CAP,
                                    io);

        else
            return handleOutput(
                    currentRecipe, getForgeStepByRecipeType(recipe.getType()), false)
                            ? ActionResult.SUCCESS
                            : ActionResult.fail(Component.translatable("gtceu.recipe_logic.insufficient_out")
                                    .append(": ").append(ItemRecipeCapability.CAP.getName()), ItemRecipeCapability.CAP,
                                    io);
    }

    private boolean consumeRecipeInputItems(TFCAnvilRecipeData currentRecipe, boolean simulate) {
        if (currentRecipe.getInput() == null || currentRecipe.getInput().isEmpty())
            return true;

        List<IRecipeHandler<?>> inputHandlers = new ArrayList<>();
        ((IRecipeCapabilityHolder) getMachine()).getCapabilitiesForIO(IO.IN)
                .forEach(handlers -> inputHandlers.addAll(handlers.getCapability(ItemRecipeCapability.CAP)));
        inputHandlers.sort(IRecipeHandler.ENTRY_COMPARATOR);

        SizedIngredient inputToConsume = currentRecipe.getInput();
        ItemStack extracted = null;

        for (IRecipeHandler<?> inputHandler : inputHandlers) {
            if (inputHandler instanceof NotifiableItemStackHandler stackHandler) {
                int amount = inputToConsume.getAmount();

                for (int index = 0; index < stackHandler.getSlots(); index++) {
                    ItemStack iStack = stackHandler.getStackInSlot(index);
                    if (inputToConsume.getInner().test(iStack)) {
                        ItemStack result = stackHandler.extractItemInternal(index, amount, simulate);
                        if (result.getCount() < amount) {
                            amount = amount - result.getCount();
                            extracted = result;
                        } else {
                            extracted = result;
                            break;
                        }
                    }
                }
            } else {
                TFGCore.LOGGER.warn(
                        "Unexpected input capability proxy: Expected NotifiableItemStackHandler, actual: {}",
                        inputHandler.getClass());
            }
        }

        if (extracted == null)
            return false;

        if (simulate)
            simulatedStack = extracted;
        else
            currentStack = extracted;

        return true;
    }

    private boolean handleOutput(TFCAnvilRecipeData currentRecipe, ForgeStep forgeStep, boolean simulate) {
        if (currentRecipe.getOutputIsp() == null || forgeStep == null)
            return true;

        ItemStack inputStack = simulate ? simulatedStack : currentStack;

        Forging forge;
        // TODO: Figure out why this breaks on world load
        try {
            forge = ForgingCapability.get(inputStack);
        } catch (Exception e) {
            forge = null;
        }
        if (forge == null)
            return false;

        // Create a simulated anvil inventory and anvil recipe to use TFC's forging logic
        SimulatedAnvilInventory inventory = new SimulatedAnvilInventory(inputStack);
        AnvilRecipe anvilRecipe = new AnvilRecipe(currentRecipe.getId(), currentRecipe.getInput(), currentRecipe.getMinTier(), currentRecipe.getRules(), currentRecipe.isApplyForgingBonus(),
                currentRecipe.getOutputIsp());

        // Check machine tier is valid for this recipe
        if (!anvilRecipe.isCorrectTier(inventory.getTier()))
            return false;

        // Prevent overworking
        int newWork = forge.getWork() + forgeStep.step();
        if (newWork < 0 || newWork > ForgeStep.LIMIT)
            return false;

        // Work item
        forge.addStep(forgeStep);

        // Check if recipe is complete
        ItemStack outputStack = null;
        if (anvilRecipe.checkComplete(inventory)) {
            outputStack = currentRecipe.getOutputIsp().getStack(inputStack);

            // Apply forging bonus
            // TODO: Verify this works and create a way to select anvil recipes (e.g steel_ingot -> all tools)
            if (currentRecipe.isApplyForgingBonus()) {
                float ratio = (float) forge.getSteps().total() / ForgeRule.calculateOptimalStepsToTarget(anvilRecipe.computeTarget(inventory), currentRecipe.getRules());
                ForgingBonus bonus = ForgingBonus.byRatio(ratio);
                ForgingBonus.set(outputStack, bonus);
            }
        }

        // Output item
        List<IRecipeHandler<?>> outputHandlers = new ArrayList<>();
        ((IRecipeCapabilityHolder) getMachine()).getCapabilitiesForIO(IO.OUT)
                .forEach(handlers -> outputHandlers.addAll(handlers.getCapability(ItemRecipeCapability.CAP)));
        outputHandlers.sort(IRecipeHandler.ENTRY_COMPARATOR);

        for (IRecipeHandler<?> outputHandler : outputHandlers) {
            if (outputHandler instanceof NotifiableItemStackHandler stackHandler) {
                for (int index = 0; index < stackHandler.getSlots(); index++) {
                    if (outputStack != null)
                        stackHandler.insertItemInternal(index, outputStack, simulate);
                    else
                        stackHandler.insertItemInternal(index, inputStack, simulate);
                }
            } else {
                TFGCore.LOGGER.warn(
                        "Unexpected output capability proxy: Expected NotifiableItemStackHandler, actual: {}",
                        outputHandler.getClass());
            }
        }

        return true;
    }

    private ForgeStep getForgeStepByRecipeType(GTRecipeType recipeType) {
        if (recipeType.equals(TFGTRecipeTypes.ANVIL_LIGHT_HIT_RECIPES))
            return ForgeStep.HIT_LIGHT;
        if (recipeType.equals(TFGTRecipeTypes.ANVIL_MEDIUM_HIT_RECIPES))
            return ForgeStep.HIT_MEDIUM;
        if (recipeType.equals(TFGTRecipeTypes.ANVIL_HARD_HIT_RECIPES))
            return ForgeStep.HIT_HARD;
        if (recipeType.equals(TFGTRecipeTypes.ANVIL_DRAW_RECIPES))
            return ForgeStep.DRAW;
        if (recipeType.equals(TFGTRecipeTypes.ANVIL_PUNCH_RECIPES))
            return ForgeStep.PUNCH;
        if (recipeType.equals(TFGTRecipeTypes.ANVIL_BEND_RECIPES))
            return ForgeStep.BEND;
        if (recipeType.equals(TFGTRecipeTypes.ANVIL_UPSET_RECIPES))
            return ForgeStep.UPSET;
        if (recipeType.equals(TFGTRecipeTypes.ANVIL_SHRINK_RECIPES))
            return ForgeStep.SHRINK;

        return null;
    }

    class SimulatedAnvilInventory implements AnvilRecipe.Inventory {

        private final ItemStack itemStack;

        public SimulatedAnvilInventory(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        @Override
        public @NotNull ItemStack getItem() {
            return itemStack;
        }

        @Override
        public int getTier() {
            // TODO: Tier based on voltage
            return 0;
        }

        @Override
        public long getSeed() {
            return getMachine().self().getLevel() instanceof ServerLevel level ? level.getSeed() : 0;
        }
    }
}
