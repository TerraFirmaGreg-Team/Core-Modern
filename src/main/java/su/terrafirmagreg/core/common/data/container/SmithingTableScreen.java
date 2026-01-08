package su.terrafirmagreg.core.common.data.container;

import javax.annotation.Nullable;

import net.dries007.tfc.client.screen.TFCContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.container.widgets.SmithingButton;

public class SmithingTableScreen extends TFCContainerScreen<SmithingTableContainer> {
    private final ResourceLocation buttonActiveTexture;
    @Nullable
    private final ResourceLocation buttonInactiveTexture;

    public SmithingTableScreen(SmithingTableContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, TFGCore.id("textures/gui/smithing_test.png"));
        this.imageHeight = 186;
        this.inventoryLabelY += 21;
        this.titleLabelY -= 2;

        buttonActiveTexture = getActiveTexture();
        buttonInactiveTexture = getInactiveTexture();
    }

    public static ResourceLocation getActiveTexture() {
        return TFGCore.id("textures/gui/abutton.png");
    }

    @Nullable
    public static ResourceLocation getInactiveTexture() {
        return TFGCore.id("textures/gui/iabutton.png");
    }

    @Override
    protected void init() {
        super.init();
        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 6; y++) {
                int bx = (width - getXSize()) / 2 + 17 + 12 * x;
                int by = (height - getYSize()) / 2 + 17 + 12 * y;
                addRenderableWidget(new SmithingButton(x + 5 * y, bx, by, 12, 12, 12, 12, buttonActiveTexture, SoundEvents.ITEM_PICKUP));
            }
        }
    }

}
