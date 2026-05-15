package su.terrafirmagreg.core.common.tfgt.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.common.cover.voiding.ItemVoidingCover;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

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

/**
 * A voiding cover for GT that only voids rotten food.
 */
public class RottenVoidCover extends ItemVoidingCover {

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
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 176, 140);
        group.addWidget(new LabelWidget(10, 5, getUITitle()));
        group.addWidget(new ToggleButtonWidget(
                10,
                20,
                20,
                20,
                GuiTextures.BUTTON_POWER,
                this::isWorkingEnabled,
                this::setWorkingEnabled));
        group.addWidget(new LabelWidget(
                10,
                45,
                "tfg.gui.cover.rotten_void_days"));
        group.addWidget(new IntInputWidget(
                10,
                58,
                80,
                20,
                () -> minimumDaysRemaining,
                value -> minimumDaysRemaining = Math.max(0, value))
                .setMin(0));
        group.addWidget(filterHandler.createFilterConfigUI(
                10,
                82,
                126,
                60));
        group.addWidget(filterHandler.createFilterSlotUI(
                148,
                111));
        return group;
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("minimum_days_remaining", minimumDaysRemaining);
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        minimumDaysRemaining = Math.max(0, tag.getInt("minimum_days_remaining"));
        super.pasteConfig(player, tag);
    }
}
