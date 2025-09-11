package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import java.util.*;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.GhostCircuitSlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.gui.widget.layout.Align;
import com.lowdragmc.lowdraglib.gui.widget.layout.Layout;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork.*;

public class InterplanetaryLogisticsMonitorMachine extends MetaMachine implements IUIMachine {
    public InterplanetaryLogisticsMonitorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    private static final int GUI_WIDTH = 300;
    private static final int GUI_HEIGHT = 300;
    private static final int INNER_WIDTH = GUI_WIDTH - 8;
    private static final int INNER_HEIGHT = GUI_HEIGHT - 8;

    private class InterplanetaryLogisticsManagerWidget extends WidgetGroup {

        private final List<NetworkPart> parts;
        private InterplanetaryLogisticsNetwork network;

        private void updateIfServer() {
            if (network == null)
                return;
            network.markDirty();
        }

        public InterplanetaryLogisticsManagerWidget(Player accessor) {
            super(0, 0, GUI_WIDTH, GUI_HEIGHT);

            if (!InterplanetaryLogisticsMonitorMachine.this.isRemote()) {
                network = InterplanetaryLogisticsNetwork.get(getHolder());
                parts = network.getPartsVisibleToPlayer(accessor);
                createUI();
            } else {
                parts = new ArrayList<>();
            }
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            buffer.writeInt(parts.size());
            for (var part : parts) {
                buffer.writeNbt(part.save());
            }
        }

        private DraggableScrollableWidgetGroup getDragContainer() {
            return new DraggableScrollableWidgetGroup(4, 4, INNER_WIDTH, INNER_HEIGHT).setDraggable(false)
                    .setScrollable(true)
                    .setScrollWheelDirection(DraggableScrollableWidgetGroup.ScrollWheelDirection.VERTICAL)
                    .setYScrollBarWidth(4)
                    .setYBarStyle(ColorPattern.RED.rectTexture(), ColorPattern.WHITE.rectTexture().setRadius(2));
        }

        private final DraggableScrollableWidgetGroup mainPage = getDragContainer();

        @Override
        @OnlyIn(Dist.CLIENT)
        public void readInitialData(FriendlyByteBuf buffer) {
            var count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                var nbt = buffer.readNbt();
                if (nbt == null)
                    continue;

                var part = new NetworkPart(nbt);
                parts.add(part);
            }
            createUI();
        }

        private void createUI() {
            var container = new WidgetGroup(0, 0, INNER_WIDTH, 0);
            container.setLayout(Layout.VERTICAL_CENTER);
            container.setDynamicSized(true);

            setAlign(Align.CENTER);
            setBackground(ResourceBorderTexture.BORDERED_BACKGROUND);
            addWidget(mainPage);
            List<WidgetGroup> senderPages = new ArrayList<>();
            List<WidgetGroup> receiverPages = new ArrayList<>();
            for (var part : parts) {
                if (part.getUiLabel().isBlank())
                    part.setUiLabel("[unnamed]");

                WidgetGroup rowGroup = new WidgetGroup(0, 0, INNER_WIDTH - 5, 22);
                rowGroup.setLayout(Layout.HORIZONTAL_CENTER);
                rowGroup.setLayoutPadding(2);
                rowGroup.addWidget(new TextFieldWidget(0, 0, 80, 16, part::getUiLabel, (s) -> {
                    part.setUiLabel(s);
                    updateIfServer();
                }));

                var lbl = new LabelWidget(0, 0, part.getPartId().getUiString());
                rowGroup.addWidget(lbl);

                rowGroup.addWidget(new ButtonWidget(0, 0, 16, 16, GuiTextures.BUTTON_RIGHT, (v) -> {
                    addWidget(part.isReceiverPart() ? createReceiverPartStatusPage(part)
                            : createSenderPartStatusPage(part));
                    mainPage.setVisible(false);
                }));

                if (part.isReceiverPart())
                    receiverPages.add(rowGroup);
                else
                    senderPages.add(rowGroup);
            }

            container.addWidget(new LabelWidget(0, 0, "Senders"));
            for (var page : senderPages) {
                container.addWidget(page);
            }
            container.addWidget(new LabelWidget(0, 0, "Receivers"));
            for (var page : receiverPages) {
                container.addWidget(page);
            }

            mainPage.addWidget(container);
        }

        private DraggableScrollableWidgetGroup createSenderPartStatusPage(NetworkPart part) {
            DraggableScrollableWidgetGroup dragContainer = getDragContainer();
            dragContainer.setId(part.getPartId().getUiString());
            var group = new WidgetGroup(0, 0, 0, 0);
            group.setLayout(Layout.VERTICAL_CENTER);
            group.setDynamicSized(true);

            var topRow = new WidgetGroup(0, 0, INNER_WIDTH - 5, 20)
                    .addWidget(new ButtonWidget(2, 2, 16, 16, GuiTextures.BUTTON_LEFT, (v) -> {
                        removeWidget(dragContainer);
                        mainPage.setVisible(true);
                    })).addWidget(new TextFieldWidget(18, 2, 80, 16, part::getUiLabel, (s) -> {
                        part.setUiLabel(s);
                        updateIfServer();
                    })).addWidget(new LabelWidget(102, 5, () -> part.getPartId().getUiString()));

            var configListContainer = new WidgetGroup(0, 0, INNER_WIDTH - 5, 20);
            configListContainer.setDynamicSized(true);
            configListContainer.setLayout(Layout.VERTICAL_CENTER);
            configListContainer.setBackground(GuiTextures.BACKGROUND_INVERSE);

            for (var config : part.senderLogisticsConfigs) {
                configListContainer.addWidget(createSenderLogisticsConfigRow(part, config));
            }

            configListContainer.addWidget(new ButtonWidget(0, 0, 12, 12, GuiTextures.BUTTON_INT_CIRCUIT_PLUS, (c) -> {
                WidgetGroup containerPadding = new WidgetGroup(0, 0, INNER_WIDTH - 13, 46);
                containerPadding.setDynamicSized(true);
                var newEntry = new NetworkSenderConfigEntry(part.getPartId());
                part.senderLogisticsConfigs.add(newEntry);
                updateIfServer();
                configListContainer.addWidget(configListContainer.widgets.size() - 1,
                        createSenderLogisticsConfigRow(part, newEntry));
            }));

            group.addWidget(topRow).addWidget(configListContainer);
            dragContainer.addWidget(group);

            return dragContainer;
        }

        private WidgetGroup createSenderLogisticsConfigRow(NetworkPart part, NetworkSenderConfigEntry config) {
            var group = new WidgetGroup(4, 4, INNER_WIDTH - 13, 50);
            group.setId("sendConfigRow");

            group.addWidget(new ButtonWidget(262, 19, 12, 12, GuiTextures.BUTTON_INT_CIRCUIT_MINUS, (c) -> {
                part.senderLogisticsConfigs.remove(config);
                updateIfServer();
                group.getParent().removeWidget(group);
            }));

            group.addWidget(new LabelWidget(0, 6, "Destination:"));

            var destinationSelector = new SelectorWidget(57, 2, 80, 18, new ArrayList<>(), -1)
                    .setButtonBackground(GuiTextures.BUTTON);
            if (!isRemote())
                destinationSelector.setCandidatesSupplier(() -> parts.stream()
                        .filter((p) -> p.isReceiverPart() && !Objects.equals(p.getUiLabel(), "[unnamed]"))
                        .map(NetworkPart::getUiLabel).toList());
            destinationSelector.setValue("[none]");
            for (var rPart : parts) {
                if (rPart.getPartId() == config.getReceiverPartID())
                    destinationSelector.setValue(rPart.getUiLabel());
            }

            destinationSelector.setOnChanged((v) -> {
                parts.stream().filter(p -> Objects.equals(p.getUiLabel(), v)).findFirst()
                        .ifPresent(s -> config.setReceiverPartID(s.getPartId()));
                updateIfServer();
            });

            group.addWidget(destinationSelector);

            var inactivityIntInput = new IntInputWidget(160, 1, 100, 20, config::getCurrentInactivityTimeout, (v) -> {
                config.setCurrentInactivityTimeout(v);
                updateIfServer();
            });

            var itemFilterGroup = new WidgetGroup(Position.of(160, 2))
                    .addWidget(new PhantomSlotWidget(config.getCurrentSendFilter(), 0, 0, 0)
                            .setChangeListener(this::updateIfServer))
                    .addWidget(new PhantomSlotWidget(config.getCurrentSendFilter(), 1, 18, 0)
                            .setChangeListener(this::updateIfServer))
                    .addWidget(new PhantomSlotWidget(config.getCurrentSendFilter(), 2, 36, 0)
                            .setChangeListener((this::updateIfServer)));

            inactivityIntInput
                    .setVisible(config.getCurrentSendTrigger() == NetworkSenderConfigEntry.TriggerMode.INACTIVITY);
            itemFilterGroup.setVisible(config.getCurrentSendTrigger() == NetworkSenderConfigEntry.TriggerMode.ITEM);

            var modeSelector = new EnumSelectorWidget<>(140, 1, 20, 20, NetworkSenderConfigEntry.TriggerMode.values(),
                    config.getCurrentSendTrigger(), (v) -> {
                        config.setCurrentSendTrigger(v);
                        inactivityIntInput.setVisible(
                                config.getCurrentSendTrigger() == NetworkSenderConfigEntry.TriggerMode.INACTIVITY);
                        itemFilterGroup.setVisible(
                                config.getCurrentSendTrigger() == NetworkSenderConfigEntry.TriggerMode.ITEM);
                        updateIfServer();
                    });

            group.addWidget(inactivityIntInput).addWidget(itemFilterGroup).addWidget(modeSelector);

            group.addWidget(new LabelWidget(0, 27, "Sender:"));

            var senderCircuitInv = new CustomItemStackHandler(1);
            var receiverCircuitInv = new CustomItemStackHandler(1);

            var senderDistinctCircuit = new GhostCircuitSlotWidget();
            senderDistinctCircuit.setBackground(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY).setSelfPosition(40,
                    25);
            senderDistinctCircuit.setCircuitInventory(senderCircuitInv);
            senderDistinctCircuit.setCircuitValue(config.getSenderDistinctInventory());
            senderCircuitInv.setOnContentsChanged(() -> {
                config.setSenderDistinctInventory(
                        IntCircuitBehaviour.getCircuitConfiguration(senderCircuitInv.getStackInSlot(0)));
                updateIfServer();

            });

            group.addWidget(new LabelWidget(65, 27, "Receiver:"));

            var receiverDistinctCircuit = new GhostCircuitSlotWidget();
            receiverDistinctCircuit.setBackground(GuiTextures.SLOT, GuiTextures.INT_CIRCUIT_OVERLAY)
                    .setSelfPosition(115, 25);
            receiverDistinctCircuit.setCircuitInventory(receiverCircuitInv);
            receiverDistinctCircuit.setCircuitValue(config.getReceiverDistinctInventory());
            receiverCircuitInv.setOnContentsChanged(() -> {
                config.setReceiverDistinctInventory(
                        IntCircuitBehaviour.getCircuitConfiguration(receiverCircuitInv.getStackInSlot(0)));
                updateIfServer();

            });

            group.addWidget(senderDistinctCircuit).addWidget(receiverDistinctCircuit);

            WidgetGroup containerPadding = new WidgetGroup(0, 0, INNER_WIDTH - 13, 46);
            containerPadding.setDynamicSized(true);
            return containerPadding.addWidget(group);
        }

        private DraggableScrollableWidgetGroup createReceiverPartStatusPage(NetworkPart part) {
            DraggableScrollableWidgetGroup dragContainer = getDragContainer();
            dragContainer.setId(part.getPartId().getUiString());
            var group = new WidgetGroup(0, 0, 0, 0);
            group.setLayout(Layout.VERTICAL_CENTER);
            group.setDynamicSized(true);

            var topRow = new WidgetGroup(0, 0, INNER_WIDTH - 5, 20)
                    .addWidget(new ButtonWidget(2, 2, 16, 16, GuiTextures.BUTTON_LEFT, (v) -> {
                        dragContainer.setVisible(false);
                        mainPage.setVisible(true);
                    }))
                    .addWidget(new TextFieldWidget(18, 2, 80, 16, part::getUiLabel, (s) -> {
                        part.setUiLabel(s);
                        updateIfServer();
                    }))
                    .addWidget(new LabelWidget(102, 5, part.getPartId().getUiString()));

            var configListContainer = new WidgetGroup(0, 0, INNER_WIDTH - 5, 20);
            configListContainer.setDynamicSized(true);
            configListContainer.setLayout(Layout.VERTICAL_CENTER);
            configListContainer.setBackground(GuiTextures.BACKGROUND_INVERSE);

            for (var config : part.receiverLogisticsConfigs) {
                var logisticsConfigGroup = new WidgetGroup(4, 4, INNER_WIDTH - 13, 30);

                GuiTextureGroup circuitTexture;
                if (config.getDistinctInventory() == 0)
                    circuitTexture = new GuiTextureGroup(GuiTextures.SLOT,
                            new ItemStackTexture(IntCircuitBehaviour.stack(0)), new ItemStackTexture(Items.BARRIER));
                else
                    circuitTexture = new GuiTextureGroup(GuiTextures.SLOT,
                            new ItemStackTexture(IntCircuitBehaviour.stack(config.getDistinctInventory())));
                var circuitWidget = new ImageWidget(6, 6, 18, 18, circuitTexture);

                var cooldownIntInput = new IntInputWidget(160, 5, 100, 20, config::getCurrentCooldown, (v) -> {
                    config.setCurrentCooldown(v);
                    updateIfServer();

                });
                cooldownIntInput.setMin(0).setMax(1800);
                cooldownIntInput.setVisible(config.getCurrentMode() == NetworkReceiverConfigEntry.LogicMode.COOLDOWN);

                var modeSelector = new EnumSelectorWidget<>(140, 5, 20, 20,
                        NetworkReceiverConfigEntry.LogicMode.values(), config.getCurrentMode(), (v) -> {
                            config.setCurrentMode(v);
                            cooldownIntInput.setVisible(v == NetworkReceiverConfigEntry.LogicMode.COOLDOWN);
                            updateIfServer();
                        });

                logisticsConfigGroup.addWidget(circuitWidget).addWidget(modeSelector).addWidget(cooldownIntInput);
                configListContainer.addWidget(logisticsConfigGroup);
            }

            group.addWidget(topRow).addWidget(configListContainer);
            dragContainer.addWidget(group);
            return dragContainer;
        }
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var gui = new ModularUI(GUI_WIDTH, GUI_HEIGHT, this, entityPlayer);
        return gui.widget(new InterplanetaryLogisticsManagerWidget(entityPlayer));
    }
}
