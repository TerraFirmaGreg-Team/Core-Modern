package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.DecompressionSoundInstance;

/**
 * Server → Client packet to start or stop a decompression sound at a position.
 * The client-side sound instance handles volume/pitch decay autonomously.
 */
public class DecompressionSoundPacket {

    private final BlockPos pos;
    private final boolean start;
    private final int durationTicks;

    public DecompressionSoundPacket(BlockPos pos, boolean start, int durationTicks) {
        this.pos = pos;
        this.start = start;
        this.durationTicks = durationTicks;
    }

    /** Convenience: create a stop packet */
    public static DecompressionSoundPacket stop(BlockPos pos) {
        return new DecompressionSoundPacket(pos, false, 0);
    }

    /** Convenience: create a start packet */
    public static DecompressionSoundPacket start(BlockPos pos, int durationTicks) {
        return new DecompressionSoundPacket(pos, true, durationTicks);
    }

    public static void encode(DecompressionSoundPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeBoolean(pkt.start);
        if (pkt.start) {
            buf.writeVarInt(pkt.durationTicks);
        }
    }

    public static DecompressionSoundPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean start = buf.readBoolean();
        int duration = start ? buf.readVarInt() : 0;
        return new DecompressionSoundPacket(pos, start, duration);
    }

    public static void handle(DecompressionSoundPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (pkt.start) {
                DecompressionSoundInstance.start(pkt.pos, pkt.durationTicks);
            } else {
                DecompressionSoundInstance.stop(pkt.pos);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
