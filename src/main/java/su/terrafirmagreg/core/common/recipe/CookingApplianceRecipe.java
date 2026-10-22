package su.terrafirmagreg.core.common.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.recipes.ISimpleRecipe;
import net.dries007.tfc.common.recipes.RecipeHelpers;
import net.dries007.tfc.common.recipes.RecipeSerializerImpl;
import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.util.JsonHelpers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;

import su.terrafirmagreg.core.common.blockentity.CookingApplianceBlockEntity;

public abstract class CookingApplianceRecipe implements ISimpleRecipe<Container> {

    @FunctionalInterface
    public interface Factory<R extends CookingApplianceRecipe> {
        R create(
                ResourceLocation id,
                NonNullList<Ingredient> itemIngredients,
                List<FluidStackIngredient> fluidIngredients,
                Ingredient catalyst,
                Ingredient tool,
                int toolDamage,
                List<ItemStackProvider> itemOutputs,
                List<FluidStack> fluidOutputs,
                int duration,
                float minTemp);
    }

    public static class MatchResult {
        public final boolean matches;
        public final int[] consumedPerSlot;

        public MatchResult(boolean matches, int[] consumedPerSlot) {
            this.matches = matches;
            this.consumedPerSlot = consumedPerSlot;
        }
    }

    @Getter
    protected final ResourceLocation id;
    @Getter
    protected final NonNullList<Ingredient> itemIngredients;
    @Getter
    protected final List<FluidStackIngredient> fluidIngredients;
    @Getter
    protected final Ingredient catalyst;
    @Getter
    protected final Ingredient tool;
    @Getter
    protected final int toolDamage;
    @Getter
    protected final List<ItemStackProvider> itemOutputs;
    @Getter
    protected final List<FluidStack> fluidOutputs;
    @Getter
    protected final int duration;
    @Getter
    protected final float minTemp;

    public CookingApplianceRecipe(
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
        this.id = id;
        this.itemIngredients = itemIngredients;
        this.fluidIngredients = fluidIngredients;
        this.catalyst = catalyst != null ? catalyst : Ingredient.EMPTY;
        this.tool = tool != null ? tool : Ingredient.EMPTY;
        this.toolDamage = Math.max(0, toolDamage);
        this.itemOutputs = itemOutputs;
        this.fluidOutputs = fluidOutputs;
        this.duration = duration;
        this.minTemp = minTemp;
    }

    /**
     * Item inputs need to be checked so that multi-stacks can be all in one slot
     * or spread across multiple slots. Helps prevent annoyances like the mixing
     * bowl only allowing stack sizes of 1 per "slot".
     */
    public MatchResult matchItemInputs(CookingApplianceBlockEntity appliance) {
        if (this.itemIngredients.isEmpty()) {
            return new MatchResult(true, new int[appliance.getItemInputCount()]);
        }

        int reqCount = this.itemIngredients.size();
        int slotCount = appliance.getItemInputCount();

        int totalAvailable = 0;
        record ItemUnit(int slot) {
        }
        List<ItemUnit> units = new ArrayList<>();

        for (int s = 0; s < slotCount; s++) {
            ItemStack stack = appliance.getItemInputStack(s);
            if (stack.isEmpty()) {
                continue;
            }
            final IFood food = FoodCapability.get(stack);
            if (food != null && food.isRotten()) {
                continue;
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
                ItemStack stack = appliance.getItemInputStack(slot);
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

    public boolean matches(CookingApplianceBlockEntity appliance, Level level) {
        if (appliance.getTemperature() < this.minTemp) {
            return false;
        }

        // Check catalyst.
        if (!this.catalyst.isEmpty()) {
            ItemStack catalystStack = appliance.getCatalystStack();
            if (catalystStack.isEmpty() || !this.catalyst.test(catalystStack)) {
                return false;
            }
            final IFood food = FoodCapability.get(catalystStack);
            if (food != null && food.isRotten()) {
                return false;
            }
        }

        // Check tool.
        if (!this.tool.isEmpty()) {
            ItemStack toolStack = appliance.getToolStack();
            if (toolStack.isEmpty() || !this.tool.test(toolStack)) {
                return false;
            }
        }

        // Check item inputs.
        MatchResult itemMatch = matchItemInputs(appliance);
        if (!itemMatch.matches) {
            return false;
        }

        // Check fluid inputs.
        if (!this.fluidIngredients.isEmpty()) {
            List<FluidStack> availableFluids = new ArrayList<>();
            for (int i = 0; i < appliance.getFluidInputCount(); i++) {
                FluidStack fluid = appliance.getFluidInputTank(i);
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
        }

        // Check if output spaces are available.
        return canFitOutputs(appliance);
    }

    public boolean canFitOutputs(CookingApplianceBlockEntity appliance) {
        // Test item outputs fit.
        if (!this.itemOutputs.isEmpty()) {
            int availableSlots = 0;
            for (int i = 0; i < appliance.getItemOutputCount(); i++) {
                ItemStack stack = appliance.getItemOutputStack(i);
                if (stack.isEmpty()) {
                    availableSlots++;
                }
            }
            if (availableSlots < this.itemOutputs.size()) {
                return false;
            }
        }

        // Test fluid outputs fit with overflow into available output tanks.
        if (!this.fluidOutputs.isEmpty()) {
            List<FluidStack> simulatedTanks = new ArrayList<>();
            for (int i = 0; i < appliance.getFluidOutputCount(); i++) {
                simulatedTanks.add(appliance.getFluidOutputTank(i).copy());
            }

            int capacity = appliance.getFluidOutputCapacity();
            for (FluidStack outputFluid : this.fluidOutputs) {
                if (outputFluid.isEmpty())
                    continue;
                int remaining = outputFluid.getAmount();

                // Try to fill into tanks with matching fluid.
                for (int i = 0; i < simulatedTanks.size() && remaining > 0; i++) {
                    FluidStack tank = simulatedTanks.get(i);
                    if (!tank.isEmpty() && tank.isFluidEqual(outputFluid)) {
                        int space = capacity - tank.getAmount();
                        if (space > 0) {
                            int toAdd = Math.min(space, remaining);
                            tank.grow(toAdd);
                            remaining -= toAdd;
                        }
                    }
                }

                // Overflow into first available empty tank
                for (int i = 0; i < simulatedTanks.size() && remaining > 0; i++) {
                    FluidStack tank = simulatedTanks.get(i);
                    if (tank.isEmpty()) {
                        int toAdd = Math.min(capacity, remaining);
                        simulatedTanks.set(i, new FluidStack(outputFluid, toAdd));
                        remaining -= toAdd;
                    }
                }

                if (remaining > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    public void assembleOutputs(CookingApplianceBlockEntity appliance) {
        List<ItemStack> nonNullInputs = new ArrayList<>();
        List<ItemStack> foodInputs = new ArrayList<>();
        for (int i = 0; i < appliance.getItemInputCount(); i++) {
            ItemStack stack = appliance.getItemInputStack(i);
            if (!stack.isEmpty()) {
                nonNullInputs.add(stack);
                if (FoodCapability.get(stack) != null) {
                    foodInputs.add(stack);
                }
            }
        }

        appliance.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> RecipeHelpers.setCraftingInput(handler, 0, appliance.getItemInputCount()));

        try {
            // Produce item outputs.
            int outputSlotIndex = 0;
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
                }
                while (outputSlotIndex < appliance.getItemOutputCount()) {
                    if (appliance.getItemOutputStack(outputSlotIndex).isEmpty()) {
                        appliance.setItemOutputStack(outputSlotIndex, out);
                        outputSlotIndex++;
                        break;
                    }
                    outputSlotIndex++;
                }
            }
        } finally {
            RecipeHelpers.clearCraftingInput();
        }

        // Produce fluid outputs with overflow.
        int capacity = appliance.getFluidOutputCapacity();
        for (FluidStack outputFluid : this.fluidOutputs) {
            if (outputFluid.isEmpty())
                continue;
            int remaining = outputFluid.getAmount();

            // Fill existing matching output tanks.
            for (int i = 0; i < appliance.getFluidOutputCount() && remaining > 0; i++) {
                FluidStack tank = appliance.getFluidOutputTank(i);
                if (!tank.isEmpty() && tank.isFluidEqual(outputFluid)) {
                    int space = capacity - tank.getAmount();
                    if (space > 0) {
                        int toAdd = Math.min(space, remaining);
                        tank.grow(toAdd);
                        remaining -= toAdd;
                    }
                }
            }

            // Fill empty output tanks.
            for (int i = 0; i < appliance.getFluidOutputCount() && remaining > 0; i++) {
                FluidStack tank = appliance.getFluidOutputTank(i);
                if (tank.isEmpty()) {
                    int toAdd = Math.min(capacity, remaining);
                    appliance.setFluidOutputTank(i, new FluidStack(outputFluid, toAdd));
                    remaining -= toAdd;
                }
            }
        }
    }

    @Override
    public boolean matches(@NotNull Container container, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Container container, @NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        if (!itemOutputs.isEmpty()) {
            return itemOutputs.get(0).getEmptyStack();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public abstract @NotNull RecipeSerializer<?> getSerializer();

    @Override
    public abstract @NotNull RecipeType<?> getType();

    public static class Serializer<R extends CookingApplianceRecipe> extends RecipeSerializerImpl<R> {

        protected final Factory<R> factory;

        public Serializer(Factory<R> factory) {
            this.factory = factory;
        }

        @Override
        public @NotNull R fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            NonNullList<Ingredient> itemIngredients = NonNullList.create();
            if (json.has("ingredients") || json.has("item_ingredients")) {
                JsonArray array = json.has("ingredients") ? JsonHelpers.getAsJsonArray(json, "ingredients") : JsonHelpers.getAsJsonArray(json, "item_ingredients");
                for (JsonElement el : array) {
                    if (el.isJsonObject()) {
                        JsonObject obj = el.getAsJsonObject();
                        int count = JsonHelpers.getAsInt(obj, "count", 1);
                        Ingredient ing = obj.has("ingredient") ? Ingredient.fromJson(obj.get("ingredient")) : Ingredient.fromJson(obj);
                        for (int i = 0; i < count; i++) {
                            itemIngredients.add(ing);
                        }
                    } else {
                        itemIngredients.add(Ingredient.fromJson(el));
                    }
                }
            }

            List<FluidStackIngredient> fluidIngredients = new ArrayList<>();
            if (json.has("fluid_ingredients")) {
                JsonElement fluidElement = json.get("fluid_ingredients");
                if (fluidElement.isJsonArray()) {
                    for (JsonElement el : fluidElement.getAsJsonArray()) {
                        fluidIngredients.add(FluidStackIngredient.fromJson(el.getAsJsonObject()));
                    }
                } else if (fluidElement.isJsonObject()) {
                    fluidIngredients.add(FluidStackIngredient.fromJson(fluidElement.getAsJsonObject()));
                }
            } else if (json.has("fluid_ingredient")) {
                fluidIngredients.add(FluidStackIngredient.fromJson(JsonHelpers.getAsJsonObject(json, "fluid_ingredient")));
            }

            Ingredient catalyst = Ingredient.EMPTY;
            if (json.has("catalyst")) {
                catalyst = Ingredient.fromJson(json.get("catalyst"));
            }

            Ingredient tool = Ingredient.EMPTY;
            if (json.has("tool")) {
                tool = Ingredient.fromJson(json.get("tool"));
            }
            int toolDamage = JsonHelpers.getAsInt(json, "tool_damage", 1);

            List<ItemStackProvider> itemOutputs = new ArrayList<>();
            if (json.has("results") || json.has("item_outputs")) {
                JsonArray array = json.has("results") ? JsonHelpers.getAsJsonArray(json, "results") : JsonHelpers.getAsJsonArray(json, "item_outputs");
                for (JsonElement el : array) {
                    itemOutputs.add(ItemStackProvider.fromJson(el.getAsJsonObject()));
                }
            } else if (json.has("result")) {
                itemOutputs.add(ItemStackProvider.fromJson(JsonHelpers.getAsJsonObject(json, "result")));
            }

            List<FluidStack> fluidOutputs = new ArrayList<>();
            if (json.has("fluid_results") || json.has("fluid_outputs")) {
                JsonArray array = json.has("fluid_results") ? JsonHelpers.getAsJsonArray(json, "fluid_results") : JsonHelpers.getAsJsonArray(json, "fluid_outputs");
                for (JsonElement el : array) {
                    fluidOutputs.add(JsonHelpers.getFluidStack(el.getAsJsonObject()));
                }
            } else if (json.has("fluid_result") || json.has("fluid_output")) {
                JsonObject fluidObj = json.has("fluid_result") ? JsonHelpers.getAsJsonObject(json, "fluid_result") : JsonHelpers.getAsJsonObject(json, "fluid_output");
                fluidOutputs.add(JsonHelpers.getFluidStack(fluidObj));
            }

            int duration = JsonHelpers.getAsInt(json, "duration", JsonHelpers.getAsInt(json, "cooking_time", 100));
            float minTemp = JsonHelpers.getAsFloat(json, "temperature", JsonHelpers.getAsFloat(json, "min_temp", 0.0f));

            return factory.create(
                    recipeId,
                    itemIngredients,
                    fluidIngredients,
                    catalyst,
                    tool,
                    toolDamage,
                    itemOutputs,
                    fluidOutputs,
                    duration,
                    minTemp);
        }

        @Override
        public @Nullable R fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            int inputCount = buffer.readVarInt();
            NonNullList<Ingredient> itemIngredients = NonNullList.create();
            for (int i = 0; i < inputCount; i++) {
                itemIngredients.add(Ingredient.fromNetwork(buffer));
            }

            int fluidInputCount = buffer.readVarInt();
            List<FluidStackIngredient> fluidIngredients = new ArrayList<>();
            for (int i = 0; i < fluidInputCount; i++) {
                fluidIngredients.add(FluidStackIngredient.fromNetwork(buffer));
            }

            Ingredient catalyst = Ingredient.fromNetwork(buffer);
            Ingredient tool = Ingredient.fromNetwork(buffer);
            int toolDamage = buffer.readVarInt();

            int outputCount = buffer.readVarInt();
            List<ItemStackProvider> itemOutputs = new ArrayList<>();
            for (int i = 0; i < outputCount; i++) {
                itemOutputs.add(ItemStackProvider.fromNetwork(buffer));
            }

            int fluidOutputCount = buffer.readVarInt();
            List<FluidStack> fluidOutputs = new ArrayList<>();
            for (int i = 0; i < fluidOutputCount; i++) {
                fluidOutputs.add(buffer.readFluidStack());
            }

            int duration = buffer.readVarInt();
            float minTemp = buffer.readFloat();

            return factory.create(
                    recipeId,
                    itemIngredients,
                    fluidIngredients,
                    catalyst,
                    tool,
                    toolDamage,
                    itemOutputs,
                    fluidOutputs,
                    duration,
                    minTemp);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull R recipe) {
            buffer.writeVarInt(recipe.itemIngredients.size());
            for (Ingredient ingredient : recipe.itemIngredients) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeVarInt(recipe.fluidIngredients.size());
            for (FluidStackIngredient fluid : recipe.fluidIngredients) {
                fluid.toNetwork(buffer);
            }

            recipe.catalyst.toNetwork(buffer);
            recipe.tool.toNetwork(buffer);
            buffer.writeVarInt(recipe.toolDamage);

            buffer.writeVarInt(recipe.itemOutputs.size());
            for (ItemStackProvider out : recipe.itemOutputs) {
                out.toNetwork(buffer);
            }

            buffer.writeVarInt(recipe.fluidOutputs.size());
            for (FluidStack fluid : recipe.fluidOutputs) {
                buffer.writeFluidStack(fluid);
            }

            buffer.writeVarInt(recipe.duration);
            buffer.writeFloat(recipe.minTemp);
        }
    }
}
