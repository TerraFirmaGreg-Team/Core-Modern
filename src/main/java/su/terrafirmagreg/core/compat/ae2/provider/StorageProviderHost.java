package su.terrafirmagreg.core.compat.ae2.provider;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;

public interface StorageProviderHost {
    StorageProviderLogic getLogic();

    BlockEntity getBlockEntity();

    EnumSet<Direction> getTargets();

    AEItemKey getTerminalIcon();

    void saveChanges();

    static List<StorageProviderHost> allOn(IGrid grid) {
        var hosts = new ArrayList<StorageProviderHost>();
        for (var machineClass : grid.getMachineClasses()) {
            if (StorageProviderHost.class.isAssignableFrom(machineClass)) {
                for (var machine : grid.getMachines(machineClass)) {
                    hosts.add((StorageProviderHost) machine);
                }
            }
        }
        return hosts;
    }

    @Nullable
    static StorageProviderHost find(IGrid grid, String id) {
        for (var host : allOn(grid)) {
            if (host.getLogic().getId().toString().equals(id)) {
                return host;
            }
        }
        return null;
    }
}
