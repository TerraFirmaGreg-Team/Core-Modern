package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.List;

import javax.annotation.Nullable;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.server.level.ServerLevel;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

import su.terrafirmagreg.core.common.tfgt.machine.electric.IOxygenDistributorHost;
import su.terrafirmagreg.core.common.tfgt.machine.electric.OxygenDistributorMachine;
import su.terrafirmagreg.core.common.tfgt.machine.trait.EnvironmentRecipeLogic;

/**
 * GT multiblock wrapper for {@link OxygenDistributorMachine}.
 * Handles GT registration, lifecycle hooks.
 * All machine logic lives in {@link OxygenDistributorMachine}.
 */
public class OxygenDistributorMultiblock extends WorkableElectricMultiblockMachine implements IOxygenDistributorHost {

    @SaveField
    @SyncToClient
    private boolean showTraceButton;

    private final OxygenDistributorMachine machine;

    public OxygenDistributorMultiblock(BlockEntityCreationInfo info) {
        super(info, new EnvironmentRecipeLogic());
        recipeLogic.setRegressWhenWaiting(false);
        this.machine = new OxygenDistributorMachine(this);
    }

    //////////////////////////////////////
    // ********* GT Overrides **********//
    //////////////////////////////////////

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        machine.beforeWorking(recipe);
        return super.beforeWorking(recipe);
    }

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return super.getPanelBuilder(data, syncManager, settings)
                .rightConfigurators(flow -> flow
                        .child(machine.createFillSuitButton(data.getPlayer()))
                        .child(machine.createTraceButton(syncManager)));
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);
        widgets.addAll(machine.createStatusWidgets(syncManager));
        return widgets;
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
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        if (substructureName.equals(DEFAULT_STRUCTURE) && getLevel() instanceof ServerLevel sLvl) {
            machine.onLoad(sLvl);
        }
    }

    @Override
    public void invalidateStructure(String substructureName) {
        super.invalidateStructure(substructureName);
        if (substructureName.equals(DEFAULT_STRUCTURE))
            machine.onRemoved();
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        machine.onRemoved();
    }

    //////////////////////////////////////
    // **** IOxygenDistributorHost *****//
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

    /** Scales energy consumption based on the room's volume. */
    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (machine instanceof OxygenDistributorMultiblock distributor) {
            double energy = distributor.machine.computeEnergyCostPerTick();
            double baseEUt = recipe.getInputEUt().getTotalEU();
            if (baseEUt <= 0) {
                return ModifierFunction.NULL;
            }
            return ModifierFunction.builder().eutMultiplier(Math.max(0, energy / baseEUt)).build();
        }
        return ModifierFunction.NULL;
    }
}
