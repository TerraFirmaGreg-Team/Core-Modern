package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

public class GasPressurizerMachine extends SimpleTieredMachine {

	public GasPressurizerMachine(IMachineBlockEntity holder, int tier, Object... args) {
		super(holder, tier, GTMachineUtils.defaultTankSizeFunction, args);
	}
}
