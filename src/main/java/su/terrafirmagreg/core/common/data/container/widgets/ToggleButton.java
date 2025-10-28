package su.terrafirmagreg.core.common.data.container.widgets;

import java.util.function.BooleanSupplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ToggleButton extends Button {
    private final ResourceLocation texture;
    private final int texWidth;
    private final int texHeight;
    private final BooleanSupplier isOnSupplier;

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

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float delta) {
        final boolean on = this.isOnSupplier != null && this.isOnSupplier.getAsBoolean();
        final int frameW = this.texWidth / 2;
        final int frameH = this.texHeight;
        final int drawW = Math.min(this.width, frameW);
        final int drawH = Math.min(this.height, frameH);

        final int u = on ? frameW : 0;
        final int v = 0;

        gg.blit(this.texture, this.getX(), this.getY(), u, v, drawW, drawH, this.texWidth, this.texHeight);

        if (this.active && this.isMouseOver(mouseX, mouseY)) {
            gg.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
        }
        if (!this.active) {
            gg.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x80000000);
        }
    }
}
