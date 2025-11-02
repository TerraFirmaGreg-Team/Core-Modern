package su.terrafirmagreg.core.common.data.entities.astikorcarts;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.container.widgets.Slider;
import su.terrafirmagreg.core.common.data.container.widgets.ToggleButton;

/**
 * GUI for the RNR Plow entity.
 */
public final class RNRPlowScreen extends AbstractContainerScreen<RNRPlowContainer> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/rnr_plow.png");
    private static final ResourceLocation TOGGLE_TEX = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/rnr_plow_toggle.png");
    private static final ResourceLocation SLIDER_BG_TEX = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/rnr_plow_slider_bg.png");
    private static final ResourceLocation SLIDER_HANDLE_TEX = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "textures/gui/rnr_plow_slider_handle.png");

    private Button randomToggleButton;
    private Slider widthSlider;

    /**
     * Constructs the RNR Plow screen.
     *
     * @param menu  The container menu associated with this screen.
     * @param inv   The player's inventory.
     * @param title The title of the screen.
     */
    public RNRPlowScreen(RNRPlowContainer menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 256;
        this.imageHeight = 256;
    }

    /**
     * Initializes the button and slider.
     */
    @Override
    protected void init() {
        super.init();
        int checkX = this.leftPos + 131;
        int checkY = this.topPos + 141;

        // Initialize the random mode toggle button.
        this.randomToggleButton = new ToggleButton(
                checkX, checkY, 16, 16,
                TOGGLE_TEX, 32, 16,
                this.menu::isRandomModeClient,
                btn -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                });
        this.addRenderableWidget(this.randomToggleButton);

        // Initialize the width slider (range: 1-5).
        final int sliderW = 90;
        final int sliderH = 16;
        final int sliderX = this.leftPos + 29;
        final int sliderY = this.topPos + 141;

        this.widthSlider = new Slider(
                sliderX, sliderY, sliderW, sliderH,
                SLIDER_BG_TEX, sliderW, sliderH,
                SLIDER_HANDLE_TEX, 8, 16,
                1, 5,
                this.menu::getPlowWidthClient,
                v -> {
                    if (this.minecraft != null && this.minecraft.gameMode != null) {
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, v);
                    }
                });
        this.addRenderableWidget(this.widthSlider);
    }

    /**
     * Renders the background of the GUI.
     *
     * @param gg          The graphics context for rendering.
     * @param partialTick The partial tick time.
     * @param mouseX      The x-coordinate of the mouse.
     * @param mouseY      The y-coordinate of the mouse.
     */
    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        final int x = (this.width - this.imageWidth) / 2;
        final int y = (this.height - this.imageHeight) / 2;
        gg.blit(BG, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    /**
     * Renders the title and inventory name.
     *
     * @param gg     The graphics context for rendering.
     * @param mouseX The x-coordinate of the mouse.
     * @param mouseY The y-coordinate of the mouse.
     */
    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        gg.drawString(this.font, this.title, 8, 6, 0x404040, false);
        gg.drawString(this.font, this.playerInventoryTitle, 8, 159, 0x404040, false);
    }

    /**
     * Renders the entire GUI.
     *
     * @param gg          The graphics context for rendering.
     * @param mouseX      The x-coordinate of the mouse.
     * @param mouseY      The y-coordinate of the mouse.
     * @param partialTick The partial tick time.
     */
    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg);
        super.render(gg, mouseX, mouseY, partialTick);

        // Render tooltip for the random toggle button if hovered.
        if (this.randomToggleButton != null && this.randomToggleButton.isMouseOver(mouseX, mouseY)) {
            gg.renderTooltip(this.font, Component.translatable("tfg.gui.rnr_plow.random_mode"), mouseX, mouseY);
        }
        // Render tooltip for the width slider if hovered.
        if (this.widthSlider != null && this.widthSlider.isMouseOver(mouseX, mouseY)) {
            final int displayWidth = this.widthSlider.getDisplayValue();
            gg.renderTooltip(this.font, Component.translatable("tfg.gui.rnr_plow.width", displayWidth), mouseX, mouseY);
        }

        this.renderTooltip(gg, mouseX, mouseY);
    }
}
