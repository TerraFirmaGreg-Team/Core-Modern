package su.terrafirmagreg.core.common.tfgt.machine.electric;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import lombok.Getter;
import lombok.Setter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.environment.*;

/**
 * Heat Pump machine.
 */
public class HeatPumpMachine implements IBlockSensitiveMachine, IEnvironmentMachine {

    /** GT multiblock wrapper hosting this logic. */
    private final IHeatPumpHost host;

    /** The provider that holds our region data and handles temperature queries. */
    @Nullable
    private TemperatureProvider provider;

    private ServerLevel level;
    private DimEnvManager manager;

    @Getter
    @Setter
    private boolean dirty;

    /** Tick when this machine last had a validation dispatched. */
    private long lastValidationTick;

    /** Pending scan results from async validation, written on the async thread. */
    private RoomScan newFrontScan = RoomScan.empty();
    private RoomScan newBackScan = RoomScan.empty();
    private boolean newBlocked = false;
    private BlockPos newExhaustAnchor = BlockPos.ZERO;

    /** Default scan block limit for the flood fills. */
    static final int SCAN_MAX_BLOCKS = 2_000_000;
    static final int MAX_HORIZONTAL_DIMENSION = 128;

    /** Radius (blocks) of the proximity-based exhaust heat field behind the machine. */
    static final int EXHAUST_RADIUS = 12;

    private static final int TRACE_MAX_BLOCKS = 1_000_000;
    private static final int TRACE_COOLDOWN_TICKS = 100;
    private long lastTraceRequestTick = 0;

    /** Throttled revalidation period for block changes. */
    private static final int REVALIDATION_GAP_TICKS = 40;

    public HeatPumpMachine(IHeatPumpHost host) {
        this.host = host;
    }

    //////////////////////////////////////
    // ********* Energy Cost ***********//
    //////////////////////////////////////

    /** @return the expected EU/t consumption, or 0 when not working. */
    public double computeEnergyCostPerTick() {
        if (isBlocked() || !isFrontSealed())
            return 0;
        int size = getFrontInteriorSize();
        if (size <= 0)
            return 0;
        return EnclosedRoomEnergyCurve.eutForVolume(size);
    }

    public boolean isBlocked() {
        return provider != null ? provider.isBlocked() : newBlocked;
    }

    public boolean isFrontSealed() {
        return provider != null ? provider.getFrontScan().isSealed() : newFrontScan.isSealed();
    }

    public RoomScan getFrontScan() {
        return provider != null ? provider.getFrontScan() : newFrontScan;
    }

    public RoomScan getBackScan() {
        return provider != null ? provider.getBackScan() : newBackScan;
    }

    public int getFrontInteriorSize() {
        return getFrontScan().interiorSize();
    }

    /** @return the anchor of the proximity exhaust heat field (centre of the machine's back). */
    public BlockPos getExhaustAnchor() {
        return provider != null ? provider.getExhaustAnchor() : newExhaustAnchor;
    }

    //////////////////////////////////////
    // ********* Recipe Logic **********//
    //////////////////////////////////////

    /** Called by the wrapper's beforeWorking. Re-searches the recipe so energy reacts to region size. */
    public void beforeWorking(@Nullable GTRecipe recipe) {
        if (host.getRecipeLogic() != null) {
            host.getRecipeLogic().markLastRecipeDirty();
        }
    }

    //////////////////////////////////////
    // ****** Revalidation logic *******//
    //////////////////////////////////////

    @Override
    public void validateAsync(AsyncBlockReader reader) {
        TFGCore.LOGGER.debug("[heatpump] validateAsync START, pos={}", getPos());
        long start = System.nanoTime();

        BlockPos pos = getPos();
        BlockPos frontStart = pos.above(1).relative(host.self().getFrontFacing());
        BlockPos backStart = pos.above(1).relative(host.self().getFrontFacing().getOpposite(), 3);

        RoomScan frontScan = FloodFill.fill(reader, frontStart, SCAN_MAX_BLOCKS, MAX_HORIZONTAL_DIMENSION, FloodFill.PassMode.PERMISSIVE);
        RoomScan backScan = FloodFill.fill(reader, backStart, SCAN_MAX_BLOCKS, MAX_HORIZONTAL_DIMENSION, FloodFill.PassMode.PERMISSIVE);

        // The exhaust can only escape if the back does not form a sealed room.
        boolean blocked = backScan.isSealed();

        newFrontScan = frontScan;
        newBackScan = backScan;
        newBlocked = blocked;
        newExhaustAnchor = backStart;

        long elapsed = (System.nanoTime() - start) / 1_000_000;
        TFGCore.LOGGER.debug("[heatpump] validateAsync DONE, pos={}, elapsedMs={}, frontStatus={}, frontSize={}, backStatus={}, blocked={}",
                getPos(), elapsed, frontScan.status(), frontScan.interiorSize(), backScan.status(), blocked);
    }

    @Override
    public ServerLevel getServerLevel() {
        return level;
    }

    @Override
    public void processValidationResult() {
        TFGCore.LOGGER.debug("[heatpump] processValidationResult, pos={}, provider={}", getPos(), provider != null);
        if (provider == null)
            return;

        Set<ChunkPos> oldChunks = provider.getAffectedChunks();

        provider.setRegions(newFrontScan, newBackScan, newBlocked, newExhaustAnchor, EXHAUST_RADIUS);

        Set<ChunkPos> newChunks = provider.getAffectedChunks();

        Set<ChunkPos> toRemoveListeners = new HashSet<>(oldChunks);
        toRemoveListeners.removeAll(newChunks);
        if (!toRemoveListeners.isEmpty() || !newChunks.isEmpty()) {
            manager.blockChangeListeners.update(this, toRemoveListeners, newChunks);
        }

        manager.updateTempProviderRegions(provider, oldChunks, newChunks);

        host.setShowTraceButton(!newFrontScan.isSealed() && newFrontScan.hasEscapePoint());

        if (host.getRecipeLogic() != null) {
            host.getRecipeLogic().onRecipeFinish();
        }
    }

    @Override
    public void onBlockChangeAt(BlockPos pos) {
        if (provider == null)
            return;
        if (dirty)
            return;
        if (pos.equals(getPos()))
            return;
        if (provider.getAffectedChunks().contains(new ChunkPos(pos))) {
            requestValidation();
        }
    }

    @Override
    public void onGridSpatialEvent(BlockPos min, BlockPos max) {
        requestValidation();
    }

    @Override
    public void onChunkLoad(ChunkPos chunkPos) {
        requestValidation();
    }

    private void requestValidation() {
        setDirty(true);
        long now = level.getServer().getTickCount();
        long earliestTick = Math.max(lastValidationTick + REVALIDATION_GAP_TICKS, now);
        EnvironmentSystem.requestValidation(this, earliestTick);
    }

    @Override
    public void setLastValidationTick(long tick) {
        this.lastValidationTick = tick;
    }

    @Override
    public void requestRevalidation() {
        requestValidation();
    }

    public void requestFrontBreachTrace() {
        if (level == null)
            return;
        long currentTick = level.getServer().getTickCount();
        if (currentTick - lastTraceRequestTick < TRACE_COOLDOWN_TICKS)
            return;
        lastTraceRequestTick = currentTick;

        ServerLevel traceLevel = level;
        BlockPos tracePos = getPos().above(1).relative(host.self().getFrontFacing());
        AsyncBlockReader reader = new AsyncBlockReader(traceLevel);

        EnvironmentSystem.EXECUTOR.submit(() -> {
            try {
                RoomScan result = DiagnosticFloodFill.fill(reader, tracePos, TRACE_MAX_BLOCKS, MAX_HORIZONTAL_DIMENSION, FloodFill.PassMode.PERMISSIVE);
                if (result.escapePath() != null && !result.escapePath().isEmpty()) {
                    DiagnosticFloodFill.spawnTrace(traceLevel, result.escapePath());
                } else {
                    TFGCore.LOGGER.debug("[heatpump] front trace found no escape, revalidating, pos={}", getPos());
                    traceLevel.getServer().execute(this::requestValidation);
                }
            } catch (Exception e) {
                TFGCore.LOGGER.error("Heat pump front breach trace failed at {}", tracePos, e);
            }
        });
    }

    //////////////////////////////////////
    // ****** Machine Lifecycle ******* //
    //////////////////////////////////////

    public void onLoad(ServerLevel serverLevel) {
        TFGCore.LOGGER.debug("[heatpump] onLoad, pos={}", getPos());
        level = serverLevel;
        manager = EnvironmentSystem.getManager(level);
        provider = manager.getOrCreateTempProvider(getPos());
        provider.attach(this);
        requestValidation();
    }

    public void onUnload() {
        TFGCore.LOGGER.debug("[heatpump] onUnload, pos={}", getPos());
        if (provider == null)
            return;
        provider.detach();
        deregisterListeners();
        provider = null;
    }

    public void onRemoved() {
        TFGCore.LOGGER.debug("[heatpump] onRemoved, pos={}", getPos());
        if (manager == null)
            return;
        deregisterListeners();
        manager.removeTempProvider(getPos());
        provider = null;
    }

    private void deregisterListeners() {
        EnvironmentSystem.cancelValidation(this);
        if (provider == null)
            return;
        manager.blockChangeListeners.remove(this, provider.getAffectedChunks());
    }

    // IEnvironmentMachine / IBlockSensitiveMachine
    @Override
    public BlockPos getPos() {
        return host.self().getPos();
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public boolean isWorking() {
        if (isBlocked() || !isFrontSealed())
            return false;
        return host.getRecipeLogic() != null && host.getRecipeLogic().isWorking();
    }
}
