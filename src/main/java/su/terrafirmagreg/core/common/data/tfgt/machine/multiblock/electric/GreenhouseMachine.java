package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class GreenhouseMachine extends WorkableElectricMultiblockMachine {

    public GreenhouseMachine(BlockEntityCreationInfo info) {
        super(info, GreenhouseRecipeLogic::new);
    }

    @Override
    public @NotNull GreenhouseRecipeLogic getRecipeLogic() {
        return (GreenhouseRecipeLogic) super.getRecipeLogic();
    }

    public static class GreenhouseRecipeLogic extends RecipeLogic {
        public GreenhouseRecipeLogic(IRecipeLogicMachine machine) {
            super(machine);
        }

        @Override
        protected ActionResult checkRecipe(GTRecipe recipe) {
            ActionResult base = super.checkRecipe(recipe);
            if (!base.isSuccess())
                return base;

            // Fail if any input stack is rotten
            List<IRecipeHandler<?>> inputHandlers = new ArrayList<>();
            ((IRecipeCapabilityHolder) getMachine()).getCapabilitiesForIO(IO.IN)
                    .forEach(v -> inputHandlers.addAll(v.getCapability(ItemRecipeCapability.CAP)));
            inputHandlers.sort(IRecipeHandler.ENTRY_COMPARATOR);

            for (IRecipeHandler<?> handler : inputHandlers) {
                if (handler instanceof com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler stackHandler) {
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        IFood food = FoodCapability.get(stack);
                        if (food != null && food.isRotten()) {
                            return ActionResult.fail(
                                    net.minecraft.network.chat.Component.translatable("gtceu.recipe_logic.insufficient_in")
                                            .append(": ").append(ItemRecipeCapability.CAP.getName()),
                                    ItemRecipeCapability.CAP, IO.IN);
                        }
                    }
                }
            }
            return base;
        }

        @Override
        protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            if (io == IO.IN)
                return super.handleRecipeIO(recipe, io);
            Map<RecipeCapability<?>, List<Content>> contents = new HashMap<>();
            contents.put(FluidRecipeCapability.CAP, recipe.getOutputContents(FluidRecipeCapability.CAP));

            // Regenerate item outputs so tfc crops have correct attributes
            List<Content> modifiedItemOutputs = new ArrayList<>();
            for (Content content : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
                Object obj = content.content;
                if (obj instanceof SizedIngredient sized) {
                    ItemStack[] matches = sized.getInner().getItems();
                    ItemStack template = matches.length > 0 ? matches[0].copy() : ItemStack.EMPTY;
                    if (!template.isEmpty()) {
                        ItemStackProvider isp = ItemStackProvider.of(template);
                        ItemStack regenerated = isp.getStack(template);
                        regenerated.setCount(sized.getAmount());
                        modifiedItemOutputs.add(
                                new Content(
                                        SizedIngredient.create(Ingredient.of(regenerated), sized.getAmount()),
                                        content.chance, content.maxChance, content.tierChanceBoost));
                    } else {
                        modifiedItemOutputs.add(content);
                    }
                } else {
                    modifiedItemOutputs.add(content);
                }
            }
            contents.put(ItemRecipeCapability.CAP, modifiedItemOutputs);

            return RecipeHelper.handleRecipe((IRecipeCapabilityHolder) getMachine(), recipe, io, contents, chanceCaches,
                    false, false);
        }
    }

}
