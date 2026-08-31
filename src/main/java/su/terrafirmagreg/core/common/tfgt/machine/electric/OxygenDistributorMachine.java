package su.terrafirmagreg.core.common.tfgt.machine.electric;

import java.util.*;

import javax.annotation.Nullable;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import earth.terrarium.adastra.common.blocks.SlidingDoorBlock;
import earth.terrarium.adastra.common.blocks.properties.SlidingDoorPartProperty;
import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.item.ItemStackHolder;
import lombok.Getter;
import lombok.Setter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.machine.trait.EnvironmentRecipeLogic;
import su.terrafirmagreg.core.common.environment.*;
import su.terrafirmagreg.core.common.environment.RoomScan.Status;

/**
 * Oxygen Distributor machine logic. Maintains a sealed room with breathable atmosphere via flood fill.
 * Attach to a GT machine wrapper ({@link OxygenDistributorSingleblock} or a multiblock) via composition.
 * The GT wrapper handles registration and lifecycle hooks; this class handles everything else.
 */
public class OxygenDistributorMachine implements IBlockSensitiveMachine, IEnvironmentMachine {

    /** GT machine wrapper hosting this logic */
    private final IOxygenDistributorHost host;

    /** The provider that holds our room data and handles oxygen queries */
    @Nullable
    private OxygenProvider provider;

    /** Pending scan result from async validation */
    private RoomScan newRoomScan;

    /** Tick when this machine last had a validation dispatched */
    private long lastValidationTick;

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

    /** Block limit for find leak button */
    private static final int TRACE_MAX_BLOCKS = 1_000_000;
    private static final int MAX_HORIZONTAL_DIMENSION = 128;

    /** Hard cap for a single flood fill scan. */
    private static final int SCAN_MAX_BLOCKS = 2_000_000;

    /** Reference volume (in blocks) used to normalize energy and fluid consumption. */
    public static final int BASE_VOLUME = 10_000;

    public OxygenDistributorMachine(IOxygenDistributorHost host) {
        this.host = host;
    }

    /** @return the expected EU/t consumption based on room size. */
    public double computeEnergyCostPerTick() {
        int effectiveVolume = computeEffectiveVolume();
        double normalized = effectiveVolume / (double) BASE_VOLUME;
        return 32 * Math.pow(normalized, Math.log10(4));
    }

    public RoomScan getRoomScan() {
        return provider != null ? provider.getRoomScan() : RoomScan.empty();
    }

    //////////////////////////////////////
    // ********* Recipe Logic **********//
    //////////////////////////////////////

    /** Called by the wrapper's beforeWorking. */
    public void beforeWorking(@Nullable GTRecipe recipe) {
        updateFluidCost(recipe);
    }

    private void updateFluidCost(@Nullable GTRecipe recipe) {
        if (!(host.getRecipeLogic() instanceof EnvironmentRecipeLogic envLogic))
            return;
        if (recipe == null) {
            envLogic.setFluidCostPerTick(0);
            return;
        }

        int baseCost = 0;
        var fluidInputs = recipe.getTickInputContents(FluidRecipeCapability.CAP);
        if (!fluidInputs.isEmpty()) {
            baseCost = FluidRecipeCapability.CAP.of(fluidInputs.get(0).getContent()).getAmount();
        }
        // baseCost mB/min per 10k blocks -> mB/tick for actual volume
        envLogic.setFluidCostPerTick(baseCost * computeEffectiveVolume() / (60.0 * 20 * 10_000));
    }

    private int computeEffectiveVolume() {
        RoomScan scan = getRoomScan();
        if (scan.isSealed()) {
            return Math.max(1, scan.interiorSize());

        } else if (scan.status() == Status.NULL) {
            return 1;

        } else {
            // Unsealed: cost based on BASE_VOLUME, scaled by pressure difference
            float pressure = manager != null ? manager.getPressure(getPos()) : 0.0f;
            if (pressure < 1.0f) {
                float leakFactor = 1.0f - pressure;
                return Math.max(1, (int) (BASE_VOLUME * (1.0f + 0.3f * leakFactor)));
            } else {
                return Math.max(1, (int) (BASE_VOLUME / pressure));
            }
        }
    }

    //////////////////////////////////////
    // ************* GUI ************** //
    //////////////////////////////////////

    public static final int UI_WIDTH = 164 + 10;
    public static final int UI_HEIGHT = 78;
    public static final int UI_RIGHT_COL_X = UI_WIDTH - 40;

    /** Adds the shared widgets (status, find leak, fill suit, progress bar) to an existing group. */
    public void addSharedWidgets(WidgetGroup group) {
        // Status text
        group.addWidget(new ComponentPanelWidget(4, 4, this::addStatusText)
                .setMaxWidthLimit(UI_RIGHT_COL_X - 4));

        // "Find Leak" button, only visible when room is unsealed with an escape point
        var traceButton = new ButtonWidget(41 - 23, UI_HEIGHT - 19, 18, 18,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("💨")), cd -> {
                    if (!cd.isRemote) {
                        requestBreachTrace();
                    }
                }) {
            @Override
            public void updateScreen() {
                super.updateScreen();
                setVisible(host.showTraceButton());
            }
        };
        traceButton.setHoverTooltips(Component.translatable("tfg.machine.oxygen_distributor.find_leak"));
        group.addWidget(traceButton);

        // "Fill Suit" button
        ButtonWidget fillButton = new ButtonWidget(41 + 5, UI_HEIGHT - 19, 18, 18,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("⛽")), null);
        fillButton.setOnPressCallback(cd -> {
            if (!cd.isRemote) {
                fillSpaceSuit(fillButton.getGui().entityPlayer);
            }
        });
        fillButton.setHoverTooltips(Component.translatable("tfg.machine.oxygen_distributor.fill_suit"));
        group.addWidget(fillButton);

        // Progress bar
        group.addWidget(new ProgressWidget(host.getRecipeLogic()::getProgressPercent,
                UI_RIGHT_COL_X, UI_HEIGHT - 19 - 20 - 8, 20, 20,
                host.getRecipeType().getRecipeUI().getProgressBarTexture()));
    }

    private void addStatusText(List<Component> textList) {
        RoomScan scan = getRoomScan();
        boolean elevated = !scan.isSealed();

        Component statusText = switch (scan.status()) {
            case SEALED -> Component.translatable("tfg.machine.oxygen_distributor.status.sealed").withStyle(ChatFormatting.GREEN);
            case ESCAPED_BUILD_HEIGHT -> Component.translatable("tfg.machine.oxygen_distributor.status.breached").withStyle(ChatFormatting.RED);
            case ESCAPED_DIMENSION -> Component.translatable("tfg.machine.oxygen_distributor.status.too_wide").withStyle(ChatFormatting.YELLOW);
            case ESCAPED_UNLOADED -> Component.translatable("tfg.machine.oxygen_distributor.status.chunk_unloaded").withStyle(ChatFormatting.YELLOW);
            case BLOCK_LIMIT -> Component.translatable("tfg.machine.oxygen_distributor.status.scan_limit").withStyle(ChatFormatting.YELLOW);
            case SAVED_DATA -> Component.translatable("tfg.machine.oxygen_distributor.status.restoring").withStyle(ChatFormatting.GREEN);
            case NULL -> Component.translatable("tfg.machine.oxygen_distributor.status.scanning").withStyle(ChatFormatting.GRAY);
        };

        if (pendingChunkLoad != null) {
            statusText = Component.translatable("tfg.machine.oxygen_distributor.status.chunk_unloaded").withStyle(ChatFormatting.YELLOW);
        }

        textList.add(Component.translatable("tfg.machine.oxygen_distributor.status").append(statusText));

        if (scan.isSealed() && scan.interiorSize() > 0) {
            textList.add(Component.translatable("tfg.machine.oxygen_distributor.size",
                    FormattingUtil.formatNumbers(scan.interiorSize())).withStyle(ChatFormatting.AQUA));
        }

        if (isWorking()) {
            String consumptionText = getFluidConsumptionDisplay();
            if (consumptionText != null) {
                textList.add(Component.translatable("tfg.machine.oxygen_distributor.consumption", consumptionText)
                        .withStyle(elevated ? ChatFormatting.RED : ChatFormatting.AQUA));
            }
            textList.add(Component.translatable("tfg.machine.oxygen_distributor.energy",
                    String.format("%,.0f", computeEnergyCostPerTick()))
                    .withStyle(elevated ? ChatFormatting.RED : ChatFormatting.AQUA));
        } else if (host.getRecipeLogic() != null && host.getRecipeLogic().isIdle()
                && !host.getRecipeLogic().getFailureReasons().isEmpty()) {
            for (Component reason : host.getRecipeLogic().getFailureReasons()) {
                textList.add(reason.copy().withStyle(ChatFormatting.RED));
            }
        } else {
            textList.add(Component.translatable("tfg.machine.oxygen_distributor.idle").withStyle(ChatFormatting.GRAY));
        }
    }

    @Nullable
    private String getFluidConsumptionDisplay() {
        if (!(host.getRecipeLogic() instanceof EnvironmentRecipeLogic envLogic))
            return null;
        double costPerTick = envLogic.getFluidCostPerTick();
        if (costPerTick <= 0)
            return null;

        double mbPerMinute = costPerTick * 1200;
        String formatString = mbPerMinute >= 10 ? "%.0f mB/min" : "%.2f mB/min";
        return String.format(formatString, mbPerMinute);
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
        AsyncBlockReader reader = new AsyncBlockReader(traceLevel);

        EnvironmentSystem.EXECUTOR.submit(() -> {
            try {
                RoomScan result = DiagnosticFloodFill.fill(reader, tracePos, TRACE_MAX_BLOCKS, MAX_HORIZONTAL_DIMENSION);
                if (result.escapePath() != null && !result.escapePath().isEmpty()) {
                    DiagnosticFloodFill.spawnTrace(traceLevel, result.escapePath());
                }
            } catch (Exception e) {
                TFGCore.LOGGER.error("Breach trace failed at {}", tracePos, e);
            }
        });
    }

    private void fillSpaceSuit(net.minecraft.world.entity.player.Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof SpaceSuitItem))
            return;

        // Find first import fluid handler
        var handlers = host.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
        NotifiableFluidTank tank = null;
        for (var h : handlers) {
            if (h instanceof NotifiableFluidTank t) {
                tank = t;
                break;
            }
        }
        if (tank == null)
            return;

        FluidStack inTank = tank.getFluidInTank(0);
        if (inTank.isEmpty())
            return;

        var holder = new ItemStackHolder(chest);
        FluidContainer suitContainer = FluidContainer.of(holder);
        if (suitContainer == null)
            return;

        FluidHolder fluidToInsert = FluidHolder.ofMillibuckets(inTank.getFluid(), FluidConstants.toMillibuckets(inTank.getAmount()));
        long inserted = suitContainer.insertFluid(fluidToInsert, true);
        if (inserted <= 0)
            return;

        long insertMb = FluidConstants.toMillibuckets(inserted);
        FluidStack toDrain = new FluidStack(inTank.getFluid(), (int) insertMb);
        FluidStack drained = tank.drainInternal(toDrain, FluidAction.EXECUTE);
        if (!drained.isEmpty()) {
            FluidHolder actualInsert = FluidHolder.ofMillibuckets(drained.getFluid(), drained.getAmount());
            suitContainer.insertFluid(actualInsert, false);
        }
    }

    //////////////////////////////////////
    // ****** Revalidation logic ****** //
    //////////////////////////////////////

    @Override
    public void validateAsync(AsyncBlockReader reader) {
        TFGCore.LOGGER.info("[validation] validateAsync START, pos={}", getPos());
        long start = System.nanoTime();
        newRoomScan = FloodFill.fill(reader, getPos(), SCAN_MAX_BLOCKS, MAX_HORIZONTAL_DIMENSION);
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        TFGCore.LOGGER.info("[validation] validateAsync DONE, pos={}, elapsedMs={}, status={}, size={}",
                getPos(), elapsed, newRoomScan.status(), newRoomScan.interiorSize());
    }

    @Override
    public ServerLevel getServerLevel() {
        return level;
    }

    @Override
    public void processValidationResult() {
        TFGCore.LOGGER.info("[validation] processValidationResult, pos={}, provider={}, newRoomScan={}",
                getPos(), provider != null, newRoomScan != null);
        if (provider == null || newRoomScan == null)
            return;

        long start = System.nanoTime();
        RoomScan oldScan = provider.getRoomScan();
        RoomScan newScan = newRoomScan;
        newRoomScan = null;

        if (pendingChunkLoad != null) {
            manager.chunkLoadListeners.removeSingle(this, pendingChunkLoad);
            pendingChunkLoad = null;
        }

        if (newScan.status() == Status.ESCAPED_UNLOADED && newScan.escapePoint() != null) {
            if (level.isLoaded(newScan.escapePoint())) {
                requestValidation();
            } else {
                pendingChunkLoad = new ChunkPos(newScan.escapePoint());
                manager.chunkLoadListeners.addSingle(this, pendingChunkLoad);
            }
            return;
        }

        // Compute chunk diff for block change listeners
        Set<ChunkPos> oldListenerChunks = oldScan.status() == Status.SAVED_DATA ? Set.of() : oldScan.touchedChunks();
        Set<ChunkPos> newChunks = newScan.touchedChunks();

        Set<ChunkPos> toRemove = new HashSet<>(oldListenerChunks);
        toRemove.removeAll(newChunks);

        Set<ChunkPos> toAdd = new HashSet<>(newChunks);
        toAdd.removeAll(oldListenerChunks);

        manager.blockChangeListeners.update(this, toRemove, toAdd);
        TFGCore.LOGGER.info("Registered in {} new chunks for block change listening", toAdd.size());

        manager.updateProvider(provider, oldScan, newScan);

        host.setShowTraceButton(!newScan.isSealed() && newScan.status() != Status.NULL && newScan.hasEscapePoint());

        // Decompression: sealed && working -> breached
        if (isWorking()
                && oldScan.isSealed()
                && newScan.status() == Status.ESCAPED_BUILD_HEIGHT
                && manager.getPressure(getPos()) < DimensionEnvironment.DECOMPRESSION_THRESHOLD) {
            BlockPos breachPoint = findBreachPoint(oldScan, newScan);
            if (breachPoint != null) {
                activeDecompression = manager.startDecompression(breachPoint, oldScan);
            }
        }

        // Breach shifted: still unsealed, breach point moved
        if (activeDecompression != null && !oldScan.isSealed() && !newScan.isSealed()) {
            BlockPos newBreachPoint = findBreachPoint(activeDecompression.getOldRoomScan(), newScan);
            if (newBreachPoint != null && !newBreachPoint.equals(activeDecompression.getBreachPoint())) {
                activeDecompression.shiftBreachPoint(level, newBreachPoint);
            }
        }

        // Room fixed: escaped -> sealed
        if (!oldScan.isSealed() && newScan.isSealed() && activeDecompression != null) {
            activeDecompression.cancel(level);
            activeDecompression = null;
        }

        if (host.getRecipeLogic() != null) {
            host.getRecipeLogic().onRecipeFinish();
        }

        long elapsed = (System.nanoTime() - start) / 1_000_000;
        TFGCore.LOGGER.info("[validation] processValidationResult DONE, pos={}, elapsedMs={}, oldStatus={}, newStatus={}",
                getPos(), elapsed, oldScan.status(), newScan.status());
    }

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

    private static BlockPos snapToAirlockCenter(BlockPos pos, BlockState state) {
        SlidingDoorPartProperty part = state.getValue(SlidingDoorBlock.PART);
        net.minecraft.core.Direction clockwise = state.getValue(SlidingDoorBlock.FACING).getClockWise();
        return pos.relative(clockwise, part.xOffset()).above(1 - part.yOffset());
    }

    private int getCooldownTicks() {
        if (provider == null)
            return 2;
        int size = provider.getRoomScan().interiorSize();
        return size < 100_000 ? 2 : 10;
    }

    @Override
    public void onBlockChangeAt(BlockPos pos) {
        if (provider == null)
            return;
        if (!dirty) {
            if (pos.equals(getPos()))
                return;
            RoomScan roomScan = provider.getRoomScan();
            TFGCore.LOGGER.info("Sealed {}, inEnvelope {}, inInterior {}", roomScan.isSealed(), roomScan.containsEnvelope(pos), roomScan.containsInterior(pos));
            if ((roomScan.isSealed() && roomScan.containsEnvelope(pos)) || roomScan.containsInterior(pos)) {
                requestValidation();
            } else {
                TFGCore.LOGGER.info("Ignored");
            }
        }
    }

    @Override
    public void onGridSpatialEvent(BlockPos min, BlockPos max) {
        if (provider == null)
            return;
        if (provider.getRoomScan().bounds().intersects(new AABB(min, max))) {
            requestValidation();
        }
    }

    @Override
    public void onChunkLoad(ChunkPos chunkPos) {
        requestValidation();
    }

    private void requestValidation() {
        setDirty(true);

        long now = level.getServer().getTickCount();
        long earliestTick = Math.max(lastValidationTick + getCooldownTicks(), now);

        TFGCore.LOGGER.info("[validation] requestValidation, pos={}, earliestTick={}", getPos(), earliestTick);
        EnvironmentSystem.requestValidation(this, earliestTick);

        if (provider != null && provider.getRoomScan().status() == Status.ESCAPED_UNLOADED) {
            BlockPos escapePoint = provider.getRoomScan().escapePoint();
            if (escapePoint != null) {
                manager.chunkLoadListeners.remove(this, Set.of(new ChunkPos(escapePoint)));
            }
        }
    }

    @Override
    public void setLastValidationTick(long tick) {
        this.lastValidationTick = tick;
    }

    @Override
    public void requestRevalidation() {
        requestValidation();
    }

    //////////////////////////////////////
    // ****** Machine Lifecycle ******* //
    //////////////////////////////////////

    public void onLoad(ServerLevel serverLevel) {
        TFGCore.LOGGER.info("onLoad, pos={}", getPos());
        level = serverLevel;
        manager = EnvironmentSystem.getManager(level);
        provider = manager.getOrCreateProvider(getPos());
        provider.attach(this);
        requestValidation();
    }

    public void onUnload() {
        TFGCore.LOGGER.info("onUnload, pos={}, provider={}", getPos(), provider != null);
        if (provider == null)
            return;
        provider.detach();
        deregisterListeners();
        provider = null;
    }

    public void onRemoved() {
        TFGCore.LOGGER.info("onRemoved, pos={}, provider={}", getPos(), provider != null);
        if (manager == null)
            return;
        deregisterListeners();
        manager.removeProvider(getPos());
        provider = null;
    }

    private void deregisterListeners() {
        EnvironmentSystem.cancelValidation(this);
        if (provider == null)
            return;
        manager.blockChangeListeners.remove(this, provider.getRoomScan().touchedChunks());
        if (pendingChunkLoad != null) {
            manager.chunkLoadListeners.removeSingle(this, pendingChunkLoad);
            pendingChunkLoad = null;
        }
        if (activeDecompression != null) {
            activeDecompression.cancel(level);
            activeDecompression = null;
        }
    }

    // IEnvironmentMachine / IBlockSensitiveMachine
    @Override
    public BlockPos getPos() {
        return host.self().getPos();
    }

    @Override
    public net.minecraft.world.level.Level getLevel() {
        return level;
    }

    @Override
    public boolean isWorking() {
        return host.getRecipeLogic() != null && host.getRecipeLogic().isWorking();
    }
}
