package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BioreactorMachine extends WorkableElectricMultiblockMachine {

    public BioreactorMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public @NotNull BioreactorRecipeLogic getRecipeLogic() {
        return (BioreactorRecipeLogic) super.getRecipeLogic();
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull ... args) {
        return new BioreactorRecipeLogic(this);
    }

    public static class BioreactorRecipeLogic extends RecipeLogic {
        public BioreactorRecipeLogic(IRecipeLogicMachine machine) {
            super(machine);
        }

        @Override
        protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            if (io == IO.IN) return super.handleRecipeIO(recipe, io);
            Map<RecipeCapability<?>, List<Content>> contents = new HashMap<>();
            contents.put(FluidRecipeCapability.CAP, recipe.getOutputContents(FluidRecipeCapability.CAP));

            List<Content> modifiedItemOutputs = new ArrayList<>();
            for (Content content : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
                Object obj = content.content;
                if (obj instanceof SizedIngredient sized) {
                    ItemStackProvider isp = ItemStackProvider.of(new ItemStack(sized.getInner().getItems()[0].getItem(), sized.getAmount()));
                    modifiedItemOutputs.add(new Content(SizedIngredient.create(Ingredient.of(isp.getEmptyStack()), sized.getAmount()), content.chance, content.maxChance, content.tierChanceBoost));
                }
            }
            contents.put(ItemRecipeCapability.CAP, modifiedItemOutputs);

            return RecipeHelper.handleRecipe((IRecipeCapabilityHolder)getMachine(), recipe, io, contents, chanceCaches, false, false);
        }
    }

}
