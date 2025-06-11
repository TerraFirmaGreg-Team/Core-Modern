package su.terrafirmagreg.core.common.data.machines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class GreenhouseMachine extends WorkableElectricMultiblockMachine {

    public GreenhouseMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public GreenhouseRecipeLogic getRecipeLogic() {
        return (GreenhouseRecipeLogic) super.getRecipeLogic();
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new GreenhouseRecipeLogic(this);
    }

    class GreenhouseRecipeLogic extends RecipeLogic {
        public GreenhouseRecipeLogic(IRecipeLogicMachine machine) {
            super(machine);
        }

        @Override
        protected boolean handleRecipeIO(GTRecipe recipe, IO io) {
            if (io == IO.IN) return super.handleRecipeIO(recipe, io);
            Map<RecipeCapability<?>, List<Content>> contents = new HashMap<>();
            contents.put(FluidRecipeCapability.CAP, recipe.getOutputContents(FluidRecipeCapability.CAP));

            // Regenerate item outputs so tfc crops have correct attributes
            List<Content> modifiedItemOutputs = new ArrayList<>();
            for (Content content : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
                Object obj = content.content;
                if (obj instanceof SizedIngredient sized) {
                    ItemStackProvider isp = ItemStackProvider.of(new ItemStack(sized.getInner().getItems()[0].getItem(), sized.getAmount()));
                    modifiedItemOutputs.add(new Content(SizedIngredient.create(Ingredient.of(isp.getEmptyStack()), sized.getAmount()), content.chance, content.maxChance, content.tierChanceBoost, content.slotName, content.uiName));
                }
            }
            contents.put(ItemRecipeCapability.CAP, modifiedItemOutputs);

            return recipe.handleRecipe(io, (IRecipeCapabilityHolder)getMachine(), false, contents, chanceCaches);
        }
    }

}
