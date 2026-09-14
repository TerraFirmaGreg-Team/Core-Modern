/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.dries007.tfc.common.capabilities.player.PlayerData;

public record SprintKeyPacket(boolean isDown)
{
    public SprintKeyPacket(FriendlyByteBuf buffer)
    {
        this(buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(isDown);
    }

    public void handle(ServerPlayer player)
    {
        PlayerData.get(player).setSprintKeyDown(isDown);
    }
}
