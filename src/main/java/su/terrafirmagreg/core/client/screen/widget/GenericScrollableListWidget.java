package su.terrafirmagreg.core.client.screen.widget;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.resources.ResourceLocation;

import lombok.Setter;

/**
 * Generic scrollable list widget.
 */
@Setter
public class GenericScrollableListWidget<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {

    protected int x;

    protected ResourceLocation scrollbarBackgroundTexture;
    protected int scrollbarBackgroundWidth;
    protected ResourceLocation scrollbarGrabberTexture;
    protected int scrollbarGrabberWidth;

    public void setScrollbarBackgroundTexture(ResourceLocation texture, int width) {
        this.scrollbarBackgroundTexture = texture;
        this.scrollbarBackgroundWidth = width;
    }

    public void setScrollbarGrabberTexture(ResourceLocation texture, int width) {
        this.scrollbarGrabberTexture = texture;
        this.scrollbarGrabberWidth = width;
    }

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

        int scrollbarWidth = scrollbarBackgroundTexture != null ? scrollbarBackgroundWidth : 6;
        int scrollbarLeft = this.getScrollbarPosition();
        int scrollbarRight = scrollbarLeft + scrollbarWidth;

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

            if (scrollbarBackgroundTexture != null) {
                int totalHeight = this.y1 - this.y0;
                // 1st row.
                graphics.blit(scrollbarBackgroundTexture, scrollbarLeft, this.y0, scrollbarWidth, 1, 0, 0, scrollbarWidth, 1, scrollbarWidth, 3);
                // 2nd row stretched.
                if (totalHeight > 2) {
                    graphics.blit(scrollbarBackgroundTexture, scrollbarLeft, this.y0 + 1, scrollbarWidth, totalHeight - 2, 0, 1, scrollbarWidth, 1, scrollbarWidth, 3);
                }
                // 3rd row.
                if (totalHeight > 1) {
                    graphics.blit(scrollbarBackgroundTexture, scrollbarLeft, this.y1 - 1, scrollbarWidth, 1, 0, 2, scrollbarWidth, 1, scrollbarWidth, 3);
                }
            } else {
                graphics.fill(scrollbarLeft, this.y0, scrollbarRight, this.y1, -16777216);
            }

            if (scrollbarGrabberTexture != null) {
                int grabberWidth = scrollbarGrabberWidth > 0 ? scrollbarGrabberWidth : scrollbarWidth;
                // 1st row.
                graphics.blit(scrollbarGrabberTexture, scrollbarLeft, handleTop, grabberWidth, 1, 0, 0, grabberWidth, 1, grabberWidth, 3);
                // 2nd row stretched.
                graphics.blit(scrollbarGrabberTexture, scrollbarLeft, handleTop + 1, grabberWidth, handleHeight - 2, 0, 1, grabberWidth, 1, grabberWidth, 3);
                // 3rd row.
                graphics.blit(scrollbarGrabberTexture, scrollbarLeft, handleTop + handleHeight - 1, grabberWidth, 1, 0, 2, grabberWidth, 1, grabberWidth, 3);
            } else {
                graphics.fill(scrollbarLeft, handleTop, scrollbarRight, handleTop + handleHeight, -8355712);
                graphics.fill(scrollbarLeft, handleTop, scrollbarRight - 1, handleTop + handleHeight - 1, -4144960);
            }
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
