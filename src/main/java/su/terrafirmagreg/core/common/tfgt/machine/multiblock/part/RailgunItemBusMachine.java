package su.terrafirmagreg.core.common.tfgt.machine.multiblock.part;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.tfgt.TFGMachines;

public class RailgunItemBusMachine extends ItemBusPartMachine {

    public RailgunItemBusMachine(BlockEntityCreationInfo info, int tier, IO io) {
        super(info, tier, io, new NotifiableItemStackHandler(getInventorySize(tier), io)
                .setFilter(v -> !v.getTags().toList().contains(TFGTags.Items.CannotLaunchInRailgun)
                        && v.getItem().canFitInsideContainerItems()));
        circuitSlot.setEnabled(true);
    }

    @Override
    public boolean canConnectRedstone(@NotNull Direction side) {
        return side != getFrontFacing();
    }

    @Override
    public boolean swapIO() {
        BlockPos blockPos = getBlockPos();
        MachineDefinition newDefinition = null;
        if (io == IO.IN) {
            newDefinition = TFGMachines.RAILGUN_ITEM_LOADER_OUT[this.getTier()];
        } else if (io == IO.OUT) {
            newDefinition = TFGMachines.RAILGUN_ITEM_LOADER_IN[this.getTier()];
        }

        if (newDefinition == null)
            return false;
        BlockState newBlockState = newDefinition.getBlock().defaultBlockState();

        getLevel().setBlockAndUpdate(blockPos, newBlockState);

        if (getLevel().getBlockEntity(blockPos) instanceof ItemBusPartMachine newMachine) {
            // We don't set the circuit or distinct busses, since
            // that doesn't make sense on an output bus.
            // Furthermore, existing inventory items
            // and conveyors will drop to the floor on block override.
            newMachine.setFrontFacing(this.getFrontFacing());
            newMachine.setUpwardsFacing(this.getUpwardsFacing());
            newMachine.setPaintingColor(this.getPaintingColor());
        }
        return true;
    }
}
