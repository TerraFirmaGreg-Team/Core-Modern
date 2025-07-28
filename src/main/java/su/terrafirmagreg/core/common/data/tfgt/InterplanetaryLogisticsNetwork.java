package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunItemBusMachine;

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

        boolean isReceiver = machine instanceof ILogisticsNetworkReceiver;

        var owner = machine.getMachine().getOwner();
        if (owner instanceof FTBOwner ftbOwner) {
            loadedMachines.put(machine.getDimensionalPos(), machine);
            data.parts.computeIfAbsent(machine.getDimensionalPos(), k -> {
                data.setDirty();
                return new NetworkPart(k, ftbOwner.getTeam().getTeamId(), isReceiver);
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

    public sealed interface ILogisticsNetworkMachine permits ILogisticsNetworkSender, ILogisticsNetworkReceiver {
        default DimensionalBlockPos getDimensionalPos() {
            return new DimensionalBlockPos(getMachine());
        }
        default InterplanetaryLogisticsNetwork getLogisticsNetwork() {
            return InterplanetaryLogisticsNetwork.get(getMachine().getHolder());
        }

        MetaMachine getMachine();
        boolean isMachineInvalid();

        List<RailgunItemBusMachine> getInventories();
        Component getCurrentStatusText();
    }

    public non-sealed interface ILogisticsNetworkSender extends ILogisticsNetworkMachine {
        default List<NetworkSenderConfigEntry> getSendConfigurations() {
            return Collections.unmodifiableList(Objects.requireNonNull(getLogisticsNetwork().getPart(getDimensionalPos())).senderLogisticsConfigs);
        }
    }

    public non-sealed interface ILogisticsNetworkReceiver extends ILogisticsNetworkMachine {
        boolean canAcceptItems(int inventoryIndex, List<ItemStack> stacks);
        void onPackageSent(int inventoryIndex, List<ItemStack> items, int travelDuration);
    }

    public static class NetworkPart {
        @Getter
        private final DimensionalBlockPos partId;
        @Getter @Setter
        private String uiLabel;
        @Getter
        private final boolean isReceiverPart;

        public final List<NetworkSenderConfigEntry> senderLogisticsConfigs;
        public final List<NetworkReceiverConfigEntry> receiverLogisticsConfigs;
        @Getter
        private final UUID ownerId;
        public NetworkPart(DimensionalBlockPos pos, UUID owner, boolean rec) {
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
            partId = new DimensionalBlockPos(tag.getCompound("partId"));
            uiLabel = tag.getString("uiLabel");
            ownerId = tag.getUUID("ftbOwner");
            isReceiverPart = tag.getBoolean("isReceiverPart");
            senderLogisticsConfigs = new ArrayList<>();
            receiverLogisticsConfigs = new ArrayList<>();
            if (isReceiverPart) tag.getList("receiverLogisticsConfigs", Tag.TAG_COMPOUND).forEach(t -> receiverLogisticsConfigs.add(new NetworkReceiverConfigEntry((CompoundTag)t)));
            else tag.getList("senderLogisticsConfigs", Tag.TAG_COMPOUND).forEach(t -> senderLogisticsConfigs.add(new NetworkSenderConfigEntry((CompoundTag)t)));
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.put("partId", partId.save());
            tag.putString("uiLabel", uiLabel);
            tag.putUUID("ftbOwner", ownerId);
            tag.putBoolean("isReceiverPart", isReceiverPart);
            var sendConfigs = new ListTag();
            var receiveConfigs = new ListTag();
            if (isReceiverPart) receiverLogisticsConfigs.forEach(c -> receiveConfigs.add(c.save()));
            else senderLogisticsConfigs.forEach(c -> {
                if (c.receiverPartID != null) sendConfigs.add(c.save());
            });
            tag.put("senderLogisticsConfigs", sendConfigs);
            tag.put("receiverLogisticsConfigs", receiveConfigs);
            return tag;
        }
    }

    public static class NetworkReceiverConfigEntry {
        @Getter @Setter
        private int distinctInventory;
        @Getter @Setter
        private LogicMode currentMode;
        @Getter @Setter
        private int currentCooldown;

        public enum LogicMode implements EnumSelectorWidget.SelectableEnum {
            COOLDOWN("Cooldown after receiving (seconds)", "transfer_any"),
            REDSTONE_ENABLE("Enable when receiving redstone signal", "transfer_any"),
            REDSTONE_DISABLE("Disable when receiving redstone signal", "transfer_any");

            @Getter
            public final String tooltip;
            @Getter
            public final IGuiTexture icon;

            LogicMode(String tooltip, String textureName) {
                this.tooltip = tooltip;
                this.icon = new ResourceTexture("gtceu:textures/gui/icon/transfer_mode/" + textureName + ".png");
            }

        }

        public NetworkReceiverConfigEntry(int inv) {
            distinctInventory = inv;
            currentCooldown = 0;
            currentMode = LogicMode.COOLDOWN;

        }

        public NetworkReceiverConfigEntry(CompoundTag tag) {
            distinctInventory = tag.getInt("circuit");
            currentMode = LogicMode.values()[tag.getInt("currentMode")];
            currentCooldown = tag.getInt("currentCooldown");
        }

        public CompoundTag save() {
            var tag = new CompoundTag();
            tag.putInt("circuit", distinctInventory);
            tag.putInt("currentMode", currentMode.ordinal());
            tag.putInt("currentCooldown", currentCooldown);
            return tag;
        }
    }

    public static class NetworkSenderConfigEntry {
        @Getter
        private final DimensionalBlockPos senderPartID;
        @Getter @Setter
        private DimensionalBlockPos receiverPartID;
        @Getter @Setter
        private int senderDistinctInventory = 0;
        @Getter @Setter
        private int receiverDistinctInventory = 0;
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
            senderPartID = new DimensionalBlockPos(tag.getCompound("senderPartID"));
            receiverPartID = new DimensionalBlockPos(tag.getCompound("receiverPartID"));
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
            tag.put("receiverPartID", receiverPartID.save());
            tag.put("currentSendFilter", currentSendFilter.serializeNBT());
            tag.putInt("currentInactivityTimeout", currentInactivityTimeout);
            tag.putInt("senderDistinctInventory", senderDistinctInventory);
            tag.putInt("receiverDistinctInventory", receiverDistinctInventory);
            tag.putInt("currentSendTrigger", currentSendTrigger.ordinal());
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

        private InterplanetaryLogisticsNetworkSavedData() {}
        private InterplanetaryLogisticsNetworkSavedData(CompoundTag tag) {

            var partsTag = tag.getList("networkParts", ListTag.TAG_COMPOUND);
            partsTag.forEach(t -> {
                var part = new NetworkPart((CompoundTag)t);
                parts.put(part.partId, part);
            });

        }

        @Override
        public CompoundTag save(CompoundTag pCompoundTag) {
            var partsTag = new ListTag();
            for (var part: parts.values()) {
                partsTag.add(part.save());
            }
            pCompoundTag.put("networkParts", partsTag);
            return pCompoundTag;
        }
    }
}
