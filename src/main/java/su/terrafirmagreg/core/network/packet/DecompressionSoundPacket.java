package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.DecompressionSoundInstance;

/**
 * Server → Client packet to start or stop a decompression sound at a position.
 * The client-side sound instance handles volume/pitch decay autonomously.
 */
public class DecompressionSoundPacket {

    private enum Action {
        START, STOP, SHIFT
    }

    private final BlockPos pos;
    private final Action action;
    private final int durationTicks;
    private final int elapsedTicks;
    /** Only used for SHIFT: the new breach position. */
    @Nullable
    private final BlockPos newPos;

    private DecompressionSoundPacket(BlockPos pos, Action action, int durationTicks, int elapsedTicks, @Nullable BlockPos newPos) {
        this.pos = pos;
        this.action = action;
        this.durationTicks = durationTicks;
        this.elapsedTicks = elapsedTicks;
        this.newPos = newPos;
    }

    public static DecompressionSoundPacket stop(BlockPos pos) {
        return new DecompressionSoundPacket(pos, Action.STOP, 0, 0, null);
    }

    public static DecompressionSoundPacket start(BlockPos pos, int durationTicks) {
        return new DecompressionSoundPacket(pos, Action.START, durationTicks, 0, null);
    }

    public static DecompressionSoundPacket shift(BlockPos oldPos, BlockPos newPos, int durationTicks, int elapsedTicks) {
        return new DecompressionSoundPacket(oldPos, Action.SHIFT, durationTicks, elapsedTicks, newPos);
    }

    public static void encode(DecompressionSoundPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.action);
        buf.writeBlockPos(pkt.pos);
        if (pkt.action == Action.START)
            buf.writeVarInt(pkt.durationTicks);
        if (pkt.action == Action.SHIFT) {
            buf.writeBlockPos(pkt.newPos);
            buf.writeVarInt(pkt.durationTicks);
            buf.writeVarInt(pkt.elapsedTicks);
        }
    }

    public static DecompressionSoundPacket decode(FriendlyByteBuf buf) {
        Action action = buf.readEnum(Action.class);
        BlockPos pos = buf.readBlockPos();
        int duration = 0, elapsed = 0;
        BlockPos newPos = null;
        if (action == Action.START)
            duration = buf.readVarInt();
        if (action == Action.SHIFT) {
            newPos = buf.readBlockPos();
            duration = buf.readVarInt();
            elapsed = buf.readVarInt();
        }
        return new DecompressionSoundPacket(pos, action, duration, elapsed, newPos);
    }

    public static void handle(DecompressionSoundPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            switch (pkt.action) {
                case START -> DecompressionSoundInstance.start(pkt.pos, pkt.durationTicks);
                case STOP -> DecompressionSoundInstance.stop(pkt.pos);
                case SHIFT -> DecompressionSoundInstance.shift(pkt.pos, pkt.newPos, pkt.durationTicks, pkt.elapsedTicks);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
