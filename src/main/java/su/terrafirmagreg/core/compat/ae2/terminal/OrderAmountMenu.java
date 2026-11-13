package su.terrafirmagreg.core.compat.ae2.terminal;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ISubMenuHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuLocator;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.InaccessibleSlot;
import appeng.util.inv.AppEngInternalInventory;

public class OrderAmountMenu extends AEBaseMenu implements ISubMenu {
    public static final MenuType<OrderAmountMenu> TYPE = MenuTypeBuilder
            .create(OrderAmountMenu::new, OrderTerminalHost.class)
            .build("tfg_order_amount");

    private static final String ACTION_CONFIRM = "confirm";

    private final AppEngSlot displayedStack = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
    private final OrderTerminalHost terminal;

    @GuiSync(1)
    public boolean craftMissing;

    @GuiSync(2)
    public boolean staging;

    @GuiSync(3)
    public boolean editingStaged;

    private AEKey what;
    private String destination = "";

    public static void init() {
    }

    public OrderAmountMenu(int id, Inventory playerInventory, OrderTerminalHost terminal) {
        super(TYPE, id, playerInventory, terminal);
        this.terminal = terminal;
        displayedStack.setHideAmount(true);
        addSlot(displayedStack, SlotSemantics.MACHINE_OUTPUT);
        registerClientAction(ACTION_CONFIRM, Long.class, this::confirm);
    }

    public static void open(ServerPlayer player, MenuLocator locator, AEKey what, long initialAmount, boolean craftMissing, String destination, boolean editingStaged) {
        MenuOpener.open(TYPE, player, locator);
        if (player.containerMenu instanceof OrderAmountMenu menu) {
            menu.what = what;
            menu.craftMissing = craftMissing;
            menu.destination = destination;
            menu.staging = menu.terminal.getOrderLogic().isStaging();
            menu.editingStaged = editingStaged;
            menu.displayedStack.set(GenericStack.wrapInItemStack(what, initialAmount));
            menu.broadcastChanges();
        }
    }

    @Nullable
    public GenericStack getDisplayedStack() {
        return GenericStack.unwrapItemStack(displayedStack.getItem());
    }

    public void confirm(Long amount) {
        if (isClientSide()) {
            sendClientAction(ACTION_CONFIRM, amount);
            return;
        }
        if (what != null && amount > 0) {
            if (editingStaged) {
                terminal.getOrderLogic().setStagedAmount(destination, what, amount);
            } else {
                terminal.getOrderLogic().dispatch(destination, what, amount, craftMissing, getActionSource());
            }
        }
        terminal.returnToMainMenu(getPlayer(), this);
    }

    @Override
    public ISubMenuHost getHost() {
        return terminal;
    }
}
