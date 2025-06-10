package su.terrafirmagreg.core.common.data.machines;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

public class GreenhouseMachine extends WorkableElectricMultiblockMachine {

    public GreenhouseMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ISPOutputRecipeLogic getRecipeLogic() {
        return (ISPOutputRecipeLogic) super.getRecipeLogic();
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new ISPOutputRecipeLogic(this);
    }

}
