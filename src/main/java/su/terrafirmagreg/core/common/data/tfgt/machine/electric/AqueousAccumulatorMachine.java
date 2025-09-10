package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

public class AqueousAccumulatorMachine extends SimpleTieredMachine {

    public AqueousAccumulatorMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, GTMachineUtils.defaultTankSizeFunction, args);
    }

    @Override
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
    }
}
