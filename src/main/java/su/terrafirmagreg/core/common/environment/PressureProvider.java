package su.terrafirmagreg.core.common.environment;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.Getter;
import lombok.Setter;

/**
 * Lightweight pressure provider that persists independently of the GT machine.
 * Stays in memory as long as the dimension is loaded, even if the machine's chunk unloads.
 * Handles pressure queries using cached RoomScan data until the machine can revalidate.
 */
public class PressureProvider {

    @Getter
    private final BlockPos machinePos;

    @Setter
    @Getter
    private RoomScan roomScan = RoomScan.empty();

    @Nullable
    private IEnvironmentMachine attachedMachine;

    public PressureProvider(BlockPos machinePos) {
        this.machinePos = machinePos;
    }

    /**
     * Checks if this provider supplies pressure protection to the given position.
     * Works even when the machine is unloaded, using cached room data.
     */
    public boolean hasSafePressure(BlockPos pos) {
        if (!roomScan.isSealed()) {
            return false;
        }

        if (attachedMachine != null && !attachedMachine.isWorking()) {
            return false;
        }

        return roomScan.containsEnvelope(pos);
    }

    public boolean isMachineLoaded() {
        return attachedMachine != null;
    }

    public boolean isMachineWorking() {
        return attachedMachine != null && attachedMachine.isWorking();
    }

    public void attach(IEnvironmentMachine machine) {
        this.attachedMachine = machine;
    }

    public void detach() {
        this.attachedMachine = null;
    }

    public Set<ChunkPos> getTouchedChunks() {
        return roomScan.touchedChunks();
    }

    // ==================== Persistence ====================

    public void save(CompoundTag tag) {
        tag.putLong("pos", machinePos.asLong());

        if (roomScan.isSealed()) {
            tag.putLongArray("envelope", roomScan.envelope().toLongArray());

            long[] chunkLongs = roomScan.touchedChunks().stream()
                    .mapToLong(ChunkPos::toLong)
                    .toArray();
            tag.putLongArray("chunks", chunkLongs);
        }
    }

    public static PressureProvider load(CompoundTag tag) {
        BlockPos pos = BlockPos.of(tag.getLong("pos"));
        PressureProvider provider = new PressureProvider(pos);

        if (tag.contains("envelope")) {
            LongOpenHashSet envelope = new LongOpenHashSet(tag.getLongArray("envelope"));

            Set<ChunkPos> chunks = new HashSet<>();
            for (long chunkLong : tag.getLongArray("chunks")) {
                chunks.add(new ChunkPos(chunkLong));
            }

            provider.roomScan = new RoomScan(
                    new LongOpenHashSet(),
                    envelope,
                    RoomScan.Status.SAVED_DATA,
                    null,
                    null,
                    new AABB(0, 0, 0, 0, 0, 0),
                    chunks);
        }

        return provider;
    }
}
