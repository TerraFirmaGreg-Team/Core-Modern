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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.common.atmosphere.AtmosphereSystem;
import su.terrafirmagreg.core.common.atmosphere.DimensionAtmosphereManager;
import su.terrafirmagreg.core.common.atmosphere.IFloodFillMachine;
import su.terrafirmagreg.core.common.atmosphere.RoomScan;
import net.minecraft.world.phys.AABB;

import java.util.Set;

public class OxygenDistributorMachine extends SimpleTieredMachine implements IFloodFillMachine, AtmosphereSystem.IOxygenProvider {

    //TODO data persistence
    //TODO for testing purposes, right now, it doesn't provide a bubble fallback, it's pure flood fill or nothing

    public RoomScan roomScan;
    private RoomScan newRoomScan;
    private long tickOffset;
    @Getter
    @Setter
    private boolean dirty;
    private ServerLevel level;
    private DimensionAtmosphereManager manager;

    /** TODO: Javadoc goes here? */
    public OxygenDistributorMachine(IMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction, Object... args) {
        super(holder, tier, tankScalingFunction, args);
        if (holder.level() instanceof ServerLevel serverLevel) {
            level = serverLevel;
            manager = AtmosphereSystem.getManager(level);
            tickOffset = holder.pos().hashCode();
        }
    }

    /** @return whether this machine is providing oxygen to the given BlockPos */
    public boolean hasOxygen(BlockPos pos) {
        return isWorking() && roomScan.containsEnvelope(pos);
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

        var roomSize = oxygenMachine.roomScan.interiorSize();

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
     * TODO Bruh how do you write links
     */
    public void validateAsync() {
        // TODO
        // Run new floodfill
        //  maybe the machine outputs air on one specific side, and we should use the neighbor block? For now use the block itself, tag it passable
        // Store result in newRoomScan
        // Manager handles memory edge
    }

    /**
     * Call this on the main thread to apply the revalidation results when they're ready.
     * Handles transition to the new RoomScan:
     * Spawn vortex if status from sealed to escaped build height
     * Update MachineRegistries (listeners, provider)
     */
    public void processValidationResult() {
        // TODO
        // We have roomScan and newRoomScan both available here, for comparison
        // handle roomScan = null (first floodfill)
        // if it was sealed, and now it's escapes build height
        //    spawn vortex
        // update which chunks we're listening to
        // update which chunks we're providing air for
        // add listener for chunkload if needed
        // update tooltips with new status
        // update roomScan = newRoomScan, newRoomScan = null
        // maybe update the recipe? Maybe that goes automagically
    }

    /** Calculate the earliest tick at which we want to revalidate.
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
        BlockPos pos = event.getPos();
        if (!dirty) {
            if ((roomScan.isSealed() && roomScan.containsEnvelope(pos))
                    || roomScan.containsInterior(pos)) {
                requestValidation();
            }
        }
    }

    public void onGridSpatialEvent(BlockPos min, BlockPos max) {
        if (roomScan.bounds().intersects(new AABB(min, max))) {
            // Technically the AABB can overlap without any of the room being in the spatial event.
            //  However, this is way faster than iterating over all the blocks and spatial events are rare anyway.
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

        if (roomScan.status() == RoomScan.Status.ESCAPED_UNLOADED) {
            assert roomScan.escapePoint() != null;
            manager.chunkLoadListeners.remove(this, Set.of(new ChunkPos(roomScan.escapePoint())));
        }
    }

    //////////////////////////////////////
    // ****** Machine Lifecycle ******* //
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        // TODO: How do I run this serverside only? I'm not getting any arguments with onLoad? Maybe I check if class attribute level != null?
        // TODO: Maybe we have stored data? How does that work?
        requestValidation();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        deregister();
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        deregister();
    }

    /**
     * Deregister from all registries
     */
    private void deregister() {
        manager.blockChangeListeners.remove(this, roomScan.touchedChunks());

        if (roomScan.status() == RoomScan.Status.ESCAPED_UNLOADED) {
            assert roomScan.escapePoint() != null;
            manager.chunkLoadListeners.remove(this, Set.of(new ChunkPos(roomScan.escapePoint())));
        }

        if (roomScan.isSealed()) {
            manager.oxygenMachines.remove(this, roomScan.touchedChunks());
        }
    }
}
