package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.common.data.tfgt.machine.trait.ISPOutputRecipeLogic;

public class BioreactorMachine extends WorkableElectricMultiblockMachine {

    public BioreactorMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public @NotNull ISPOutputRecipeLogic getRecipeLogic() {
        return (ISPOutputRecipeLogic) super.getRecipeLogic();
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull ... args) {
        return new ISPOutputRecipeLogic(this);
    }
}
