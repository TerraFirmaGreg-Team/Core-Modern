package su.terrafirmagreg.core.common.data.tfgt.interdim_logistics;

import java.util.ArrayList;
import java.util.List;

import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RailgunConfigUpdatePacket implements GTNetwork.INetPacket {

    private final List<NetworkPart> parts;

    public RailgunConfigUpdatePacket(FriendlyByteBuf buf) {
        parts = new ArrayList<>();
        var count = buf.readInt();
        for (int i = 0; i < count; i++) {
            var nbt = buf.readNbt();
            if (nbt == null)
                continue;

            var part = new NetworkPart(nbt);
            parts.add(part);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(parts.size());
        for (var part : parts) {
            buffer.writeNbt(part.save());
        }
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        InterplanetaryLogisticsNetwork network = InterplanetaryLogisticsNetwork.get(context.getSender().getServer().overworld());
        for (var part : parts) {
            if (network.parts.containsKey(part.getPartId())) {
                network.parts.put(part.getPartId(), part);
            }
        }
    }

    static {
        GTNetwork.register(RailgunConfigUpdatePacket.class, RailgunConfigUpdatePacket::new, NetworkDirection.PLAY_TO_SERVER);
    }
}
