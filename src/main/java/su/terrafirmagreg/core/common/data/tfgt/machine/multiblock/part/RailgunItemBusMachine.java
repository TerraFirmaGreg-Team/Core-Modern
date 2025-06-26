package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.terrafirmagreg.core.common.data.tfgt.TFGTItems;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RailgunItemBusMachine extends ItemBusPartMachine {
    protected static ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(RailgunItemBusMachine.class, ItemBusPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private static final Map<Long, RailgunItemBusMachine> OPEN_LINKS = new HashMap<>();

    private @Nullable RailgunItemBusMachine linkedBus;
    @Persisted
    protected CustomItemStackHandler linkInventory;

    @DescSynced
    private boolean isLinked;

    private long oldLinkId = 0;

    private void updateLink() {
        if (isRemote()) return;
        long newId;
        if (linkInventory.getStackInSlot(0).isEmpty()) newId = 0;
        else newId = linkInventory.getStackInSlot(0).getOrCreateTag().getLong("linkId");

        if (oldLinkId == newId) return;

        if (linkedBus != null) {
            OPEN_LINKS.put(oldLinkId, linkedBus);
            linkedBus.isLinked = false;
            linkedBus.linkedBus = null;
            linkedBus = null;
            isLinked = false;
        }

        oldLinkId = newId;
        if (newId == 0) return;
        if (OPEN_LINKS.containsKey(newId) && OPEN_LINKS.get(newId).isReciever == isReciever) return;
        linkedBus = OPEN_LINKS.remove(newId);
        if (linkedBus != null) {
            linkedBus.linkedBus = this;
            linkedBus.isLinked = true;
            isLinked = true;
            if (isReciever) {
                _currentInactivityTrigger = linkedBus._currentInactivityTrigger;
                _currentMode = linkedBus._currentMode;
                _currentItemFilters = linkedBus._currentItemFilters;
            }
        } else OPEN_LINKS.put(newId, this);
    }

    public enum Mode implements EnumSelectorWidget.SelectableEnum {
        ITEM("Item", "transfer_any"),
        REDSTONE_SIGNAL("Redstone signal", "transfer_any"),
        INACTIVITY("Inactivity", "transfer_any");

        @Getter
        public final String tooltip;
        @Getter
        public final IGuiTexture icon;

        Mode(String tooltip, String textureName) {
            this.tooltip = tooltip;
            this.icon = new ResourceTexture("gtceu:textures/gui/icon/transfer_mode/" + textureName + ".png");
        }
    }

    private final boolean isReciever;

    public RailgunItemBusMachine(IMachineBlockEntity holder, IO ioMode) {
        super(holder, 3, ioMode);
        linkInventory = new CustomItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        isLinked = false;
        linkInventory.setFilter((stack) -> stack.is(TFGTItems.INTERPLANETARY_LINK.asItem()));
        isReciever = ioMode == IO.OUT;
        _currentMode = RailgunItemBusMachine.Mode.ITEM;
        _currentInactivityTrigger = 0;
        _currentItemFilters = new CustomItemStackHandler(3);
    }

    @Override
    protected int getInventorySize() {
        return 9;
    }

    @Override
    public void addedToController(IMultiController controller) {
        updateLink();
        super.addedToController(controller);
    }

    @Override
    public void removedFromController(IMultiController controller) {
        updateLink();
        super.removedFromController(controller);
    }

    @Override
    public void onMachineRemoved() {
        updateLink();
        super.onMachineRemoved();
        clearInventory(linkInventory);
    }

    @DescSynced
    @Persisted
    private RailgunItemBusMachine.Mode _currentMode;
    private RailgunItemBusMachine.Mode getCurrentMode() {
        if (isReciever && linkedBus != null) return linkedBus.getCurrentMode();
        return _currentMode;
    }
    private void setCurrentMode(Mode newMode) {
        if (isReciever && linkedBus != null) linkedBus.setCurrentMode(newMode);
        _currentMode = newMode;
    }

    @DescSynced
    @Persisted
    private int _currentInactivityTrigger;
    private int getCurrentInactivityTrigger() {
        if (isReciever && linkedBus != null) return linkedBus.getCurrentInactivityTrigger();
        return _currentInactivityTrigger;
    }
    private void setCurrentInactivityTrigger(int triggerTime) {
        if (isReciever && linkedBus != null) linkedBus.setCurrentInactivityTrigger(triggerTime);
        _currentInactivityTrigger = triggerTime;
    }

    @Persisted
    @DescSynced
    private CustomItemStackHandler _currentItemFilters;
    private CustomItemStackHandler getCurrentItemFilters() {
        if (isReciever && linkedBus != null) return linkedBus.getCurrentItemFilters();
        return _currentItemFilters;
    }

    public class InterplanetaryLinkGUI implements IFancyUIProvider {
        @Override
        public IGuiTexture getTabIcon() {
            return GuiTextures.TOOL_COVER_SETTINGS;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("tfg.machine.railgun_loader.title");
        }


        private final WidgetGroup linkConfigWidget = new WidgetGroup(0, 25, 200, 100);;
        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            if (!isFormed()) return new LabelWidget(0, 0, Component.translatable("tfg.machine.railgun_loader.parent_not_formed").getString());

            var mainPage = new WidgetGroup(4, 4, 200, 150);
            WidgetGroup linkStatusGroup = new WidgetGroup(0, 0, 200, 22);

            var slot = new SlotWidget(linkInventory, 0, 0, 2, true, true);
            var currentStatusLabel = new LabelWidget(22, 6, "");
            slot.setChangeListener(() -> {
                updateLink();
                currentStatusLabel.setComponent(getCurrentStatusLabel());
                createLinkConfigWidget();
                markAsDirty();
            });
            currentStatusLabel.setComponent(getCurrentStatusLabel());
            linkStatusGroup.addWidget(slot);
            linkStatusGroup.addWidget(currentStatusLabel);
            mainPage.addWidget(linkStatusGroup);


            createLinkConfigWidget();
            mainPage.addWidget(linkConfigWidget);

            return mainPage;
        }

        private void createLinkConfigWidget() {
            linkConfigWidget.clearAllWidgets();

            var modeConfigWidget = new WidgetGroup(Position.of(4, 28));
            if (!isLinked) return;
            var button = new EnumSelectorWidget<>(4, 4, 20, 20, Mode.values(), RailgunItemBusMachine.this.getCurrentMode(), (newMode) -> {
                setCurrentMode(newMode);
                updateModeConfigWidget(modeConfigWidget);
                markAsDirty();
            });
            linkConfigWidget.addWidget(button);

            linkConfigWidget.addWidget(modeConfigWidget);
            updateModeConfigWidget(modeConfigWidget);
        }

        private void updateModeConfigWidget(WidgetGroup widget) {
            widget.clearAllWidgets();
            if (getCurrentMode() == Mode.REDSTONE_SIGNAL) return;
            if (getCurrentMode() == Mode.INACTIVITY) {
                widget.addWidget(new IntInputWidget(0, 0, 100, 20, RailgunItemBusMachine.this::getCurrentInactivityTrigger, RailgunItemBusMachine.this::setCurrentInactivityTrigger));
            } else {
                WidgetGroup group = new WidgetGroup();
                group.addWidget(new PhantomSlotWidget(getCurrentItemFilters(), 0, 0, 0));
                group.addWidget(new PhantomSlotWidget(getCurrentItemFilters(), 1, 18, 0));
                group.addWidget(new PhantomSlotWidget(getCurrentItemFilters(), 2, 36, 0));
                widget.addWidget(group);
            }
        }

        private Component getCurrentStatusLabel() {
            if (linkInventory.getStackInSlot(0) == ItemStack.EMPTY) {
                return Component.translatable("tfg.machine.railgun_loader.no_filter");
            } else if (linkInventory.getStackInSlot(0).getOrCreateTag().getLong("linkId") == 0) {
                return Component.translatable("tfg.machine.railgun_loader.filter_not_linked");
            } else if (!isLinked) {
                return Component.translatable("tfg.machine.railgun_loader.waiting_for_match");
            } else {
                return Component.translatable("item.tfg.interplanetarylink.linked");
            }
        }
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        superAttachConfigurators(configuratorPanel);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.attachSubTab(new InterplanetaryLinkGUI());
        super.attachSideTabs(sideTabs);
    }
}
