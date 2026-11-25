package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import static su.terrafirmagreg.core.common.tfgt.ui.TFGUITextures.BUTTON_FIND_LEAK;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.InteractionSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import earth.terrarium.adastra.common.registry.ModBlocks;

import su.terrafirmagreg.core.common.environment.TemperatureProvider;
import su.terrafirmagreg.core.common.tfgt.machine.electric.HeatPumpMachine;
import su.terrafirmagreg.core.common.tfgt.machine.electric.IHeatPumpHost;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.part.RotorVentPartMachine;
import su.terrafirmagreg.core.common.tfgt.machine.trait.EnvironmentRecipeLogic;

/**
 * GT multiblock wrapper for HeatPumpMachine.
 */
public class HeatPumpMultiblock extends WorkableElectricMultiblockMachine implements IHeatPumpHost {

    private final HeatPumpMachine machine;

    @SaveField
    @SyncToClient
    private boolean showTraceButton;

    @SaveField
    @SyncToClient
    private float targetTemperature = TemperatureProvider.DEFAULT_TARGET_TEMPERATURE;

    public HeatPumpMultiblock(BlockEntityCreationInfo info) {
        super(info, new EnvironmentRecipeLogic());
        recipeLogic.setRegressWhenWaiting(false);
        this.machine = new HeatPumpMachine(this);
    }

    @Nullable
    private BlockPos frontVentPos = null;
    @Nullable
    private BlockPos backVentPos = null;

    //////////////////////////////////////
    // ********* GT Overrides **********//
    //////////////////////////////////////

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        machine.beforeWorking(recipe);
        return super.beforeWorking(recipe);
    }

    public boolean isWorking() {
        return machine.isWorking();
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        InteractionSyncHandler traceHandler = new InteractionSyncHandler();
        traceHandler.setOnMousePressed(mouseData -> {
            if (!mouseData.isClient())
                machine.requestFrontBreachTrace();
        });

        BooleanSyncValue showTraceSync = new BooleanSyncValue(this::showTraceButton);
        syncManager.syncValue("showTraceButton", showTraceSync);

        return super.getPanelBuilder(data, syncManager, settings)
                .rightConfigurators(flow -> flow.child(new ButtonWidget<>()
                        .overlay(BUTTON_FIND_LEAK)
                        .size(18, 18)
                        .syncHandler(traceHandler)
                        .setEnabledIf(w -> showTraceSync.getBoolValue())
                        .tooltip(tooltip -> tooltip.addLine(
                                Component.translatable("tfg.machine.oxygen_distributor.find_leak")))));
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);

        GenericSyncValue<Component> statusSync = componentSync(this::computeStatus);
        GenericSyncValue<Component> scanSync = componentSync(this::computeScanStatus);
        GenericSyncValue<Component> sizeSync = componentSync(() -> Component.translatable(
                "tfg.machine.oxygen_distributor.size",
                FormattingUtil.formatNumbers(machine.getFrontInteriorSize())).withStyle(ChatFormatting.AQUA));
        GenericSyncValue<Component> energySync = componentSync(() -> Component.translatable(
                "tfg.machine.oxygen_distributor.energy",
                String.format("%,.0f", machine.computeEnergyCostPerTick())).withStyle(ChatFormatting.AQUA));
        syncManager.syncValue("heatPumpStatus", statusSync);
        syncManager.syncValue("heatPumpScan", scanSync);
        syncManager.syncValue("heatPumpSize", sizeSync);
        syncManager.syncValue("heatPumpEnergy", energySync);

        if (!isFormed())
            return widgets;

        widgets.add(dynamicLine(statusSync));
        widgets.add(dynamicLine(scanSync));
        widgets.add(dynamicLine(sizeSync));
        widgets.add(dynamicLine(energySync));

        widgets.add(Flow.row()
                .coverChildren()
                .childPadding(4)
                .child(Text.lang("tfg.machine.heat_pump.temperature").asWidget())
                .child(new TextFieldWidget()
                        .size(62, 14)
                        .setNumbersDouble(v -> Math.max(TemperatureProvider.MIN_TARGET_TEMPERATURE,
                                Math.min(v, TemperatureProvider.MAX_TARGET_TEMPERATURE)))
                        .setDefaultNumber(TemperatureProvider.DEFAULT_TARGET_TEMPERATURE)
                        .value(SyncHandlers.doubleNumber(() -> targetTemperature, v -> setTargetTemperature((float) v))
                                .allowC2S())
                        .tooltip(t -> t.addLine(Component.translatable("tfg.machine.heat_pump.temperature.tooltip")))));

        return widgets;
    }

    private static GenericSyncValue<Component> componentSync(Supplier<Component> getter) {
        return GenericSyncValue.builder(Component.class)
                .adapter(ByteBufAdapters.COMPONENT)
                .getter(getter)
                .build();
    }

    private static IWidget dynamicLine(GenericSyncValue<Component> sync) {
        return Text.dynamic(() -> {
            Component value = sync.getValue();
            return value == null ? Component.empty() : value;
        }).asWidget();
    }

    private Component computeStatus() {
        if (machine.isBlocked())
            return Component.translatable("tfg.machine.heat_pump.status.blocked").withStyle(ChatFormatting.RED);

        Component ventProblem = getVentProblem();
        if (isWorking())
            return Component.translatable("tfg.machine.oxygen_distributor.active").withStyle(ChatFormatting.GREEN);
        if (ventProblem != null)
            return ventProblem.copy().withStyle(ChatFormatting.RED);
        if (getEnergyInputPerSec() < machine.computeEnergyCostPerTick())
            return Component.translatable("tfg.machine.oxygen_distributor.status.no_energy").withStyle(ChatFormatting.RED);
        if (recipeLogic.isIdle() && recipeLogic.getBestFailureReason() != null)
            return recipeLogic.getBestFailureReason().copy().withStyle(ChatFormatting.RED);
        return Component.translatable("tfg.machine.oxygen_distributor.idle").withStyle(ChatFormatting.GRAY);
    }

    private Component computeScanStatus() {
        return switch (machine.getFrontScan().status()) {
            case SEALED, SAVED_DATA -> Component.translatable("tfg.machine.oxygen_distributor.status.sealed")
                    .withStyle(ChatFormatting.AQUA);
            case ESCAPED_DIMENSION, ESCAPED_BUILD_HEIGHT, ESCAPED_UNLOADED -> Component
                    .translatable("tfg.machine.oxygen_distributor.status.breached").withStyle(ChatFormatting.YELLOW);
            case BLOCK_LIMIT -> Component.translatable("tfg.machine.oxygen_distributor.status.scan_limit")
                    .withStyle(ChatFormatting.YELLOW);
            case NULL -> Component.translatable("tfg.machine.oxygen_distributor.status.scanning")
                    .withStyle(ChatFormatting.YELLOW);
            default -> Component.empty();
        };
    }

    //////////////////////////////////////
    // ******* GT Lifecycle ************//
    //////////////////////////////////////

    @Override
    public void onUnload() {
        super.onUnload();
        machine.onUnload();
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (!DEFAULT_STRUCTURE.equals(substructureName))
            return;

        findVents(substructureName);
        if (getLevel() instanceof ServerLevel serverLevel) {
            machine.onLoad(serverLevel);
        }
    }

    /**
     * GTm8 don't seems to do TFGPredicates.markedBlocks
     * only Ad Astra vent is adjacent to the controller so getting it that way</li>
     */
    private void findVents(String substructureName) {
        this.frontVentPos = null;
        this.backVentPos = null;

        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof RotorVentPartMachine) {
                this.backVentPos = part.getBlockPos().immutable();
                break;
            }
        }

        BlockPos controllerPos = getBlockPos();
        var cache = getPatternState(substructureName).getCache();
        for (var entry : cache.long2ObjectEntrySet()) {
            BlockPos pos = BlockPos.of(entry.getLongKey());
            if (pos.distManhattan(controllerPos) == 1 && entry.getValue().getBlockState().is(ModBlocks.VENT.get())) {
                this.frontVentPos = pos;
                break;
            }
        }
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        if (DEFAULT_STRUCTURE.equals(name)) {
            machine.onRemoved();
            this.frontVentPos = null;
            this.backVentPos = null;
        }
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        machine.onRemoved();
    }

    //////////////////////////////////////
    // **** IHeatPumpHost **************//
    //////////////////////////////////////

    @Override
    public long getEnergyInputPerSec() {
        return Math.max(0, getEnergyContainer().getInputPerSec());
    }

    @Override
    public boolean showTraceButton() {
        return showTraceButton;
    }

    @Override
    public void setShowTraceButton(boolean show) {
        if (showTraceButton != show) {
            showTraceButton = show;
            getSyncDataHolder().markClientSyncFieldDirty("showTraceButton");
        }
    }

    @Override
    public @Nullable BlockPos getFrontScanStart() {
        return frontVentPos != null ? frontVentPos.relative(getFrontFacing()) : null;
    }

    @Override
    public @Nullable BlockPos getBackScanStart() {
        return backVentPos != null ? backVentPos.relative(getFrontFacing().getOpposite()) : null;
    }

    @Override
    public float getTargetTemperature() {
        return targetTemperature;
    }

    @Override
    public void setTargetTemperature(float temperature) {
        targetTemperature = temperature;
        if (!isRemote()) {
            getSyncDataHolder().markClientSyncFieldDirty("targetTemperature");
            machine.applyTargetTemperature(targetTemperature);
        }
    }

    /** Scales energy consumption based on the front region size. */
    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (machine instanceof HeatPumpMultiblock pump) {
            if (pump.machine.isBlocked() || !pump.machine.isFrontSealed()) {
                return ModifierFunction.NULL;
            }
            double energy = pump.machine.computeEnergyCostPerTick();
            double baseEUt = recipe.getInputEUt().getTotalEU();
            if (baseEUt <= 0) {
                return ModifierFunction.NULL;
            }
            return ModifierFunction.builder()
                    .eutMultiplier(Math.max(0, energy / baseEUt))
                    .inputModifier(ContentModifier.multiplier(pump.getConsumptionMultiplier()))
                    .build();
        }
        return ModifierFunction.NULL;
    }
}
