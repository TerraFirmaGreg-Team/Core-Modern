package su.terrafirmagreg.core.common.data.container;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.dries007.tfc.client.screen.TFCContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageWidget;
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

    public ArtisanTableScreen(ArtisanTableContainer container, Inventory playerInventory, Component name) {
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
            borderImage = new ImageWidget((width - getXSize()) / 2 + 14, (height - getYSize()) / 2 + 14, 78, 78, borderTexture) {
                @Override
                public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                    return false;
                }
            };

            addRenderableWidget(borderImage);
        }

        this.getMenu().setScreenState(true);
    }

    private void AddAndUpdateButtons() {
        AddButtons();
        ArtisanPattern pattern = getMenu().getPattern();
        output(pattern);
        for (SmithingButton button : allButtons) {
            //System.out.println(button.id);
            //System.out.println(!pattern.get((long) button.id));
            if (!pattern.get(button.id)) {
                System.out.println("button " + button.id + " activated");
                button.activateButton();
            }
        }
    }

    public void output(ArtisanPattern pattern) {
        // Allocate an 8-byte buffer, the size of a long
        ByteBuffer buffers = ByteBuffer.allocate(Long.BYTES);
        // Put the long into the buffer
        buffers.putLong(pattern.getData());
        // Return the underlying byte array
        System.out.println(bytesToBinaryString(buffers.array()));
    }

    public static String bytesToBinaryString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            // Convert the byte to an integer, mask with 0xFF to handle signedness
            // and get the unsigned integer value (0-255).
            int unsignedInt = b & 0xFF;

            // Convert the unsigned integer to a binary string.
            String binaryString = Integer.toBinaryString(unsignedInt);

            // Pad with leading zeros to ensure an 8-bit representation.
            String paddedBinaryString = String.format("%8s", binaryString).replace(' ', '0');

            sb.append(paddedBinaryString); // Add a space for readability
        }
        sb.delete(0, 28);

        for (int i = 6; i < sb.length(); i += 6 + "\n".length()) {
            sb.insert(i, "\n");
        }
        sb.insert(0, "\n");
        return sb.toString();
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
        //This technically works but until I find a way to actually delete them as long as the screen is open they will keep stacking ontop of each other
        //Making new buttons does allow for much easier switching of the button textures
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
        System.out.println(getMenu());
        System.out.println(this.menu.activeScreen);
        output(this.menu.getPattern());
        if (this.menu.activeScreen) {
            RemoveButtons();
            AddAndUpdateButtons();
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
            AddButtons();
        } else {
            RemoveButtons();
        }
        counter = 0;
    }
}
