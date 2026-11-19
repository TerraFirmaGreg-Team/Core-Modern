package su.terrafirmagreg.core.client.screen.ae2;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.AESubScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.NumberEntryWidget;

import su.terrafirmagreg.core.compat.ae2.terminal.OrderAmountMenu;

public class OrderAmountScreen extends AEBaseScreen<OrderAmountMenu> {
    private final Button send;
    private final NumberEntryWidget amount;
    private boolean amountInitialized;

    public OrderAmountScreen(OrderAmountMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        send = widgets.addButton("next", Component.translatable("gui.tfg.order_terminal.send"), this::confirm);
        AESubScreen.addBackButton(menu, "back", widgets);
        amount = widgets.addNumberEntryWidget("amountToCraft", NumberEntryType.UNITLESS);
        amount.setMinValue(1);
        amount.setMaxValue(Long.MAX_VALUE);
        amount.setLongValue(1);
        amount.setTextFieldStyle(style.getWidget("amountToCraftInput"));
        amount.setHideValidationIcon(true);
        amount.setOnConfirm(this::confirm);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (!amountInitialized) {
            var stack = menu.getDisplayedStack();
            if (stack != null) {
                amount.setType(NumberEntryType.of(stack.what()));
                amount.setLongValue(stack.amount());
                amountInitialized = true;
            }
        }
        send.setMessage(Component.translatable(confirmLabel()));
        send.active = amount.getLongValue().orElse(0) > 0;
    }

    private String confirmLabel() {
        if (menu.editingStaged) {
            return "gui.tfg.order_terminal.set";
        }
        if (menu.staging) {
            return "gui.tfg.order_terminal.stage";
        }
        return menu.craftMissing ? "gui.tfg.order_terminal.craft" : "gui.tfg.order_terminal.send";
    }

    private void confirm() {
        long value = amount.getLongValue().orElse(0);
        if (value > 0) {
            menu.confirm(value);
        }
    }
}
