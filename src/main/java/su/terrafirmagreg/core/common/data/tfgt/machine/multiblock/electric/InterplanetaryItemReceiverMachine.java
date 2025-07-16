package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
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
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork.*;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunItemBusMachine;

import java.util.*;

public class InterplanetaryItemReceiverMachine extends WorkableElectricMultiblockMachine implements ILogisticsNetworkReceiver, IMachineLife, IFancyUIMachine, IDisplayUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(InterplanetaryItemReceiverMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private List<ItemPayload> payloads = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private final List<RailgunItemBusMachine>[] itemOutputs = new List[33];
    private final long[] lastActiveTime = new long[33];

    public InterplanetaryItemReceiverMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        Arrays.fill(itemOutputs, new ArrayList<>());
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
        if (getLevel() instanceof ServerLevel sLvl) sLvl.getServer().tell(new TickTask(0, () -> getLogisticsNetwork().loadOrCreatePart(this)));
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (!isRemote()) getLogisticsNetwork().unloadPart(this);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var server = Objects.requireNonNull(getLevel()).getServer();
        if (server == null) return;
        Arrays.fill(lastActiveTime, getLevel().getGameTime());
        for (var inventory: getInventories()) {
            var circuit = IntCircuitBehaviour.getCircuitConfiguration(inventory.getCircuitInventory().getStackInSlot(0));
            inventory.setCurrentCircuit(circuit);
            itemOutputs[circuit].add(inventory);
            TFGCore.LOGGER.info("Inventory: {} {} registered on circuit {}", inventory.getDefinition().getName(), inventory.getDefinition().getTier(), circuit);
            inventory.getCircuitInventory().addChangedListener(() -> {
                var newCircuit = IntCircuitBehaviour.getCircuitConfiguration(inventory.getCircuitInventory().getStackInSlot(0));
                if (newCircuit == inventory.getCurrentCircuit()) return;
                itemOutputs[inventory.getCurrentCircuit()].remove(inventory);
                TFGCore.LOGGER.info("Inventory: {} {} circuit {} -> {}", inventory.getDefinition().getName(), inventory.getDefinition().getTier(), inventory.getCurrentCircuit(), newCircuit);
                inventory.setCurrentCircuit(newCircuit);
                itemOutputs[newCircuit].add(inventory);
            });

        }

    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        for (List<RailgunItemBusMachine> itemOutput : itemOutputs) {
            itemOutput.clear();
        }
    }

    @Override
    public void onMachineRemoved() {
        if (!isRemote()) getLogisticsNetwork().destroyPart(this);
    }

    @Override
    public List<RailgunItemBusMachine> getInventories() {
        if (isMachineInvalid()) return List.of();
        List<RailgunItemBusMachine> parts = new ArrayList<>();
        for (var part: getParts()) {
            if (part instanceof RailgunItemBusMachine r) parts.add(r);
        }
        return parts;
    }

    @Override
    public boolean canAcceptItems(int inventoryIndex, List<ItemStack> stacks) {
        if (itemOutputs[inventoryIndex].isEmpty()) return false;

        var config = Objects.requireNonNull(getLogisticsNetwork().getPart(getDimensionalPos())).receiverLogisticsConfigs.get(inventoryIndex);
        var currentTick = Objects.requireNonNull(getLevel()).getGameTime();
        if (config.getCurrentMode() == NetworkReceiverConfigEntry.LogicMode.COOLDOWN && lastActiveTime[inventoryIndex]+ 20L *config.getCurrentCooldown() > currentTick) {
            return false;
        } else if (config.getCurrentMode() == NetworkReceiverConfigEntry.LogicMode.REDSTONE_DISABLE) {
            for (var bus: itemOutputs[inventoryIndex]) {
                if (getLevel().hasNeighborSignal(bus.getPos())) return false;
            }
        } else if (config.getCurrentMode() == NetworkReceiverConfigEntry.LogicMode.REDSTONE_ENABLE) {
            var hasFoundSignal = false;
            for (var bus: itemOutputs[inventoryIndex]) {
                if (getLevel().hasNeighborSignal(bus.getPos())) {
                    hasFoundSignal = true;
                    break;
                }
            }
            if (!hasFoundSignal) return false;
        }

        for (ItemStack itemToInsert : stacks) {
            var amountLeft = itemToInsert.copy();
            for (RailgunItemBusMachine outputBus: itemOutputs[inventoryIndex]) {
                var inventory = outputBus.getInventory();
                for (int i=0; i<inventory.getSlots(); i++) {
                    amountLeft = inventory.insertItemInternal(i, amountLeft, true);
                    if (amountLeft == ItemStack.EMPTY) break;
                }
                if (amountLeft == ItemStack.EMPTY) break;
            }
            if (amountLeft != ItemStack.EMPTY) return false;
        }
        return true;
    }

    private void onPackageArrival(ItemPayload payload) {
        payloads.remove(payload);
        lastActiveTime[payload.inventoryIndex] = Objects.requireNonNull(getLevel()).getGameTime();

        for (ItemStack itemToInsert : payload.items) {
            var amountLeft = itemToInsert.copy();
            for (RailgunItemBusMachine outputBus: itemOutputs[payload.inventoryIndex]) {
                var inventory = outputBus.getInventory();
                for (int i=0; i<inventory.getSlots(); i++) {
                    amountLeft = inventory.insertItemInternal(i, amountLeft, false);
                    if (amountLeft == ItemStack.EMPTY) break;
                }
                if (amountLeft == ItemStack.EMPTY) break;
            }
        }
    }

    @Override
    public void onPackageSent(int inventoryIndex, List<ItemStack> items, int travelDuration) {
        var server = Objects.requireNonNull(getLevel()).getServer();
        var currentTick = getLevel().getGameTime();
        var payload = new ItemPayload(currentTick, travelDuration, items, inventoryIndex);
        payloads.add(payload);
        assert server != null;
        server.tell(new TickTask(travelDuration, () -> onPackageArrival(payload)));
    }

    @Override
    public Component getCurrentStatusText() {
        if (!isFormed()) return Component.literal("§cMultiblock not formed");
        return null;
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (forDrop) return;
        var newTag = new ListTag();
        for (var payload: payloads) {
            newTag.add(payload.serializeNBT());
        }
        tag.put("payloads", newTag);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        var listTag = tag.getList("payloads", Tag.TAG_COMPOUND);
        for (var entry: listTag) {
            if (!(entry instanceof CompoundTag ctag)) return;
            var saved = new ItemPayload();
            saved.deserializeNBT(ctag);
            payloads.add(saved);
        }
    }

    private static class ItemPayload implements ITagSerializable<CompoundTag> {
        public long sentTick;
        public long travelDuration;
        public List<ItemStack> items;
        public int inventoryIndex;

        public ItemPayload() {
            sentTick = 0;
            travelDuration = 0;
            items = new ArrayList<>();
            inventoryIndex = 0;
        }

        public ItemPayload(long sentTick, long travelDuration, List<ItemStack> items, int inventoryIndex) {
            this.sentTick = sentTick;
            this.travelDuration = travelDuration;
            this.items = items;
            this.inventoryIndex = inventoryIndex;
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.putLong("sentTick", sentTick);
            tag.putLong("travelDuration", travelDuration);
            tag.putInt("inventoryIndex", inventoryIndex);
            var list = new ListTag();
            for (var s: items) {
                list.add(s.save(new CompoundTag()));
            }
            tag.put("items", list);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            sentTick = nbt.getLong("sentTick");
            travelDuration = nbt.getLong("travelDuration");
            inventoryIndex = nbt.getInt("inventoryIndex");
            var list = nbt.getList("items", Tag.TAG_COMPOUND);
            for (Tag tag : list) {
                items.add(ItemStack.of((CompoundTag)tag));
            }
        }
    }
}
