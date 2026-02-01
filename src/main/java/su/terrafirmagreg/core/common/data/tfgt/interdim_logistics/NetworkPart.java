package su.terrafirmagreg.core.common.data.tfgt.interdim_logistics;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NetworkPart {
    @Getter
    private final InterplanetaryLogisticsNetwork.DimensionalBlockPos partId;
    @Getter
    @Setter
    private String uiLabel;
    @Getter
    private final boolean isReceiverPart;

    public final List<NetworkSenderConfigEntry> senderLogisticsConfigs;
    public final List<NetworkReceiverConfigEntry> receiverLogisticsConfigs;
    @Getter
    private final UUID ownerId;

    public NetworkPart(InterplanetaryLogisticsNetwork.DimensionalBlockPos pos, UUID owner, boolean rec) {
        partId = pos;
        uiLabel = "[unnamed]";
        ownerId = owner;
        isReceiverPart = rec;
        senderLogisticsConfigs = new ArrayList<>();
        receiverLogisticsConfigs = new ArrayList<>();
        if (isReceiverPart) {
            for (int i = 0; i < 33; i++) {
                receiverLogisticsConfigs.add(new NetworkReceiverConfigEntry(i));
            }
        }
    }

    public NetworkPart(CompoundTag tag) {
        partId = new InterplanetaryLogisticsNetwork.DimensionalBlockPos(tag.getCompound("partId"));
        uiLabel = tag.getString("uiLabel");
        ownerId = tag.getUUID("ftbOwner");
        isReceiverPart = tag.getBoolean("isReceiverPart");
        senderLogisticsConfigs = new ArrayList<>();
        receiverLogisticsConfigs = new ArrayList<>();
        if (isReceiverPart)
            tag.getList("receiverLogisticsConfigs", Tag.TAG_COMPOUND)
                    .forEach(t -> receiverLogisticsConfigs.add(new NetworkReceiverConfigEntry((CompoundTag) t)));
        else
            tag.getList("senderLogisticsConfigs", Tag.TAG_COMPOUND)
                    .forEach(t -> senderLogisticsConfigs.add(new NetworkSenderConfigEntry((CompoundTag) t)));
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.put("partId", partId.save());
        tag.putString("uiLabel", uiLabel);
        tag.putUUID("ftbOwner", ownerId);
        tag.putBoolean("isReceiverPart", isReceiverPart);
        var sendConfigs = new ListTag();
        var receiveConfigs = new ListTag();
        if (isReceiverPart)
            receiverLogisticsConfigs.forEach(c -> receiveConfigs.add(c.save()));
        else
            senderLogisticsConfigs.forEach(c -> sendConfigs.add(c.save()));
        tag.put("senderLogisticsConfigs", sendConfigs);
        tag.put("receiverLogisticsConfigs", receiveConfigs);
        return tag;
    }
}
