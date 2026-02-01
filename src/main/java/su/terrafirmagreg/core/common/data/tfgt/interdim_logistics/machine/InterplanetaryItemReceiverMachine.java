package su.terrafirmagreg.core.common.data.tfgt.interdim_logistics.machine;

import java.util.*;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.data.tfgt.interdim_logistics.InterplanetaryLogisticsNetwork.*;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunItemBusMachine;

public class InterplanetaryItemReceiverMachine extends WorkableElectricMultiblockMachine
        implements ILogisticsNetworkReceiver, IMachineLife, IFancyUIMachine, IDisplayUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            InterplanetaryItemReceiverMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private final List<ItemPayload> payloads = new ArrayList<>();

    private final List<RailgunItemBusMachine> itemOutputs = new ArrayList<>();
    private final long[] lastActiveTime = new long[33];

    public InterplanetaryItemReceiverMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public InterplanetaryItemReceiverMachine getMachine() {
        return this;
    }

    @Override
    public boolean isMachineInvalid() {
        return !isFormed() || isInValid();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel sLvl)
            sLvl.getServer().tell(new TickTask(0, () -> {
                getLogisticsNetwork().loadOrCreatePart(this);
                payloads.forEach(p -> sLvl.getServer().tell(new TickTask(p.travelDuration, () -> onPackageArrival(p))));
            }));
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (!isRemote())
            getLogisticsNetwork().unloadPart(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var server = Objects.requireNonNull(getLevel()).getServer();
        if (server == null)
            return;
        Arrays.fill(lastActiveTime, getLevel().getGameTime());
        itemOutputs.clear();
        itemOutputs.addAll(getInventories());

    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        itemOutputs.clear();
    }

    @Override
    public void onMachineRemoved() {
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
                .filter((c) -> IntCircuitBehaviour
                        .getCircuitConfiguration(c.getCircuitInventory().getStackInSlot(0)) == inventoryIndex
                        && c.isWorkingEnabled())
                .toList();
        if (withCircuit.isEmpty())
            return false;

        var config = Objects.requireNonNull(getLogisticsNetwork().getPart(getDimensionalPos())).receiverLogisticsConfigs
                .get(inventoryIndex);
        var currentTick = Objects.requireNonNull(getLevel()).getGameTime();
        if (config.getCurrentMode() == NetworkReceiverConfigEntry.LogicMode.COOLDOWN
                && lastActiveTime[inventoryIndex] + 20L * config.getCurrentCooldown() > currentTick) {
            return false;
        } else if (config.getCurrentMode() == NetworkReceiverConfigEntry.LogicMode.REDSTONE_DISABLE) {
            for (var bus : withCircuit) {
                if (getLevel().hasNeighborSignal(bus.getPos()))
                    return false;
            }
        } else if (config.getCurrentMode() == NetworkReceiverConfigEntry.LogicMode.REDSTONE_ENABLE) {
            var hasFoundSignal = false;
            for (var bus : withCircuit) {
                if (getLevel().hasNeighborSignal(bus.getPos())) {
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
        payloads.remove(payload);
        lastActiveTime[payload.inventoryIndex] = Objects.requireNonNull(getLevel()).getGameTime();
        var withCircuit = itemOutputs.stream()
                .filter((c) -> IntCircuitBehaviour
                        .getCircuitConfiguration(c.getCircuitInventory().getStackInSlot(0)) == payload.inventoryIndex
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
        var server = Objects.requireNonNull(getLevel()).getServer();
        var payload = new ItemPayload(travelDuration, items, inventoryIndex);
        payloads.add(payload);
        assert server != null;
        server.tell(new TickTask(travelDuration, () -> onPackageArrival(payload)));
    }

    @Override
    public Component getCurrentStatusText() {
        if (!isFormed())
            return Component.literal("§cMultiblock not formed");
        return null;
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (forDrop)
            return;
        var newTag = new ListTag();
        for (var payload : payloads) {
            newTag.add(payload.serializeNBT());
        }
        tag.put("payloads", newTag);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        var listTag = tag.getList("payloads", Tag.TAG_COMPOUND);
        for (var entry : listTag) {
            if (!(entry instanceof CompoundTag ctag))
                return;
            var saved = new ItemPayload();
            saved.deserializeNBT(ctag);
            payloads.add(saved);
        }
    }

    private static class ItemPayload implements ITagSerializable<CompoundTag> {
        public int travelDuration;
        public List<ItemStack> items;
        public int inventoryIndex;

        public ItemPayload() {
            travelDuration = 0;
            items = new ArrayList<>();
            inventoryIndex = 0;
        }

        public ItemPayload(int travelDuration, List<ItemStack> items, int inventoryIndex) {
            this.travelDuration = travelDuration;
            this.items = items;
            this.inventoryIndex = inventoryIndex;
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putInt("travelDuration", travelDuration);
            tag.putInt("inventoryIndex", inventoryIndex);
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
            var list = nbt.getList("items", Tag.TAG_COMPOUND);
            for (Tag tag : list) {
                items.add(ItemStack.of((CompoundTag) tag));
            }
        }
    }
}
