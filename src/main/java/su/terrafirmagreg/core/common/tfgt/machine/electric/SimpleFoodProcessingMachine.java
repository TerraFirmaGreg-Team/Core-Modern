package su.terrafirmagreg.core.common.tfgt.machine.electric;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import su.terrafirmagreg.core.common.tfgt.machine.trait.ISPOutputRecipeLogic;

public class SimpleFoodProcessingMachine extends SimpleTieredMachine {

    public SimpleFoodProcessingMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, GTMachineUtils.defaultTankSizeFunction);
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
