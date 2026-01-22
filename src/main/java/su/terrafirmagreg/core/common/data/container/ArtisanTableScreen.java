package su.terrafirmagreg.core.common.data.container;

import java.util.ArrayList;

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

public class ArtisanTableScreen extends TFCContainerScreen<ArtisanTableContainer> {

    public final ArrayList<SmithingButton> allButtons = new ArrayList<>();

    private ArtisanType activeType;
    private ImageWidget borderImage;
    private InvisibleButton emiButton;
    private boolean buttonsInitialized = false;

    public ArtisanTableScreen(ArtisanTableContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, TFGCore.id("textures/gui/smithing_test.png"));
        this.imageHeight = 186;
        this.inventoryLabelY += 21;
        this.titleLabelY -= 2;
    }

    private static class InvisibleButton extends AbstractWidget {
        private final Runnable onClick;

        public InvisibleButton(int x, int y, int width, int height, Component tooltip, Runnable onClick) {
            super(x, y, width, height, Component.empty());
            this.onClick = onClick;
            this.setTooltip(Tooltip.create(tooltip));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
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
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    public void AddButtons() {
        if (!allButtons.isEmpty())
            return;
        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 6; y++) {
                int bx = (width - getXSize()) / 2 + 17 + 12 * x;
                int by = (height - getYSize()) / 2 + 17 + 12 * y;

                SmithingButton button = new SmithingButton(x + 6 * y, activeType, bx, by, 12, 12, 12, 12, activeType.getActiveTexture(), activeType.getInactiveTexture(), activeType.getClickSound());
                allButtons.add(button);

                addRenderableWidget(button);
            }
        }
        ResourceLocation borderTexture = activeType.getBorderTexture();
        if (borderTexture != null) {
            //78 is a 3 pixel buffer around the button area
            borderImage = new ImageWidget((width - getXSize()) / 2 + 14, (height - getYSize()) / 2 + 14, 78, 78, borderTexture) {
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

    private void AddAndUpdateButtons() {
        AddButtons();
        ArtisanPattern pattern = getMenu().getPattern();
        for (SmithingButton button : allButtons) {
            if (!pattern.get(button.id)) {
                button.activateButton();
            }
        }
    }

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

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        super.renderLabels(guiGraphics, pMouseX, pMouseY);
        if (this.menu.getScreenState()) {
            renderSlotHighlight(guiGraphics, 123, 25, 1);
            renderSlotHighlight(guiGraphics, 145, 25, 1);
            renderSlotHighlight(guiGraphics, 123, 46, 1);
            renderSlotHighlight(guiGraphics, 145, 46, 1);
        }
    }

    @Override
    protected void init() {
        super.init();

        int emiButtonX = leftPos + 134 - 20;
        int emiButtonY = topPos + 72;
        emiButton = new InvisibleButton(emiButtonX, emiButtonY, 16, 16,
                Component.translatable("tfg.tooltip.show_recipes"), this::openEmiRecipes);
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

    private void openEmiRecipes() {
        try {
            Class<?> emiApiClass = Class.forName("dev.emi.emi.api.EmiApi");
            var displayRecipes = emiApiClass.getMethod("displayRecipeCategory",
                    Class.forName("dev.emi.emi.api.recipe.EmiRecipeCategory"));

            Class<?> pluginClass = Class.forName("su.terrafirmagreg.core.compat.emi.TFGEmiPlugin");
            var categoryField = pluginClass.getField("ARTISAN_TABLE");
            var category = categoryField.get(null);

            displayRecipes.invoke(null, category);
        } catch (Exception e) {
        }
    }

    int counter = 0;

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
