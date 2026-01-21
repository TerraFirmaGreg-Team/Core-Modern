package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import su.terrafirmagreg.core.common.data.tfgt.machine.trait.ISPOutputRecipeLogic;

public class BioreactorMachine extends WorkableElectricMultiblockMachine {

    public BioreactorMachine(BlockEntityCreationInfo info) {
        super(info, ISPOutputRecipeLogic::new);
    }

    @Override
    public @NotNull ISPOutputRecipeLogic getRecipeLogic() {
        return (ISPOutputRecipeLogic) super.getRecipeLogic();
    }
}
