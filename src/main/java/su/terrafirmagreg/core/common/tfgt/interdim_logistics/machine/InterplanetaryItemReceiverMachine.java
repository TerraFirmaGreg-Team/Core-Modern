package su.terrafirmagreg.core.common.tfgt.interdim_logistics.machine;

import java.util.*;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.tfgt.interdim_logistics.InterplanetaryLogisticsNetwork;
import su.terrafirmagreg.core.common.tfgt.interdim_logistics.InterplanetaryLogisticsNetwork.*;
import su.terrafirmagreg.core.common.tfgt.interdim_logistics.NetworkReceiverConfigEntry;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.part.RailgunItemBusMachine;

public class InterplanetaryItemReceiverMachine extends WorkableElectricMultiblockMachine
        implements ILogisticsNetworkReceiver {

    @SaveField
    private final List<ItemPayload> payloads = new ArrayList<>();

    private final List<RailgunItemBusMachine> itemOutputs = new ArrayList<>();
    private final long[] lastActiveTime = new long[33];
    protected TickableSubscription tickSubscription;

    public InterplanetaryItemReceiverMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    public InterplanetaryItemReceiverMachine getMachine() {
        return this;
    }

    @Override
    public boolean isMachineInvalid() {
        return !isFormed() || isRemoved();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel sLvl && InterplanetaryLogisticsNetwork.DIMENSION_DISTANCES.containsKey(getDimensionalPos().dimension()))
            sLvl.getServer().tell(new TickTask(0, () -> getLogisticsNetwork().loadOrCreatePart(this)));
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (!isRemote())
            getLogisticsNetwork().unloadPart(this);
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        var server = Objects.requireNonNull(getLevel()).getServer();
        if (server == null)
            return;
        Arrays.fill(lastActiveTime, getLevel().getGameTime());
        itemOutputs.clear();
        itemOutputs.addAll(getInventories());
        updateSubscription();
    }

    @Override
    public void invalidateStructure(@NotNull String substructureName) {
        super.invalidateStructure(substructureName);
        updateSubscription();
        itemOutputs.clear();
    }

    @Override
    public void onMachineDestroyed() {
        if (!isRemote())
            getLogisticsNetwork().destroyPart(this);
    }

    @Override
    public List<RailgunItemBusMachine> getInventories() {
        if (isMachineInvalid())
            return List.of();
        List<RailgunItemBusMachine> parts = new ArrayList<>();
        for (var part : getParts()) {
            if (part instanceof RailgunItemBusMachine r)
                parts.add(r);
        }
        return parts;
    }

    @Override
    public boolean canAcceptItems(int inventoryIndex, List<ItemStack> stacks) {
        var withCircuit = itemOutputs.stream()
                .filter((c) -> c.getCircuitSlot().getCurrentCircuit() == inventoryIndex
                        && c.isWorkingEnabled())
                .toList();
        if (withCircuit.isEmpty())
            return false;

        var part = getLogisticsNetwork().getPart(getDimensionalPos());
        if (part == null) {
            TFGCore.LOGGER.warn("Interplanetary logistics receiver is missing its network part. {}", getDimensionalPos());
            return false;
        }

        var config = part.receiverLogisticsConfigs.get(inventoryIndex);
        var currentTick = Objects.requireNonNull(getLevel()).getGameTime();
        if (config.getCurrentMode() == NetworkReceiverConfigEntry.LogicMode.COOLDOWN
                && lastActiveTime[inventoryIndex] + 20L * config.getCurrentCooldown() > currentTick) {
            return false;
        } else if (config.getCurrentMode() == NetworkReceiverConfigEntry.LogicMode.REDSTONE_DISABLE) {
            for (var bus : withCircuit) {
                if (getLevel().hasNeighborSignal(bus.getBlockPos()))
                    return false;
            }
        } else if (config.getCurrentMode() == NetworkReceiverConfigEntry.LogicMode.REDSTONE_ENABLE) {
            var hasFoundSignal = false;
            for (var bus : withCircuit) {
                if (getLevel().hasNeighborSignal(bus.getBlockPos())) {
                    hasFoundSignal = true;
                    break;
                }
            }
            if (!hasFoundSignal)
                return false;
        }

        List<ItemStack> remaining = new ArrayList<>();

        for (var stack : stacks) {
            remaining.add(stack.copy());
        }

        for (RailgunItemBusMachine outputBus : withCircuit) {
            var inventory = outputBus.getInventory();
            CustomItemStackHandler simulatedInsert = new CustomItemStackHandler(outputBus.getInventory().getSlots());
            for (int i = 0; i < inventory.getSlots(); i++) {
                simulatedInsert.setStackInSlot(i, inventory.getStackInSlot(i).copy());
            }

            for (var iter = remaining.listIterator(); iter.hasNext();) {
                var stack = iter.next();
                for (int i = 0; i < simulatedInsert.getSlots(); i++) {
                    stack = simulatedInsert.insertItem(i, stack, false);
                }
                if (stack.isEmpty())
                    iter.remove();
            }
        }

        return remaining.isEmpty();
    }

    private void onPackageArrival(ItemPayload payload) {
        lastActiveTime[payload.inventoryIndex] = Objects.requireNonNull(getLevel()).getGameTime();
        var withCircuit = itemOutputs.stream()
                .filter((c) -> c.getCircuitSlot().getCurrentCircuit() == payload.inventoryIndex
                        && c.isWorkingEnabled())
                .toList();

        for (ItemStack itemToInsert : payload.items) {
            var amountLeft = itemToInsert.copy();
            for (RailgunItemBusMachine outputBus : withCircuit) {
                var inventory = outputBus.getInventory();
                for (int i = 0; i < inventory.getSlots(); i++) {
                    amountLeft = inventory.insertItemInternal(i, amountLeft, false);
                    if (amountLeft == ItemStack.EMPTY)
                        break;
                }
                if (amountLeft == ItemStack.EMPTY)
                    break;
            }
        }
    }

    @Override
    public void onPackageSent(int inventoryIndex, List<ItemStack> items, int travelDuration) {
        var payload = new ItemPayload(getLevel().getGameTime(), travelDuration, items, inventoryIndex);
        payloads.add(payload);
    }

    @Override
    public Component getCurrentStatusText() {
        if (!isFormed())
            return Component.literal("§cMultiblock not formed");
        return null;
    }

    private void updateSubscription() {
        if (isRemote())
            return;
        if (isFormed() && isWorkingEnabled()) {
            tickSubscription = subscribeServerTick(tickSubscription, this::tick);
        } else {
            if (tickSubscription != null)
                tickSubscription.unsubscribe();
        }
    }

    private void tick() {
        if (Objects.requireNonNull(getLevel()).getGameTime() % 20 != 0) {
            var payloadIter = payloads.iterator();
            while (payloadIter.hasNext()) {
                var payload = payloadIter.next();
                if (payload.launchTick + payload.travelDuration <= getLevel().getGameTime()) {
                    onPackageArrival(payload);
                    payloadIter.remove();
                }
            }
        }
    }

    private static class ItemPayload implements INBTSerializable<CompoundTag> {
        public int travelDuration;
        public List<ItemStack> items;
        public int inventoryIndex;
        public long launchTick;

        @SuppressWarnings("unused")
        public ItemPayload() {
            travelDuration = 0;
            items = new ArrayList<>();
            inventoryIndex = 0;
        }

        public ItemPayload(long launchTick, int travelDuration, List<ItemStack> items, int inventoryIndex) {
            this.launchTick = launchTick;
            this.travelDuration = travelDuration;
            this.items = items;
            this.inventoryIndex = inventoryIndex;
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putInt("travelDuration", travelDuration);
            tag.putInt("inventoryIndex", inventoryIndex);
            tag.putLong("launchTick", launchTick);
            var list = new ListTag();
            for (var s : items) {
                list.add(s.save(new CompoundTag()));
            }
            tag.put("items", list);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            travelDuration = nbt.getInt("travelDuration");
            inventoryIndex = nbt.getInt("inventoryIndex");
            launchTick = nbt.getLong("launchTick");
            var list = nbt.getList("items", Tag.TAG_COMPOUND);
            for (Tag tag : list) {
                items.add(ItemStack.of((CompoundTag) tag));
            }
        }
    }
}
