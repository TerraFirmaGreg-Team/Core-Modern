package su.terrafirmagreg.core.common.tfgt.machine.electric;

import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.machine.trait.EnvironmentRecipeLogic;
import su.terrafirmagreg.core.common.environment.*;

/**
 * Space Heater machine that creates a bubble of safe temperature around itself.
 */
public class SpaceHeaterMachine extends SimpleTieredMachine implements IEnvironmentMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SpaceHeaterMachine.class, SimpleTieredMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private final int maxRadius;

    @Nullable
    private TemperatureProvider provider;

    private DimEnvManager manager;

    @Getter
    @Persisted
    @DescSynced
    private int radius;

    public SpaceHeaterMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, GTMachineUtils.defaultTankSizeFunction);
        this.maxRadius = getMaxRadiusForTier(tier);
        this.radius = maxRadius;
    }

    private static int getMaxRadiusForTier(int tier) {
        return (int) Math.pow(2, tier + 2); // LV=8, MV=16, HV=32, EV=64
    }

    // ==================== Recipe ====================

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new EnvironmentRecipeLogic(this);
    }

    public boolean isWorking() {
        return recipeLogic != null && recipeLogic.isWorking();
    }

    /**
     * Recipe modifier: scales EU/t cost by radius.
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof SpaceHeaterMachine heater)) {
            return RecipeModifier.nullWrongType(SpaceHeaterMachine.class, machine);
        }

        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(Math.max(1, heater.getRadius())))
                .build();
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    // ==================== Radius ====================

    public void setRadius(int newRadius) {
        newRadius = Math.max(1, Math.min(newRadius, maxRadius));
        if (newRadius == this.radius)
            return;
        this.radius = newRadius;
        markDirty();

        if (!isRemote() && manager != null) {
            provider = manager.updateTempProvider(getPos(), newRadius);
            provider.attach(this);
        }

        // Radius change affects EU/t, so re-search the recipe with new modifier
        if (recipeLogic != null) {
            recipeLogic.markLastRecipeDirty();
        }
    }

    // ==================== UI ====================

    @Override
    public Widget createUIWidget() {
        int width = 164;
        int height = 78;
        var group = new WidgetGroup(0, 0, width, height);

        // Energy bar
        var editableUI = createEnergyBar();
        var energyBar = editableUI.createDefault();
        energyBar.setSelfPosition(new Position(3, 4));
        energyBar.setSize(energyBar.getSize().width, height);
        group.addWidget(energyBar);
        editableUI.setupUI(group, this);

        // Battery Slot
        var batterySlot = createBatterySlot().createDefault();
        batterySlot.setSelfPosition(new Position(width - 4 - 20, height - 14));
        group.addWidget(batterySlot);
        createBatterySlot().setupUI(group, this);

        int contentX = energyBar.getSize().width + 8;

        // Status text panel
        group.addWidget(new ComponentPanelWidget(contentX, 4, this::addStatusText)
                .setMaxWidthLimit(width - contentX - 4));

        // Radius input label + widget
        group.addWidget(new LabelWidget(contentX + 16 + 24, height - 26,
                Component.translatable("tfg.machine.space_heater.radius_label").getString()));
        var radiusInput = new IntInputWidget(contentX + 16, height - 16, 80, 20,
                this::getRadius, this::setRadius);
        radiusInput.setMin(1);
        radiusInput.setMax(maxRadius);
        group.addWidget(radiusInput);

        return group;
    }

    private void addStatusText(List<Component> textList) {
        // Working state
        if (isWorking()) {
            textList.add(Component.translatable("tfg.machine.space_heater.active").withStyle(ChatFormatting.GREEN));
        } else if (recipeLogic != null && recipeLogic.isIdle() && !recipeLogic.getFailureReasons().isEmpty()) {
            for (Component reason : recipeLogic.getFailureReasons()) {
                textList.add(reason.copy().withStyle(ChatFormatting.RED));
            }
        } else {
            textList.add(Component.translatable("tfg.machine.space_heater.idle").withStyle(ChatFormatting.GRAY));
        }

        // Max radius info
        textList.add(Component.translatable("tfg.machine.space_heater.max_radius",
                FormattingUtil.formatNumbers(maxRadius)).withStyle(ChatFormatting.AQUA));
    }

    // ==================== Lifecycle ====================

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(getLevel() instanceof ServerLevel serverLevel))
            return;

        TFGCore.LOGGER.info("SpaceHeater onLoad, pos={}", getPos());

        manager = EnvironmentSystem.getManager(serverLevel);
        provider = manager.getOrCreateTempProvider(getPos(), radius);
        provider.attach(this);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        TFGCore.LOGGER.info("SpaceHeater onUnload, pos={}", getPos());
        if (provider != null) {
            provider.detach();
            provider = null;
        }
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        TFGCore.LOGGER.info("SpaceHeater onMachineRemoved, pos={}", getPos());
        if (manager != null) {
            manager.removeTempProvider(getPos());
            provider = null;
        }
    }

}
