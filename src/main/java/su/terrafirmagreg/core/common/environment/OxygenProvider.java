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
 * Lightweight oxygen provider that persists independently of the GT machine.
 * Stays in memory as long as the dimension is loaded, even if the machine's chunk unloads.
 * Handles oxygen queries using cached RoomScan data until the machine can revalidate.
 */
public class OxygenProvider {

    @Getter
    private final BlockPos machinePos;

    @Setter
    @Getter
    private RoomScan roomScan = RoomScan.empty();

    /** The attached machine, if its chunk is loaded */
    @Nullable
    private IAtmosphereMachine attachedMachine;

    public OxygenProvider(BlockPos machinePos) {
        this.machinePos = machinePos;
    }

    /**
     * Checks if this provider currently supplies oxygen to the given position.
     * Works even when the machine is unloaded, using cached room data.
     */
    public boolean hasOxygen(BlockPos pos) {
        // Need sealed room data and either the machine is loaded and working,
        //  or the machine is unloaded (TODO: And was working last time it was loaded)
        if (!roomScan.isSealed()) {
            return false;
        }

        if (attachedMachine != null && !attachedMachine.isWorking()) {
            // Machine is loaded but not working (no power, etc.)
            return false;
        }

        // Machine is either working or unloaded
        // TODO Maybe cache whether the machine was working when it was last loaded. Cos now it's a bit weird that
        // TODO  machines that don't even have infra around them count as providing oxygen when unloaded.
        // TODO  Maybe we cache on unload only? Or just whenever work status changes?
        return roomScan.containsEnvelope(pos);
    }

    /** @return Whether the machine is currently loaded and attached */
    public boolean isMachineLoaded() {
        return attachedMachine != null;
    }

    /** @return Whether the machine is loaded and actively working */
    public boolean isMachineWorking() {
        return attachedMachine != null && attachedMachine.isWorking();
    }

    /** Called when the machine's chunk loads and the machine attaches to this provider. */
    public void attach(IAtmosphereMachine machine) {
        this.attachedMachine = machine;
    }

    /** Called when the machine's chunk unloads. */
    public void detach() {
        this.attachedMachine = null;
    }

    /**
     * @return Set of chunks this provider's room touches (for registry updates)
     */
    public Set<ChunkPos> getTouchedChunks() {
        return roomScan.touchedChunks();
    }

    // ==================== Persistence ====================

    public void save(CompoundTag tag) {
        tag.putLong("pos", machinePos.asLong());

        if (roomScan.isSealed()) {
            tag.putLongArray("envelope", roomScan.envelope().toLongArray());

            // Save touched chunks
            long[] chunkLongs = roomScan.touchedChunks().stream()
                    .mapToLong(ChunkPos::toLong)
                    .toArray();
            tag.putLongArray("chunks", chunkLongs);
        }
    }

    public static OxygenProvider load(CompoundTag tag) {
        BlockPos pos = BlockPos.of(tag.getLong("pos"));
        OxygenProvider provider = new OxygenProvider(pos);

        if (tag.contains("status")) {
            LongOpenHashSet envelope = new LongOpenHashSet(tag.getLongArray("envelope"));

            Set<ChunkPos> chunks = new HashSet<>();
            for (long chunkLong : tag.getLongArray("chunks")) {
                chunks.add(new ChunkPos(chunkLong));
            }

            // Reconstruct RoomScan from cached data with SAVED_DATA status
            // Only reconstructs the envelope and the touched chunks.
            // On next validation we get the full data
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
