package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import su.terrafirmagreg.core.common.data.items.TFGItems;
import su.terrafirmagreg.core.common.tfgt.machine.trait.InputHistoryTrait;

public class MicrobialCultivatorMachine extends WorkableElectricMultiblockMachine {

    public MicrobialCultivatorMachine(BlockEntityCreationInfo info) {
        super(info);
        attachPersistentTrait("inputHistory",
                new InputHistoryTrait(100, stack -> stack.is(TFGItems.FILLED_DNA_SYRINGE.get())));
    }
}
