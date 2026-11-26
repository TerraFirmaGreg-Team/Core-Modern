package su.terrafirmagreg.core.common.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.dries007.tfc.common.capabilities.food.FoodTraits;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.recipes.RecipeHelpers;
import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import su.terrafirmagreg.core.common.blockentity.appliances.BakingDishBlockEntity;
import su.terrafirmagreg.core.common.data.TFGFoodTraits;
import su.terrafirmagreg.core.common.data.TFGRecipeSerializers;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;
import su.terrafirmagreg.core.common.recipe.attachment.RecipeAttachment;

public class BakingDishRecipe extends CookingApplianceRecipe {

    public BakingDishRecipe(
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
        super(id, itemIngredients, fluidIngredients, catalyst, 1.0f, tool, toolDamage, itemOutputs, fluidOutputs, duration, minTemp, List.of());
    }

    public BakingDishRecipe(
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
        super(id, itemIngredients, fluidIngredients, catalyst, 1.0f, tool, toolDamage, itemOutputs, fluidOutputs, duration, minTemp, attachments);
    }

    public BakingDishRecipe(
            ResourceLocation id,
            NonNullList<Ingredient> itemIngredients,
            List<FluidStackIngredient> fluidIngredients,
            Ingredient catalyst,
            float catalystChance,
            Ingredient tool,
            int toolDamage,
            List<ItemStackProvider> itemOutputs,
            List<FluidStack> fluidOutputs,
            int duration,
            float minTemp,
            List<RecipeAttachment> attachments) {
        super(id, itemIngredients, fluidIngredients, catalyst, catalystChance, tool, toolDamage, itemOutputs, fluidOutputs, duration, minTemp, attachments);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return TFGRecipeSerializers.BAKING_DISH.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return TFGRecipeTypes.BAKING_DISH.get();
    }

    public boolean matches(IItemHandler items, IFluidHandler fluids, float temperature) {
        if (temperature < this.minTemp) {
            return false;
        }
        MatchResult itemMatch = matchItemInputs(items, false);
        if (!itemMatch.matches()) {
            return false;
        }
        if (!matchFluidInputs(fluids)) {
            return false;
        }
        return canFitOutputs(items, fluids);
    }

    public MatchResult matchItemInputs(IItemHandler items, boolean allowRotten) {
        if (this.itemIngredients.isEmpty()) {
            return new MatchResult(true, new int[items.getSlots()]);
        }

        int reqCount = this.itemIngredients.size();
        int slotCount = items.getSlots();

        int totalAvailable = 0;
        record ItemUnit(int slot) {
        }
        List<ItemUnit> units = new ArrayList<>();

        for (int s = 0; s < slotCount; s++) {
            ItemStack stack = items.getStackInSlot(s);
            if (stack.isEmpty()) {
                continue;
            }
            if (!allowRotten) {
                final IFood food = FoodCapability.get(stack);
                if (food != null && food.isRotten()) {
                    continue;
                }
            }
            int countInSlot = stack.getCount();
            totalAvailable += countInSlot;
            int toAdd = Math.min(countInSlot, reqCount);
            for (int u = 0; u < toAdd; u++) {
                units.add(new ItemUnit(s));
            }
        }

        if (totalAvailable < reqCount || units.size() < reqCount) {
            return new MatchResult(false, new int[slotCount]);
        }

        int unitCount = units.size();
        List<List<Integer>> adj = new ArrayList<>(reqCount);
        for (int i = 0; i < reqCount; i++) {
            Ingredient ing = this.itemIngredients.get(i);
            List<Integer> edges = new ArrayList<>();
            for (int j = 0; j < unitCount; j++) {
                int slot = units.get(j).slot;
                ItemStack stack = items.getStackInSlot(slot);
                if (ing.test(stack)) {
                    edges.add(j);
                }
            }
            if (edges.isEmpty()) {
                return new MatchResult(false, new int[slotCount]);
            }
            adj.add(edges);
        }

        int[] match = new int[unitCount];
        Arrays.fill(match, -1);

        int matchCount = 0;
        for (int i = 0; i < reqCount; i++) {
            boolean[] visited = new boolean[unitCount];
            if (dfsMatch(i, adj, match, visited)) {
                matchCount++;
            }
        }

        if (matchCount < reqCount) {
            return new MatchResult(false, new int[slotCount]);
        }

        int[] consumed = new int[slotCount];
        for (int j = 0; j < unitCount; j++) {
            if (match[j] != -1) {
                consumed[units.get(j).slot]++;
            }
        }

        return new MatchResult(true, consumed);
    }

    private static boolean dfsMatch(int u, List<List<Integer>> adj, int[] match, boolean[] visited) {
        for (int v : adj.get(u)) {
            if (!visited[v]) {
                visited[v] = true;
                if (match[v] < 0 || dfsMatch(match[v], adj, match, visited)) {
                    match[v] = u;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean matchFluidInputs(IFluidHandler fluids) {
        if (this.fluidIngredients.isEmpty()) {
            return true;
        }
        List<FluidStack> availableFluids = new ArrayList<>();
        for (int i = 0; i < fluids.getTanks(); i++) {
            FluidStack fluid = fluids.getFluidInTank(i);
            if (!fluid.isEmpty()) {
                availableFluids.add(fluid.copy());
            }
        }
        for (FluidStackIngredient reqFluid : this.fluidIngredients) {
            boolean matched = false;
            for (FluidStack avail : availableFluids) {
                if (reqFluid.test(avail) && avail.getAmount() >= reqFluid.amount()) {
                    avail.shrink(reqFluid.amount());
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    public boolean canFitOutputs(IItemHandler items, IFluidHandler fluids) {
        MatchResult itemMatch = matchItemInputs(items, false);
        if (!itemMatch.matches()) {
            return false;
        }

        List<ItemStack> simulatedSlots = new ArrayList<>();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i).copy();
            int consumed = itemMatch.consumedPerSlot()[i];
            if (consumed > 0) {
                stack.shrink(consumed);
            }
            simulatedSlots.add(stack);
        }

        List<ItemStack> nonNullInputs = new ArrayList<>();
        List<ItemStack> foodInputs = new ArrayList<>();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                nonNullInputs.add(stack);
                if (FoodCapability.get(stack) != null) {
                    foodInputs.add(stack);
                }
            }
        }

        RecipeHelpers.setCraftingInput(items, 0, items.getSlots());
        try {
            for (int outIdx = 0; outIdx < this.itemOutputs.size(); outIdx++) {
                ItemStackProvider provider = this.itemOutputs.get(outIdx);
                ItemStack input = ItemStack.EMPTY;
                if (!foodInputs.isEmpty()) {
                    input = foodInputs.get(Math.min(outIdx, foodInputs.size() - 1));
                } else if (!nonNullInputs.isEmpty()) {
                    input = nonNullInputs.get(Math.min(outIdx, nonNullInputs.size() - 1));
                }

                ItemStack out = provider.getStack(input);
                if (out.isEmpty()) {
                    continue;
                }

                ItemStack toInsert = out.copy();
                for (int s = 0; s < simulatedSlots.size() && !toInsert.isEmpty(); s++) {
                    ItemStack inSlot = simulatedSlots.get(s);
                    if (!inSlot.isEmpty()) {
                        if (FoodCapability.get(inSlot) != null && FoodCapability.get(toInsert) != null) {
                            if (FoodCapability.areFoodStacksStackable(inSlot, toInsert, 0.30f)) {
                                inSlot = FoodCapability.mergeItemStacks(inSlot, toInsert);
                                simulatedSlots.set(s, inSlot);
                            }
                        } else if (ItemHandlerHelper.canItemStacksStack(inSlot, toInsert)) {
                            int space = inSlot.getMaxStackSize() - inSlot.getCount();
                            if (space > 0) {
                                int countToAdd = Math.min(space, toInsert.getCount());
                                inSlot.grow(countToAdd);
                                toInsert.shrink(countToAdd);
                            }
                        }
                    }
                }

                for (int s = 0; s < simulatedSlots.size() && !toInsert.isEmpty(); s++) {
                    ItemStack inSlot = simulatedSlots.get(s);
                    if (inSlot.isEmpty()) {
                        int countToPut = Math.min(toInsert.getCount(), toInsert.getMaxStackSize());
                        ItemStack putStack = toInsert.split(countToPut);
                        simulatedSlots.set(s, putStack);
                    }
                }

                if (!toInsert.isEmpty()) {
                    return false;
                }
            }
        } finally {
            RecipeHelpers.clearCraftingInput();
        }

        if (!this.fluidOutputs.isEmpty()) {
            int capacity = fluids.getTanks() > 0 ? fluids.getTankCapacity(0) : 0;
            int totalOutput = 0;
            for (FluidStack outputFluid : this.fluidOutputs) {
                if (!outputFluid.isEmpty()) {
                    totalOutput += outputFluid.getAmount();
                }
            }
            return totalOutput <= capacity;
        }

        return true;
    }

    public void assembleOutputs(IItemHandler items, IFluidHandler fluids, RegistryAccess registryAccess, float containerTemp) {
        MatchResult itemMatch = matchItemInputs(items, false);
        if (!itemMatch.matches()) {
            return;
        }

        List<ItemStack> nonNullInputs = new ArrayList<>();
        List<ItemStack> foodInputs = new ArrayList<>();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                nonNullInputs.add(stack.copy());
                if (FoodCapability.get(stack) != null) {
                    foodInputs.add(stack.copy());
                }
            }
        }

        RecipeHelpers.setCraftingInput(items, 0, items.getSlots());

        List<ItemStack> outputsToInsert = new ArrayList<>();
        try {
            for (int outIdx = 0; outIdx < this.itemOutputs.size(); outIdx++) {
                ItemStackProvider provider = this.itemOutputs.get(outIdx);
                ItemStack input = ItemStack.EMPTY;
                if (!foodInputs.isEmpty()) {
                    input = foodInputs.get(Math.min(outIdx, foodInputs.size() - 1));
                } else if (!nonNullInputs.isEmpty()) {
                    input = nonNullInputs.get(Math.min(outIdx, nonNullInputs.size() - 1));
                }

                ItemStack out = provider.getStack(input);
                if (!out.isEmpty()) {
                    final IFood food = FoodCapability.get(out);
                    if (food != null && !food.isTransientNonDecaying() && food.getRottenDate() != FoodHandler.NEVER_DECAY_DATE && food.getCreationDate() <= 0) {
                        FoodCapability.updateFoodDecayOnCreate(out);
                    }
                    HeatCapability.setTemperature(out, containerTemp);
                    FoodCapability.applyTrait(out, TFGFoodTraits.FIRE_BOX_BAKED);
                    FoodCapability.applyTrait(out, FoodTraits.PRESERVED);
                    outputsToInsert.add(out.copy());
                }
            }
        } finally {
            RecipeHelpers.clearCraftingInput();
        }

        // Consume items
        if (items instanceof IItemHandlerModifiable modifiable) {
            for (int i = 0; i < items.getSlots(); i++) {
                int consumed = itemMatch.consumedPerSlot()[i];
                if (consumed > 0) {
                    ItemStack current = items.getStackInSlot(i);
                    current.shrink(consumed);
                    modifiable.setStackInSlot(i, current.isEmpty() ? ItemStack.EMPTY : current);
                }
            }
        }

        // Handle existing fluid in the tank.
        if (!this.fluidOutputs.isEmpty()) {
            // Clear all existing fluid in the tank if expecting an output fluid.
            for (int i = 0; i < fluids.getTanks(); i++) {
                FluidStack inTank = fluids.getFluidInTank(i);
                if (!inTank.isEmpty()) {
                    fluids.drain(inTank.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                }
            }
        } else {
            // Otherwise only consume the exact fluid amounts required by the recipe.
            for (FluidStackIngredient reqFluid : this.fluidIngredients) {
                int remainingToDrain = reqFluid.amount();
                for (int i = 0; i < fluids.getTanks() && remainingToDrain > 0; i++) {
                    FluidStack inTank = fluids.getFluidInTank(i);
                    if (!inTank.isEmpty() && reqFluid.test(inTank)) {
                        int toDrain = Math.min(inTank.getAmount(), remainingToDrain);
                        FluidStack drained = fluids.drain(new FluidStack(inTank, toDrain), IFluidHandler.FluidAction.EXECUTE);
                        remainingToDrain -= drained.getAmount();
                    }
                }
            }
        }

        // Insert outputs
        if (items instanceof IItemHandlerModifiable modifiable) {
            for (ItemStack toInsert : outputsToInsert) {
                // Merge with existing
                for (int s = 0; s < items.getSlots() && !toInsert.isEmpty(); s++) {
                    ItemStack inSlot = items.getStackInSlot(s);
                    if (!inSlot.isEmpty()) {
                        if (FoodCapability.get(inSlot) != null && FoodCapability.get(toInsert) != null) {
                            if (FoodCapability.areFoodStacksStackable(inSlot, toInsert, 0.30f)) {
                                inSlot = FoodCapability.mergeItemStacks(inSlot, toInsert);
                                modifiable.setStackInSlot(s, inSlot);
                            }
                        } else if (ItemHandlerHelper.canItemStacksStack(inSlot, toInsert)) {
                            int space = inSlot.getMaxStackSize() - inSlot.getCount();
                            if (space > 0) {
                                int countToAdd = Math.min(space, toInsert.getCount());
                                inSlot.grow(countToAdd);
                                toInsert.shrink(countToAdd);
                                modifiable.setStackInSlot(s, inSlot);
                            }
                        }
                    }
                }
                // Put into empty slots
                for (int s = 0; s < items.getSlots() && !toInsert.isEmpty(); s++) {
                    ItemStack inSlot = items.getStackInSlot(s);
                    if (inSlot.isEmpty()) {
                        int countToPut = Math.min(toInsert.getCount(), toInsert.getMaxStackSize());
                        ItemStack putStack = toInsert.split(countToPut);
                        modifiable.setStackInSlot(s, putStack);
                    }
                }
            }
        }

        // Insert fluid output
        for (FluidStack outputFluid : this.fluidOutputs) {
            if (!outputFluid.isEmpty()) {
                fluids.fill(outputFluid.copy(), IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    public static @Nullable BakingDishRecipe getRecipe(ItemStack dishStack, @Nullable Level level) {
        if (dishStack.isEmpty() || level == null) {
            return null;
        }
        CompoundTag beTag = dishStack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
        if (beTag == null) {
            return null;
        }
        ItemStackHandler items = new ItemStackHandler(BakingDishBlockEntity.ITEM_SLOTS);
        FluidTank tank = new FluidTank(BakingDishBlockEntity.FLUID_CAPACITY);

        if (beTag.contains("inventory", Tag.TAG_COMPOUND)) {
            CompoundTag invTag = beTag.getCompound("inventory");
            if (invTag.contains("inventory", Tag.TAG_COMPOUND)) {
                items.deserializeNBT(invTag.getCompound("inventory"));
            } else if (invTag.contains("Items", Tag.TAG_LIST)) {
                items.deserializeNBT(invTag);
            }
            if (invTag.contains("tanks", Tag.TAG_LIST)) {
                ListTag tanksTag = invTag.getList("tanks", Tag.TAG_COMPOUND);
                if (!tanksTag.isEmpty()) {
                    tank.readFromNBT(tanksTag.getCompound(0));
                }
            }
        }
        if (tank.isEmpty()) {
            if (beTag.contains("tanks", Tag.TAG_LIST)) {
                ListTag tanksTag = beTag.getList("tanks", Tag.TAG_COMPOUND);
                if (!tanksTag.isEmpty()) {
                    tank.readFromNBT(tanksTag.getCompound(0));
                }
            } else if (beTag.contains("tank", Tag.TAG_COMPOUND)) {
                tank.readFromNBT(beTag.getCompound("tank"));
            }
        }

        boolean hasItems = false;
        for (int i = 0; i < items.getSlots(); i++) {
            if (!items.getStackInSlot(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }
        if (!hasItems && tank.isEmpty()) {
            return null;
        }

        for (BakingDishRecipe recipe : level.getRecipeManager().getAllRecipesFor(TFGRecipeTypes.BAKING_DISH.get())) {
            if (recipe.matchItemInputs(items, false).matches() && recipe.matchFluidInputs(tank) && recipe.canFitOutputs(items, tank)) {
                return recipe;
            }
        }
        return null;
    }
}
