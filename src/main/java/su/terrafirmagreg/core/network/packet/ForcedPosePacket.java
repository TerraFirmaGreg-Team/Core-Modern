package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server → Client packet to set or clear a forced pose on the local player.
 * Also shifts the player by yShift so the eye level stays in place during the pose transition.
 */
public class ForcedPosePacket {

    private final boolean prone;
    /** Y shift to apply on the client to keep eye level stable. */
    private final float yShift;

    public ForcedPosePacket(boolean prone, float yShift) {
        this.prone = prone;
        this.yShift = yShift;
    }

    public static void encode(ForcedPosePacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.prone);
        buf.writeFloat(pkt.yShift);
    }

    public static ForcedPosePacket decode(FriendlyByteBuf buf) {
        return new ForcedPosePacket(buf.readBoolean(), buf.readFloat());
    }

    public static void handle(ForcedPosePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player == null)
                return;
            player.setForcedPose(pkt.prone ? Pose.SWIMMING : null);
            if (pkt.yShift != 0f)
                player.setPos(player.getX(), player.getY() + pkt.yShift, player.getZ());
        });
        ctx.get().setPacketHandled(true);
    }
}
