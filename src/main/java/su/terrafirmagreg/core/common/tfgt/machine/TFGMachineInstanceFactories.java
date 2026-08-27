package su.terrafirmagreg.core.common.tfgt.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.trait.EnvironmentalExplosionTrait;

import su.terrafirmagreg.core.common.data.items.TFGItems;
import su.terrafirmagreg.core.common.tfgt.machine.trait.ISPOutputRecipeLogic;

public interface TFGMachineInstanceFactories {

    MachineInstanceFactory<ItemBusPartMachine> RAILGUN_AMMO_LOADER = (info) -> {
        var machine = new ItemBusPartMachine(info, 0, IO.IN);
        machine.getCircuitSlot().setEnabled(false);
        machine.getInventory().setFilter((s) -> s.is(TFGItems.RAILGUN_AMMO_SHELL.get()));
        return machine;
    };

    MachineInstanceFactory.Tiered<SimpleTieredMachine> AQUEOUS_ACCUMULATOR = (info, tier) -> {
        var machine = new SimpleTieredMachine(info, tier);
        machine.getTraitOrThrow(EnvironmentalExplosionTrait.TYPE).setEnableEnvironmentalExplosions(false);

        return machine;
    };

    MachineInstanceFactory.Tiered<SimpleTieredMachine> SIMPLE_FOOD_PROCESSOR = (info, tier) -> {
        return new SimpleTieredMachine(info, tier, new ISPOutputRecipeLogic(), GTMachineUtils.defaultTankSizeFunction);
    };

}
