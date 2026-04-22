package su.terrafirmagreg.core.client.screen.widget;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import lombok.Getter;

/**
 * A custom multi-state toggle button widget for GUI's.
 * This button cycles through n states and supports a unique texture for each state.
 */
public final class MultiToggleButton extends Button {
    /**
     * The total number of states this button can cycle through.
     */
    @Getter
    private final int stateCount;

    private final int texWidth;
    private final int texHeight;
    private final IntSupplier stateSupplier;
    private final IntConsumer stateSetter;
    private final IntFunction<ResourceLocation> textureByState;
    private final Button.OnPress onPress;

    /**
     * Constructs a new MultiToggleButton instance.
     *
     * @param x The x of the button.
     * @param y The y of the button.
     * @param width The width of the button.
     * @param height The height of the button.
     * @param stateCount The total number of button states.
     * @param texWidth The width of each state texture.
     * @param texHeight The height of each state texture.
     * @param stateSupplier A supplier that provides the current state index.
     * @param stateSetter A consumer that applies the next state index.
     * @param textureByState A function that provides the texture for a state index.
     * @param onPress A callback for when the button is pressed.
     */
    public MultiToggleButton(
            int x, int y, int width, int height,
            int stateCount,
            int texWidth, int texHeight,
            IntSupplier stateSupplier,
            IntConsumer stateSetter,
            IntFunction<ResourceLocation> textureByState,
            Button.OnPress onPress) {
        super(x, y, width, height, Component.empty(), btn -> {
        }, DEFAULT_NARRATION);
        this.stateCount = Math.max(1, stateCount);
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.stateSupplier = stateSupplier;
        this.stateSetter = stateSetter;
        this.textureByState = textureByState;
        this.onPress = onPress;
    }

    /**
     * @return The current state index in range [0, stateCount - 1].
     */
    public int getCurrentState() {
        final int supplied = this.stateSupplier != null ? this.stateSupplier.getAsInt() : 0;
        return normalizeState(supplied, this.stateCount);
    }

    /**
     * Computes the next state index.
     * @param currentState The current state index.
     * @param stateCount The total number of states.
     * @return The next state index.
     */
    public static int nextState(int currentState, int stateCount) {
        final int safeCount = Math.max(1, stateCount);
        final int normalized = normalizeState(currentState, safeCount);
        return (normalized + 1) % safeCount;
    }

    @Override
    public void onPress() {
        if (this.stateSetter != null) {
            this.stateSetter.accept(nextState(getCurrentState(), this.stateCount));
        }
        if (this.onPress != null) {
            this.onPress.onPress(this);
        }
    }

    /**
     * Renders the multi-toggle button widget.
     * @param gg Graphics.
     * @param mouseX The x of the mouse.
     * @param mouseY The y of the mouse.
     * @param delta The partial tick time.
     */
    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float delta) {
        final int state = getCurrentState();
        final ResourceLocation texture = this.textureByState != null ? this.textureByState.apply(state) : null;
        if (texture != null) {
            final int drawW = Math.min(this.width, this.texWidth);
            final int drawH = Math.min(this.height, this.texHeight);
            gg.blit(texture, this.getX(), this.getY(), 0, 0, drawW, drawH, this.texWidth, this.texHeight);
        }

        if (this.active && this.isMouseOver(mouseX, mouseY)) {
            gg.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
        }

        if (!this.active) {
            gg.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x80000000);
        }
    }

    private static int normalizeState(int state, int stateCount) {
        if (stateCount <= 1) {
            return 0;
        }
        return Mth.positiveModulo(state, stateCount);
    }
}
