package su.terrafirmagreg.core.common.data.container.widgets;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class TexturedIntSlider extends AbstractSliderButton {
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

    public TexturedIntSlider(
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
        this.value = toNorm(snapToStep(valueSupplier.getAsInt()));
        this.updateMessage();
    }

    private int steps() {
        return this.max - this.min;
    }

    public int getDisplayValue() {
        return toValue(this.value);
    }

    private int snapToStep(int v) {
        return Mth.clamp(v, this.min, this.max);
    }

    private double toNorm(int v) {
        int clamped = snapToStep(v);
        int range = steps();
        return (double) (clamped - this.min) / (double) range;
    }

    private int toValue(double norm) {
        int range = steps();
        int nearest = (int) Math.round(norm * range);
        return Mth.clamp(this.min + nearest, this.min, this.max);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.empty());
    }

    @Override
    protected void applyValue() {
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        double norm = (mouseX - (double) (this.getX())) / (double) (this.width - 1);
        norm = Mth.clamp(norm, 0.0D, 1.0D);

        int steps = steps();
        int nearest = (int) Math.round(norm * steps);
        this.value = (double) nearest / (double) steps;

        this.updateMessage();

        if (this.onReleased != null) {
            this.onReleased.accept(toValue(this.value));
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        if (this.onReleased != null) {
            this.onReleased.accept(toValue(this.value));
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        if (!this.isFocused()) {
            int v = this.valueSupplier != null ? snapToStep(this.valueSupplier.getAsInt()) : toValue(this.value);
            double snappedNorm = toNorm(v);
            this.value = snappedNorm;
        }
        final int drawBgW = Math.min(this.width, this.bgTexW);
        final int drawBgH = Math.min(this.height, this.bgTexH);
        gg.blit(this.bgTex, this.getX(), this.getY(), 0, 0, drawBgW, drawBgH, this.bgTexW, this.bgTexH);
        final int travel = Math.max(0, this.width - this.handleW);
        final int handleX = this.getX() + Mth.clamp((int) Math.round(this.value * travel), 0, travel);
        final int handleY = this.getY() + Math.max(0, (this.height - this.handleH) / 2);

        gg.blit(this.handleTex, handleX, handleY, 0, 0, this.handleW, this.handleH, this.handleW, this.handleH);

        if (this.active && this.isMouseOver(mouseX, mouseY)) {
            gg.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
        }
        if (!this.active) {
            gg.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x80000000);
        }
    }
}
