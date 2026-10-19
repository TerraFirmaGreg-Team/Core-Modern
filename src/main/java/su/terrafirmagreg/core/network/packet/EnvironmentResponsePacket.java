package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.EnvironmentClientCache;
import su.terrafirmagreg.core.client.EnvironmentClientCache.EnvironmentState;

/**
 * Packet sent from server to client with environment query result.
 */
public record EnvironmentResponsePacket(BlockPos pos, boolean hasOxygen, float gravity, float temperature, float pressure) {
    public EnvironmentResponsePacket(BlockPos pos, EnvironmentState state) {
        this(pos, state.hasOxygen(), state.gravity(), state.temperature(), state.pressure());
    }

    public static void encode(EnvironmentResponsePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeBoolean(pkt.hasOxygen);
        buf.writeFloat(pkt.gravity);
        buf.writeFloat(pkt.temperature);
        buf.writeFloat(pkt.pressure);
    }

    public static EnvironmentResponsePacket decode(FriendlyByteBuf buf) {
        return new EnvironmentResponsePacket(
                buf.readBlockPos(),
                buf.readBoolean(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat());
    }

    public static void handle(EnvironmentResponsePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> EnvironmentClientCache.receive(pkt.pos,
                new EnvironmentState(pkt.hasOxygen, pkt.gravity, pkt.temperature, pkt.pressure)));
        ctx.get().setPacketHandled(true);
    }
}
