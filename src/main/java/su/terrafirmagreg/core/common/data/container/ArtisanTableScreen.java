package su.terrafirmagreg.core.common.data.container;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.systems.RenderSystem;

import net.dries007.tfc.client.screen.TFCContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.container.widgets.SmithingButton;
import su.terrafirmagreg.core.common.data.recipes.ArtisanPattern;
import su.terrafirmagreg.core.common.data.recipes.ArtisanType;

/**
 * The GUI screen for the Artisan Table.
 */
public class ArtisanTableScreen extends TFCContainerScreen<ArtisanTableContainer> {

    public final ArrayList<SmithingButton> allButtons = new ArrayList<>();
    // Sets the gap between vertical sections of the GUI.
    public static final int SCREEN_SPACING = 5;

    private ArtisanType activeType;
    private ImageWidget borderImage;
    private boolean buttonsInitialized = false;

    /**
     * Constructs a new ArtisanTableScreen.
     * @param container The ArtisanTableContainer.
     * @param playerInventory The player's inventory.
     * @param name The screen title.
     */
    public ArtisanTableScreen(ArtisanTableContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, TFGCore.id("textures/gui/artisan_table/artisan_table.png"));
        this.imageHeight = 186 + SCREEN_SPACING + SCREEN_SPACING;
        this.inventoryLabelY += 20 + SCREEN_SPACING + SCREEN_SPACING;
        this.titleLabelY -= 1;
    }

    /**
     * EMI recipe viewer button.
     */
    private static class InvisibleButton extends AbstractWidget {
        private final Runnable onClick;

        /**
         * Constructs an InvisibleButton.
         * @param x The x position.
         * @param y The y position.
         * @param width The width.
         * @param height The height.
         * @param tooltip The tooltip to display.
         * @param onClick The action to perform on click.
         */
        public InvisibleButton(int x, int y, int width, int height, Component tooltip, Runnable onClick) {
            super(x, y, width, height, Component.empty());
            this.onClick = onClick;
            this.setTooltip(Tooltip.create(tooltip));
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.isMouseOver(mouseX, mouseY) && button == 0) {
                onClick.run();
                return true;
            }
            return false;
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    /**
     * Adds all smithing buttons and the border image to the screen.
     * Only runs once per screen state.
     */
    public void AddButtons() {
        if (!allButtons.isEmpty())
            return;
        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 6; y++) {
                int bx = (width - getXSize()) / 2 + 17 + 12 * x;
                int by = (height - getYSize()) / 2 + 17 + 12 * y + SCREEN_SPACING;
                SmithingButton button = new SmithingButton(x + 6 * y, activeType, bx, by, 12, 12, 12, 12, activeType.getActiveTexture(), activeType.getInactiveTexture(), activeType.getClickSound());
                allButtons.add(button);
                addRenderableWidget(button);
            }
        }
        ResourceLocation borderTexture = activeType.getBorderTexture();
        if (borderTexture != null) {
            borderImage = new ImageWidget((width - getXSize()) / 2 + 12, (height - getYSize()) / 2 + 12 + SCREEN_SPACING, 82, 82, borderTexture) {
                @Override
                public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    super.renderWidget(graphics, mouseX, mouseY, partialTick);
                    RenderSystem.disableBlend();
                }

                @Override
                public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                    return false;
                }
            };
            addRenderableWidget(borderImage);
        }
        this.getMenu().setScreenState(true);
        buttonsInitialized = true;
    }

    /**
     * Adds and updates smithing buttons based on the current artisan pattern.
     */
    private void AddAndUpdateButtons() {
        AddButtons();
        ArtisanPattern pattern = getMenu().getPattern();
        for (SmithingButton button : allButtons) {
            if (!pattern.get(button.id)) {
                button.activateButton();
            }
        }
    }

    /**
     * Removes all smithing buttons and the border image from the screen.
     */
    public void RemoveButtons() {
        for (SmithingButton button : allButtons) {
            if (button.active) {
                button.active = false;
            }
            button.visible = false;
        }
        allButtons.clear();
        if (borderImage != null)
            borderImage.visible = false;
        buttonsInitialized = false;
    }

    /**
     * Updates the state of smithing buttons based on the current pattern.
     */
    private void updateButtonStatesFromPattern() {
        ArtisanPattern pattern = getMenu().getPattern();
        for (SmithingButton button : allButtons) {
            if (!pattern.get(button.id)) {
                button.activateButton();
            } else {
                button.active = true;
                button.visible = true;
            }
        }
    }

    /**
     * Renders slot highlights when the artisan table is active.
     */
    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        super.renderLabels(guiGraphics, pMouseX, pMouseY);
        if (this.menu.getScreenState()) {
            renderSlotHighlight(guiGraphics, 123, 25 + SCREEN_SPACING, 1);
            renderSlotHighlight(guiGraphics, 145, 25 + SCREEN_SPACING, 1);
            renderSlotHighlight(guiGraphics, 123, 46 + SCREEN_SPACING, 1);
            renderSlotHighlight(guiGraphics, 145, 46 + SCREEN_SPACING, 1);
        }
    }

    /**
     * Initializes the screen, adding widgets and updating buttons.
     */
    @Override
    protected void init() {
        super.init();
        int emiButtonX = leftPos + 134 - 40;
        int emiButtonY = topPos + 72 + SCREEN_SPACING;
        InvisibleButton emiButton = new InvisibleButton(emiButtonX, emiButtonY, 32, 16,
                Component.translatable(TFGCore.MOD_ID + ".tooltip.show_recipes"), this::openEmiRecipes);
        addRenderableWidget(emiButton);

        if (this.menu.getScreenState()) {
            activeType = this.menu.getCurrentType();
            if (activeType != null) {
                RemoveButtons();
                AddAndUpdateButtons();
                updateButtonStatesFromPattern();
            }
        }
    }

    /**
     * Opens the EMI recipe viewer.
     */
    private void openEmiRecipes() {
        try {
            Class<?> emiApiClass = Class.forName("dev.emi.emi.api.EmiApi");
            var displayRecipes = emiApiClass.getMethod("displayRecipeCategory",
                    Class.forName("dev.emi.emi.api.recipe.EmiRecipeCategory"));

            Class<?> pluginClass = Class.forName("su.terrafirmagreg.core.compat.emi.TFGEmiPlugin");
            var categoryField = pluginClass.getField("ARTISAN_TABLE");
            var category = categoryField.get(null);

            displayRecipes.invoke(null, category);
        } catch (Exception ignored) {
        }
    }

    int counter = 0;

    /**
     * Handles ticking logic for the container.
     */
    @Override
    protected void containerTick() {
        if (counter != 2) {
            counter += 1;
            return;
        }
        if (this.menu.getScreenState()) {
            activeType = this.menu.getCurrentType();
            if (activeType != null && !buttonsInitialized) {
                AddAndUpdateButtons();
            }
            updateButtonStatesFromPattern();
        } else {
            if (buttonsInitialized) {
                RemoveButtons();
            }
        }
        counter = 0;
    }
}
