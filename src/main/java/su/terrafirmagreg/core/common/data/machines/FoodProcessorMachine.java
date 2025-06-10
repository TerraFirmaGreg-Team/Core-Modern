package su.terrafirmagreg.core.common.data.machines;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

public class FoodProcessorMachine extends SimpleTieredMachine {
    
    public FoodProcessorMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, GTMachineUtils.defaultTankSizeFunction, args);
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
