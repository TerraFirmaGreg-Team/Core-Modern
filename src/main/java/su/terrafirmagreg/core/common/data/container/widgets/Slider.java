package su.terrafirmagreg.core.common.data.container.widgets;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * A custom slider widget for GUI's.
 * This slider supports a background texture, a handle texture, and a range of values.
 */
public final class Slider extends AbstractSliderButton {
    private final ResourceLocation bgTex;
    private final int bgTexW;
    private final int bgTexH;
    private final ResourceLocation handleTex;
    private final int handleW;
    private final int handleH;
    private final int min;
    private final int max;
    private final IntSupplier valueSupplier;
    private final IntConsumer onReleased;

    /**
     * Constructs a new Slider instance.
     *
     * @param x              The x-coordinate of the slider.
     * @param y              The y-coordinate of the slider.
     * @param width          The width of the slider.
     * @param height         The height of the slider.
     * @param backgroundTex  The texture for the slider background.
     * @param bgTexWidth     The width of the background texture.
     * @param bgTexHeight    The height of the background texture.
     * @param handleTex      The texture for the slider handle.
     * @param handleWidth    The width of the slider handle.
     * @param handleHeight   The height of the slider handle.
     * @param min            The minimum value of the slider.
     * @param max            The maximum value of the slider.
     * @param valueSupplier  A supplier for the current value of the slider.
     * @param onReleased     A consumer to handle actions when the slider is released.
     */
    public Slider(
            int x, int y, int width, int height,
            ResourceLocation backgroundTex, int bgTexWidth, int bgTexHeight,
            ResourceLocation handleTex, int handleWidth, int handleHeight,
            int min, int max,
            IntSupplier valueSupplier,
            IntConsumer onReleased) {
        super(x, y, width, height, Component.empty(), 0.0D);
        this.bgTex = backgroundTex;
        this.bgTexW = bgTexWidth;
        this.bgTexH = bgTexHeight;
        this.handleTex = handleTex;
        this.handleW = handleWidth;
        this.handleH = handleHeight;
        this.min = min;
        this.max = max;
        this.valueSupplier = valueSupplier;
        this.onReleased = onReleased;
        int supplied = valueSupplier != null ? valueSupplier.getAsInt() : min;
        this.value = toSliderValue(supplied);
        this.updateMessage();
    }

    /**
     * Converts the slider's current value to an integer within the defined range.
     *
     * @return The integer value of the slider.
     */
    private int getIntValue() {
        int range = max - min;
        return Mth.clamp(min + (int) Math.round(this.value * range), min, max);
    }

    /**
     * Converts an integer value to a normalized slider value.
     *
     * @param v The integer value to convert.
     * @return The normalized slider value.
     */
    private double toSliderValue(int v) {
        int range = max - min;
        if (range == 0)
            return 0.0D;
        return (double) (Mth.clamp(v, min, max) - min) / (double) range;
    }

    /**
     * Retrieves the current display value of the slider.
     *
     * @return The integer value displayed by the slider.
     */
    public int getDisplayValue() {
        return getIntValue();
    }

    /**
     * Sets an empty message.
     */
    @Override
    protected void updateMessage() {
        this.setMessage(Component.empty());
    }

    /**
     * Applies the current slider value. This does nothing here :)
     */
    @Override
    protected void applyValue() {
    }

    /**
     * Handles mouse click events on the slider.
     *
     * @param mouseX The x-coordinate of the mouse.
     * @param mouseY The y-coordinate of the mouse.
     */
    @Override
    public void onClick(double mouseX, double mouseY) {
        setValueFromMouse(mouseX);
    }

    /**
     * Handles mouse drag events on the slider.
     * This does not work very well and kinda needs improvement.
     *
     * @param mouseX The x-coordinate of the mouse.
     * @param mouseY The y-coordinate of the mouse.
     * @param dragX  The x-drag distance.
     * @param dragY  The y-drag distance.
     */
    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        setValueFromMouse(mouseX);
        super.onDrag(mouseX, mouseY, dragX, dragY);
    }

    /**
     * Sets the slider value based on the mouse's x-coordinate.
     *
     * @param mouseX The x-coordinate of the mouse.
     */
    private void setValueFromMouse(double mouseX) {
        this.value = Mth.clamp((mouseX - (double) (this.getX() + 4)) / (double) (this.width - 8), 0.0D, 1.0D);
        this.updateMessage();
    }

    /**
     * Handles mouse release events on the slider.
     *
     * @param mouseX The x-coordinate of the mouse.
     * @param mouseY The y-coordinate of the mouse.
     */
    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        if (this.onReleased != null) {
            this.onReleased.accept(getIntValue());
        }
    }

    /**
     * Renders the slider widget, including its background and handle.
     *
     * @param gg          The graphics context for rendering.
     * @param mouseX      The x-coordinate of the mouse.
     * @param mouseY      The y-coordinate of the mouse.
     * @param partialTick The partial tick time.
     */
    @Override
    public void renderWidget(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        if (!this.active) {
            int v = valueSupplier != null ? valueSupplier.getAsInt() : getIntValue();
            double newValue = toSliderValue(v);
            if (Double.compare(this.value, newValue) != 0) {
                this.value = newValue;
            }
        }
        final int drawBgW = Math.min(this.width, this.bgTexW);
        final int drawBgH = Math.min(this.height, this.bgTexH);
        gg.blit(this.bgTex, this.getX(), this.getY(), 0, 0, drawBgW, drawBgH, this.bgTexW, this.bgTexH);
        final int travel = Math.max(0, this.width - this.handleW);
        int handleTravel = this.width - 8;
        int handleX = this.getX() + 4 + Mth.clamp((int) Math.round(this.value * (handleTravel - this.handleW)), 0, handleTravel - this.handleW);
        int handleY = this.getY() + Math.max(0, (this.height - this.handleH) / 2);

        gg.blit(this.handleTex, handleX, handleY, 0, 0, this.handleW, this.handleH, this.handleW, this.handleH);

        if (this.active && this.isMouseOver(mouseX, mouseY)) {
            gg.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
        }
        if (!this.active) {
            gg.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x80000000);
        }
    }
}
