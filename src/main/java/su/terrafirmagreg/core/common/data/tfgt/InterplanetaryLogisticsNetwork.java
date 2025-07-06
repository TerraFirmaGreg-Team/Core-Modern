package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import su.terrafirmagreg.core.TFGCore;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

public class InterplanetaryLogisticsNetwork {

    private static InterplanetaryLogisticsNetwork NETWORK = null;
    public static InterplanetaryLogisticsNetwork get(IMachineBlockEntity entity) {
        if (NETWORK == null) {
            NETWORK = new InterplanetaryLogisticsNetwork(Objects.requireNonNull(entity.level().getServer()).overworld());
        }
        return NETWORK;
    }

    private final Map<DimensionalBlockPos, ILogisticsNetworkMachine> loadedMachines = new HashMap<>();
    private final InterplanetaryLogisticsNetworkSavedData data;

    private InterplanetaryLogisticsNetwork(ServerLevel lvl) {
        data = InterplanetaryLogisticsNetworkSavedData.get(lvl);
    }

    public void loadOrCreatePart(ILogisticsNetworkMachine machine) {

        boolean isReciever = machine instanceof ILogisticsNetworkReciever;

        var owner = machine.getMachine().getHolder().getOwner();
        if (owner instanceof FTBOwner ftbOwner) {
            loadedMachines.put(machine.getDimensionalPos(), machine);
            data.parts.computeIfAbsent(machine.getDimensionalPos(), k -> {
                data.setDirty();
                return new NetworkPart(k, ftbOwner.getTeam().getTeamId(), isReciever);
            });
            return;
        }
        TFGCore.LOGGER.warn("Interplanetary logistics machine does not have a valid FTB owner. {} {}", machine.getDimensionalPos(), machine.getMachine());

    }

    public void unloadPart(ILogisticsNetworkMachine machine) {
        loadedMachines.remove(machine.getDimensionalPos());
    }

    public void destroyPart(ILogisticsNetworkMachine machine) {
        loadedMachines.remove(machine.getDimensionalPos());
        data.parts.remove(machine.getDimensionalPos());
        data.setDirty();
    }

    public List<NetworkPart> getPartsVisibleToPlayer(Player player) {
        var id = player.getUUID();
        List<NetworkPart> parts = new ArrayList<>();
        data.parts.forEach((k, v) -> {
            var team = FTBTeamsAPI.api().getManager().getTeamByID(v.getOwnerId());

            if (team.isPresent() && team.get().getRankForPlayer(id).isAllyOrBetter()) {
                parts.add(v);
            }
        });
        return Collections.unmodifiableList(parts);
    }

    public @Nullable NetworkPart getPart(DimensionalBlockPos partId) {
        return data.parts.get(partId);
    }

    public @Nullable ILogisticsNetworkMachine getNetworkMachine(DimensionalBlockPos partId) {
        return loadedMachines.get(partId);
    }

    public void markDirty() {
        data.setDirty();
    }

    /// Helper types & network part interfaces

    private static final int INV_ANY = -1;

    public sealed interface ILogisticsNetworkMachine permits ILogisticsNetworkSender, ILogisticsNetworkReciever  {
        default DimensionalBlockPos getDimensionalPos() {
            return new DimensionalBlockPos(getMachine());
        }
        default InterplanetaryLogisticsNetwork getLogisticsNetwork() {
            return InterplanetaryLogisticsNetwork.get(getMachine().getHolder());
        }

        MetaMachine getMachine();
        boolean isMachineValid();

        List<NotifiableItemStackHandler> getInventories();
        boolean makeInventoryDistinct(int invIndex);
        boolean removeDistinctInventory(int invIndex);
    }

    public non-sealed interface ILogisticsNetworkSender extends ILogisticsNetworkMachine {
        default List<NetworkSenderConfigEntry> getSendConfigurations() {
            return Collections.unmodifiableList(Objects.requireNonNull(getLogisticsNetwork().getPart(getDimensionalPos())).logisticsConfigurations);
        }

        void onLogisticsConfigurationsChanged();
    }

    public non-sealed interface ILogisticsNetworkReciever extends ILogisticsNetworkMachine {
        boolean canAcceptItems(int inventoryIndex, List<ItemStack> stacks);
        boolean isRecieverReady();
        void onPackageSent(ItemTransitPackage itemPackage);
    }

    public static class NetworkPart {
        @Getter
        private final DimensionalBlockPos partId;
        @Getter @Setter
        private String uiLabel;
        @Getter
        private final boolean isRecieverPart;

        public final List<NetworkSenderConfigEntry> logisticsConfigurations;
        @Getter
        private final UUID ownerId;
        public NetworkPart(DimensionalBlockPos pos, UUID owner, boolean reciever) {
            partId = pos;
            uiLabel = "[unnamed]";
            ownerId = owner;
            isRecieverPart = reciever;
            logisticsConfigurations = new ArrayList<>();
        }

        public NetworkPart(CompoundTag tag) {
            partId = new DimensionalBlockPos(tag.getCompound("partId"));
            uiLabel = tag.getString("uiLabel");
            ownerId = tag.getUUID("ftbOwner");
            isRecieverPart = tag.getBoolean("isRecieverPart");
            logisticsConfigurations = new ArrayList<>();
            if (!isRecieverPart) tag.getList("logisticsConfigurations", Tag.TAG_COMPOUND).forEach(t -> logisticsConfigurations.add(new NetworkSenderConfigEntry((CompoundTag)t)));
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("partId", partId.save());
            tag.putString("uiLabel", uiLabel);
            tag.putUUID("ftbOwner", ownerId);
            tag.putBoolean("isRecieverPart", isRecieverPart);
            var configTags = new ListTag();
            if (!isRecieverPart) logisticsConfigurations.forEach(c -> {
                if (c.recieverPartID != null) configTags.add(c.save());
            });
            tag.put("logisticsConfigurations", configTags);
            return tag;
        }
    }

    public static class NetworkSenderConfigEntry {
        @Getter
        private final DimensionalBlockPos senderPartID;
        @Getter @Setter
        private DimensionalBlockPos recieverPartID;
        @Getter @Setter
        private int senderDistinctInventory = INV_ANY;
        @Getter @Setter
        private int recieverDistinctInventory = INV_ANY;
        @Getter @Setter
        private TriggerMode currentSendTrigger = TriggerMode.ITEM;
        @Getter @Setter
        private int currentInactivityTimeout = 0;
        @Getter
        private CustomItemStackHandler currentSendFilter = new CustomItemStackHandler(3);
        public NetworkSenderConfigEntry(DimensionalBlockPos sender) {
            senderPartID = sender;
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
            currentSendFilter = new CustomItemStackHandler(3);
            currentSendFilter.deserializeNBT(tag.getCompound("currentSendFilter"));
            currentInactivityTimeout = tag.getInt("currentInactivityTimeout");
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("senderPartID", senderPartID.save());
            tag.put("recieverPartID", recieverPartID.save());
            tag.put("currentSendFilter", currentSendFilter.serializeNBT());
            tag.putInt("currentInactivityTimeout", currentInactivityTimeout);
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

        public String getUiString() {
            return "%s (%s, %s, %s)".formatted(dimension, pos.getX(), pos.getY(), pos.getZ());
        }
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
            itemTransitTag.forEach(t -> itemsInTransit.add(new ItemTransitPackage((CompoundTag)t)));
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
