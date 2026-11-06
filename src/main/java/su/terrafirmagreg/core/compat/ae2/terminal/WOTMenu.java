package su.terrafirmagreg.core.compat.ae2.terminal;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;
import de.mari_023.ae2wtlib.AE2wtlibSlotSemantics;
import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.wut.ItemWUT;

public class WOTMenu extends OrderTerminalMenu {
    public static final MenuType<OrderTerminalMenu> TYPE = MenuTypeBuilder
            .<OrderTerminalMenu, WOTHost>create(WOTMenu::new, WOTHost.class)
            .build("tfg_wireless_order_terminal");

    private final WOTHost host;

    public static void init() {
    }

    public WOTMenu(int id, Inventory playerInventory, WOTHost host) {
        super(TYPE, id, playerInventory, host);
        this.host = host;
        addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.QE_SINGULARITY, host.getSubInventory(WTMenuHost.INV_SINGULARITY), 0), AE2wtlibSlotSemantics.SINGULARITY);
    }

    public boolean isWUT() {
        return host.getItemStack().getItem() instanceof ItemWUT;
    }
}
