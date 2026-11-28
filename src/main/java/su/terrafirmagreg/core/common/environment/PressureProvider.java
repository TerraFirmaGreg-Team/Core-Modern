package su.terrafirmagreg.core.common.environment;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
    private IEnvironmentMachine.IAtmosphereMachine attachedMachine;

    /** Last pressure supplied by the machine, in Pa, used while its chunk is unloaded */
    @Getter
    @Setter
    @Nullable
    private Float pressure = DimensionEnvironment.EARTH_PRESSURE;

    public PressureProvider(BlockPos machinePos) {
        this.machinePos = machinePos;
    }

    /**
     * Gets the pressure this provider supplies to the given position.
     * Works even when the machine is unloaded, using cached room data.
     */
    public Optional<Float> getPressure(BlockPos pos) {
        if (!roomScan.isSealed() || !roomScan.containsEnvelope(pos)) {
            return Optional.empty();
        }
        if (attachedMachine != null) {
            return attachedMachine.isWorking() ? Optional.of(attachedMachine.getPressure()) : Optional.empty();
        }
        return Optional.ofNullable(pressure);
    }

    public boolean isMachineLoaded() {
        return attachedMachine != null;
    }

    public boolean isMachineWorking() {
        return attachedMachine != null && attachedMachine.isWorking();
    }

    public void attach(IEnvironmentMachine.IAtmosphereMachine machine) {
        this.attachedMachine = machine;
        this.pressure = machine.getPressure();
    }

    public void detach() {
        if (attachedMachine != null) {
            this.pressure = attachedMachine.isWorking() ? attachedMachine.getPressure() : null;
        }
        this.attachedMachine = null;
    }

    public Set<ChunkPos> getTouchedChunks() {
        return roomScan.touchedChunks();
    }

    // ==================== Persistence ====================

    public void save(CompoundTag tag) {
        tag.putLong("pos", machinePos.asLong());
        if (pressure != null) {
            tag.putFloat("pressure", pressure);
        }

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
        if (tag.contains("pressure", Tag.TAG_FLOAT)) {
            provider.pressure = tag.getFloat("pressure");
        }

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
