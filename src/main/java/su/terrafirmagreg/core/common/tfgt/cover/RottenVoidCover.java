package su.terrafirmagreg.core.common.tfgt.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.cover.voiding.ItemVoidingCover;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import lombok.Setter;

/**
 * A voiding cover for GT that only voids rotten food.
 */
public class RottenVoidCover extends ItemVoidingCover {

    @SaveField
    @Getter
    @Setter
    protected int minimumDaysRemaining = 0;

    public RottenVoidCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    protected void doVoidItems() {
        if (!isWorkingEnabled())
            return;

        IItemHandler handler = getOwnItemHandler();
        if (!(handler instanceof IItemHandlerModifiable modifiable))
            return;

        ItemFilter filter = filterHandler.getFilter();

        final long now = Calendars.get().getTicks();
        final long thresholdTicks = (long) minimumDaysRemaining * ICalendar.TICKS_IN_DAY;

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack sourceStack = handler.getStackInSlot(slot);
            if (sourceStack.isEmpty())
                continue;

            IFood food = FoodCapability.get(sourceStack);
            if (food == null)
                continue;

            if (!filter.test(sourceStack))
                continue;

            if (food.isRotten() || food.getRottenDate() - now <= thresholdTicks) {
                modifiable.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {

        IntSyncValue minDays = new IntSyncValue(this::getMinimumDaysRemaining, this::setMinimumDaysRemaining).allowC2S();

        syncManager.syncValue("minDays", minDays);

        column.child(Text.lang("tfg.gui.cover.rotten_void_days").asWidget());
        column.child(GTMuiWidgets.createIntInputWithButtons(minDays, () -> 0, () -> Integer.MAX_VALUE));

        column.child(GTMuiWidgets.createFilterRow(
                coverUIRow().child(
                        GTMuiWidgets.createPowerButton(this)),
                filterHandler, data, syncManager,
                settings));
    }

    @Override
    public void copyConfig(CompoundTag tag) {
        tag.putInt("minimum_days_remaining", minimumDaysRemaining);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        minimumDaysRemaining = Math.max(0, tag.getInt("minimum_days_remaining"));
        super.pasteConfig(player, tag);
    }
}
