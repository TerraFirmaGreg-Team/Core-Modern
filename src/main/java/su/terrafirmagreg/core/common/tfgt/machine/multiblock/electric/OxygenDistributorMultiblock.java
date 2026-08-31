package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.server.level.ServerLevel;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.machine.trait.EnvironmentRecipeLogic;
import su.terrafirmagreg.core.common.tfgt.machine.electric.IOxygenDistributorHost;
import su.terrafirmagreg.core.common.tfgt.machine.electric.OxygenDistributorMachine;

/**
 * GT multiblock wrapper for {@link OxygenDistributorMachine}.
 * Handles GT registration, lifecycle hooks, and LDLib field sync.
 * All machine logic lives in {@link OxygenDistributorMachine}.
 */
public class OxygenDistributorMultiblock extends WorkableElectricMultiblockMachine implements IOxygenDistributorHost, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OxygenDistributorMultiblock.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Persisted
    @DescSynced
    private boolean showTraceButton;

    private final OxygenDistributorMachine machine;

    public OxygenDistributorMultiblock(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.machine = new OxygenDistributorMachine(this);
    }

    //////////////////////////////////////
    // ********* GT Overrides **********//
    //////////////////////////////////////

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new EnvironmentRecipeLogic(this);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        machine.beforeWorking(recipe);
        return super.beforeWorking(recipe);
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public @NotNull Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, OxygenDistributorMachine.UI_WIDTH, OxygenDistributorMachine.UI_HEIGHT);
        machine.addSharedWidgets(group);
        return group;
    }

    //////////////////////////////////////
    // ******* GT Lifecycle ************//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        TFGCore.LOGGER.info("[oxy-multiblock] onLoad, pos={}", getPos());
    }

    @Override
    public void onUnload() {
        super.onUnload();
        TFGCore.LOGGER.info("[oxy-multiblock] onUnload, pos={}", getPos());
        machine.onUnload();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        TFGCore.LOGGER.info("[oxy-multiblock] onStructureFormed, pos={}", getPos());
        if (getLevel() instanceof ServerLevel serverLevel) {
            machine.onLoad(serverLevel);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        TFGCore.LOGGER.info("[oxy-multiblock] onStructureInvalid, pos={}", getPos());
        machine.onRemoved();
    }

    @Override
    public void onMachineRemoved() {
        TFGCore.LOGGER.info("[oxy-multiblock] onMachineRemoved, pos={}", getPos());
        machine.onRemoved();
    }

    //////////////////////////////////////
    // **** IOxygenDistributorHost *****//
    //////////////////////////////////////

    @Override
    public boolean showTraceButton() {
        return showTraceButton;
    }

    @Override
    public void setShowTraceButton(boolean show) {
        showTraceButton = show;
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
