package su.terrafirmagreg.core.common.tfgt.machine.electric;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.Getter;
import lombok.Setter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.environment.*;

/**
 * Space Heater machine.
 * The front is flood filled from just in front of the controller. If it produces a sealed room,
 * the whole sealed envelope is the comfort region. If it escapes (i.e. in a building on earth), a
 * greedy fill heats only a small local radius immediately in front. The back always gets a small
 * greedy-fill hazard region (1/64 of the front's volume). If the two regions reach each other the
 * machine cannot operate.
 */
public class SpaceHeaterMachine implements IBlockSensitiveMachine, IEnvironmentMachine {

    /** GT multiblock wrapper hosting this logic. */
    private final ISpaceHeaterHost host;

    /** The provider that holds our front/back region data and handles temperature queries. */
    @Nullable
    private TemperatureProvider provider;

    private ServerLevel level;
    private DimEnvManager manager;

    @Getter
    @Setter
    private boolean dirty;

    /** Tick when this machine last had a validation dispatched. */
    private long lastValidationTick;

    /** Last validation dispatch from a change in the separation zone, throttled separately. */
    private long lastSeparationValidationTick;

    /** Pending scan results from async validation, written on the async thread. */
    private Set<BlockPos> newFrontGood = Set.of();
    private Set<BlockPos> newBackHazard = Set.of();
    private TemperatureProvider.Mode newMode = TemperatureProvider.Mode.VENTED;
    private boolean newBlocked = false;
    private boolean newBackVentInsufficient = false;
    private RoomScan newFrontScan = RoomScan.empty();

    /** Default scan block limit for the flood fills. */
    static final int SCAN_MAX_BLOCKS = 2_000_000;
    static final int MAX_HORIZONTAL_DIMENSION = 128;

    private static final int TRACE_MAX_BLOCKS = 1_000_000;
    private static final int TRACE_COOLDOWN_TICKS = 100;
    private long lastTraceRequestTick = 0;

    /** Radius of blocks to check to validate that the front and back regions don't touch in case of unsealed rooms. */
    private static final int SEPARATION_RADIUS = 20;

    /** Slower revalidation period for block changes  in case of unsealed rooms. */
    private static final int SEPARATION_REVALIDATION_GAP_TICKS = 200;

    public SpaceHeaterMachine(ISpaceHeaterHost host) {
        this.host = host;
    }

    //////////////////////////////////////
    // ********* Energy Cost ***********//
    //////////////////////////////////////

    /** @return the expected EU/t consumption, or 0 when not working. */
    public double computeEnergyCostPerTick() {
        if (isBlocked() || isBackVentInsufficient())
            return 0;
        int size = getFrontGoodCount();
        if (size <= 0)
            return 0;

        TemperatureProvider provider = this.provider;
        if (provider != null && provider.getMode() == TemperatureProvider.Mode.VENTED) {
            long voltage = host.getHatchVoltage();
            if (voltage <= 0)
                return 0;
            return voltage * 0.5;
        }
        return EnclosedRoomEnergyCurve.eutForVolume(size);
    }

    /** Maximum unsealed room size. */
    private int greedyMaxFrontForEnergy() {
        long voltage = host.getHatchVoltage();
        if (voltage <= 0)
            voltage = GTValues.V[GTValues.MV];
        double volume = EnclosedRoomEnergyCurve.volumeForEut(voltage * 0.5);
        return Math.max(1, Math.min(SCAN_MAX_BLOCKS, (int) Math.round(volume)));
    }

    public boolean isBlocked() {
        return provider != null ? provider.isBlocked() : newBlocked;
    }

    /** Whether the back region cannot vent into enough open air to complete its fill. */
    public boolean isBackVentInsufficient() {
        return newBackVentInsufficient;
    }

    public int getFrontGoodCount() {
        return provider != null ? provider.getFrontGood().size() : newFrontGood.size();
    }

    public int getBackHazardCount() {
        return provider != null ? provider.getBackHazard().size() : newBackHazard.size();
    }

    public TemperatureProvider.Mode getMode() {
        return provider != null ? provider.getMode() : newMode;
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
        TFGCore.LOGGER.debug("[spaceheater] validateAsync START, pos={}", getPos());
        long start = System.nanoTime();

        BlockPos pos = getPos();
        BlockPos frontStart = pos.relative(host.self().getFrontFacing());
        BlockPos backStart = pos.relative(host.self().getFrontFacing().getOpposite(), host.getBackOffset());

        RoomScan frontScan = FloodFill.fill(reader, frontStart, SCAN_MAX_BLOCKS, MAX_HORIZONTAL_DIMENSION);

        boolean blocked = false;
        TemperatureProvider.Mode mode;
        Set<BlockPos> frontGood;

        if (frontScan.isSealed()) {
            frontGood = envelopeToPosSet(frontScan.envelope());
            mode = TemperatureProvider.Mode.SEALED;
        } else {
            GreedyResult frontGreedy = greedyFill(reader, frontStart, greedyMaxFrontForEnergy());
            frontGood = frontGreedy.blocks;
            mode = TemperatureProvider.Mode.VENTED;
        }

        int backMax = Math.max(1, frontGood.size() / 64);
        GreedyResult backGreedy = greedyFill(reader, backStart, backMax);
        Set<BlockPos> backHazard = backGreedy.blocks;
        boolean backVentInsufficient = !backGreedy.reachedCap;
        if (mode == TemperatureProvider.Mode.SEALED) {
            blocked = backHazard.stream().anyMatch(frontScan::containsInterior);
        } else {
            blocked = frontStart.equals(backStart) || backHazard.stream().anyMatch(frontGood::contains);
        }

        newFrontGood = frontGood;
        newBackHazard = backHazard;
        newMode = mode;
        newBlocked = blocked;
        newBackVentInsufficient = backVentInsufficient;
        newFrontScan = frontScan;

        long elapsed = (System.nanoTime() - start) / 1_000_000;
        TFGCore.LOGGER.debug("[spaceheater] validateAsync DONE, pos={}, elapsedMs={}, mode={}, front={}, back={}, blocked={}",
                getPos(), elapsed, mode, frontGood.size(), backHazard.size(), blocked);
    }

    private static Set<BlockPos> envelopeToPosSet(LongOpenHashSet envelope) {
        Set<BlockPos> result = new HashSet<>(envelope.size());
        for (long l : envelope) {
            result.add(BlockPos.of(l));
        }
        return result;
    }

    /**
     * BFS greedy fill collecting the nearest fillable blocks up to {@code maxBlocks}.
     */
    private GreedyResult greedyFill(AsyncBlockReader reader, BlockPos start, int maxBlocks) {
        LongOpenHashSet collected = new LongOpenHashSet();
        Set<BlockPos> blocks = new HashSet<>();
        LongOpenHashSet visited = new LongOpenHashSet();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();

        queue.add(start);
        visited.add(start.asLong());

        while (!queue.isEmpty() && collected.size() < maxBlocks) {
            BlockPos current = queue.poll();
            long currentLong = current.asLong();

            BlockState state = reader.getBlockState(current);
            if (state == null)
                continue; // unloaded chunk, skip expansion

            if (blocks.size() >= maxBlocks)
                break;

            collected.add(currentLong);
            blocks.add(current.immutable());

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                long neighborLong = neighbor.asLong();
                if (visited.contains(neighborLong) || !reader.hasChunkAt(neighbor))
                    continue;
                BlockState neighborState = reader.getBlockState(neighbor);
                if (neighborState == null)
                    continue;
                if (PassabilityChecker.getCachedPassInfo(neighborState).type() == PassabilityChecker.PassInfo.PassType.EMPTY) {
                    visited.add(neighborLong);
                    queue.add(neighbor.immutable());
                }
            }
        }

        return new GreedyResult(blocks, blocks.size() >= maxBlocks);
    }

    private record GreedyResult(Set<BlockPos> blocks, boolean reachedCap) {
    }

    @Override
    public ServerLevel getServerLevel() {
        return level;
    }

    @Override
    public void processValidationResult() {
        TFGCore.LOGGER.debug("[spaceheater] processValidationResult, pos={}, provider={}", getPos(), provider != null);
        if (provider == null)
            return;

        Set<ChunkPos> oldChunks = provider.getAffectedChunks();

        provider.setRegions(newFrontGood, newBackHazard, newMode, newBlocked, newFrontScan);

        Set<ChunkPos> newChunks = provider.getAffectedChunks();

        Set<ChunkPos> toRemoveListeners = new HashSet<>(oldChunks);
        toRemoveListeners.removeAll(newChunks);
        if (!toRemoveListeners.isEmpty() || !newChunks.isEmpty()) {
            manager.blockChangeListeners.update(this, toRemoveListeners, newChunks);
        }

        manager.updateTempProviderRegions(provider, oldChunks, newChunks);

        host.setShowTraceButton(newMode == TemperatureProvider.Mode.VENTED);

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
        if (provider.getFrontGood().contains(pos) || provider.getBackHazard().contains(pos)) {
            requestValidation();
            return;
        }
        BlockPos machinePos = getPos();
        if (Math.abs(pos.getX() - machinePos.getX()) <= SEPARATION_RADIUS
                && Math.abs(pos.getY() - machinePos.getY()) <= SEPARATION_RADIUS
                && Math.abs(pos.getZ() - machinePos.getZ()) <= SEPARATION_RADIUS) {
            requestSeparationValidation();
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
        long earliestTick = Math.max(lastValidationTick + 40, now);
        EnvironmentSystem.requestValidation(this, earliestTick);
    }

    /** Throttled revalidation for nearby block changes if we're in an unsealed room. */
    private void requestSeparationValidation() {
        setDirty(true);
        long now = level.getServer().getTickCount();
        long earliestTick = Math.max(lastSeparationValidationTick + SEPARATION_REVALIDATION_GAP_TICKS, now);
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
        BlockPos tracePos = getPos().relative(host.self().getFrontFacing());
        AsyncBlockReader reader = new AsyncBlockReader(traceLevel);

        EnvironmentSystem.EXECUTOR.submit(() -> {
            try {
                RoomScan result = DiagnosticFloodFill.fill(reader, tracePos, TRACE_MAX_BLOCKS, MAX_HORIZONTAL_DIMENSION);
                if (result.escapePath() != null && !result.escapePath().isEmpty()) {
                    DiagnosticFloodFill.spawnTrace(traceLevel, result.escapePath());
                } else {
                    TFGCore.LOGGER.debug("[spaceheater] front trace found no escape, revalidating, pos={}", getPos());
                    traceLevel.getServer().execute(this::requestValidation);
                }
            } catch (Exception e) {
                TFGCore.LOGGER.error("Space heater front breach trace failed at {}", tracePos, e);
            }
        });
    }

    //////////////////////////////////////
    // ****** Machine Lifecycle ******* //
    //////////////////////////////////////

    public void onLoad(ServerLevel serverLevel) {
        TFGCore.LOGGER.debug("[spaceheater] onLoad, pos={}", getPos());
        level = serverLevel;
        manager = EnvironmentSystem.getManager(level);
        provider = manager.getOrCreateTempProvider(getPos());
        provider.attach(this);
        requestValidation();
    }

    public void onUnload() {
        TFGCore.LOGGER.debug("[spaceheater] onUnload, pos={}", getPos());
        if (provider == null)
            return;
        provider.detach();
        deregisterListeners();
        provider = null;
    }

    public void onRemoved() {
        TFGCore.LOGGER.debug("[spaceheater] onRemoved, pos={}", getPos());
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
        if (isBlocked() || isBackVentInsufficient())
            return false;
        return host.getRecipeLogic() != null && host.getRecipeLogic().isWorking();
    }
}
