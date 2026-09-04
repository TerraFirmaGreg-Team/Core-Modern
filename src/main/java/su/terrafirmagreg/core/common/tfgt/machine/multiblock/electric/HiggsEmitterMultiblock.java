package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.common.data.tfgt.machine.trait.EnvironmentRecipeLogic;
import su.terrafirmagreg.core.common.environment.*;

/**
 * Higgs Emitter multiblock that creates a sphere of normal gravity around itself based on the provided hatch.
 */
public class HiggsEmitterMultiblock extends WorkableElectricMultiblockMachine implements IEnvironmentMachine, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HiggsEmitterMultiblock.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Nullable
    private GravityProvider provider;

    private DimEnvManager manager;

    public HiggsEmitterMultiblock(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
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
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new EnvironmentRecipeLogic(this);
    }

    @Override
    public boolean isWorking() {
        return recipeLogic != null && recipeLogic.isWorking();
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
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
    public Widget createUIWidget() {
        int width = 180;
        int height = 78;
        var group = new WidgetGroup(0, 0, width, height);
        group.setBackground(ResourceBorderTexture.BORDERED_BACKGROUND);
        group.addWidget(new ComponentPanelWidget(4, 4, this::addStatusText)
                .setMaxWidthLimit(width - 8));
        return group;
    }

    private void addStatusText(List<Component> textList) {
        if (isWorking()) {
            textList.add(Component.translatable("tfg.machine.higgs_emitter.active").withStyle(ChatFormatting.GREEN));
        } else if (getEnergyInputPerSec() < computeEnergyPerTick()) {
            textList.add(Component.translatable("tfg.machine.higgs_emitter.status.no_energy").withStyle(ChatFormatting.RED));
        } else if (recipeLogic != null && recipeLogic.isIdle() && !recipeLogic.getFailureReasons().isEmpty()) {
            for (Component reason : recipeLogic.getFailureReasons()) {
                textList.add(reason.copy().withStyle(ChatFormatting.RED));
            }
        } else {
            textList.add(Component.translatable("tfg.machine.higgs_emitter.idle").withStyle(ChatFormatting.GRAY));
        }

        textList.add(Component.translatable("tfg.machine.higgs_emitter.radius",
                FormattingUtil.formatNumbers(getRadius())).withStyle(ChatFormatting.AQUA));
        textList.add(Component.translatable("tfg.machine.higgs_emitter.energy",
                String.format("%,d", computeEnergyPerTick()))
                .withStyle(ChatFormatting.AQUA));
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
    public void onStructureFormed() {
        super.onStructureFormed();
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
    public void onStructureInvalid() {
        super.onStructureInvalid();
        detachProvider();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        detachProvider();
    }

    @Override
    public void onMachineRemoved() {
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
            provider = null;
        }
    }

    // ==================== IEnvironmentMachine ====================

    @Override
    public BlockPos getPos() {
        return super.getPos();
    }

    @Override
    public Level getLevel() {
        return super.getLevel();
    }
}
