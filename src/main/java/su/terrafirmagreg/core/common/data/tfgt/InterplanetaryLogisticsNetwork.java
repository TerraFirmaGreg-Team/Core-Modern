package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class InterplanetaryLogisticsNetwork {

    // TODO: Return network based on entity team
    private static InterplanetaryLogisticsNetwork NETWORK = null;
    public static InterplanetaryLogisticsNetwork get(IMachineBlockEntity entity) {
        if (NETWORK == null) {
            NETWORK = new InterplanetaryLogisticsNetwork(Objects.requireNonNull(entity.level().getServer()).overworld());
        }
        return NETWORK;
    }

    private final InterplanetaryLogisticsNetworkSavedData data;
    private final ServerLevel level;

    private InterplanetaryLogisticsNetwork(ServerLevel lvl) {
        level = lvl;
        data = InterplanetaryLogisticsNetworkSavedData.get(lvl);
    }

    public void loadPart(ILogisticsNetworkMachine machine) {
        data.parts.get(machine.getDimensionalPos()).setMachine(machine);
    }

    public void unloadPart(ILogisticsNetworkMachine machine) {
        data.parts.get(machine.getDimensionalPos()).setMachine(null);
    }

    public void createPart(ILogisticsNetworkMachine machine) {
        var newPart = new NetworkPart(machine.getDimensionalPos());
        newPart.setMachine(machine);

        data.parts.put(machine.getDimensionalPos(), newPart);
        data.setDirty();
    }

    public void destroyPart(ILogisticsNetworkMachine machine) {
        data.parts.remove(machine.getDimensionalPos());
        data.setDirty();
    }

    public @Nullable NetworkPart getPart(DimensionalBlockPos partId) {
        return data.parts.getOrDefault(partId, null);
    }

    /// Helper types & network part interfaces

    public record DimensionalBlockPos(String dimension, BlockPos pos) {
        public DimensionalBlockPos(MetaMachine machine) {
            this(Objects.requireNonNull(machine.getLevel()).dimension().location().toString(), machine.getPos());
        }

        public DimensionalBlockPos(CompoundTag tag) {
            this(tag.getString("dim"), new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.putString("dim", dimension);
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            return tag;
        }
    }

    public static class NetworkPart {
        @Getter
        private final DimensionalBlockPos partId;
        @Getter @Setter
        private @Nullable ILogisticsNetworkMachine machine;
        @Getter @Setter
        private String uiLabel;
        private final List<NetworkSenderConfigEntry> logisticsConfigurations;

        public NetworkPart(DimensionalBlockPos pos) {
            partId = pos;
            uiLabel = "";
            logisticsConfigurations = new ArrayList<>();
        }

        public NetworkPart(CompoundTag tag) {
            partId = new DimensionalBlockPos(tag.getCompound("partId"));
            uiLabel = tag.getString("uiLabel");
            logisticsConfigurations = new ArrayList<>();
            var configTags = tag.getList("logisticsConfigurations", Tag.TAG_COMPOUND);
            configTags.forEach(t -> logisticsConfigurations.add(new NetworkSenderConfigEntry((CompoundTag)t)));

        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("partId", partId.save());
            tag.putString("uiLabel", uiLabel);
            var configTags = new ListTag();
            logisticsConfigurations.forEach(c -> configTags.add(c.save()));
            tag.put("logisticsConfigurations", configTags);
            return tag;
        }
    }

    public static class NetworkSenderConfigEntry {
        @Getter
        private final DimensionalBlockPos senderPartID;
        @Getter
        private final DimensionalBlockPos recieverPartID;
        @Getter
        private int senderDistinctInventory;
        @Getter
        private int recieverDistinctInventory;
        @Getter
        private TriggerMode currentSendTrigger;

        public NetworkSenderConfigEntry(DimensionalBlockPos sender, DimensionalBlockPos reciever) {
            senderPartID = sender;
            recieverPartID = reciever;
            senderDistinctInventory = 0;
            recieverDistinctInventory = 0;
            currentSendTrigger = TriggerMode.ITEM;
        }

        public enum TriggerMode implements EnumSelectorWidget.SelectableEnum {
            ITEM("Item", "transfer_any"),
            REDSTONE_SIGNAL("Redstone signal", "transfer_any"),
            INACTIVITY("Inactivity", "transfer_any");

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
            senderPartID = new DimensionalBlockPos(tag.getCompound("senderPartID"));
            recieverPartID = new DimensionalBlockPos(tag.getCompound("recieverPartID"));
            senderDistinctInventory = tag.getInt("senderDistinctInventory");
            recieverDistinctInventory = tag.getInt("recieverDistinctInventory");
            currentSendTrigger = TriggerMode.values()[tag.getInt("currentSendTrigger")];
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("senderPartID", senderPartID.save());
            tag.put("recieverPartID", recieverPartID.save());
            tag.putInt("senderDistinctInventory", senderDistinctInventory);
            tag.putInt("recieverDistinctInventory", recieverDistinctInventory);
            tag.putInt("currentSendTrigger", currentSendTrigger.ordinal());
            return tag;
        }
    }


    public record ItemTransitPackage(DimensionalBlockPos sender, DimensionalBlockPos reciever, List<ItemStack> items, long travelTime, long launchedTick) {

        public ItemTransitPackage(CompoundTag tag) {
            this(new DimensionalBlockPos(tag.getCompound("sender")),
                    new DimensionalBlockPos(tag.getCompound("reciever")),
                    tag.getList("items", Tag.TAG_COMPOUND).stream().map(t -> ItemStack.of((CompoundTag) t)).toList(),
                    tag.getLong("travelTime"),
                    tag.getLong("launchedTick"));
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("sender", sender.save());
            tag.put("reciever", reciever.save());
            tag.putLong("travelTime", travelTime);
            tag.putLong("launchedTick", launchedTick);
            ListTag itemTags = new ListTag();
            items.forEach(i -> itemTags.add(i.save(new CompoundTag())));
            tag.put("items", itemTags);
            return tag;
        }
    }

    public sealed interface ILogisticsNetworkMachine permits ILogisticsNetworkSender, ILogisticsNetworkReciever  {
        default DimensionalBlockPos getDimensionalPos() {
            return new DimensionalBlockPos(getMachine());
        }
        MetaMachine getMachine();
    }

    public non-sealed interface ILogisticsNetworkSender extends ILogisticsNetworkMachine {
        void onLogisticsConfigurationsChanged();
    }

    public non-sealed interface ILogisticsNetworkReciever extends ILogisticsNetworkMachine {
        boolean canAcceptItems(int inventoryIndex, List<ItemStack> stacks);
        boolean isRecieverReady();
        void onPackageSent(ItemTransitPackage itemPackage);
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    private static class InterplanetaryLogisticsNetworkSavedData extends SavedData {
        private static final String DATA_ID = "tfg_interdim_logistics";

        public static InterplanetaryLogisticsNetworkSavedData get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(InterplanetaryLogisticsNetworkSavedData::new, InterplanetaryLogisticsNetworkSavedData::new, DATA_ID);
        }

        public final Map<DimensionalBlockPos, NetworkPart> parts = new HashMap<>();
        public final List<ItemTransitPackage> itemsInTransit = new ArrayList<>();

        private InterplanetaryLogisticsNetworkSavedData() {}
        private InterplanetaryLogisticsNetworkSavedData(CompoundTag tag) {

            var partsTag = tag.getList("networkParts", ListTag.TAG_COMPOUND);
            partsTag.forEach(t -> {
                var part = new NetworkPart((CompoundTag)t);
                parts.put(part.partId, part);
            });

            var itemTransitTag = tag.getList("itemsInTransit", ListTag.TAG_COMPOUND);
            itemTransitTag.forEach(t -> {
                itemsInTransit.add(new ItemTransitPackage((CompoundTag)t));
            });
        }

        @Override
        public CompoundTag save(CompoundTag pCompoundTag) {
            var partsTag = new ListTag();
            for (var part: parts.values()) {
                partsTag.add(part.save());
            }
            pCompoundTag.put("networkParts", partsTag);

            var itemsInTransitTag = new ListTag();
            for (var items: itemsInTransit) {
                itemsInTransitTag.add(items.save());
            }
            pCompoundTag.put("itemsInTransit", itemsInTransitTag);
            return pCompoundTag;
        }
    }
}
