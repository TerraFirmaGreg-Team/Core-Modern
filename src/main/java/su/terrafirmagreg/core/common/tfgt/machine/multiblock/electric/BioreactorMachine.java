package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;

import su.terrafirmagreg.core.common.tfgt.machine.trait.ISPOutputRecipeLogic;

public class BioreactorMachine extends WorkableElectricMultiblockMachine {

    public BioreactorMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public @NotNull ISPOutputRecipeLogic getRecipeLogic() {
        return (ISPOutputRecipeLogic) super.getRecipeLogic();
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new ISPOutputRecipeLogic(this);
    }
}
