package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class InterplanetaryLogisticsNetwork {

    private static InterplanetaryLogisticsNetwork NETWORK = null;
    public static InterplanetaryLogisticsNetwork get() {
        if (NETWORK == null) {
            NETWORK = new InterplanetaryLogisticsNetwork();
        }
        return NETWORK;
    }

    private final Map<DimensionalBlockPos, ILogisticsNetworkMachine> loadedMachines = new HashMap<>();

    public void loadPart(ILogisticsNetworkMachine machine) {
        loadedMachines.put(machine.getDimensionalPos(), machine);
    }

    public void unloadPart(ILogisticsNetworkMachine machine) {
        loadedMachines.remove(machine.getDimensionalPos());
    }

    public List<ILogisticsNetworkMachine> getMachinesVisibleToPlayer(Player player) {
        var id = player.getUUID();
        List<ILogisticsNetworkMachine> parts = new ArrayList<>();
        loadedMachines.forEach((k, v) -> {
            var owner = v.getMachine().getHolder().getOwner();
            if (owner instanceof FTBOwner ftbOwner) {
                var team = FTBTeamsAPI.api().getManager().getTeamByID(ftbOwner.getTeam().getTeamId());

                if (team.isPresent() && team.get().getRankForPlayer(id).isAllyOrBetter()) {
                    parts.add(v);
                }
            }
        });
        return Collections.unmodifiableList(parts);
    }

    public @Nullable ILogisticsNetworkMachine getNetworkMachine(DimensionalBlockPos partId) {
        return loadedMachines.get(partId);
    }

    /// Helper types & network part interfaces

    private static final int INV_ANY = -1;

    public sealed interface ILogisticsNetworkMachine permits ILogisticsNetworkSender, ILogisticsNetworkReciever  {
        default DimensionalBlockPos getDimensionalPos() {
            return new DimensionalBlockPos(getMachine());
        }

        String getLogisticsUILabel();
        void setLogisticsUILabel(String v);

        MetaMachine getMachine();
        boolean isMachineValid();

        List<NotifiableItemStackHandler> getInventories();
        boolean makeInventoryDistinct(int invIndex);
        boolean removeDistinctInventory(int invIndex);
    }

    public non-sealed interface ILogisticsNetworkSender extends ILogisticsNetworkMachine {
        List<NetworkSenderConfigEntry> getSendConfigurations();

        void onLogisticsConfigurationsChanged();
    }

    public non-sealed interface ILogisticsNetworkReciever extends ILogisticsNetworkMachine {
        boolean canAcceptItems(int inventoryIndex, List<ItemStack> stacks);
        boolean isRecieverReady();
        void onPackageSent(DimensionalBlockPos sender, List<ItemStack> items, long travelTime, long launchedTick);
    }

    public static class NetworkSenderConfigEntry implements ITagSerializable<CompoundTag> {
        @Getter @Setter
        private DimensionalBlockPos recieverPartID;
        @Getter @Setter
        private int senderDistinctInventory = INV_ANY;
        @Getter @Setter
        private int recieverDistinctInventory = INV_ANY;
        @Getter @Setter
        private TriggerMode currentSendTrigger = TriggerMode.ITEM;

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

        public void deserializeNBT(CompoundTag tag) {
            recieverPartID = new DimensionalBlockPos();
            recieverPartID.deserializeNBT(tag.getCompound("recieverPartID"));
            senderDistinctInventory = tag.getInt("senderDistinctInventory");
            recieverDistinctInventory = tag.getInt("recieverDistinctInventory");
            currentSendTrigger = TriggerMode.values()[tag.getInt("currentSendTrigger")];
        }

        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.put("recieverPartID", recieverPartID.serializeNBT());
            tag.putInt("senderDistinctInventory", senderDistinctInventory);
            tag.putInt("recieverDistinctInventory", recieverDistinctInventory);
            tag.putInt("currentSendTrigger", currentSendTrigger.ordinal());
            return tag;
        }
    }

    public static class DimensionalBlockPos implements ITagSerializable<CompoundTag> {
        public String dimension;
        public BlockPos pos;

        public DimensionalBlockPos() {}

        public DimensionalBlockPos(MetaMachine machine) {
            dimension = Objects.requireNonNull(machine.getLevel()).dimension().location().toString();
            pos = machine.getPos();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            dimension = tag.getString("dim");
            pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }

        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putString("dim", dimension);
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            return tag;
        }
    }
}
