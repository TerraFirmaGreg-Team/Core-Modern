package su.terrafirmagreg.core.client.screen.widget;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import su.terrafirmagreg.core.client.util.TFGTooltipUtils;

/**
 * Simple value display scrollable list for compact text rows with optional tooltip support.
 */
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class ValueDisplayListWidget extends GenericScrollableListWidget<ValueDisplayListWidget.ValueDisplayEntry> {

    private final Font font;

    @Nullable
    private List<Component> hoveredTooltip;
    private int tooltipX;
    private int tooltipY;

    /**
     * List widget for displaying text rows with optional tooltips.
     * @param minecraft Minecraft instance.
     * @param font Font to use for rendering.
     * @param width The width of the list widget.
     * @param height The height of the list widget.
     * @param top The top position of the list widget.
     * @param bottom The bottom position of the list widget.
     * @param itemHeight The height of each item in the list.
     */
    public ValueDisplayListWidget(Minecraft minecraft, Font font, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraft, width, height, top, bottom, itemHeight);
        this.font = font;
    }

    public ValueDisplayListWidget addValue(Component label, @Nullable Component tooltip) {
        this.addEntry(new ValueDisplayEntry(label, TFGTooltipUtils.normalize(tooltip)));
        return this;
    }

    public ValueDisplayListWidget addValue(Component label, @Nullable List<Component> tooltip) {
        this.addEntry(new ValueDisplayEntry(label, TFGTooltipUtils.normalize(tooltip)));
        return this;
    }

    public void clearValues() {
        this.clearEntries();
    }

    @Override
    protected void beforeRenderList(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.hoveredTooltip = null;
    }

    public void renderTooltip(GuiGraphics graphics) {
        if (this.hoveredTooltip != null && !this.hoveredTooltip.isEmpty()) {
            graphics.renderComponentTooltip(this.font, this.hoveredTooltip, this.tooltipX, this.tooltipY);
        }
    }

    public class ValueDisplayEntry extends ObjectSelectionList.Entry<ValueDisplayEntry> {

        private final Component label;
        @Nullable
        private final List<Component> tooltip;

        public ValueDisplayEntry(Component label, @Nullable List<Component> tooltip) {
            this.label = label;
            this.tooltip = tooltip;
        }

        /**
         * Renders the List Widget Entry.
         * @param graphics The GuiGraphics object.
         * @param index The index of the entry.
         * @param top The top position of the entry.
         * @param left The left position of the entry.
         * @param width The width of the entry.
         * @param height The height of the entry.
         * @param mouseX The mouse X position.
         * @param mouseY The mouse Y position.
         * @param isHovered Whether the entry is hovered.
         * @param partialTick The partial tick.
         */
        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            final int textY = top + (height - font.lineHeight) / 2;
            graphics.drawString(font, this.label, left + 2, textY, 0xFFFFFF, true);

            final boolean rowHovered = mouseX >= left && mouseX <= left + width
                    && mouseY >= top && mouseY <= top + height
                    && mouseY >= ValueDisplayListWidget.this.y0 && mouseY <= ValueDisplayListWidget.this.y1;

            if (rowHovered && this.tooltip != null && !this.tooltip.isEmpty()) {
                if (Screen.hasShiftDown()) {
                    ValueDisplayListWidget.this.hoveredTooltip = this.tooltip;
                    ValueDisplayListWidget.this.tooltipX = mouseX;
                    ValueDisplayListWidget.this.tooltipY = mouseY;
                }
            }
        }

        @Override
        public @NotNull Component getNarration() {
            return this.label;
        }
    }
}
