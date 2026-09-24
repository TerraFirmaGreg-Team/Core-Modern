package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;

import su.terrafirmagreg.core.common.environment.*;
import su.terrafirmagreg.core.common.tfgt.machine.trait.EnvironmentRecipeLogic;

/**
 * Higgs Emitter multiblock that creates a sphere of normal gravity around itself based on the provided hatch.
 */
public class HiggsEmitterMultiblock extends WorkableElectricMultiblockMachine implements IEnvironmentMachine {

    @Nullable
    private GravityProvider provider;

    private @Nullable DimEnvManager manager;

    public HiggsEmitterMultiblock(BlockEntityCreationInfo info) {
        super(info, new EnvironmentRecipeLogic());
        recipeLogic.setRegressWhenWaiting(false);
    }

    public int computeEnergyPerTick() {
        EnergyContainerList energy = getEnergyContainer();
        long voltage = energy != null ? energy.getHighestInputVoltage() : 0;
        return (int) Math.round(voltage * 0.5);
    }

    /** If a higgs emitter with an HV hatch were to be placed in the middle of a sphere that is being supplied by an air distributor that takes 0.5A@HV it will provide a gravity sphere of the exact same size as the sphere. */
    public static int radiusForEUt(double eut) {
        if (eut <= 0) {
            return 4;
        }
        double volume = EnclosedRoomEnergyCurve.volumeForEut(eut);
        return Math.max(4, (int) Math.ceil(Math.cbrt(3.0 * volume / (4.0 * Math.PI))));
    }

    /** @return the current bubble radius, derived from the energy the hatch supports. */
    public int getRadius() {
        return radiusForEUt(computeEnergyPerTick());
    }

    // ==================== Recipe ====================

    @Override
    public boolean isWorking() {
        return recipeLogic.isWorking();
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof HiggsEmitterMultiblock emitter)) {
            return RecipeModifier.nullWrongType(HiggsEmitterMultiblock.class, machine);
        }
        double energy = emitter.computeEnergyPerTick();
        double baseEUt = recipe.getInputEUt().getTotalEU();
        if (baseEUt <= 0) {
            return ModifierFunction.NULL;
        }
        return ModifierFunction.builder().eutMultiplier(Math.max(0, energy / baseEUt)).build();
    }

    // ==================== UI ====================

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();

        widgets.add(GTMultiblockTextUtil.addWorkingStatusLine(this, syncManager, () -> Component.translatable("tfg.machine.oxygen_distributor.active").withStyle(ChatFormatting.GREEN)));

        widgets.add(Text.lang("tfg.machine.higgs_emitter.radius",
                FormattingUtil.formatNumbers(getRadius())).withStyle(ChatFormatting.AQUA).asWidget());
        widgets.add(Text.lang("tfg.machine.oxygen_distributor.energy",
                String.format("%,d", computeEnergyPerTick()))
                .withStyle(ChatFormatting.AQUA).asWidget());

        return widgets;
    }

    private long getEnergyInputPerSec() {
        EnergyContainerList energy = getEnergyContainer();
        return energy != null ? Math.max(0, energy.getInputPerSec()) : 0;
    }

    // ==================== Lifecycle ====================

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (!(getLevel() instanceof ServerLevel serverLevel))
            return;

        manager = EnvironmentSystem.getManager(serverLevel);
        int radius = getRadius();
        if (manager.getGravityProviders().containsKey(getPos())) {
            provider = manager.updateGravityProvider(getPos(), radius);
        } else {
            provider = manager.getOrCreateGravityProvider(getPos(), radius);
        }
        provider.attach(this);
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        if (provider != null) {
            provider.detach();
            provider = null;
        }
    }

    @Override
    public void invalidateStructure(String name) {
        if (DEFAULT_STRUCTURE.equals(name)) {
            removeProvider();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        detachProvider();
    }

    @Override
    public void onMachineDestroyed() {
        removeProvider();
    }

    private void detachProvider() {
        if (provider != null) {
            provider.detach();
            provider = null;
        }
    }

    private void removeProvider() {
        if (manager != null) {
            manager.removeGravityProvider(getPos());
        }
        provider = null;
    }

    // ==================== IEnvironmentMachine ====================

    @Override
    public BlockPos getPos() {
        return super.getBlockPos();
    }

    @Override
    public Level getLevel() {
        return super.getLevel();
    }
}
