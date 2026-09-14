package su.terrafirmagreg.core.common.environment;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

import lombok.Getter;

/**
 * Lightweight gravity provider that uses a sphere (bubble) around the machine.
 * Persists in DimEnvManager's SavedData so queries work even before the machine's chunk loads.
 */
public class GravityProvider {

    @Getter
    private final BlockPos machinePos;

    @Getter
    private final int radius;

    private final int radiusSq;

    @Nullable
    private IEnvironmentMachine attachedMachine;

    public GravityProvider(BlockPos machinePos, int radius) {
        this.machinePos = machinePos;
        this.radius = radius;
        this.radiusSq = radius * radius;
    }

    /**
     * Checks if this provider supplies normal gravity to the given position.
     * Works even when the machine is unloaded — assumes working when unloaded.
     */
    public boolean hasNormalGravity(BlockPos pos) {
        if (attachedMachine != null && !attachedMachine.isWorking()) {
            return false;
        }

        // Machine is either working or unloaded (assumed working)
        return machinePos.distSqr(pos) <= radiusSq;
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

    /**
     * @return All chunks this bubble touches, for chunk-indexed registry.
     */
    public Set<ChunkPos> getAffectedChunks() {
        Set<ChunkPos> chunks = new HashSet<>();
        int mx = machinePos.getX();
        int mz = machinePos.getZ();
        int minCX = (mx - radius) >> 4;
        int maxCX = (mx + radius) >> 4;
        int minCZ = (mz - radius) >> 4;
        int maxCZ = (mz + radius) >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                // Find nearest point in chunk to machine (clamp machine pos to chunk bounds)
                int nearestX = Math.max(cx << 4, Math.min(mx, (cx << 4) + 15));
                int nearestZ = Math.max(cz << 4, Math.min(mz, (cz << 4) + 15));
                int dx = nearestX - mx;
                int dz = nearestZ - mz;
                if (dx * dx + dz * dz <= radiusSq) {
                    chunks.add(new ChunkPos(cx, cz));
                }
            }
        }
        return chunks;
    }

    // ==================== Persistence ====================

    public void save(CompoundTag tag) {
        tag.putLong("pos", machinePos.asLong());
        tag.putInt("radius", radius);
    }

    public static GravityProvider load(CompoundTag tag) {
        BlockPos pos = BlockPos.of(tag.getLong("pos"));
        int radius = tag.getInt("radius");
        return new GravityProvider(pos, radius);
    }
}
