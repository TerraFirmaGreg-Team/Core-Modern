package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class GreenhouseMachine extends WorkableElectricMultiblockMachine {

    public GreenhouseMachine(BlockEntityCreationInfo info) {
        super(info, new GreenhouseRecipeLogic());
    }

    @Override
    public @NotNull GreenhouseRecipeLogic getRecipeLogic() {
        return (GreenhouseRecipeLogic) super.getRecipeLogic();
    }

    public static class GreenhouseRecipeLogic extends RecipeLogic {

        private static final BiConsumer<GTRecipe, Object> REFRESH_FOOD_DATE = (recipe, output) -> {
            if (output instanceof ItemStack stack)
                FoodCapability.updateFoodDecayOnCreate(stack);
        };

        public GreenhouseRecipeLogic() {
            super();
        }

        @Override
        protected ActionResult checkRecipe(GTRecipe recipe) {
            ActionResult base = super.checkRecipe(recipe);
            if (!base.isSuccess())
                return base;

            // Fail if a rotten stack could be consumed as one of the recipe item inputs
            List<Ingredient> itemInputs = recipe.getInputContents(ItemRecipeCapability.CAP).stream()
                    .map(content -> ItemRecipeCapability.CAP.of(content.content()))
                    .toList();
            if (itemInputs.isEmpty())
                return base;

            var holder = (IRecipeCapabilityHolder) getMachine();
            for (var handler : holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
                if (!(handler instanceof NotifiableItemStackHandler stackHandler))
                    continue;
                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    if (stack.isEmpty() || !FoodCapability.isRotten(stack))
                        continue;
                    for (Ingredient ingredient : itemInputs) {
                        if (ingredient.test(stack)) {
                            return ActionResult.fail(Component.translatable("tfg.recipe_logic.rotten_input",
                                    stack.getHoverName()).withStyle(ChatFormatting.RED), ItemRecipeCapability.CAP, IO.IN);
                        }
                    }
                }
            }
            return base;
        }

        @Override
        protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            if (io != IO.OUT)
                return super.handleRecipeIO(recipe, io);

            Map<RecipeCapability<?>, List<Content>> contents = new HashMap<>(recipe.outputs);

            List<Content> itemOutputs = new ArrayList<>();
            for (Content content : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
                if (content.content() instanceof SizedIngredient sized) {
                    ItemStack[] matches = sized.getInner().getItems();
                    if (matches.length > 0 && !matches[0].isEmpty()) {
                        ItemStack fresh = FoodCapability.updateFoodDecayOnCreate(matches[0].copyWithCount(sized.getAmount()));
                        itemOutputs.add(new Content(SizedIngredient.create(Ingredient.of(fresh), sized.getAmount()),
                                content.chance(), content.maxChance()));
                        continue;
                    }
                }
                itemOutputs.add(content);
            }
            contents.put(ItemRecipeCapability.CAP, itemOutputs);

            return RecipeHelper.handleRecipe((IRecipeCapabilityHolder) getMachine(), recipe, io, contents, chanceCaches,
                    false, false);
        }
    }
}
