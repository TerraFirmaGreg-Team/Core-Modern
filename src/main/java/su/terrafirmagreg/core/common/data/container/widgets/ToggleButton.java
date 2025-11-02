package su.terrafirmagreg.core.common.data.container.widgets;

import java.util.function.BooleanSupplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A custom toggle button widget for GUI's.
 * This button uses textures to visually represent its "on" and "off" states.
 */
public final class ToggleButton extends Button {
    private final ResourceLocation texture;
    private final int texWidth;
    private final int texHeight;
    private final BooleanSupplier isOnSupplier;

    /**
     * Constructs a new ToggleButton instance.
     *
     * @param x           The x-coordinate of the button.
     * @param y           The y-coordinate of the button.
     * @param width       The width of the button.
     * @param height      The height of the button.
     * @param texture     The texture resource for the button.
     * @param texWidth    The width of the texture.
     * @param texHeight   The height of the texture.
     * @param isOn        A supplier to determine the current "on" state of the button.
     * @param onPress     The action to perform when the button is pressed.
     */
    public ToggleButton(
            int x, int y, int width, int height,
            ResourceLocation texture, int texWidth, int texHeight,
            BooleanSupplier isOn,
            Button.OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.isOnSupplier = isOn;
    }

    /**
     * Renders the toggle button widget, including its texture and hover/disabled effects.
     *
     * @param gg     The graphics context for rendering.
     * @param mouseX The x-coordinate of the mouse.
     * @param mouseY The y-coordinate of the mouse.
     * @param delta  The partial tick time.
     */
    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float delta) {
        final boolean on = this.isOnSupplier != null && this.isOnSupplier.getAsBoolean();
        // on/off occupies half the texture width. So create the texture with the two states side by side.
        final int frameW = this.texWidth / 2;
        final int frameH = this.texHeight;
        final int drawW = Math.min(this.width, frameW);
        final int drawH = Math.min(this.height, frameH);

        final int u = on ? frameW : 0;
        final int v = 0;

        // Draw the button texture.
        gg.blit(this.texture, this.getX(), this.getY(), u, v, drawW, drawH, this.texWidth, this.texHeight);

        // Draw a hover effect if the button is active and the mouse is over it.
        if (this.active && this.isMouseOver(mouseX, mouseY)) {
            gg.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
        }

        // Draw a disabled overlay if the button is not active.
        if (!this.active) {
            gg.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x80000000);
        }
    }
}
