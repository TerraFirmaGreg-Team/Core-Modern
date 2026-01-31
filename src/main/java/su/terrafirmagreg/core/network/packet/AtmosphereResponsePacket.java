package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.AtmosphereClientCache;
import su.terrafirmagreg.core.client.AtmosphereClientCache.AtmosphereState;

/**
 * Packet sent from server to client with atmosphere query result.
 */
public class AtmosphereResponsePacket {
    private final BlockPos pos;
    private final byte stateFlags;

    public AtmosphereResponsePacket(BlockPos pos, AtmosphereState state) {
        this.pos = pos;
        this.stateFlags = state.toByte();
    }

    private AtmosphereResponsePacket(BlockPos pos, byte stateFlags) {
        this.pos = pos;
        this.stateFlags = stateFlags;
    }

    public static void encode(AtmosphereResponsePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeByte(pkt.stateFlags);
    }

    public static AtmosphereResponsePacket decode(FriendlyByteBuf buf) {
        return new AtmosphereResponsePacket(buf.readBlockPos(), buf.readByte());
    }

    public static void handle(AtmosphereResponsePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            AtmosphereClientCache.receive(pkt.pos, AtmosphereState.fromByte(pkt.stateFlags));
        });
        ctx.get().setPacketHandled(true);
    }
}
