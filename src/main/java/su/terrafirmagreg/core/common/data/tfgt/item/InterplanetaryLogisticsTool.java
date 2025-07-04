package su.terrafirmagreg.core.common.data.tfgt.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ProspectingMapWidget;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.layout.Align;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.TickTask;
import net.minecraft.world.entity.player.Player;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork.*;

import java.util.Objects;

public class InterplanetaryLogisticsTool implements IItemUIFactory, IInteractionItem {

    private static final int GUI_WIDTH = 300;
    private static final int GUI_HEIGHT = 200;
    private static final int INNER_WIDTH = GUI_WIDTH - 8;
    private static final int INNER_HEIGHT = GUI_HEIGHT - 8;

    private static class InterplanetaryLogisticsManagerWidget extends DraggableScrollableWidgetGroup {
        public InterplanetaryLogisticsManagerWidget() {
            super(4, 4, INNER_WIDTH, INNER_HEIGHT);
            setDraggable(false);
            setScrollable(true);
            setScrollWheelDirection(ScrollWheelDirection.VERTICAL);
            setYScrollBarWidth(4);
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            TFGCore.LOGGER.info("logisticsManagerGUI client: reading init data: {}", buffer);
            super.readInitialData(buffer);
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            TFGCore.LOGGER.info("logisticsManagerGUI client: reading gui update buffer {} {}", id, buffer);
            super.readUpdateInfo(id, buffer);
        }

    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        TFGCore.LOGGER.info("CreateUI, clientside: {}", holder.isRemote());
        var gui = new ModularUI(GUI_WIDTH, GUI_HEIGHT, holder, entityPlayer);
        var uiContainerWidget = new WidgetGroup(0, 0, GUI_WIDTH, GUI_HEIGHT);
        uiContainerWidget.setAlign(Align.CENTER).setBackground(GuiTextures.BACKGROUND);
        var scrollContainer = new InterplanetaryLogisticsManagerWidget();
        uiContainerWidget.addWidget(scrollContainer);
        gui.widget(uiContainerWidget);

        if (!holder.isRemote()) {
            Objects.requireNonNull(holder.getPlayer().getServer()).tell(new TickTask(0, () -> createUIDeferred(entityPlayer, scrollContainer)));
        }

        return gui;
    }

    private static void createUIDeferred(Player entityPlayer, WidgetGroup group) {
        var network = InterplanetaryLogisticsNetwork.get();
        TFGCore.LOGGER.info("ServersideGUI, got network: {}", network);
        var parts = network.getPartsVisibleToPlayer(entityPlayer);
        TFGCore.LOGGER.info("ServersideGUI, player has access to {} network parts", parts.size());
        int indx = 0;
        for (var part: parts) {
            var row = buildNetworkPartRow(network, part);
            row.setSelfPosition(0, indx*20);
            group.addWidget(row);
            indx++;
        }
    }

    private static WidgetGroup buildNetworkPartRow(InterplanetaryLogisticsNetwork network, NetworkPart part) {
        var group = new WidgetGroup(0, 0, 200, 50);
        var partIdlabel = new LabelWidget();
        partIdlabel.setSelfPosition(2, 2);
        partIdlabel.setText(part.getPartId().toString());

        group.addWidget(partIdlabel);
        var partNameLabel = new TextFieldWidget(2, 12, 70, 15, part::getUiLabel, (String lbl) -> {
            part.setUiLabel(lbl);
            network.markDirty();
        });
        partNameLabel.setBackground(GuiTextures.BUTTON);
        group.addWidget(partNameLabel);
        return group;
    }



}
