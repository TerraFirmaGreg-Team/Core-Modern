package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.layout.Align;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork.*;

import java.util.List;

public class InterplanetaryLogisticsMonitorMachine extends MetaMachine implements IUIMachine {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(InterplanetaryLogisticsMonitorMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private static final int GUI_WIDTH = 300;
    private static final int GUI_HEIGHT = 200;
    private static final int INNER_WIDTH = GUI_WIDTH - 8;
    private static final int INNER_HEIGHT = GUI_HEIGHT - 8;

    public InterplanetaryLogisticsMonitorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {


        var gui = new ModularUI(GUI_WIDTH, GUI_HEIGHT, this, entityPlayer);
        var uiContainerWidget = new WidgetGroup(0, 0, GUI_WIDTH, GUI_HEIGHT);
        uiContainerWidget.setAlign(Align.CENTER).setBackground(GuiTextures.BACKGROUND);

        var scrollContainer = new DraggableScrollableWidgetGroup(4, 4, INNER_WIDTH, INNER_HEIGHT);
        scrollContainer.setDraggable(false).setScrollable(true).setScrollWheelDirection(DraggableScrollableWidgetGroup.ScrollWheelDirection.VERTICAL).setYScrollBarWidth(4);

        uiContainerWidget.addWidget(scrollContainer);
        gui.widget(uiContainerWidget);
        return gui;
    }

    private WidgetGroup buildNetworkPartRow(ILogisticsNetworkMachine part) {
        var group = new WidgetGroup(0, 0, 200, 50);
        var partIdlabel = new LabelWidget();
        partIdlabel.setSelfPosition(2, 2);
        partIdlabel.setText(part.getDimensionalPos().toString());

        group.addWidget(partIdlabel);
        var partNameLabel = new TextFieldWidget(2, 12, 70, 15, part::getLogisticsUILabel, part::setLogisticsUILabel);
        partNameLabel.setBackground(GuiTextures.BUTTON);
        group.addWidget(partNameLabel);
        return group;
    }

}
