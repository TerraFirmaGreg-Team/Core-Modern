package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.PoseSnapHelper;

/**
 * Server -> Client packet to force or release a pose on the local player.
 * The actual changes are applied atomically in Camera.tick() via CameraMixin.
 */
public class ForcedPosePacket {

    // Eye heights are hardcoded in Player.getStandingEyeHeight(): STANDING = 1.62, SWIMMING = 0.4
    public static final float SWIMMING_EYE_SHIFT = 1.62f - 0.4f;

    /** Null means release the forced pose. */
    @Nullable
    private final Pose pose;

    public ForcedPosePacket(@Nullable Pose pose) {
        this.pose = pose;
    }

    public static void encode(ForcedPosePacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.pose != null);
        if (pkt.pose != null)
            buf.writeEnum(pkt.pose);
    }

    public static ForcedPosePacket decode(FriendlyByteBuf buf) {
        Pose pose = buf.readBoolean() ? buf.readEnum(Pose.class) : null;
        return new ForcedPosePacket(pose);
    }

    public static void handle(ForcedPosePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player == null)
                return;
            float yShift = pkt.pose == Pose.SWIMMING ? SWIMMING_EYE_SHIFT : 0f;
            PoseSnapHelper.pendingSnap = new PoseSnapHelper.PoseSnap(pkt.pose, yShift);
        });
        ctx.get().setPacketHandled(true);
    }
}
