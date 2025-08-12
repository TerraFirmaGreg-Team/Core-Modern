package su.terrafirmagreg.core.common.data.contianer;

import net.dries007.tfc.client.screen.TFCContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LargeNestBoxScreen  extends TFCContainerScreen<LargeNestBoxContainer> {
    public LargeNestBoxScreen(LargeNestBoxContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name, INVENTORY_2x2);
    }
}
