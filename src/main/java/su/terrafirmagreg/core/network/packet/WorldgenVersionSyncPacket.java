package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.ClientWorldgenVersion;

/**
 * Packet to send resolved OVERWORLD_VERSION on login
 */
public record WorldgenVersionSyncPacket(int overworldVersion) {

    public static void encode(WorldgenVersionSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.overworldVersion);
    }

    public static WorldgenVersionSyncPacket decode(FriendlyByteBuf buffer) {
        return new WorldgenVersionSyncPacket(buffer.readVarInt());
    }

    public static void handle(WorldgenVersionSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet)));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(WorldgenVersionSyncPacket packet) {
        ClientWorldgenVersion.apply(packet.overworldVersion());
    }
}
