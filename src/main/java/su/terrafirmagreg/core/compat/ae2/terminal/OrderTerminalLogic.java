package su.terrafirmagreg.core.compat.ae2.terminal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import lombok.Getter;

import su.terrafirmagreg.core.compat.ae2.provider.StorageProviderHost;

public class OrderTerminalLogic {
    private static final String NBT_DESTINATION = "lastDestination";
    private static final String NBT_STAGING = "staging";
    private static final String NBT_STAGED = "staged";

    private final Supplier<IGrid> grid;
    private final Runnable onChanged;
    @Getter
    private String lastDestination = "";
    @Getter
    private boolean staging;
    private final Map<String, List<StagedStack>> staged = new HashMap<>();

    public OrderTerminalLogic(Supplier<IGrid> grid, Runnable onChanged) {
        this.grid = grid;
        this.onChanged = onChanged;
    }

    @Nullable
    public IGrid getGrid() {
        return grid.get();
    }

    public void setLastDestination(String lastDestination) {
        this.lastDestination = lastDestination;
        onChanged.run();
    }

    public void setStaging(boolean staging) {
        this.staging = staging;
        onChanged.run();
    }

    public List<StagedStack> getStaged(String destination) {
        return staged.getOrDefault(destination, List.of());
    }

    public void dispatch(String destination, AEKey what, long amount, boolean craftMissing, IActionSource source) {
        if (staging) {
            stage(destination, what, amount, craftMissing);
        } else {
            send(destination, what, amount, craftMissing, source);
        }
    }

    public void setStagedAmount(String destination, AEKey what, long amount) {
        var stacks = staged.get(destination);
        if (stacks == null) {
            return;
        }
        for (var it = stacks.listIterator(); it.hasNext();) {
            var stack = it.next();
            if (stack.what().equals(what)) {
                if (amount > 0) {
                    it.set(new StagedStack(what, amount, stack.craftMissing()));
                } else {
                    it.remove();
                }
                break;
            }
        }
        if (stacks.isEmpty()) {
            staged.remove(destination);
        }
        onChanged.run();
    }

    public void confirmStaged(String destination, IActionSource source) {
        var stacks = staged.remove(destination);
        if (stacks == null) {
            return;
        }
        var remaining = new ArrayList<StagedStack>();
        for (var stack : stacks) {
            long left = send(destination, stack.what(), stack.amount(), stack.craftMissing(), source);
            if (left > 0) {
                remaining.add(new StagedStack(stack.what(), left, stack.craftMissing()));
            }
        }
        if (!remaining.isEmpty()) {
            staged.put(destination, remaining);
        }
        onChanged.run();
    }

    private void stage(String destination, AEKey what, long amount, boolean craftMissing) {
        var currentGrid = grid.get();
        if (currentGrid == null || StorageProviderHost.find(currentGrid, destination) == null) {
            return;
        }
        var stacks = staged.computeIfAbsent(destination, id -> new ArrayList<>());
        for (var it = stacks.listIterator(); it.hasNext();) {
            var stack = it.next();
            if (stack.what().equals(what)) {
                long total = stack.amount() + amount;
                it.set(new StagedStack(what, total < 0 ? Long.MAX_VALUE : total, stack.craftMissing() || craftMissing));
                onChanged.run();
                return;
            }
        }
        stacks.add(new StagedStack(what, amount, craftMissing));
        onChanged.run();
    }

    private long send(String destination, AEKey what, long amount, boolean craftMissing, IActionSource source) {
        var currentGrid = grid.get();
        var host = currentGrid != null ? StorageProviderHost.find(currentGrid, destination) : null;
        if (host == null) {
            return amount;
        }
        long left = amount - host.getLogic().order(what, amount, source);
        if (craftMissing && left > 0 && currentGrid.getCraftingService().isCraftable(what)) {
            host.getLogic().requestCraft(what, left);
            return 0;
        }
        return left;
    }

    public void readFromNBT(CompoundTag data) {
        lastDestination = data.getString(NBT_DESTINATION);
        staging = data.getBoolean(NBT_STAGING);
        staged.clear();
        var stagedTag = data.getCompound(NBT_STAGED);
        for (var destination : stagedTag.getAllKeys()) {
            var stacks = new ArrayList<StagedStack>();
            var stacksTag = stagedTag.getList(destination, Tag.TAG_COMPOUND);
            for (int i = 0; i < stacksTag.size(); i++) {
                var stack = StagedStack.fromTag(stacksTag.getCompound(i));
                if (stack != null) {
                    stacks.add(stack);
                }
            }
            if (!stacks.isEmpty()) {
                staged.put(destination, stacks);
            }
        }
    }

    public void writeToNBT(CompoundTag data) {
        data.putString(NBT_DESTINATION, lastDestination);
        data.putBoolean(NBT_STAGING, staging);
        var stagedTag = new CompoundTag();
        for (var entry : staged.entrySet()) {
            var stacksTag = new ListTag();
            for (var stack : entry.getValue()) {
                stacksTag.add(stack.toTag());
            }
            stagedTag.put(entry.getKey(), stacksTag);
        }
        data.put(NBT_STAGED, stagedTag);
    }
}
