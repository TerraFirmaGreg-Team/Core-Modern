package su.terrafirmagreg.core.compat.ae2.terminal;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.features.GridLinkables;
import appeng.items.tools.powered.WirelessTerminalItem;
import de.mari_023.ae2wtlib.AE2WTLibCreativeTab;
import de.mari_023.ae2wtlib.terminal.ItemWT;
import de.mari_023.ae2wtlib.wut.WUTHandler;

public class WOTItem extends ItemWT {
    public static final String TERMINAL_NAME = "order";

    public static void onRegister(WOTItem item) {
        WUTHandler.addTerminal(TERMINAL_NAME, item::tryOpen, WOTHost::new, WOTMenu.TYPE, item, "item.tfg.wireless_order_terminal");
        GridLinkables.register(item, WirelessTerminalItem.LINKABLE_HANDLER);
        AE2WTLibCreativeTab.addTerminal(item);
    }

    @Override
    public MenuType<?> getMenuType(ItemStack stack) {
        return WOTMenu.TYPE;
    }
}
