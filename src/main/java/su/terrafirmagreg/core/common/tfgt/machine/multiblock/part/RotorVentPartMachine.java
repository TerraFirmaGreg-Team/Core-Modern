package su.terrafirmagreg.core.common.tfgt.machine.multiblock.part;

import javax.annotation.Nullable;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import net.minecraft.network.chat.Component;

public class RotorVentPartMachine extends RotorHolderPartMachine {

    /** The Rotor Holder makes damage every seconds, here it's every 5 minutes and it's also disabled*/

    private static final int DAMAGE_INTERVAL = 20 * 60 * 5;

    /** We remove the bonus from the Holder to keep it exclusively on the Rotors
     * But we could also use these to specifically alter the strenght of the rotor */
    private static final int VENT_EFFICIENCY = 100; // mB/t
    private static final int VENT_POWER_MULTIPLIER = 1; // EU/t be wary though this isn't a percentage

    /** The higher the number, the stronger the bonuses of the Rotor */
    private static final double STAT_EXPONENT = 0.5;

    @Nullable
    private ISubscription wakeSubs;

    public RotorVentPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            wakeSubs = inventory.addChangedListener(this::wakeController);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (wakeSubs != null) {
            wakeSubs.unsubscribe();
            wakeSubs = null;
        }
    }

    private void wakeController() {
        if (isFormed() && getControllers().first() instanceof IWorkableMultiController workable) {
            workable.getRecipeLogic().updateTickSubscription();
        }
    }

    @Override
    public boolean onWorking(IWorkableMultiController controller) {
        if (!hasRotor())
            return false;

        if (getRotorSpeed() < getMaxRotorHolderSpeed()) {
            setRotorSpeed(getRotorSpeed() + SPEED_INCREMENT);
            updateRotorSubscription();
        }
        /*
        if (self().getOffsetTimer() % DAMAGE_INTERVAL == 0) {
            int problems = 0;
            if (isFormed() && getControllers().first() instanceof IMaintenanceMachine maintenance) {
                problems = maintenance.getNumMaintenanceProblems();
            }
            damageRotor(1 + problems);
        }
        */
        return true;
    }

    // With this part we get the stats of the Rotors, also will be easy to duplicate for future mechanics

    @Override
    public int getHolderEfficiency() {
        return VENT_EFFICIENCY;
    }

    @Override
    public int getHolderPowerMultiplier() {
        return VENT_POWER_MULTIPLIER;
    }

    // Power stat - Cost less eu/t
    public double getEnergyCostMultiplier() {
        return energyMultiplier(getRotorPower());
    }

    // Efficiency stat - Recipe consumption multiplier because recipe is per tick anyway
    public double getConsumptionMultiplier() {
        return consumptionMultiplier(getRotorEfficiency());
    }

    public static double energyMultiplier(int rotorPower) {
        return statMultiplier(rotorPower * VENT_POWER_MULTIPLIER);
    }

    public static double consumptionMultiplier(int rotorEfficiency) {
        return statMultiplier(rotorEfficiency * VENT_EFFICIENCY / 100);
    }

    /*
    No penalty for a rotor with stats under 100
     */
    private static double statMultiplier(int stat) {
        return Math.pow(100.0 / Math.max(100, stat), STAT_EXPONENT);
    }

    // Bring back the Rotor not working if obstructed or empty
    @Nullable
    public Component getProblem() {
        if (!hasRotor())
            return Component.translatable("tfg.machine.rotor_vent.no_rotor");
        if (!isFrontFaceFree())
            return Component.translatable("gtceu.multiblock.universal.rotor_obstructed");
        return null;
    }
}
