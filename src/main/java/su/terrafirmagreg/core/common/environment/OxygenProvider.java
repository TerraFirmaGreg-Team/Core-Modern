package su.terrafirmagreg.core.common.environment;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.Getter;
import lombok.Setter;

import su.terrafirmagreg.core.common.data.TFGTags;

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
    private IEnvironmentMachine.IAtmosphereMachine attachedMachine;

    /** Last atmosphere supplied by the machine, used while its chunk is unloaded */
    @Getter
    @Setter
    @Nullable
    private Fluid atmosphere;

    /** Last pressure supplied by the machine, in Pa */
    @Getter
    @Setter
    private float pressure = DimensionEnvironment.EARTH_PRESSURE;

    public OxygenProvider(BlockPos machinePos) {
        this.machinePos = machinePos;
    }

    /**
     * Gets the atmosphere this provider supplies to the given position.
     * Works even when the machine is unloaded, using cached room data.
     */
    @Nullable
    public Fluid getAtmosphere(BlockPos pos) {
        if (!roomScan.isSealed()) {
            return null;
        }

        if (!roomScan.containsEnvelope(pos)) {
            return null;
        }

        if (attachedMachine != null) {
            if (!attachedMachine.isWorking()) {
                return null;
            }
            atmosphere = attachedMachine.getAtmosphere();
            pressure = attachedMachine.getPressure();
        }
        return atmosphere;
    }

    /**
     * Use getAtmosphere instead.
     */
    @Deprecated
    public boolean hasOxygen(BlockPos pos) {
        Fluid supplied = getAtmosphere(pos);
        return supplied != null && supplied.is(TFGTags.Fluids.BreathableAir);
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
    public void attach(IEnvironmentMachine.IAtmosphereMachine machine) {
        this.attachedMachine = machine;
        this.atmosphere = machine.getAtmosphere();
        this.pressure = machine.getPressure();
    }

    /** Called when the machine's chunk unloads. */
    public void detach() {
        if (attachedMachine != null) {
            if (attachedMachine.isWorking()) {
                this.atmosphere = attachedMachine.getAtmosphere();
                this.pressure = attachedMachine.getPressure();
            } else {
                this.atmosphere = null;
            }
        }
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
        if (atmosphere != null) {
            tag.putString("atmosphere", ForgeRegistries.FLUIDS.getKey(atmosphere).toString());
        }
        tag.putFloat("pressure", pressure);

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
        if (tag.contains("atmosphere", Tag.TAG_STRING)) {
            provider.atmosphere = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(tag.getString("atmosphere")));
        }
        if (tag.contains("pressure", Tag.TAG_FLOAT)) {
            provider.pressure = tag.getFloat("pressure");
        }

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
