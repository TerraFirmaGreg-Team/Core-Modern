package su.terrafirmagreg.core.common.environment;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

/**
 * Temperature provider for the Heat Pump multiblock.
 */
public class TemperatureProvider {

    /** The target ambiental temperature. */
    public static final float FRONT_TARGET_TEMP = 15f;

    private final BlockPos machinePos;

    /** The sealed heated-space scan from the front of the machine. */
    private RoomScan frontScan = RoomScan.empty();

    /** The scan from behind the machine, used only to detect a trapped exhaust. */
    private RoomScan backScan = RoomScan.empty();

    /** Anchor of the proximity exhaust heat field (centre of the machine's back). */
    private BlockPos exhaustAnchor = BlockPos.ZERO;

    /** Radius (blocks) of the proximity exhaust heat field. */
    private int exhaustRadius = 0;

    /** Whether the exhaust is trapped (back region sealed), making the machine unable to work. */
    private boolean blocked = false;

    /** Cached chunk footprint. */
    private Set<ChunkPos> affectedChunks = Set.of();

    @Nullable
    private IEnvironmentMachine attachedMachine;

    @Nullable
    private ServerLevel level;

    public TemperatureProvider(BlockPos machinePos) {
        this.machinePos = machinePos;
    }

    /**
     * Sets the region data produced by an async validation run.
     * Called on the main thread by the machine driver.
     */
    public void setRegions(RoomScan frontScan, RoomScan backScan, boolean blocked,
            BlockPos exhaustAnchor, int exhaustRadius) {
        this.frontScan = frontScan == null ? RoomScan.empty() : frontScan;
        this.backScan = backScan == null ? RoomScan.empty() : backScan;
        this.blocked = blocked;
        this.exhaustAnchor = exhaustAnchor == null ? machinePos : exhaustAnchor;
        this.exhaustRadius = exhaustRadius;
        this.affectedChunks = computeAffectedChunks();
    }

    private Set<ChunkPos> computeAffectedChunks() {
        Set<ChunkPos> chunks = new HashSet<>(frontScan.touchedChunks());
        chunks.addAll(backScan.touchedChunks());

        int r = Math.max(0, exhaustRadius);
        int minCX = (exhaustAnchor.getX() - r) >> 4;
        int maxCX = (exhaustAnchor.getX() + r) >> 4;
        int minCZ = (exhaustAnchor.getZ() - r) >> 4;
        int maxCZ = (exhaustAnchor.getZ() + r) >> 4;
        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                chunks.add(new ChunkPos(cx, cz));
            }
        }
        return chunks;
    }

    public RoomScan getFrontScan() {
        return frontScan;
    }

    public RoomScan getBackScan() {
        return backScan;
    }

    public BlockPos getExhaustAnchor() {
        return exhaustAnchor;
    }

    public boolean isBlocked() {
        return blocked;
    }

    /**
     * Checks if this provider supplies safe temperature to the given position.
     */
    public boolean hasTemperature(BlockPos pos) {
        if (blocked)
            return false;
        if (attachedMachine != null && !attachedMachine.isWorking()) {
            return false;
        }
        return frontScan.isSealed() && frontScan.containsInterior(pos);
    }

    /**
     * Checks the target temperature this provider supplies to the given position.
     */
    public Optional<Float> getTargetTemperature(BlockPos pos) {
        if (blocked)
            return Optional.empty();
        if (attachedMachine != null && !attachedMachine.isWorking()) {
            return Optional.empty();
        }

        if (frontScan.isSealed() && frontScan.containsInterior(pos)) {
            return Optional.of(FRONT_TARGET_TEMP);
        }

        if (frontScan.isSealed() && level != null && isWithinExhaust(pos)) {
            float climate = Climate.getTemperature(level, pos);
            double dist = Math.sqrt(pos.distSqr(exhaustAnchor));
            float falloff = (float) Math.max(0.0, 1.0 - dist / exhaustRadius);
            return Optional.of(climate + (FRONT_TARGET_TEMP - climate) * falloff);
        }
        return Optional.empty();
    }

    private boolean isWithinExhaust(BlockPos pos) {
        return exhaustRadius > 0 && pos.distSqr(exhaustAnchor) <= (double) exhaustRadius * exhaustRadius;
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
        return new HashSet<>(affectedChunks);
    }

    // ==================== Persistence ====================

    public void save(CompoundTag tag) {
        tag.putLong("pos", machinePos.asLong());
        tag.putBoolean("blocked", blocked);
        tag.putLong("exhaustAnchor", exhaustAnchor.asLong());
        tag.putInt("exhaustRadius", exhaustRadius);
        tag.putLongArray("chunks", chunkPosToLongs(affectedChunks));
    }

    public static TemperatureProvider load(CompoundTag tag) {
        BlockPos pos = BlockPos.of(tag.getLong("pos"));
        TemperatureProvider provider = new TemperatureProvider(pos);

        provider.blocked = tag.getBoolean("blocked");
        provider.exhaustAnchor = BlockPos.of(tag.getLong("exhaustAnchor"));
        provider.exhaustRadius = tag.getInt("exhaustRadius");
        provider.affectedChunks = longsToChunkSet(tag.getLongArray("chunks"));
        return provider;
    }

    private static long[] chunkPosToLongs(Set<ChunkPos> set) {
        long[] result = new long[set.size()];
        int i = 0;
        for (ChunkPos chunk : set) {
            result[i++] = chunk.toLong();
        }
        return result;
    }

    private static Set<ChunkPos> longsToChunkSet(long[] longs) {
        Set<ChunkPos> result = new HashSet<>(longs.length);
        for (long l : longs) {
            result.add(new ChunkPos(l));
        }
        return result;
    }
}
