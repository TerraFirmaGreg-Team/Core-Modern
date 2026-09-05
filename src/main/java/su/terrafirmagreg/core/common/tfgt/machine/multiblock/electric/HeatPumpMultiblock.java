package su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric;

import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.machine.trait.EnvironmentRecipeLogic;
import su.terrafirmagreg.core.common.tfgt.machine.electric.HeatPumpMachine;
import su.terrafirmagreg.core.common.tfgt.machine.electric.IHeatPumpHost;

/**
 * GT multiblock wrapper for HeatPumpMachine.
 */
public class HeatPumpMultiblock extends WorkableElectricMultiblockMachine implements IHeatPumpHost, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HeatPumpMultiblock.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private final HeatPumpMachine machine;

    @Persisted
    @DescSynced
    private boolean showTraceButton;

    public HeatPumpMultiblock(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.machine = new HeatPumpMachine(this);
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

    public boolean isWorking() {
        return machine.isWorking();
    }

    @Override
    public @NotNull Widget createUIWidget() {
        int width = 180;
        int height = 90;
        var group = new WidgetGroup(0, 0, width, height);
        group.setBackground(ResourceBorderTexture.BORDERED_BACKGROUND);

        group.addWidget(new ComponentPanelWidget(4, 4, this::addStatusText)
                .setMaxWidthLimit(width - 8));

        var traceButton = new ButtonWidget(41 - 23, height - 19, 18, 18,
                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("💨")),
                cd -> {
                    if (!cd.isRemote) {
                        machine.requestFrontBreachTrace();
                    }
                }) {
            @Override
            public void updateScreen() {
                super.updateScreen();
                setVisible(showTraceButton());
            }
        };
        traceButton.setHoverTooltips(Component.translatable("tfg.machine.oxygen_distributor.find_leak"));
        group.addWidget(traceButton);

        return group;
    }

    private void addStatusText(List<Component> textList) {
        if (machine.isBlocked()) {
            textList.add(Component.translatable("tfg.machine.heat_pump.status.blocked")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        if (isWorking()) {
            textList.add(Component.translatable("tfg.machine.oxygen_distributor.active").withStyle(ChatFormatting.GREEN));
        } else if (getEnergyInputPerSec() < machine.computeEnergyCostPerTick()) {
            textList.add(Component.translatable("tfg.machine.oxygen_distributor.status.no_energy")
                    .withStyle(ChatFormatting.RED));
        } else if (recipeLogic != null && recipeLogic.isIdle() && !recipeLogic.getFailureReasons().isEmpty()) {
            for (Component reason : recipeLogic.getFailureReasons()) {
                textList.add(reason.copy().withStyle(ChatFormatting.RED));
            }
        } else {
            textList.add(Component.translatable("tfg.machine.oxygen_distributor.idle").withStyle(ChatFormatting.GRAY));
        }

        switch (machine.getFrontScan().status()) {
            case SEALED, SAVED_DATA -> textList.add(Component.translatable("tfg.machine.oxygen_distributor.status.sealed")
                    .withStyle(ChatFormatting.AQUA));
            case ESCAPED_DIMENSION, ESCAPED_BUILD_HEIGHT, ESCAPED_UNLOADED -> textList
                    .add(Component.translatable("tfg.machine.oxygen_distributor.status.breached").withStyle(ChatFormatting.YELLOW));
            case BLOCK_LIMIT -> textList.add(Component.translatable("tfg.machine.oxygen_distributor.status.scan_limit")
                    .withStyle(ChatFormatting.YELLOW));
            case NULL -> textList.add(Component.translatable("tfg.machine.oxygen_distributor.status.scanning")
                    .withStyle(ChatFormatting.YELLOW));
        }

        textList.add(Component.translatable("tfg.machine.oxygen_distributor.size",
                FormattingUtil.formatNumbers(machine.getFrontInteriorSize())).withStyle(ChatFormatting.AQUA));
        textList.add(Component.translatable("tfg.machine.oxygen_distributor.energy",
                String.format("%,.0f", machine.computeEnergyCostPerTick())).withStyle(ChatFormatting.AQUA));
    }

    //////////////////////////////////////
    // ******* GT Lifecycle ************//
    //////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        TFGCore.LOGGER.debug("[heatpump-multi] onLoad, pos={}", getPos());
    }

    @Override
    public void onUnload() {
        super.onUnload();
        TFGCore.LOGGER.debug("[heatpump-multi] onUnload, pos={}", getPos());
        machine.onUnload();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        TFGCore.LOGGER.debug("[heatpump-multi] onStructureFormed, pos={}", getPos());
        if (getLevel() instanceof ServerLevel serverLevel) {
            machine.onLoad(serverLevel);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        TFGCore.LOGGER.debug("[heatpump-multi] onStructureInvalid, pos={}", getPos());
        machine.onRemoved();
    }

    @Override
    public void onMachineRemoved() {
        TFGCore.LOGGER.debug("[heatpump-multi] onMachineRemoved, pos={}", getPos());
        machine.onRemoved();
    }

    //////////////////////////////////////
    // **** IHeatPumpHost **************//
    //////////////////////////////////////

    @Override
    public long getEnergyInputPerSec() {
        var energy = getEnergyContainer();
        return energy != null ? Math.max(0, energy.getInputPerSec()) : 0;
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
