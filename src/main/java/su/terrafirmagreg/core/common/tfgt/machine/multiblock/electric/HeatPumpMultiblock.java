package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.awt.*;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.layout.Flow;

import su.terrafirmagreg.core.common.tfgt.machine.electric.HeatPumpMachine;
import su.terrafirmagreg.core.common.tfgt.machine.electric.IHeatPumpHost;
import su.terrafirmagreg.core.common.tfgt.machine.trait.EnvironmentRecipeLogic;

/**
 * GT multiblock wrapper for HeatPumpMachine.
 */
public class HeatPumpMultiblock extends WorkableElectricMultiblockMachine implements IHeatPumpHost {

    private final HeatPumpMachine machine;

    @SaveField
    private boolean showTraceButton;

    public HeatPumpMultiblock(BlockEntityCreationInfo info) {
        super(info, new EnvironmentRecipeLogic());
        recipeLogic.setRegressWhenWaiting(false);
        this.machine = new HeatPumpMachine(this);
    }

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
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        super.buildMainUI(mainWidget, guiData, syncManager, settings);

        var col = Flow.col().coverChildren();

        addStatusText(col, syncManager);

        col.child(new ButtonWidget<>()
                .overlay(Text.str("💨"))
                .size(18, 18)
                .onUpdateListener(w -> w.setEnabled(showTraceButton))
                .onMousePressed((ctx, i) -> {
                    if (!isRemote()) {
                        machine.requestFrontBreachTrace();
                        return true;
                    }
                    return false;
                })
                .tooltip(tooltip -> tooltip.addLine(Component.translatable("tfg.machine.oxygen_distributor.find_leak"))));

        mainWidget.child(col);

    }

    private void addStatusText(ParentWidget<?> widget, PanelSyncManager syncManager) {
        if (machine.isBlocked()) {
            widget.child(Text.lang("tfg.machine.heat_pump.status.blocked")
                    .withStyle(ChatFormatting.RED).asWidget());
            return;
        }

        if (isWorking()) {
            widget.child(Text.lang("tfg.machine.oxygen_distributor.active").withStyle(ChatFormatting.GREEN).asWidget());
        } else if (getEnergyInputPerSec() < machine.computeEnergyCostPerTick()) {
            widget.child(Text.lang("tfg.machine.oxygen_distributor.status.no_energy")
                    .withStyle(ChatFormatting.RED).asWidget());
        } else if (recipeLogic.isIdle() && recipeLogic.getBestFailureRecipe() != null) {
            widget.child(Text.of(recipeLogic.getBestFailureReason().copy().withStyle(ChatFormatting.RED)).asWidget());
        } else {
            widget.child(Text.lang("tfg.machine.oxygen_distributor.idle").withStyle(ChatFormatting.GRAY).asWidget());
        }

        switch (machine.getFrontScan().status()) {
            case SEALED, SAVED_DATA -> widget.child(Text.lang("tfg.machine.oxygen_distributor.status.sealed")
                    .withStyle(ChatFormatting.AQUA).asWidget());
            case ESCAPED_DIMENSION, ESCAPED_BUILD_HEIGHT, ESCAPED_UNLOADED -> widget
                    .child(Text.lang("tfg.machine.oxygen_distributor.status.breached").withStyle(ChatFormatting.YELLOW).asWidget());
            case BLOCK_LIMIT -> widget.child(Text.lang("tfg.machine.oxygen_distributor.status.scan_limit")
                    .withStyle(ChatFormatting.YELLOW).asWidget());
            case NULL -> widget.child(Text.lang("tfg.machine.oxygen_distributor.status.scanning")
                    .withStyle(ChatFormatting.YELLOW).asWidget());
        }

        widget.child(Text.lang("tfg.machine.oxygen_distributor.size",
                FormattingUtil.formatNumbers(machine.getFrontInteriorSize())).withStyle(ChatFormatting.AQUA).asWidget());
        widget.child(Text.lang("tfg.machine.oxygen_distributor.energy",
                String.format("%,.0f", machine.computeEnergyCostPerTick())).withStyle(ChatFormatting.AQUA).asWidget());
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
        if (getLevel() instanceof ServerLevel serverLevel && DEFAULT_STRUCTURE.equals(substructureName)) {
            machine.onLoad(serverLevel);
        }
    }

    @Override
    public void invalidateStructure(String name) {
        if (DEFAULT_STRUCTURE.equals(name))
            machine.onRemoved();
    }

    @Override
    public void onMachineDestroyed() {
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
        showTraceButton = show;
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
            return ModifierFunction.builder().eutMultiplier(Math.max(0, energy / baseEUt)).build();
        }
        return ModifierFunction.NULL;
    }
}
