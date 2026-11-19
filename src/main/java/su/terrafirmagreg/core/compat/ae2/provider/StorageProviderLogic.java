package su.terrafirmagreg.core.compat.ae2.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageHelper;
import appeng.core.settings.TickRates;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.helpers.MachineSource;
import lombok.Getter;

public class StorageProviderLogic implements ICraftingRequester {
    private static final String NBT_ID = "providerId";
    private static final String NBT_BUFFER = "buffer";
    private static final String NBT_CRAFTS = "crafts";
    private static final int CRAFT_RETRY_TICKS = 100;

    public enum Status {
        IDLE, WAITING, CRAFTING, WAITING_FOR_CPU, MISSING_INGREDIENTS, OFFLINE
    }

    private final IManagedGridNode mainNode;
    private final StorageProviderHost host;
    private final IActionSource actionSource;
    private final List<GenericStack> buffer = new ArrayList<>();
    private final List<CraftRequest> crafts = new ArrayList<>();
    @Getter
    private UUID id = UUID.randomUUID();
    private int sideOffset;

    public StorageProviderLogic(IManagedGridNode mainNode, StorageProviderHost host) {
        this.mainNode = mainNode;
        this.host = host;
        this.actionSource = new MachineSource(mainNode::getNode);
        this.mainNode.setFlags(GridFlags.REQUIRE_CHANNEL)
                .setIdlePowerUsage(1)
                .addService(IGridTickable.class, new Ticker())
                .addService(ICraftingRequester.class, this);
    }

    public List<GenericStack> getBuffer() {
        return Collections.unmodifiableList(buffer);
    }

    public int getCraftCount() {
        return crafts.size();
    }

    public Status getStatus() {
        if (!mainNode.isActive()) {
            return Status.OFFLINE;
        }
        Status status = buffer.isEmpty() ? Status.IDLE : Status.WAITING;
        for (var craft : crafts) {
            if (craft.blockedBy != null) {
                return craft.blockedBy;
            }
            if (craft.link != null) {
                status = Status.CRAFTING;
            }
        }
        return status;
    }

    public Component getDestinationName() {
        if (host instanceof Nameable nameable && nameable.hasCustomName()) {
            return nameable.getCustomName();
        }
        var group = findAdjacentMachine();
        if (group != null) {
            return group.name();
        }
        return host instanceof Nameable nameable ? nameable.getDisplayName() : host.getTerminalIcon().getDisplayName();
    }

    public AEItemKey getDestinationIcon() {
        var group = findAdjacentMachine();
        return group != null && group.icon() != null ? group.icon() : host.getTerminalIcon();
    }

    @Nullable
    private PatternContainerGroup findAdjacentMachine() {
        var be = host.getBlockEntity();
        var level = be.getLevel();
        for (var side : host.getTargets()) {
            var pos = be.getBlockPos().relative(side);
            if (level.hasChunkAt(pos) && !isOnOwnGrid(pos, side)) {
                var group = PatternContainerGroup.fromMachine(level, pos, side.getOpposite());
                if (group != null) {
                    return group;
                }
            }
        }
        return null;
    }

    public long order(AEKey what, long amount, IActionSource source) {
        var grid = mainNode.getGrid();
        if (grid == null || !mainNode.isActive()) {
            return 0;
        }
        long moved = StorageHelper.poweredExtraction(grid.getEnergyService(), grid.getStorageService().getInventory(), what, amount, source);
        if (moved > 0) {
            addToBuffer(what, moved);
        }
        return moved;
    }

    public void requestCraft(AEKey what, long amount) {
        crafts.add(new CraftRequest(what, amount));
        wakeUp();
    }

    public void returnAll() {
        var pending = List.copyOf(crafts);
        crafts.clear();
        for (var craft : pending) {
            craft.cancel();
        }
        var grid = mainNode.getGrid();
        if (grid != null && mainNode.isActive()) {
            var storage = grid.getStorageService().getInventory();
            for (var it = buffer.listIterator(); it.hasNext();) {
                var stack = it.next();
                long inserted = StorageHelper.poweredInsert(grid.getEnergyService(), storage, stack.what(), stack.amount(), actionSource);
                if (inserted >= stack.amount()) {
                    it.remove();
                } else if (inserted > 0) {
                    it.set(new GenericStack(stack.what(), stack.amount() - inserted));
                }
            }
        }
        host.saveChanges();
    }

    private void addToBuffer(AEKey what, long amount) {
        for (var it = buffer.listIterator(); it.hasNext();) {
            var stack = it.next();
            if (stack.what().equals(what)) {
                it.set(new GenericStack(what, stack.amount() + amount));
                wakeUp();
                return;
            }
        }
        buffer.add(new GenericStack(what, amount));
        wakeUp();
    }

    private void wakeUp() {
        host.saveChanges();
        mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
    }

    private boolean pushBuffer() {
        var sides = new ArrayList<>(host.getTargets());
        Collections.rotate(sides, sideOffset++);

        boolean didSomething = false;
        for (var side : sides) {
            var target = findTarget(side);
            if (target == null) {
                continue;
            }
            for (var it = buffer.listIterator(); it.hasNext();) {
                var stack = it.next();
                long inserted = target.insert(stack.what(), stack.amount(), Actionable.MODULATE);
                if (inserted >= stack.amount()) {
                    it.remove();
                    didSomething = true;
                } else if (inserted > 0) {
                    it.set(new GenericStack(stack.what(), stack.amount() - inserted));
                    didSomething = true;
                }
            }
        }
        if (didSomething) {
            host.saveChanges();
        }
        return didSomething;
    }

    @Nullable
    private PatternProviderTarget findTarget(Direction side) {
        var be = host.getBlockEntity();
        var level = be.getLevel();
        var pos = be.getBlockPos().relative(side);
        if (!(level instanceof ServerLevel) || !level.hasChunkAt(pos) || isOnOwnGrid(pos, side)) {
            return null;
        }
        return PatternProviderTarget.get(level, pos, level.getBlockEntity(pos), side.getOpposite(), actionSource);
    }

    private boolean isOnOwnGrid(BlockPos pos, Direction side) {
        var neighbourHost = GridHelper.getNodeHost(host.getBlockEntity().getLevel(), pos);
        if (neighbourHost == null) {
            return false;
        }
        var neighbourNode = neighbourHost.getGridNode(side.getOpposite());
        return neighbourNode != null && neighbourNode.getGrid() == mainNode.getGrid();
    }

    private void advanceCrafts() {
        var grid = mainNode.getGrid();
        if (grid == null || crafts.isEmpty()) {
            return;
        }
        var level = host.getBlockEntity().getLevel();
        long now = level.getGameTime();
        var craftingService = grid.getCraftingService();
        boolean planning = isPlanning();
        for (var craft : List.copyOf(crafts)) {
            if (craft.link != null) {
                continue;
            }
            if (craft.plan != null) {
                if (!craft.plan.isDone()) {
                    continue;
                }
                var plan = getPlan(craft);
                craft.plan = null;
                craft.retryAt = now + CRAFT_RETRY_TICKS;
                if (plan == null || plan.simulation()) {
                    craft.blockedBy = Status.MISSING_INGREDIENTS;
                    continue;
                }
                var result = craftingService.submitJob(plan, this, null, false, actionSource);
                if (result.successful()) {
                    craft.link = result.link();
                    craft.blockedBy = null;
                } else {
                    craft.blockedBy = result.errorCode() == CraftingSubmitErrorCode.MISSING_INGREDIENT ? Status.MISSING_INGREDIENTS : Status.WAITING_FOR_CPU;
                }
                host.saveChanges();
            } else if (!planning && now >= craft.retryAt) {
                planning = true;
                craft.plan = craftingService.beginCraftingCalculation(level, () -> actionSource, craft.what, craft.amount, CalculationStrategy.REPORT_MISSING_ITEMS);
            }
        }
    }

    @Nullable
    private static ICraftingPlan getPlan(CraftRequest craft) {
        try {
            return craft.plan.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    private boolean isPlanning() {
        for (var craft : crafts) {
            if (craft.plan != null) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCraftsToPlan() {
        for (var craft : crafts) {
            if (craft.link == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        var links = ImmutableSet.<ICraftingLink>builder();
        for (var craft : crafts) {
            if (craft.link != null) {
                links.add(craft.link);
            }
        }
        return links.build();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        if (mode == Actionable.MODULATE) {
            addToBuffer(what, amount);
        }
        return amount;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        crafts.removeIf(craft -> craft.link == link);
        host.saveChanges();
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    public void onMainNodeStateChanged() {
        if (mainNode.isActive()) {
            mainNode.ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }
    }

    public void addDrops(List<ItemStack> drops) {
        returnAll();
        var be = host.getBlockEntity();
        for (var stack : buffer) {
            stack.what().addDrops(stack.amount(), drops, be.getLevel(), be.getBlockPos());
        }
    }

    public void clearContent() {
        buffer.clear();
        crafts.clear();
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putUUID(NBT_ID, id);
        var bufferTag = new ListTag();
        for (var stack : buffer) {
            bufferTag.add(GenericStack.writeTag(stack));
        }
        tag.put(NBT_BUFFER, bufferTag);

        var craftsTag = new ListTag();
        for (var craft : crafts) {
            var craftTag = new CompoundTag();
            craftTag.put("what", craft.what.toTagGeneric());
            craftTag.putLong("amount", craft.amount);
            if (craft.link != null) {
                var linkTag = new CompoundTag();
                craft.link.writeToNBT(linkTag);
                craftTag.put("link", linkTag);
            }
            craftsTag.add(craftTag);
        }
        tag.put(NBT_CRAFTS, craftsTag);
    }

    public void readFromNBT(CompoundTag tag) {
        if (tag.hasUUID(NBT_ID)) {
            id = tag.getUUID(NBT_ID);
        }
        buffer.clear();
        var bufferTag = tag.getList(NBT_BUFFER, Tag.TAG_COMPOUND);
        for (int i = 0; i < bufferTag.size(); i++) {
            var stack = GenericStack.readTag(bufferTag.getCompound(i));
            if (stack != null) {
                buffer.add(stack);
            }
        }

        crafts.clear();
        var craftsTag = tag.getList(NBT_CRAFTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < craftsTag.size(); i++) {
            var craftTag = craftsTag.getCompound(i);
            var what = AEKey.fromTagGeneric(craftTag.getCompound("what"));
            if (what != null) {
                var craft = new CraftRequest(what, craftTag.getLong("amount"));
                if (craftTag.contains("link", Tag.TAG_COMPOUND)) {
                    craft.link = StorageHelper.loadCraftingLink(craftTag.getCompound("link"), this);
                }
                crafts.add(craft);
            }
        }
    }

    private static class CraftRequest {
        final AEKey what;
        final long amount;
        @Nullable
        Future<ICraftingPlan> plan;
        @Nullable
        ICraftingLink link;
        @Nullable
        Status blockedBy;
        long retryAt;

        CraftRequest(AEKey what, long amount) {
            this.what = what;
            this.amount = amount;
        }

        void cancel() {
            if (plan != null) {
                plan.cancel(true);
            }
            if (link != null) {
                link.cancel();
            }
        }
    }

    private class Ticker implements IGridTickable {
        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(TickRates.Interface, buffer.isEmpty() && !hasCraftsToPlan(), true);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }
            boolean didWork = pushBuffer();
            advanceCrafts();
            if (buffer.isEmpty() && !hasCraftsToPlan()) {
                return TickRateModulation.SLEEP;
            }
            return didWork || isPlanning() ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
        }
    }
}
