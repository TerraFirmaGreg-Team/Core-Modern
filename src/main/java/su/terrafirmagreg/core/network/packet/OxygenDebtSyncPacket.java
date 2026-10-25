package su.terrafirmagreg.core.network.packet;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.ClientHypoxiaHandler;

public record OxygenDebtSyncPacket(int oxygenDebt) {

    public static void encode(OxygenDebtSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.oxygenDebt);
    }

    public static OxygenDebtSyncPacket decode(FriendlyByteBuf buffer) {
        return new OxygenDebtSyncPacket(buffer.readVarInt());
    }

    public static void handle(OxygenDebtSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet)));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(OxygenDebtSyncPacket packet) {
        ClientHypoxiaHandler.setOxygenDebt(packet.oxygenDebt());
    }
}
