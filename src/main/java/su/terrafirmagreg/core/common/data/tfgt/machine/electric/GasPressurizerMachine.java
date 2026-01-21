package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

public class GasPressurizerMachine extends SimpleTieredMachine {

    public GasPressurizerMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, GTMachineUtils.defaultTankSizeFunction);
    }
}
