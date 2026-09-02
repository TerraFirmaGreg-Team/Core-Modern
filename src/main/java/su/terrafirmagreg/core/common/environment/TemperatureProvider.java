package su.terrafirmagreg.core.common.environment;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Temperature provider for the Space Heater multiblock.
 * Persists in DimEnvManager's SavedData so queries work even before the machine's chunk loads.
 */
public class TemperatureProvider {

    public enum Mode {
        SEALED,
        VENTED
    }

    /** The target ambiental temperature. */
    public static final float FRONT_TARGET_TEMP = 15f;

    private final BlockPos machinePos;

    /** Passable blocks in the front region that get comfortable temperature. */
    private Set<BlockPos> frontGood = Set.of();

    /** Passable blocks in the back region that get uncomfortable temperature. */
    private Set<BlockPos> backHazard = Set.of();

    /** The front room scan (only meaningful when sealed). */
    private RoomScan frontScan = RoomScan.empty();

    /** Whether the front region is a sealed room (SEALED) or a vented greedy fill (VENTED). */
    private Mode mode = Mode.VENTED;

    /** Whether the front and back regions reach each other, making the machine unable to work. */
    private boolean blocked = false;

    @Nullable
    private IEnvironmentMachine attachedMachine;

    @Nullable
    private ServerLevel level;

    public TemperatureProvider(BlockPos machinePos) {
        this.machinePos = machinePos;
    }

    /**
     * Sets the front/back region data produced by an async validation run.
     * Called on the main thread by the machine driver.
     */
    public void setRegions(Set<BlockPos> frontGood, Set<BlockPos> backHazard, Mode mode,
            boolean blocked, RoomScan frontScan) {
        this.frontGood = frontGood == null ? Set.of() : frontGood;
        this.backHazard = backHazard == null ? Set.of() : backHazard;
        this.mode = mode == null ? Mode.VENTED : mode;
        this.blocked = blocked;
        this.frontScan = frontScan == null ? RoomScan.empty() : frontScan;
    }

    public Set<BlockPos> getFrontGood() {
        return frontGood;
    }

    public Set<BlockPos> getBackHazard() {
        return backHazard;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public RoomScan getFrontScan() {
        return frontScan;
    }

    /**
     * Checks if this provider supplies safe temperature to the given position.
     * Only the front region is comfortable; the back is a hazard. 
     * Works even when the machine is unloaded — assumes working when unloaded.
     */
    public boolean hasTemperature(BlockPos pos) {
        if (blocked)
            return false;
        if (attachedMachine != null && !attachedMachine.isWorking()) {
            return false;
        }
        return frontGood.contains(pos);
    }

    /**
     * Checks the target temperature this provider supplies to the given position.
     * the machine is unloaded — assumes working when unloaded.
     */
    public Optional<Float> getTargetTemperature(BlockPos pos) {
        if (blocked)
            return Optional.empty();
        if (attachedMachine != null && !attachedMachine.isWorking()) {
            return Optional.empty();
        }

        if (frontGood.contains(pos)) {
            return Optional.of(FRONT_TARGET_TEMP);
        }
        if (backHazard.contains(pos) && level != null) {
            return Optional.of((float) (15 - Climate.getTemperature(level, pos)));
        }
        return Optional.empty();
    }

    public boolean isMachineLoaded() {
        return attachedMachine != null;
    }

    public boolean isMachineWorking() {
        return attachedMachine != null && attachedMachine.isWorking();
    }

    public void attach(IEnvironmentMachine machine) {
        this.attachedMachine = machine;
        this.level = machine.getLevel() instanceof ServerLevel serverLevel ? serverLevel : null;
    }

    public void detach() {
        this.attachedMachine = null;
        this.level = null;
    }

    public BlockPos getMachinePos() {
        return machinePos;
    }

    /**
     * @return All chunks this provider affects, for chunk-indexed registry.
     */
    public Set<ChunkPos> getAffectedChunks() {
        Set<ChunkPos> chunks = new HashSet<>();
        for (BlockPos pos : frontGood) {
            chunks.add(new ChunkPos(pos));
        }
        for (BlockPos pos : backHazard) {
            chunks.add(new ChunkPos(pos));
        }
        chunks.addAll(frontScan.touchedChunks());
        return chunks;
    }

    // ==================== Persistence ====================

    public void save(CompoundTag tag) {
        tag.putLong("pos", machinePos.asLong());
        tag.putString("mode", mode.name());
        tag.putBoolean("blocked", blocked);
        LongArrayTag frontArray = new LongArrayTag(posToLongs(frontGood));
        LongArrayTag backArray = new LongArrayTag(posToLongs(backHazard));
        tag.put("frontGood", frontArray);
        tag.put("backHazard", backArray);
    }

    public static TemperatureProvider load(CompoundTag tag) {
        BlockPos pos = BlockPos.of(tag.getLong("pos"));
        TemperatureProvider provider = new TemperatureProvider(pos);

        Mode mode = Mode.VENTED;
        try {
            mode = Mode.valueOf(tag.getString("mode"));
        } catch (Exception e) {
            mode = Mode.VENTED;
        }
        provider.mode = mode;
        provider.blocked = tag.getBoolean("blocked");
        provider.frontGood = longsToPosSet(tag.getLongArray("frontGood"));
        provider.backHazard = longsToPosSet(tag.getLongArray("backHazard"));
        return provider;
    }

    private static long[] posToLongs(Set<BlockPos> set) {
        long[] result = new long[set.size()];
        int i = 0;
        for (BlockPos pos : set) {
            result[i++] = pos.asLong();
        }
        return result;
    }

    private static Set<BlockPos> longsToPosSet(long[] longs) {
        Set<BlockPos> result = new HashSet<>(longs.length);
        for (long l : longs) {
            result.add(BlockPos.of(l));
        }
        return result;
    }
}
