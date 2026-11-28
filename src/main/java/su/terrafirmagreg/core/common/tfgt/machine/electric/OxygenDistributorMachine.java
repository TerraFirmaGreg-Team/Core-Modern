package su.terrafirmagreg.core.common.tfgt.machine.electric;

import java.util.*;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.InteractionSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ProgressWidget;
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
import su.terrafirmagreg.core.common.environment.*;
import su.terrafirmagreg.core.common.environment.RoomScan.Status;
import su.terrafirmagreg.core.common.tfgt.machine.trait.EnvironmentRecipeLogic;
import su.terrafirmagreg.core.common.tfgt.ui.TFGUITextures;

/**
 * Oxygen Distributor machine logic. Maintains a sealed room with breathable atmosphere via flood fill.
 * Attach to a GT machine wrapper ({@link OxygenDistributorSingleblock} or a multiblock) via composition.
 * The GT wrapper handles registration and lifecycle hooks; this class handles everything else.
 */
public class OxygenDistributorMachine implements IBlockSensitiveMachine, IEnvironmentMachine.IAtmosphereMachine {

    /** GT machine wrapper hosting this logic */
    private final IOxygenDistributorHost host;

    /** The provider that holds our room data and handles oxygen queries */
    @Nullable
    private OxygenProvider provider;

    /** The provider that exposes this room's pressure to the dimension */
    @Nullable
    private PressureProvider pressureProvider;

    /** Atmosphere supplied by the current recipe */
    @Nullable
    private Fluid currentAtmosphere;

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

    public OxygenDistributorMachine(IOxygenDistributorHost host) {
        this.host = host;
    }

    /** @return the expected EU/t consumption based on room size. */
    public double computeEnergyCostPerTick() {
        int effectiveVolume = computeEffectiveVolume();
        return EnclosedRoomEnergyCurve.eutForVolume(effectiveVolume) * host.getEnergyCostMultiplier();
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
        currentAtmosphere = resolveAtmosphere(recipe);
    }

    private void updateFluidCost(@Nullable GTRecipe recipe) {
        if (!(host.getRecipeLogic() instanceof EnvironmentRecipeLogic envLogic))
            return;
        if (recipe == null) {
            envLogic.setFluidCostPerTick(0);
            return;
        }

        FluidIngredient ingredient = getFluidIngredient(recipe);
        int baseCost = ingredient == null ? 0 : ingredient.getAmount();
        // baseCost mB/min per 10k blocks -> mB/tick for actual volume and multiply by Rotor Efficiency
        envLogic.setFluidCostPerTick(baseCost * computeEffectiveVolume() / (60.0 * 20 * EnclosedRoomEnergyCurve.BASE_VOLUME) * host.getConsumptionMultiplier());
    }

    @Nullable
    private static FluidIngredient getFluidIngredient(GTRecipe recipe) {
        var fluidInputs = recipe.getTickInputContents(FluidRecipeCapability.CAP);
        if (fluidInputs.isEmpty())
            return null;
        return fluidInputs.get(0).content() instanceof FluidIngredient ingredient ? ingredient : null;
    }

    @Nullable
    private Fluid resolveAtmosphere(@Nullable GTRecipe recipe) {
        if (recipe == null)
            return null;

        FluidIngredient ingredient = getFluidIngredient(recipe);
        FluidStack stack = host.getAtmosphereInput();
        if (ingredient == null || stack == null || !ingredient.test(stack))
            return null;

        return stack.getFluid();
    }

    private int computeEffectiveVolume() {
        RoomScan scan = getRoomScan();
        if (scan.isSealed()) {
            return Math.max(1, scan.interiorSize());
        } else if (scan.status() == Status.NULL) {
            return 1;
        }
        return EnclosedRoomEnergyCurve.BASE_VOLUME;
    }

    //////////////////////////////////////
    // ************* GUI ************** //
    //////////////////////////////////////

    public List<IWidget> createStatusWidgets(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        if (!host.isFormed()) {
            widgets.add(Text.lang("tfg.machine.unformed").withStyle(ChatFormatting.GRAY).asWidget());
            return widgets;
        }

        GenericSyncValue<Component> status = componentSync(syncManager, "oxygenStatus", this::computeStatusLine);
        GenericSyncValue<Component> size = componentSync(syncManager, "oxygenSize", this::computeSizeLine);
        GenericSyncValue<Component> consumption = componentSync(syncManager, "oxygenConsumption", this::computeConsumptionLine);
        GenericSyncValue<Component> energy = componentSync(syncManager, "oxygenEnergy", this::computeEnergyLine);
        GenericSyncValue<Component> activity = componentSync(syncManager, "oxygenActivity", this::computeActivityLine);
        DoubleSyncValue progressSync = new DoubleSyncValue(host.getRecipeLogic()::getProgressPercent);
        syncManager.syncValue("oxygenProgress", progressSync);

        widgets.add(dynamicLine(status));
        widgets.add(dynamicLine(size));
        widgets.add(dynamicLine(consumption));
        widgets.add(dynamicLine(energy));
        widgets.add(dynamicLine(activity));
        widgets.add(new ProgressWidget()
                .widthRel(0.8f).height(20)
                .value(progressSync));
        return widgets;
    }

    public IWidget createTraceButton(PanelSyncManager syncManager) {
        InteractionSyncHandler traceHandler = new InteractionSyncHandler();
        traceHandler.setOnMousePressed(mouseData -> {
            if (!mouseData.isClient())
                requestBreachTrace();
        });
        BooleanSyncValue showTraceSync = new BooleanSyncValue(host::showTraceButton);
        syncManager.syncValue("oxygenShowTrace", showTraceSync);

        return new ButtonWidget<>()
                .size(18, 18)
                .overlay(TFGUITextures.BUTTON_FIND_LEAK)
                .syncHandler(traceHandler)
                .setEnabledIf(w -> showTraceSync.getBoolValue())
                .tooltip(tooltip -> tooltip.addLine(Component.translatable("tfg.machine.oxygen_distributor.find_leak")));
    }

    public IWidget createFillSuitButton(Player player) {
        InteractionSyncHandler fillHandler = new InteractionSyncHandler();
        fillHandler.setOnMousePressed(mouseData -> {
            if (!mouseData.isClient())
                fillSpaceSuit(player);
        });

        return new ButtonWidget<>()
                .size(18, 18)
                .overlay(TFGUITextures.BUTTON_FILL_SUIT)
                .syncHandler(fillHandler)
                .tooltip(tooltip -> tooltip.addLine(Component.translatable("tfg.machine.oxygen_distributor.fill_suit")));
    }

    private static GenericSyncValue<Component> componentSync(PanelSyncManager syncManager, String key,
            Supplier<Component> getter) {
        GenericSyncValue<Component> sync = GenericSyncValue.builder(Component.class)
                .adapter(ByteBufAdapters.COMPONENT)
                .getter(getter)
                .build();
        syncManager.syncValue(key, sync);
        return sync;
    }

    private static IWidget dynamicLine(GenericSyncValue<Component> sync) {
        return Text.dynamic(() -> {
            Component value = sync.getValue();
            return value == null ? Component.empty() : value;
        }).asWidget()
                .setEnabledIf(w -> {
                    Component value = sync.getValue();
                    return value != null && !value.getString().isEmpty();
                });
    }

    private Component computeStatusLine() {
        RoomScan scan = getRoomScan();
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

        return Component.translatable("tfg.machine.oxygen_distributor.status").append(statusText);
    }

    private Component computeSizeLine() {
        RoomScan scan = getRoomScan();
        if (!scan.isSealed() || scan.interiorSize() <= 0)
            return Component.empty();
        return Component.translatable("tfg.machine.oxygen_distributor.size",
                FormattingUtil.formatNumbers(scan.interiorSize())).withStyle(ChatFormatting.AQUA);
    }

    private Component computeConsumptionLine() {
        String consumptionText = getFluidConsumptionDisplay();
        if (consumptionText == null)
            return Component.empty();
        return Component.translatable("tfg.machine.oxygen_distributor.consumption", consumptionText)
                .withStyle(getRoomScan().isSealed() ? ChatFormatting.AQUA : ChatFormatting.RED);
    }

    private Component computeEnergyLine() {
        return Component.translatable("tfg.machine.oxygen_distributor.energy",
                String.format("%,.0f", computeEnergyCostPerTick()))
                .withStyle(getRoomScan().isSealed() ? ChatFormatting.AQUA : ChatFormatting.RED);
    }

    private Component computeActivityLine() {
        Component ventProblem = host.getVentProblem();
        if (isWorking())
            return Component.translatable("tfg.machine.oxygen_distributor.active").withStyle(ChatFormatting.GREEN);
        if (ventProblem != null)
            return ventProblem.copy().withStyle(ChatFormatting.RED);
        if (host.getEnergyInputPerSec() < computeEnergyCostPerTick())
            return Component.translatable("tfg.machine.oxygen_distributor.status.no_energy").withStyle(ChatFormatting.RED);
        if (host.getRecipeLogic().isIdle() && host.getRecipeLogic().getBestFailureReason() != null)
            return host.getRecipeLogic().getBestFailureReason().copy().withStyle(ChatFormatting.RED);
        return Component.translatable("tfg.machine.oxygen_distributor.idle").withStyle(ChatFormatting.GRAY);
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
                } else {
                    TFGCore.LOGGER.debug("[validation] breach trace found no escape, revalidating, pos={}", getPos());
                    traceLevel.getServer().execute(this::requestValidation);
                }
            } catch (Exception e) {
                TFGCore.LOGGER.error("Breach trace failed at {}", tracePos, e);
            }
        });
    }

    private void fillSpaceSuit(Player player) {
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
        TFGCore.LOGGER.debug("[validation] validateAsync START, pos={}", getPos());
        long start = System.nanoTime();
        newRoomScan = FloodFill.fill(reader, getPos(), SCAN_MAX_BLOCKS, MAX_HORIZONTAL_DIMENSION);
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        TFGCore.LOGGER.debug("[validation] validateAsync DONE, pos={}, elapsedMs={}, status={}, size={}",
                getPos(), elapsed, newRoomScan.status(), newRoomScan.interiorSize());
    }

    @Override
    public ServerLevel getServerLevel() {
        return level;
    }

    @Override
    public void processValidationResult() {
        TFGCore.LOGGER.debug("[validation] processValidationResult, pos={}, provider={}, newRoomScan={}",
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
        TFGCore.LOGGER.debug("Registered in {} new chunks for block change listening", toAdd.size());

        manager.updateProvider(provider, oldScan, newScan);
        if (pressureProvider != null)
            manager.updatePressureProvider(pressureProvider, pressureProvider.getRoomScan(), newScan);

        host.setShowTraceButton(!newScan.isSealed() && newScan.status() != Status.NULL && newScan.hasEscapePoint());

        float insidePressure = getPressure();
        float outsidePressure = manager.getDimensionPressure();
        float pressureDifferential = (insidePressure - outsidePressure) / Math.max(insidePressure, outsidePressure);
        if (isWorking()
                && oldScan.isSealed()
                && newScan.status() == Status.ESCAPED_BUILD_HEIGHT
                && Math.abs(pressureDifferential) >= 0.05f) {
            BlockPos breachPoint = findBreachPoint(oldScan, newScan);
            if (breachPoint != null) {
                activeDecompression = manager.startDecompression(breachPoint, oldScan, pressureDifferential < 0.0f);
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
        TFGCore.LOGGER.debug("[validation] processValidationResult DONE, pos={}, elapsedMs={}, oldStatus={}, newStatus={}",
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
            TFGCore.LOGGER.debug("Sealed {}, inEnvelope {}, inInterior {}", roomScan.isSealed(), roomScan.containsEnvelope(pos), roomScan.containsInterior(pos));
            if ((roomScan.isSealed() && roomScan.containsEnvelope(pos)) || roomScan.containsInterior(pos)) {
                requestValidation();
            } else {
                TFGCore.LOGGER.debug("Ignored");
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

        TFGCore.LOGGER.debug("[validation] requestValidation, pos={}, earliestTick={}", getPos(), earliestTick);
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
        TFGCore.LOGGER.debug("onLoad, pos={}", getPos());
        level = serverLevel;
        manager = EnvironmentSystem.getManager(level);
        provider = manager.getOrCreateProvider(getPos());
        provider.attach(this);
        pressureProvider = manager.getOrCreatePressureProvider(getPos());
        pressureProvider.attach(this);
        manager.updatePressureProvider(pressureProvider, pressureProvider.getRoomScan(), provider.getRoomScan());
        requestValidation();
    }

    public void onUnload() {
        TFGCore.LOGGER.debug("onUnload, pos={}, provider={}", getPos(), provider != null);
        if (provider == null && pressureProvider == null)
            return;
        if (provider != null)
            provider.detach();
        if (pressureProvider != null)
            pressureProvider.detach();
        deregisterListeners();
        provider = null;
        pressureProvider = null;
    }

    public void onRemoved() {
        TFGCore.LOGGER.debug("onRemoved, pos={}, provider={}", getPos(), provider != null);
        if (manager == null)
            return;
        deregisterListeners();
        manager.removeProvider(getPos());
        manager.removePressureProvider(getPos());
        provider = null;
        pressureProvider = null;
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
        return host.self().getBlockPos();
    }

    @Override
    public Level getEnvironmentLevel() {
        return level;
    }

    @Override
    public boolean isWorking() {
        return host.getRecipeLogic() != null && host.getRecipeLogic().isWorking();
    }

    @Override
    public float getPressure() {
        return DimensionEnvironment.EARTH_PRESSURE;
    }

    @Override
    @Nullable
    public Fluid getAtmosphere() {
        return isWorking() ? currentAtmosphere : null;
    }
}
