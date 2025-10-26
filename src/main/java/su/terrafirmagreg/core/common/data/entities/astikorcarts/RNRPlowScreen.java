package su.terrafirmagreg.core.common.data.entities.astikorcarts;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import su.terrafirmagreg.core.TFGCore;

public final class RNRPlowScreen extends AbstractContainerScreen<RNRPlowContainer> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/rnr_plow.png");

    public RNRPlowScreen(RNRPlowContainer menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 256;
        this.imageHeight = 256;
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        gg.blit(BG, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        gg.drawString(this.font, this.title, 8, 6, 0x404040, false);
        gg.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg);
        super.render(gg, mouseX, mouseY, partialTick);
        this.renderTooltip(gg, mouseX, mouseY);
    }
}
