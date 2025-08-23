package su.terrafirmagreg.core.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.client.OreHighlightVeinRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record OreHighlightVeinPacket(List<BlockPos> veinCenters) {

    public static void encode(@NotNull OreHighlightVeinPacket pkt, @NotNull FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.veinCenters.size());
        for (BlockPos pos : pkt.veinCenters) {
            buf.writeBlockPos(pos);
        }
    }

    @Contract("_ -> new")
    public static @NotNull OreHighlightVeinPacket decode(@NotNull FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<BlockPos> positions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            positions.add(buf.readBlockPos());
        }
        return new OreHighlightVeinPacket(positions);
    }

    public static void handle(OreHighlightVeinPacket pkt, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            OreHighlightVeinRenderer.addVeinHighlights(pkt.veinCenters);
        });
        ctx.get().setPacketHandled(true);
    }
}
