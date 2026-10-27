package su.terrafirmagreg.core.compat.emi.widgets;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;

/**
 * A widget that cycles through a list of widgets.
 */
public class CyclingWidget extends Widget {
    private final List<Widget> widgets;
    private final Bounds bounds;

    public CyclingWidget(Bounds bounds, Widget... widgets) {
        this.bounds = bounds;
        this.widgets = List.of(widgets);
    }

    @Override
    public Bounds getBounds() {
        return this.bounds;
    }

    private Widget getActiveWidget() {
        if (widgets.isEmpty()) {
            return null;
        }
        int index = (int) ((System.currentTimeMillis() / 1000) % widgets.size());
        return widgets.get(index);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        Widget active = getActiveWidget();
        if (active != null) {
            active.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        Widget active = getActiveWidget();
        return active != null ? active.getTooltip(mouseX, mouseY) : List.of();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        Widget active = getActiveWidget();
        return active != null && active.mouseClicked(mouseX, mouseY, button);
    }
}
