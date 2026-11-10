package su.terrafirmagreg.core.compat.ae2.terminal;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.menu.ISubMenu;
import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import lombok.Getter;

import su.terrafirmagreg.core.common.data.items.TFGItems;

public class WOTHost extends WTMenuHost implements OrderTerminalHost {
    private static final String NBT_ORDER = "tfg_order";

    @Getter
    private final OrderTerminalLogic orderLogic = new OrderTerminalLogic(() -> {
        var node = getActionableNode();
        return node != null ? node.getGrid() : null;
    }, this::saveChanges);

    public WOTHost(Player player, @Nullable Integer inventorySlot, ItemStack stack, BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(player, inventorySlot, stack, returnToMainMenu);
        readFromNbt();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(TFGItems.WIRELESS_ORDER_TERMINAL.get());
    }

    @Override
    protected void readFromNbt() {
        super.readFromNbt();
        orderLogic.readFromNBT(getItemStack().getOrCreateTag().getCompound(NBT_ORDER));
    }

    @Override
    public void saveChanges() {
        super.saveChanges();
        var tag = new CompoundTag();
        orderLogic.writeToNBT(tag);
        getItemStack().getOrCreateTag().put(NBT_ORDER, tag);
    }
}
