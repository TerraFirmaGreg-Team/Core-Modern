package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.AtmosphereClientCache.AtmosphereState;
import su.terrafirmagreg.core.common.atmosphere.AtmosphereSystem;
import su.terrafirmagreg.core.network.TFGNetworkHandler;

/**
 * Packet sent from client to server to query atmosphere at a position.
 */
public class AtmosphereQueryPacket {
    private final BlockPos pos;

    public AtmosphereQueryPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(AtmosphereQueryPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static AtmosphereQueryPacket decode(FriendlyByteBuf buf) {
        return new AtmosphereQueryPacket(buf.readBlockPos());
    }

    public static void handle(AtmosphereQueryPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            // Query all atmosphere properties
            AtmosphereSystem system = AtmosphereSystem.get();
            AtmosphereState state = new AtmosphereState(
                    system.hasOxygen(player.level(), pkt.pos),
                    system.hasNormalGravity(player.level(), pkt.pos),
                    system.hasNormalTemperature(player.level(), pkt.pos),
                    false // TODO: hasNormalPressure when implemented
            );

            // Send response back to the client
            TFGNetworkHandler.sendAtmosphereResponse(player, pkt.pos, state);
        });
        ctx.get().setPacketHandled(true);
    }
}
