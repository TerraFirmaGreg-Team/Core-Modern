package su.terrafirmagreg.core.common.data.container;

import java.util.ArrayList;

import net.dries007.tfc.client.screen.TFCContainerScreen;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.container.widgets.SmithingButton;
import su.terrafirmagreg.core.common.data.recipes.SmithingType;

public class SmithingTableScreen extends TFCContainerScreen<SmithingTableContainer> {

    public final ArrayList<SmithingButton> allButtons = new ArrayList<>();

    private SmithingType activeType;
    private ImageWidget borderImage;

    public SmithingTableScreen(SmithingTableContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, TFGCore.id("textures/gui/smithing_test.png"));
        this.imageHeight = 186;
        this.inventoryLabelY += 21;
        this.titleLabelY -= 2;

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
            borderImage = new ImageWidget((width - getXSize()) / 2 + 14, (height - getYSize()) / 2 + 14, 78, 78, borderTexture);
            addRenderableWidget(borderImage);
        }

        this.getMenu().setScreenState(true);
    }

    public void RemoveButtons() {
        for (SmithingButton button : allButtons) {
            if (button.isActive()) {
                button.active = false;
            }
            button.visible = false;
        }
        allButtons.clear();
        if (borderImage != null)
            borderImage.visible = false;
        //This technically works but until I find a way to actually delete them as long as the screen is open they will keep stacking ontop of each other
        //Making new buttons does allow for much easier switching of the button textures
    }

    @Override
    protected void init() {
        super.init();
        System.out.println("screen init");
        //AddButtons();
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
            AddButtons();
        } else {
            RemoveButtons();
        }
        counter = 0;
    }

}
