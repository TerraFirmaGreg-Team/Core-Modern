package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.ClientDecompressionEvent;

/**
 * Server -> Client packet to start or stop a decompression sound and particle effect at a position.
 * The client-side instances handle decay autonomously.
 *
 * @param newPos     Only used for SHIFT: the new breach position.
 * @param breachAxis Outward breach axis (from inside to outside). Null for STOP.
 * @param inward     Whether the breach is an implosion.
 */
public record DecompressionEventPacket(Action action, BlockPos pos, @Nullable BlockPos newPos,
        int durationTicks, int elapsedTicks, @Nullable Vec3 breachAxis, boolean inward) {

    private enum Action {
        START, STOP, SHIFT
    }

    public static DecompressionEventPacket stop(BlockPos pos) {
        return new DecompressionEventPacket(Action.STOP, pos, null, 0, 0, null, false);
    }

    public static DecompressionEventPacket start(BlockPos pos, int durationTicks, Vec3 breachAxis, boolean inward) {
        return new DecompressionEventPacket(Action.START, pos, null, durationTicks, 0, breachAxis, inward);
    }

    public static DecompressionEventPacket shift(BlockPos oldPos, BlockPos newPos, int durationTicks, int elapsedTicks,
            Vec3 breachAxis, boolean inward) {
        return new DecompressionEventPacket(Action.SHIFT, oldPos, newPos, durationTicks, elapsedTicks, breachAxis, inward);
    }

    public static void encode(DecompressionEventPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.action);
        buf.writeBlockPos(pkt.pos);
        if (pkt.action == Action.START) {
            assert pkt.breachAxis != null;
            buf.writeVarInt(pkt.durationTicks);
            writeVec3(buf, pkt.breachAxis);
            buf.writeBoolean(pkt.inward);
        }
        if (pkt.action == Action.SHIFT) {
            assert pkt.newPos != null;
            assert pkt.breachAxis != null;
            buf.writeBlockPos(pkt.newPos);
            buf.writeVarInt(pkt.durationTicks);
            buf.writeVarInt(pkt.elapsedTicks);
            writeVec3(buf, pkt.breachAxis);
            buf.writeBoolean(pkt.inward);
        }
    }

    public static DecompressionEventPacket decode(FriendlyByteBuf buf) {
        Action action = buf.readEnum(Action.class);
        BlockPos pos = buf.readBlockPos();
        BlockPos newPos = null;
        int duration = 0, elapsed = 0;
        Vec3 breachAxis = null;
        boolean inward = false;
        if (action == Action.START) {
            duration = buf.readVarInt();
            breachAxis = readVec3(buf);
            inward = buf.readBoolean();
        }
        if (action == Action.SHIFT) {
            newPos = buf.readBlockPos();
            duration = buf.readVarInt();
            elapsed = buf.readVarInt();
            breachAxis = readVec3(buf);
            inward = buf.readBoolean();
        }
        return new DecompressionEventPacket(action, pos, newPos, duration, elapsed, breachAxis, inward);
    }

    private static void writeVec3(FriendlyByteBuf buf, Vec3 v) {
        buf.writeFloat((float) v.x);
        buf.writeFloat((float) v.y);
        buf.writeFloat((float) v.z);
    }

    private static Vec3 readVec3(FriendlyByteBuf buf) {
        return new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(DecompressionEventPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            switch (pkt.action) {
                case START -> ClientDecompressionEvent.start(pkt.pos, pkt.durationTicks, pkt.breachAxis, pkt.inward);
                case STOP -> ClientDecompressionEvent.stop(pkt.pos);
                case SHIFT -> ClientDecompressionEvent.shift(pkt.pos, pkt.newPos, pkt.durationTicks, pkt.elapsedTicks, pkt.breachAxis, pkt.inward);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
