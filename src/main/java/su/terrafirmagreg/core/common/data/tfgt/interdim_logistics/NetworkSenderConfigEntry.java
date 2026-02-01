package su.terrafirmagreg.core.common.data.tfgt.interdim_logistics;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;

public class NetworkSenderConfigEntry {
    @Getter
    private final InterplanetaryLogisticsNetwork.DimensionalBlockPos senderPartID;
    @Getter
    @Setter
    private InterplanetaryLogisticsNetwork.DimensionalBlockPos receiverPartID;
    @Getter
    @Setter
    private int senderDistinctInventory = 0;
    @Getter
    @Setter
    private int receiverDistinctInventory = 0;
    @Getter
    @Setter
    private TriggerMode currentSendTrigger = TriggerMode.ITEM;
    @Getter
    @Setter
    private int currentInactivityTimeout = 0;
    @Getter
    private CustomItemStackHandler currentSendFilter = new CustomItemStackHandler(3);

    public NetworkSenderConfigEntry(InterplanetaryLogisticsNetwork.DimensionalBlockPos sender) {
        senderPartID = sender;
    }

    public enum TriggerMode implements EnumSelectorWidget.SelectableEnum {
        ITEM("Item", "transfer_any"),
        REDSTONE_SIGNAL("Redstone signal", "transfer_any"),
        INACTIVITY("Inactivity (seconds)", "transfer_any");

        @Getter
        public final String tooltip;
        @Getter
        public final IGuiTexture icon;

        TriggerMode(String tooltip, String textureName) {
            this.tooltip = tooltip;
            this.icon = new ResourceTexture("gtceu:textures/gui/icon/transfer_mode/" + textureName + ".png");
        }
    }

    public NetworkSenderConfigEntry(CompoundTag tag) {
        senderPartID = new InterplanetaryLogisticsNetwork.DimensionalBlockPos(tag.getCompound("senderPartID"));
        if (tag.contains("receiverPartID")) {
            receiverPartID = new InterplanetaryLogisticsNetwork.DimensionalBlockPos(tag.getCompound("receiverPartID"));
        }
        senderDistinctInventory = tag.getInt("senderDistinctInventory");
        receiverDistinctInventory = tag.getInt("receiverDistinctInventory");
        currentSendTrigger = TriggerMode.values()[tag.getInt("currentSendTrigger")];
        currentSendFilter = new CustomItemStackHandler(3);
        currentSendFilter.deserializeNBT(tag.getCompound("currentSendFilter"));
        currentInactivityTimeout = tag.getInt("currentInactivityTimeout");
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.put("senderPartID", senderPartID.save());
        if (receiverPartID != null) {
            tag.put("receiverPartID", receiverPartID.save());
        }
        tag.put("currentSendFilter", currentSendFilter.serializeNBT());
        tag.putInt("currentInactivityTimeout", currentInactivityTimeout);
        tag.putInt("senderDistinctInventory", senderDistinctInventory);
        tag.putInt("receiverDistinctInventory", receiverDistinctInventory);
        tag.putInt("currentSendTrigger", currentSendTrigger.ordinal());
        return tag;
    }
}
