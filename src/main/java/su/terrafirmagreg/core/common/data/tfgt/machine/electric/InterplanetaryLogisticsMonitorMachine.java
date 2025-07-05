package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.gui.widget.layout.Align;
import com.lowdragmc.lowdraglib.gui.widget.layout.Layout;
import com.lowdragmc.lowdraglib.utils.Position;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork.*;
import java.util.*;

public class InterplanetaryLogisticsMonitorMachine extends MetaMachine implements IUIMachine {
    public InterplanetaryLogisticsMonitorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    private static final int GUI_WIDTH = 300;
    private static final int GUI_HEIGHT = 300;
    private static final int INNER_WIDTH = GUI_WIDTH - 8;
    private static final int INNER_HEIGHT = GUI_HEIGHT - 8;

    private class InterplanetaryLogisticsManagerWidget extends TabContainer {

        private static DraggableScrollableWidgetGroup getTabGroup() {
            var group = (new DraggableScrollableWidgetGroup(4, 4, INNER_WIDTH, INNER_HEIGHT))
                    .setDraggable(false)
                    .setScrollable(true)
                    .setScrollWheelDirection(DraggableScrollableWidgetGroup.ScrollWheelDirection.VERTICAL)
                    .setYScrollBarWidth(4)
                    .setYBarStyle(ColorPattern.RED.rectTexture(), ColorPattern.WHITE.rectTexture().setRadius(2));
            group.setLayout(Layout.VERTICAL_CENTER);
            group.setLayoutPadding(4);
            return group;
        }

        private final DraggableScrollableWidgetGroup senderTab = getTabGroup();
        private final DraggableScrollableWidgetGroup recieverTab = getTabGroup();

        private void updateIfServer() {
            if (network != null) network.markDirty();
        }

        public InterplanetaryLogisticsManagerWidget(Player accessor) {
            super(0, 0, GUI_WIDTH, GUI_HEIGHT);
            setAlign(Align.CENTER);
            setBackground(ResourceBorderTexture.BORDERED_BACKGROUND);
            addTab(new TabButton(-32 + 4, 0, 32, 28).setTexture(
                            new GuiTextureGroup(TABS_LEFT.getSubTexture(0, 0, 0.5f, 1f / 3), new ResourceTexture("gtceu:textures/gui/icon/io_mode/export.png")),
                            new GuiTextureGroup(TABS_LEFT.getSubTexture(0.5f, 0, 0.5f, 1f / 3), new ResourceTexture("gtceu:textures/gui/icon/io_mode/export.png"))
                    ),
                    senderTab);
            addTab(new TabButton(-32 + 4, 28, 32, 28).setTexture(
                            new GuiTextureGroup(TABS_LEFT.getSubTexture(0, 1f / 3, 0.5f, 1f / 3), new ResourceTexture("gtceu:textures/gui/icon/io_mode/import.png")),
                            new GuiTextureGroup(TABS_LEFT.getSubTexture(0.5f, 1f / 3, 0.5f, 1f / 3), new ResourceTexture("gtceu:textures/gui/icon/io_mode/import.png"))
                    ),
                    recieverTab);

            if (!InterplanetaryLogisticsMonitorMachine.this.isRemote()) {
                network = InterplanetaryLogisticsNetwork.get(getHolder());
                var parts = network.getPartsVisibleToPlayer(accessor);

                for (var part: parts) {
                    if (part.isRecieverPart()) recieverParts.add(part);
                    else senderParts.add(part);
                }

                for (var part: senderParts) {
                    senderTab.addWidget(createWidgetRow(part));
                }
                for (var part: recieverParts) {
                    recieverTab.addWidget(createWidgetRow(part));
                }
            }
        }

        private final List<NetworkPart> recieverParts = new ArrayList<>();
        private final List<NetworkPart> senderParts = new ArrayList<>();
        private InterplanetaryLogisticsNetwork network;

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            buffer.writeInt(recieverParts.size());
            for (var part: recieverParts) {
                buffer.writeNbt(part.save());
            }
            buffer.writeInt(senderParts.size());
            for (var part: senderParts) {
                buffer.writeNbt(part.save());
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void readInitialData(FriendlyByteBuf buffer) {
            var rCount = buffer.readInt();
            for (int i = 0; i < rCount; i++) {
                var nbt = buffer.readNbt();
                if (nbt != null) recieverParts.add(new NetworkPart(nbt));
            }
            var sCount = buffer.readInt();
            for (int i = 0; i < sCount; i++) {
                var nbt = buffer.readNbt();
                if (nbt != null) senderParts.add(new NetworkPart(nbt));
            }

            for (var part: senderParts) {
                senderTab.addWidget(createWidgetRow(part));
            }
            for (var part: recieverParts) {
                recieverTab.addWidget(createWidgetRow(part));
            }
        }

        private WidgetGroup createWidgetRow(NetworkPart part) {
            WidgetGroup rowGroup = new WidgetGroup(0, 0, 292, 0);
            rowGroup.setDynamicSized(true);

            // Upper part of row

            var partNameLabel = new TextFieldWidget(0, 0, 100, 16, part::getUiLabel, (s) -> {
                part.setUiLabel(s);
                updateIfServer();
            });
            rowGroup.addWidget(partNameLabel);

            rowGroup.addWidget(new LabelWidget(102, 3, part.getPartId().getUiString()));

            // Lower part of GUI

            if (!part.isRecieverPart()) {
                var configListContainer = new WidgetGroup(0, 18, 292, 20);
                configListContainer.setBackground(GuiTextures.BACKGROUND_INVERSE);
                rowGroup.addWidget(configListContainer);
                redrawWidgetConfigRows(part, configListContainer);
            }

            return rowGroup;
        }

        private void redrawWidgetConfigRows(NetworkPart part, WidgetGroup container) {
            container.clearAllWidgets();
            container.setSizeHeight(8);

            for (int i = 0; i < part.logisticsConfigurations.size(); i++) {
                var configEntry = part.logisticsConfigurations.get(i);
                var newWidget = createLogisticsConfigRow(part, configEntry);
                newWidget.setSelfPosition(4, 4 + 40*i);

                container.setSizeHeight(container.getSizeHeight() + 40);
                newWidget.addWidget(new ButtonWidget(272, 4, 12, 12, GuiTextures.BUTTON_INT_CIRCUIT_MINUS, (c) -> {
                    part.logisticsConfigurations.remove(configEntry);
                    updateIfServer();
                    redrawWidgetConfigRows(part, container);
                }));

                container.addWidget(newWidget);
            }

            container.setSizeHeight(container.getSizeHeight() + 12);
            var addNewConfigButton = new ButtonWidget(134, 4 + 40*part.logisticsConfigurations.size(), 12, 12, GuiTextures.BUTTON_INT_CIRCUIT_PLUS, (c) -> {
                part.logisticsConfigurations.add(new NetworkSenderConfigEntry(part.getPartId()));
                redrawWidgetConfigRows(part, container);
                updateIfServer();
            });
            container.addWidget(addNewConfigButton);
        }

        private WidgetGroup createLogisticsConfigRow(NetworkPart part, NetworkSenderConfigEntry config) {
            var group = new WidgetGroup(0,   0, 282, 40);
            group.addWidget(new LabelWidget(0, 6, "Destination:"));

            var selectorCandidates = new ArrayList<String>();
            Map<String, DimensionalBlockPos> candidateMap = new HashMap<>();
            for (NetworkPart p : recieverParts) {
                var label = Objects.equals(p.getUiLabel(), "[unnamed]") ? "[unnamed] %s".formatted(p.getPartId()) : p.getUiLabel();
                selectorCandidates.add(label);
                candidateMap.put(label, p.getPartId());
            }

            var destinationSelector = new SelectorWidget(57, 2, 80, 18, selectorCandidates, -1).setButtonBackground(GuiTextures.BUTTON);
            var currentValue = candidateMap.entrySet().stream().filter(p -> Objects.equals(p.getValue(), config.getRecieverPartID())).findFirst();
            currentValue.ifPresent(s -> destinationSelector.setValue(s.getKey()));

            destinationSelector.setOnChanged((v) -> {
                config.setRecieverPartID(candidateMap.get(v));
                updateIfServer();
            });

            group.addWidget(destinationSelector);

            var currentModeConfig = new WidgetGroup(150, 1, 100, 20);

            var inactivityIntInput = new IntInputWidget(20, 0, 100, 20, config::getCurrentInactivityTimeout, (v) -> {
                config.setCurrentInactivityTimeout(v);
                updateIfServer();
            });

            var itemFilterGroup = new WidgetGroup(Position.of(20, 1));
            itemFilterGroup.addWidget(new com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget(config.getCurrentSendFilter(), 0, 0, 0));
            itemFilterGroup.addWidget(new com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget(config.getCurrentSendFilter(), 1, 18, 0));
            itemFilterGroup.addWidget(new com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget(config.getCurrentSendFilter(), 2, 36, 0));

            currentModeConfig.addWidget(inactivityIntInput);
            currentModeConfig.addWidget(itemFilterGroup);
            inactivityIntInput.setVisible(config.getCurrentSendTrigger() == NetworkSenderConfigEntry.TriggerMode.INACTIVITY);
            itemFilterGroup.setVisible(config.getCurrentSendTrigger() == NetworkSenderConfigEntry.TriggerMode.ITEM);

            var modeSelector = new EnumSelectorWidget<>(0, 0, 20, 20, NetworkSenderConfigEntry.TriggerMode.values(), config.getCurrentSendTrigger(), (v) -> {
                config.setCurrentSendTrigger(v);
                inactivityIntInput.setVisible(config.getCurrentSendTrigger() == NetworkSenderConfigEntry.TriggerMode.INACTIVITY);
                itemFilterGroup.setVisible(config.getCurrentSendTrigger() == NetworkSenderConfigEntry.TriggerMode.ITEM);
                updateIfServer();
            });

            currentModeConfig.addWidget(modeSelector);
            group.addWidget(currentModeConfig);

            return group;
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var gui = new ModularUI(GUI_WIDTH, GUI_HEIGHT, this, entityPlayer);

        return gui.widget(new InterplanetaryLogisticsManagerWidget(entityPlayer));
    }
}
