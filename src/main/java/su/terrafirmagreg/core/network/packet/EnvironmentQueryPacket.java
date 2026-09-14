package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.EnvironmentClientCache;
import su.terrafirmagreg.core.common.environment.EnvironmentSystem;
import su.terrafirmagreg.core.network.TFGNetworkHandler;

/**
 * Packet sent from client to server to query environment at a position.
 */
public record EnvironmentQueryPacket(BlockPos pos) {

    public static void encode(EnvironmentQueryPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static EnvironmentQueryPacket decode(FriendlyByteBuf buf) {
        return new EnvironmentQueryPacket(buf.readBlockPos());
    }

    public static void handle(EnvironmentQueryPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            // Query all environment properties
            EnvironmentClientCache.EnvironmentState state = new EnvironmentClientCache.EnvironmentState(
                    EnvironmentSystem.hasOxygen(player.level(), pkt.pos),
                    EnvironmentSystem.hasNormalGravity(player.level(), pkt.pos),
                    true,
                    true // TODO: hasNormalTemperature/hasNormalPressure when implemented
            );

            // Send response back to the client
            TFGNetworkHandler.sendEnvironmentResponse(player, pkt.pos, state);
        });
        ctx.get().setPacketHandled(true);
    }
}
