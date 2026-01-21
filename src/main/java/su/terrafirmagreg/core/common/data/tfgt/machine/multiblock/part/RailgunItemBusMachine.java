package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.world.item.BlockItem;

import su.terrafirmagreg.core.common.data.TFGTags;

public class RailgunItemBusMachine extends ItemBusPartMachine {
    public RailgunItemBusMachine(BlockEntityCreationInfo info, int tier, IO io) {
        super(info, tier, io);
    }

    @Override
    protected @NotNull NotifiableItemStackHandler createInventory() {
        return new NotifiableItemStackHandler(this, getInventorySize(), io)
                .setFilter(v -> !v.getTags().toList().contains(TFGTags.Items.CannotLaunchInRailgun)
                        && v.getItem().canFitInsideContainerItems() && (!(v.getItem() instanceof BlockItem)));
    }

}
