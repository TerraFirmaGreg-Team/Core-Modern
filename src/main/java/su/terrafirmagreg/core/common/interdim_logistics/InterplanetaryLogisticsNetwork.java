package su.terrafirmagreg.core.common.interdim_logistics;

import java.util.*;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunItemBusMachine;

public class InterplanetaryLogisticsNetwork extends SavedData {
    private static final String DATA_ID = "tfg_interdim_logistics";

    // Distances between planets
    // The travel time of item payloads is the difference in distance (measured in seconds)
    // Dimensions without a distance value are blacklisted
    public static final Map<String, Integer> DIMENSION_DISTANCES = new HashMap<>();
    static {
        DIMENSION_DISTANCES.put("minecraft:overworld", 0);
        DIMENSION_DISTANCES.put("ad_astra:earth_orbit", 0);
        DIMENSION_DISTANCES.put("ad_astra:moon", 60);
        DIMENSION_DISTANCES.put("ad_astra:moon_orbit", 60);
        DIMENSION_DISTANCES.put("ad_astra:mars", 120);
        DIMENSION_DISTANCES.put("ad_astra:mars_orbit", 120);
    }

    public final Map<DimensionalBlockPos, NetworkPart> parts = new HashMap<>();

    public static InterplanetaryLogisticsNetwork get(IMachineBlockEntity entity) {
        return Objects.requireNonNull(Objects.requireNonNull(entity.self().getLevel()).getServer()).overworld().getDataStorage().computeIfAbsent(InterplanetaryLogisticsNetwork::new,
                InterplanetaryLogisticsNetwork::new, DATA_ID);
    }

    private final Map<DimensionalBlockPos, ILogisticsNetworkMachine> loadedMachines = new HashMap<>();

    private InterplanetaryLogisticsNetwork(CompoundTag tag) {
        var partsTag = tag.getList("networkParts", ListTag.TAG_COMPOUND);
        partsTag.forEach(t -> {
            var part = new NetworkPart((CompoundTag) t);
            parts.put(part.getPartId(), part);
        });
    }

    private InterplanetaryLogisticsNetwork() {
    }

    public void loadOrCreatePart(ILogisticsNetworkMachine machine) {

        boolean isReceiver = machine instanceof ILogisticsNetworkReceiver;

        var owner = machine.getMachine().getOwner();
        if (owner instanceof FTBOwner ftbOwner) {
            loadedMachines.put(machine.getDimensionalPos(), machine);
            parts.computeIfAbsent(machine.getDimensionalPos(), k -> {
                setDirty();
                return new NetworkPart(k, ftbOwner.getTeam().getTeamId(), isReceiver);
            });
            return;
        }
        TFGCore.LOGGER.warn("Interplanetary logistics machine does not have a valid FTB owner. {} {}",
                machine.getDimensionalPos(), machine.getMachine());

    }

    public void unloadPart(ILogisticsNetworkMachine machine) {
        loadedMachines.remove(machine.getDimensionalPos());
    }

    public void destroyPart(ILogisticsNetworkMachine machine) {
        loadedMachines.remove(machine.getDimensionalPos());
        parts.remove(machine.getDimensionalPos());
        setDirty();
    }

    public List<NetworkPart> getPartsVisibleToPlayer(Player player) {
        var id = player.getUUID();
        List<NetworkPart> visible = new ArrayList<>();
        parts.forEach((k, v) -> {
            var team = FTBTeamsAPI.api().getManager().getTeamByID(v.getOwnerId());

            if (team.isPresent() && team.get().getRankForPlayer(id).isAllyOrBetter()) {
                visible.add(v);
            }
        });
        return Collections.unmodifiableList(visible);
    }

    public @Nullable NetworkPart getPart(DimensionalBlockPos partId) {
        return parts.get(partId);
    }

    public @Nullable ILogisticsNetworkMachine getNetworkMachine(DimensionalBlockPos partId) {
        return loadedMachines.get(partId);
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
            return Collections.unmodifiableList(
                    Objects.requireNonNull(getLogisticsNetwork().getPart(getDimensionalPos())).senderLogisticsConfigs);
        }
    }

    public non-sealed interface ILogisticsNetworkReceiver extends ILogisticsNetworkMachine {
        boolean canAcceptItems(int inventoryIndex, List<ItemStack> stacks);

        void onPackageSent(int inventoryIndex, List<ItemStack> items, int travelDuration);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        var partsTag = new ListTag();
        for (var part : parts.values()) {
            partsTag.add(part.save());
        }
        pCompoundTag.put("networkParts", partsTag);
        return pCompoundTag;
    }

}
