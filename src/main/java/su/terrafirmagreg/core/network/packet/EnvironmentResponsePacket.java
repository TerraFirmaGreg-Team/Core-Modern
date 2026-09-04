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
public record EnvironmentResponsePacket(BlockPos pos, byte stateFlags) {
    public EnvironmentResponsePacket(BlockPos pos, EnvironmentState state) {
        this(pos, state.toByte());
    }

    public static void encode(EnvironmentResponsePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeByte(pkt.stateFlags);
    }

    public static EnvironmentResponsePacket decode(FriendlyByteBuf buf) {
        return new EnvironmentResponsePacket(buf.readBlockPos(), buf.readByte());
    }

    public static void handle(EnvironmentResponsePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> EnvironmentClientCache.receive(pkt.pos, EnvironmentState.fromByte(pkt.stateFlags)));
        ctx.get().setPacketHandled(true);
    }
}
