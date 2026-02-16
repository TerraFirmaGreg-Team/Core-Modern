package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import java.util.*;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;

import earth.terrarium.adastra.common.blocks.SlidingDoorBlock;
import earth.terrarium.adastra.common.blocks.properties.SlidingDoorPartProperty;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;
import lombok.Setter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.environment.*;
import su.terrafirmagreg.core.common.environment.RoomScan.Status;

/**
 * Oxygen Distributor machine that maintains a sealed room with breathable environment.
 * Uses flood fill to detect room boundaries and provides oxygen to all positions within.
 * <p>
 * The actual oxygen data is stored in {@link OxygenProvider} which persists independently
 * of this machine's chunk load state, allowing oxygen queries even when this chunk is unloaded.
 */
public class OxygenDistributorMachine extends SimpleTieredMachine implements IBlockSensitiveMachine, IEnvironmentMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OxygenDistributorMachine.class, SimpleTieredMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /** The provider that holds our room data and handles oxygen queries */
    @Nullable
    private OxygenProvider provider;

    /** Synced to client for UI button visibility */
    @Persisted
    @DescSynced
    private boolean showTraceButton;

    /** Pending scan result from async validation */
    private RoomScan newRoomScan;

    /** Offset for staggering validation timing across machines */
    private long tickOffset;

    @Getter
    @Setter
    private boolean dirty;

    private ServerLevel level;
    private DimEnvManager manager;

    @Nullable
    private ChunkPos pendingChunkLoad = null;

    /** Active decompression event for this machine's room, if any */
    @Nullable
    private DecompressionEvent activeDecompression = null;

    /** Tick count of last breach trace request, for cooldown */
    private long lastTraceRequestTick = 0;
    private static final int TRACE_COOLDOWN_TICKS = 100;

    private static final int MAX_BLOCKS = 1_000_000;
    private static final int MAX_HORIZONTAL_DIMENSION = 128;

    public OxygenDistributorMachine(IMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction) {
        super(holder, tier, tankScalingFunction);
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

    /** Fixed high multiplier for unsealed rooms - makes the machine impractical to run without a sealed room */
    private static final int UNSEALED_MULTIPLIER = 10000;

    /**
     * Recipe Modifier for <b>Oxygen Distributors</b> - can be used as a valid {@link RecipeModifier}
     * Sealed rooms scale cost by interior size. Unsealed rooms use a fixed high cost.
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof OxygenDistributorMachine oxygenMachine)) {
            return RecipeModifier.nullWrongType(OxygenDistributorMachine.class, machine);
        }

        RoomScan scan = oxygenMachine.getRoomScan();
        int multiplier;
        if (scan.isSealed()) {
            multiplier = Math.max(1, scan.interiorSize());
        } else if (scan.status() == RoomScan.Status.NULL) {
            // Not yet validated - use base cost so the machine can start
            multiplier = 1;
        } else {
            // Unsealed room - high cost to discourage running without sealed room
            multiplier = UNSEALED_MULTIPLIER;
        }

        return ModifierFunction.builder()
                //.eutMultiplier(multiplier)
                .inputModifier(ContentModifier.multiplier(multiplier))
                .build();
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    //////////////////////////////////////
    // ************* GUI ************** //
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        // Content area: 164x78 fills the standard 176x166 GT window (172-8 border, 86-8 border min)
        int width = 164;
        int height = 78;
        int rightColX = width - 40; // 18px column + 4px margin from right edge

        var group = new WidgetGroup(0, 0, width, height);

        // Status text panel
        group.addWidget(new ComponentPanelWidget(4, 4, this::addStatusText)
                .textSupplier(getLevel().isClientSide ? null : this::addStatusText)
                .setMaxWidthLimit(rightColX - 8));

        // "Find Leak" button, only visible when room is unsealed with an escape point
        var traceButton = new ButtonWidget(41 - 18, height - 20, 18, 18,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("💨")), cd -> {
                    if (!cd.isRemote) {
                        requestBreachTrace();
                    }
                }) {
            @Override
            public void updateScreen() {
                super.updateScreen();
                setVisible(showTraceButton);
            }
        };
        traceButton.setHoverTooltips(Component.translatable("tfg.machine.oxygen_distributor.find_leak"));
        group.addWidget(traceButton);

        // Progress bar
        group.addWidget(new ProgressWidget(recipeLogic::getProgressPercent, rightColX, height - 19 - 20 - 8, 20, 20,
                getRecipeType().getRecipeUI().getProgressBarTexture()));

        // Input slot
        var fluidTank = new TankWidget(importFluids.getStorages()[0], rightColX + 1, height - 19, true, true);
        fluidTank.setFillDirection(ProgressTexture.FillDirection.UP_TO_DOWN);
        fluidTank.setBackground(GuiTextures.FLUID_SLOT);
        group.addWidget(fluidTank);

        // Battery slot
        SlotWidget batterySlot = createBatterySlot().createDefault();
        batterySlot.setSelfPosition(new Position(width / 2 - 9, height - 19));
        group.addWidget(batterySlot);
        createBatterySlot().setupUI(group, this);

        return group;
    }

    private void addStatusText(List<Component> textList) {
        RoomScan scan = getRoomScan();

        // Status line
        Component statusText = switch (scan.status()) {
            case SEALED -> Component.translatable("tfg.machine.oxygen_distributor.status.sealed").withStyle(ChatFormatting.GREEN);
            case ESCAPED_BUILD_HEIGHT -> Component.translatable("tfg.machine.oxygen_distributor.status.breached").withStyle(ChatFormatting.RED);
            case ESCAPED_DIMENSION -> Component.translatable("tfg.machine.oxygen_distributor.status.too_wide").withStyle(ChatFormatting.YELLOW);
            case ESCAPED_UNLOADED -> Component.translatable("tfg.machine.oxygen_distributor.status.chunk_unloaded").withStyle(ChatFormatting.YELLOW);
            case BLOCK_LIMIT -> Component.translatable("tfg.machine.oxygen_distributor.status.volume_limit",
                    FormattingUtil.formatNumbers(MAX_BLOCKS)).withStyle(ChatFormatting.YELLOW);
            case SAVED_DATA -> Component.translatable("tfg.machine.oxygen_distributor.status.restoring").withStyle(ChatFormatting.GREEN);
            case NULL -> Component.translatable("tfg.machine.oxygen_distributor.status.scanning").withStyle(ChatFormatting.GRAY);
        };
        textList.add(Component.translatable("tfg.machine.oxygen_distributor.status").append(statusText));

        // Size — only meaningful when sealed
        if (scan.isSealed() && scan.interiorSize() > 0) {
            textList.add(Component.translatable("tfg.machine.oxygen_distributor.size",
                    FormattingUtil.formatNumbers(scan.interiorSize())).withStyle(ChatFormatting.AQUA));
        }

        // Working state
        if (isWorking()) {
            textList.add(Component.translatable("tfg.machine.oxygen_distributor.active").withStyle(ChatFormatting.GREEN));
        } else {
            textList.add(Component.translatable("tfg.machine.oxygen_distributor.idle").withStyle(ChatFormatting.GRAY));
        }
    }

    private void requestBreachTrace() {
        if (level == null)
            return;

        long currentTick = level.getServer().getTickCount();
        if (currentTick - lastTraceRequestTick < TRACE_COOLDOWN_TICKS)
            return;
        lastTraceRequestTick = currentTick;

        ServerLevel traceLevel = level;
        BlockPos tracePos = getPos();
        // Create the reader on the main thread — it captures ChunkMap safely
        AsyncBlockReader reader = new AsyncBlockReader(traceLevel);

        EnvironmentSystem.EXECUTOR.submit(() -> {
            try {
                RoomScan result = DiagnosticFloodFill.fill(reader, tracePos, MAX_BLOCKS, MAX_HORIZONTAL_DIMENSION);
                if (result.escapePath() != null && !result.escapePath().isEmpty()) {
                    DiagnosticFloodFill.spawnTrace(traceLevel, result.escapePath());
                }
            } catch (Exception e) {
                TFGCore.LOGGER.error("Breach trace failed at {}", tracePos, e);
            }
        });
    }

    //////////////////////////////////////
    // ****** Revalidation logic ****** //
    //////////////////////////////////////

    /**
     * Call this async to revalidate the room.
     * Runs a new flood fill.
     * Stores the result in newRoomScan which gets processed on the main thread in {@link #processValidationResult()}.
     */
    public void validateAsync(AsyncBlockReader reader) {
        TFGCore.LOGGER.info("[validation] validateAsync START, pos={}, identity={}", getPos(), System.identityHashCode(this));
        long start = System.nanoTime();
        newRoomScan = FloodFill.fill(reader, getPos(), MAX_BLOCKS, MAX_HORIZONTAL_DIMENSION);
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        TFGCore.LOGGER.info("[validation] validateAsync DONE, pos={}, identity={}, elapsedMs={}, status={}, size={}",
                getPos(), System.identityHashCode(this), elapsed, newRoomScan.status(), newRoomScan.interiorSize());
    }

    @Override
    public ServerLevel getServerLevel() {
        return level;
    }

    /**
     * Call this on the main thread to apply the revalidation results when they're ready.
     * Handles transition to the new RoomScan:
     * - Spawn vortex if status from sealed to escaped build height
     * - Update provider and registries
     */
    public void processValidationResult() {
        TFGCore.LOGGER.info("[validation] processValidationResult, pos={}, identity={}, provider={}, newRoomScan={}",
                getPos(), System.identityHashCode(this), provider != null, newRoomScan != null);
        if (provider == null || newRoomScan == null) {
            return;
        }
        long start = System.nanoTime();

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

            // The chunk may have loaded between the async fill and now (we'd miss the event).
            // If already loaded, skip the listener and just re-request immediately.
            if (level.isLoaded(newScan.escapePoint())) {
                requestValidation();
            } else {
                pendingChunkLoad = new ChunkPos(newScan.escapePoint());
                manager.chunkLoadListeners.addSingle(this, pendingChunkLoad);
            }
            return;
        }

        // Compute chunk diff for block change listeners.
        // SAVED_DATA means this machine is new (loaded from disk) and wasn't in blockChangeListeners yet,
        // so treat old chunks as empty to force a full registration.
        Set<ChunkPos> oldListenerChunks = oldScan.status() == Status.SAVED_DATA ? Set.of() : oldScan.touchedChunks();
        Set<ChunkPos> newChunks = newScan.touchedChunks();

        Set<ChunkPos> toRemove = new HashSet<>(oldListenerChunks);
        toRemove.removeAll(newChunks);

        Set<ChunkPos> toAdd = new HashSet<>(newChunks);
        toAdd.removeAll(oldListenerChunks);

        // Update block change listener registry
        manager.blockChangeListeners.update(this, toRemove, toAdd);
        TFGCore.LOGGER.info("Registered in {} chunks for block change listening", newChunks.size());

        // Update provider's room scan and oxygen chunk registry
        manager.updateProvider(provider, oldScan, newScan);

        // Sync button visibility to client
        showTraceButton = !newScan.isSealed() && newScan.status() != Status.NULL && newScan.hasEscapePoint();

        // Room changed: finish current recipe early so it re-searches with the new room size.
        // Also resets runAttempt/runDelay recomputes the recipe modifier.
        if (recipeLogic != null) {
            recipeLogic.onRecipeFinish();
        }

        // Decompression: sealed → breached, start event
        // Only on ESCAPED_BUILD_HEIGHT (actual physical breach through the shell).
        // Not on BLOCK_LIMIT (machine too weak), ESCAPED_DIMENSION (horizontal limit), or atmosphered dimensions.
        if (oldScan.isSealed() && newScan.status() == Status.ESCAPED_BUILD_HEIGHT && !manager.getEnvironment().hasAtmosphere()) {
            BlockPos breachPoint = findBreachPoint(oldScan, newScan);
            if (breachPoint != null) {
                activeDecompression = manager.startDecompression(breachPoint, oldScan);
            }
        }

        // Room fixed: escaped → sealed, cancel active decompression
        if (!oldScan.isSealed() && newScan.isSealed() && activeDecompression != null) {
            activeDecompression.cancel(level);
            activeDecompression = null;
        }

        long elapsed = (System.nanoTime() - start) / 1_000_000;
        TFGCore.LOGGER.info("[validation] processValidationResult DONE, pos={}, identity={}, elapsedMs={}, oldStatus={}, newStatus={}",
                getPos(), System.identityHashCode(this), elapsed, oldScan.status(), newScan.status());
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
            BlockPos pos = BlockPos.of(breach.getAsLong());
            var state = level.getBlockState(pos);
            if (state.getBlock() instanceof SlidingDoorBlock) {
                pos = snapToAirlockCenter(pos, state);
            }
            return pos;
        }
        return null;
    }

    /**
     * Snap to the center part of an airlock so the decompression force
     * pulls toward the middle of the door opening.
     */
    private static BlockPos snapToAirlockCenter(BlockPos pos, BlockState state) {
        SlidingDoorPartProperty part = state.getValue(SlidingDoorBlock.PART);
        net.minecraft.core.Direction clockwise = state.getValue(SlidingDoorBlock.FACING).getClockWise();

        // Center part is at xOffset=0, yOffset=1 relative to controller.
        // Current part is at part.xOffset(), part.yOffset().
        // Move by the difference to reach center.
        return pos.relative(clockwise, part.xOffset()).above(1 - part.yOffset());
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
        if (provider == null)
            return;

        if (!dirty) {
            BlockPos pos = event.getPos();

            // Ignore our own block being broken — machine is about to be removed
            if (pos.equals(getPos()))
                return;

            RoomScan roomScan = provider.getRoomScan();

            TFGCore.LOGGER.info("Sealed {}, inEnvelope {}, inInterior {}", roomScan.isSealed(), roomScan.containsEnvelope(pos), roomScan.containsInterior(pos));
            if ((roomScan.isSealed() && roomScan.containsEnvelope(pos))
                    || roomScan.containsInterior(pos)) {
                requestValidation();
            } else {
                TFGCore.LOGGER.info("Ignored");
            }
        }
    }

    public void onGridSpatialEvent(BlockPos min, BlockPos max) {
        if (provider == null)
            return;

        RoomScan roomScan = provider.getRoomScan();
        if (roomScan.bounds().intersects(new AABB(min, max))) {
            requestValidation();
        }
    }

    public void onChunkLoad(ChunkPos chunkPos) {
        requestValidation();
    }

    private void requestValidation() {
        TFGCore.LOGGER.info("[validation] requestValidation, pos={}, identity={}", getPos(), System.identityHashCode(this));
        setDirty(true);
        //TODO: Set earliest tick interval dependent on current room size
        EnvironmentSystem.requestValidation(this, calculateEarliestTick(1));

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
        if (!(getLevel() instanceof ServerLevel serverLevel))
            return;

        TFGCore.LOGGER.info("onLoad, pos={}, identity={}", getPos(), System.identityHashCode(this));

        tickOffset = getPos().hashCode();

        level = serverLevel;
        manager = EnvironmentSystem.getManager(level);

        provider = manager.getOrCreateProvider(getPos());
        provider.attach(this);

        requestValidation();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        TFGCore.LOGGER.info("onUnload, pos={}, identity={}, provider={}", getPos(), System.identityHashCode(this), provider != null);
        if (provider == null)
            return;

        provider.detach();
        deregisterMachineListeners();
        provider = null;
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        TFGCore.LOGGER.info("onMachineRemoved, pos={}, identity={}", getPos(), System.identityHashCode(this));
        if (manager == null)
            return;

        deregisterMachineListeners();
        manager.removeProvider(getPos());
        provider = null;
    }

    /**
     * Deregister machine-specific listeners (not oxygen provider).
     * Called on both unload and removal.
     */
    private void deregisterMachineListeners() {
        EnvironmentSystem.cancelValidation(this);

        if (provider == null)
            return;

        RoomScan roomScan = provider.getRoomScan();
        manager.blockChangeListeners.remove(this, roomScan.touchedChunks());

        if (pendingChunkLoad != null) {
            manager.chunkLoadListeners.removeSingle(this, pendingChunkLoad);
            pendingChunkLoad = null;
        }

        if (activeDecompression != null) {
            activeDecompression.cancel(level);
            activeDecompression = null;
        }
    }

}
