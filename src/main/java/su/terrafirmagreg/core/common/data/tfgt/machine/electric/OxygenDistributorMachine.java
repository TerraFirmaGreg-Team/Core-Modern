package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import lombok.Getter;
import lombok.Setter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;

import org.jetbrains.annotations.NotNull;

import su.terrafirmagreg.core.common.atmosphere.*;
import su.terrafirmagreg.core.common.atmosphere.RoomScan.Status;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.OptionalLong;
import java.util.Set;

/**
 * Oxygen Distributor machine that maintains a sealed room with breathable atmosphere.
 * Uses flood fill to detect room boundaries and provides oxygen to all positions within.
 * <p>
 * The actual oxygen data is stored in {@link OxygenProvider} which persists independently
 * of this machine's chunk load state, allowing oxygen queries even when this chunk is unloaded.
 */
public class OxygenDistributorMachine extends SimpleTieredMachine implements IFloodFillMachine, IAtmosphereMachine {

    /** The provider that holds our room data and handles oxygen queries */
    @Nullable
    private OxygenProvider provider;

    /** Pending scan result from async validation */
    private RoomScan newRoomScan;

    /** Offset for staggering validation timing across machines */
    private long tickOffset;

    @Getter
    @Setter
    private boolean dirty;

    private ServerLevel level;
    private DimensionAtmosphereManager manager;

    @Nullable
    private ChunkPos pendingChunkLoad = null;

    public OxygenDistributorMachine(IMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction, Object... args) {
        super(holder, tier, tankScalingFunction, args);
        if (holder.level() instanceof ServerLevel serverLevel) {
            level = serverLevel;
            manager = AtmosphereSystem.getManager(level);
            tickOffset = holder.pos().hashCode();
        }
    }

    /**
     * @return The current room scan, or empty if no provider attached
     */
    public RoomScan getRoomScan() {
        return provider != null ? provider.getRoomScan() : RoomScan.empty();
    }

    //////////////////////////////////////
    // ********* Recipe Logic **********//
    //////////////////////////////////////

    /** @return Whether this machine is currently executing a recipe */
    public boolean isWorking() {
        return recipeLogic != null && recipeLogic.isWorking();
    }

    /**
     * Recipe Modifier for <b>Oxygen Distributors</b> - can be used as a valid {@link RecipeModifier}
     *
     * @param machine an {@link OxygenDistributorMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given OxygenDistributorMachine and recipe
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof OxygenDistributorMachine oxygenMachine)) {
            return RecipeModifier.nullWrongType(OxygenDistributorMachine.class, machine);
        }

        var roomSize = oxygenMachine.getRoomScan().interiorSize();

        return ModifierFunction.builder()
                .eutMultiplier(roomSize)
                .inputModifier(ContentModifier.multiplier(roomSize))
                .tickInputModifier(ContentModifier.multiplier(roomSize)) // Not sure yet if I want a normal or a tick input recipe
                .build();
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    //////////////////////////////////////
    // ****** Revalidation logic ****** //
    //////////////////////////////////////

    /**
     * Call this async to revalidate the room.
     * Runs a new flood fill.
     * Stores the result in newRoomScan which gets processed on the main thread in {@link #processValidationResult()}.
     */
    public void validateAsync() {
        int maxBlocks = 1_000_000;
        int maxHorizontalDimension = 128;
        newRoomScan = FloodFill.fill(level, level, getPos(), maxBlocks, maxHorizontalDimension);
    }

    /**
     * Call this on the main thread to apply the revalidation results when they're ready.
     * Handles transition to the new RoomScan:
     * - Spawn vortex if status from sealed to escaped build height
     * - Update provider and registries
     */
    public void processValidationResult() {
        if (provider == null || newRoomScan == null) {
            return;
        }

        RoomScan oldScan = provider.getRoomScan();
        RoomScan newScan = newRoomScan;
        newRoomScan = null;

        // Clean up if we were waiting for a chunk
        if (pendingChunkLoad != null) {
            manager.chunkLoadListeners.removeSingle(this, pendingChunkLoad);
            pendingChunkLoad = null;
        }

        // If we hit an unloaded chunk we don't actually want to update the room. We just pretend nothing has changed,
        // and we run a new flood fill when the chunk loads or another blockchange happens.
        if (newScan.status() == Status.ESCAPED_UNLOADED && newScan.escapePoint() != null) {
            pendingChunkLoad = new ChunkPos(newScan.escapePoint());
            manager.chunkLoadListeners.addSingle(this, pendingChunkLoad);
            return;
        }


        // Compute chunk diff for block change listeners
        // TODO: This gets done twice, once here and once in provider
        Set<ChunkPos> oldChunks = oldScan.touchedChunks();
        Set<ChunkPos> newChunks = newScan.touchedChunks();

        Set<ChunkPos> toRemove = new HashSet<>(oldChunks);
        toRemove.removeAll(newChunks);

        Set<ChunkPos> toAdd = new HashSet<>(newChunks);
        toAdd.removeAll(oldChunks);

        // Update block change listener registry
        manager.blockChangeListeners.update(this, toRemove, toAdd);

        // Update provider's room scan and oxygen chunk registry
        manager.updateProvider(provider, oldScan, newScan);

        // Vortex: was sealed, now escaped to build height
        if (oldScan.isSealed() && newScan.status() == Status.ESCAPED_BUILD_HEIGHT) {
            BlockPos breachPoint = findBreachPoint(oldScan, newScan);
            if (breachPoint != null) {
                Direction breachDirection = null;
                for (Direction dir : Direction.values()) {
                    if (oldScan.containsInterior(breachPoint.relative(dir))) {
                        breachDirection = dir.getOpposite();
                        break;
                    }
                }
                // TODO: spawn vortex at breach point and direction
            }
        }
    }

    /**
     * Find the breach point by comparing the old and new scan.
     * This finds one block that was part of the old shell and is part of the new interior.
     * There can be multiple such blocks in the case of partially passable blocks, but let's pretend there isn't.
     * @param oldScan Previous RoomScan that was sealed
     * @param newScan Current RoomScan that escaped to build height (== unsealed)
     * @return First block that's the breach point
     */
    @Nullable
    private BlockPos findBreachPoint(RoomScan oldScan, RoomScan newScan) {
        OptionalLong breach = newScan.interior().longStream()
                .filter(e -> !oldScan.interior().contains(e) && oldScan.envelope().contains(e))
                .findAny();

        if (breach.isPresent()) {
            return BlockPos.of(breach.getAsLong());
        }
        return null;
    }

    /**
     * Calculate the earliest tick at which we want to revalidate.
     * The purpose is to batch many blockchanges together instead of flood fill for each one.
     * @param interval The interval between revalidations, if it were always trying to revalidate
     * @return The next tick that's greater than the current getTickCount at which we want to start revalidating
     */
    private long calculateEarliestTick(int interval) {
        long now = level.getServer().getTickCount();

        int phase = Math.floorMod(-tickOffset, interval);
        long delta = Math.floorMod(phase - (now % interval), interval);
        return now + delta;
    }

    public void onBlockChange(BlockEvent event) {
        if (provider == null) return;

        BlockPos pos = event.getPos();
        RoomScan roomScan = provider.getRoomScan();

        if (!dirty) {
            if ((roomScan.isSealed() && roomScan.containsEnvelope(pos))
                    || roomScan.containsInterior(pos)) {
                requestValidation();
            }
        }
    }

    public void onGridSpatialEvent(BlockPos min, BlockPos max) {
        if (provider == null) return;

        RoomScan roomScan = provider.getRoomScan();
        if (roomScan.bounds().intersects(new AABB(min, max))) {
            requestValidation();
        }
    }

    public void onChunkLoaded(ChunkPos chunkPos) {
        requestValidation();
    }

    private void requestValidation() {
        setDirty(true);
        //TODO: Set earliest tick interval dependent on current room size
        AtmosphereSystem.requestValidation(this, calculateEarliestTick(100));

        if (provider != null && provider.getRoomScan().status() == Status.ESCAPED_UNLOADED) {
            BlockPos escapePoint = provider.getRoomScan().escapePoint();
            if (escapePoint != null) {
                manager.chunkLoadListeners.remove(this, Set.of(new ChunkPos(escapePoint)));
            }
        }
    }

    //////////////////////////////////////
    // ****** Machine Lifecycle ******* //
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null) return;

        provider = manager.getOrCreateProvider(getPos());
        provider.attach(this);

        requestValidation();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (provider == null) return;

        provider.detach();
        deregisterMachineListeners();
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        if (manager == null) return;

        deregisterMachineListeners();
        manager.removeProvider(getPos());
        provider = null;
    }

    /**
     * Deregister machine-specific listeners (not oxygen provider).
     * Called on both unload and removal.
     */
    private void deregisterMachineListeners() {
        if (provider == null) return;

        RoomScan roomScan = provider.getRoomScan();
        manager.blockChangeListeners.remove(this, roomScan.touchedChunks());

        if (pendingChunkLoad != null) {
            manager.chunkLoadListeners.removeSingle(this, pendingChunkLoad);
            pendingChunkLoad = null;
        }
    }
}
