package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import su.terrafirmagreg.core.common.tfgt.machine.trait.ISPOutputRecipeLogic;

public class GrowthChamberMachine extends WorkableElectricMultiblockMachine {

    public GrowthChamberMachine(BlockEntityCreationInfo info) {
        super(info, new ISPOutputRecipeLogic());
    }

    @Override
    public @NotNull ISPOutputRecipeLogic getRecipeLogic() {
        return (ISPOutputRecipeLogic) super.getRecipeLogic();
    }
}
