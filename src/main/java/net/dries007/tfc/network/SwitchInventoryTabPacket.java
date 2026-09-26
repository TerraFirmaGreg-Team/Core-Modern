/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.network;

import net.dries007.tfc.client.screen.button.PlayerInventoryTabButton;
import net.dries007.tfc.common.container.TFCContainerProviders;
import net.dries007.tfc.compat.patchouli.PatchouliIntegration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record SwitchInventoryTabPacket(PlayerInventoryTabButton.Tab tab) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.tab);
    }

    public static SwitchInventoryTabPacket decode(FriendlyByteBuf buf) {
        return new SwitchInventoryTabPacket(buf.readEnum(PlayerInventoryTabButton.Tab.class));
    }

    public void handle(ServerPlayer player) {
        if (player != null) {
            player.doCloseContainer();

            switch (this.tab) {
                case INVENTORY -> player.containerMenu = player.inventoryMenu;
                case CALENDAR -> player.openMenu(TFCContainerProviders.CALENDAR);
                case NUTRITION -> player.openMenu(TFCContainerProviders.NUTRITION);
                case CLIMATE -> player.openMenu(TFCContainerProviders.CLIMATE);
                case BOOK -> PatchouliIntegration.openGui(player);
            }
        }
    }
}