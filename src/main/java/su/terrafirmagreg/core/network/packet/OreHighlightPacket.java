package su.terrafirmagreg.core.network.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import su.terrafirmagreg.core.client.OreHighlightRenderer;

public record OreHighlightPacket(List<BlockPos> positions) {

    public static void encode(OreHighlightPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.positions.size());
        for (BlockPos pos : pkt.positions) {
            buf.writeBlockPos(pos);
        }
    }

    public static OreHighlightPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<BlockPos> positions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            positions.add(buf.readBlockPos());
        }
        return new OreHighlightPacket(positions);
    }

    public static void handle(OreHighlightPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            OreHighlightRenderer.addHighlights(pkt.positions); // fixed here
        });
        ctx.get().setPacketHandled(true);
    }
}
