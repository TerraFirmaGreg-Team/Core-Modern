package su.terrafirmagreg.core.common.command;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class DebugCommandUtils {

    private DebugCommandUtils() {
    }

    /** Equips an item into the given slot, returning any displaced item to inventory or dropping it. */
    public static void equipSlot(ServerPlayer player, EquipmentSlot slot, ItemStack newItem) {
        ItemStack existing = player.getItemBySlot(slot);
        player.setItemSlot(slot, newItem);
        if (!existing.isEmpty())
            giveOrDrop(player, existing);
    }

    /** Adds an item to the player's inventory, dropping it at their feet if it doesn't fit. */
    public static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack))
            player.drop(stack, false);
    }
}
