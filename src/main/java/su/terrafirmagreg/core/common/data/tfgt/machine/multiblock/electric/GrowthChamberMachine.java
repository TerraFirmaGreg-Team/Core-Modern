package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import su.terrafirmagreg.core.common.data.tfgt.machine.trait.ISPOutputRecipeLogic;

public class GrowthChamberMachine extends WorkableElectricMultiblockMachine {

    public GrowthChamberMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
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
