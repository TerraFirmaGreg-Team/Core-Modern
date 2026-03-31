package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.server.level.ServerLevel;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import su.terrafirmagreg.core.common.data.tfgt.machine.trait.EnvironmentRecipeLogic;

/**
 * GT singleblock wrapper for {@link OxygenDistributorMachine}.
 * Handles GT registration, lifecycle hooks, and LDLib field sync.
 * All machine logic lives in {@link OxygenDistributorMachine}.
 */
public class OxygenDistributorSingleblock extends SimpleTieredMachine implements IOxygenDistributorHost {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OxygenDistributorSingleblock.class, SimpleTieredMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    /** Synced to client for UI button visibility */
    @Persisted
    @DescSynced
    private boolean showTraceButton;

    private final OxygenDistributorMachine machine;

    public OxygenDistributorSingleblock(IMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction,
            int maxVolume) {
        super(holder, tier, tankScalingFunction);
        this.machine = new OxygenDistributorMachine(this, maxVolume);
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
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, OxygenDistributorMachine.UI_WIDTH, OxygenDistributorMachine.UI_HEIGHT);
        machine.addSharedWidgets(group);

        // Fluid tank
        var fluidTank = new TankWidget(importFluids.getStorages()[0],
                OxygenDistributorMachine.UI_RIGHT_COL_X + 1, OxygenDistributorMachine.UI_HEIGHT - 19, true, true);
        fluidTank.setFillDirection(ProgressTexture.FillDirection.UP_TO_DOWN);
        fluidTank.setBackground(GuiTextures.FLUID_SLOT);
        group.addWidget(fluidTank);

        // Battery slot
        var batterySlot = createBatterySlot().createDefault();
        batterySlot.setSelfPosition(new Position(OxygenDistributorMachine.UI_WIDTH / 2 - 9, OxygenDistributorMachine.UI_HEIGHT - 19));
        group.addWidget(batterySlot);
        createBatterySlot().setupUI(group, this);

        return group;
    }

    //////////////////////////////////////
    // ******* GT Lifecycle ************//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            machine.onLoad(serverLevel);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        machine.onUnload();
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
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
}
