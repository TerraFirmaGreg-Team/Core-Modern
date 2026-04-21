package su.terrafirmagreg.core.client.screen.widget;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;

import lombok.Setter;

/**
 * Generic scrollable list widget.
 */
@Setter
public class GenericScrollableListWidget<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {

    protected int x;

    /**
     * Constructor.
     * @param minecraft Minecraft instance.
     * @param width Width of the list widget.
     * @param height Height of the list widget.
     * @param top Top position of the list widget.
     * @param bottom Bottom position of the list widget.
     * @param itemHeight Height of each list item.
     */
    public GenericScrollableListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraft, width, height, top, bottom, itemHeight);
        this.setRenderBackground(false);
        this.setRenderHeader(false, 0);
    }

    @Override
    public void setLeftPos(int left) {
        super.setLeftPos(left);
        this.x = left;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        beforeRenderList(graphics, mouseX, mouseY, partialTick);

        int scrollbarLeft = this.getScrollbarPosition();
        int scrollbarRight = scrollbarLeft + 6;

        graphics.enableScissor(this.x, this.y0, scrollbarRight, this.y1);
        this.renderList(graphics, mouseX, mouseY, partialTick);
        graphics.disableScissor();

        afterRenderList(graphics, mouseX, mouseY, partialTick);

        if (this.getMaxScroll() > 0) {
            int handleHeight = (int) ((float) ((this.y1 - this.y0) * (this.y1 - this.y0)) / (float) this.getMaxPosition());
            handleHeight = Math.max(32, handleHeight);
            int handleTop = (int) this.getScrollAmount() * (this.y1 - this.y0 - handleHeight) / this.getMaxScroll() + this.y0;
            if (handleTop < this.y0) {
                handleTop = this.y0;
            }

            graphics.fill(scrollbarLeft, this.y0, scrollbarRight, this.y1, -16777216);
            graphics.fill(scrollbarLeft, handleTop, scrollbarRight, handleTop + handleHeight, -8355712);
            graphics.fill(scrollbarLeft, handleTop, scrollbarRight - 1, handleTop + handleHeight - 1, -4144960);
        }

        this.renderDecorations(graphics, mouseX, mouseY);
    }

    protected void beforeRenderList(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    protected void afterRenderList(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void renderBackground(@NotNull GuiGraphics graphics) {
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x + this.width;
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    public int getRowLeft() {
        return this.x;
    }
}
