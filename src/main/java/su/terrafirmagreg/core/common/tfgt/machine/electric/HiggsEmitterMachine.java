package su.terrafirmagreg.core.common.tfgt.machine.electric;

import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.machine.trait.EnvironmentRecipeLogic;
import su.terrafirmagreg.core.common.environment.*;

/**
 * Higgs Emitter machine that creates a fixed-radius bubble of normal gravity around itself.
 */
public class HiggsEmitterMachine extends SimpleTieredMachine implements IEnvironmentMachine {

    /** Fixed bubble radius. */
    public static final int RADIUS = 64;

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HiggsEmitterMachine.class, SimpleTieredMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private final int radius;

    @Nullable
    private GravityProvider provider;

    private DimEnvManager manager;

    public HiggsEmitterMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, GTMachineUtils.defaultTankSizeFunction);
        this.radius = RADIUS;
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new EnvironmentRecipeLogic(this);
    }

    public boolean isWorking() {
        return recipeLogic != null && recipeLogic.isWorking();
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof HiggsEmitterMachine emitter)) {
            return RecipeModifier.nullWrongType(HiggsEmitterMachine.class, machine);
        }

        return ModifierFunction.builder()
                .eutMultiplier(emitter.radius)
                .build();
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
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

        return group;
    }

    private void addStatusText(List<Component> textList) {
        // Working state
        if (isWorking()) {
            textList.add(Component.translatable("tfg.machine.higgs_emitter.active").withStyle(ChatFormatting.GREEN));
        } else if (recipeLogic != null && recipeLogic.isIdle() && !recipeLogic.getFailureReasons().isEmpty()) {
            for (Component reason : recipeLogic.getFailureReasons()) {
                textList.add(reason.copy().withStyle(ChatFormatting.RED));
            }
        } else {
            textList.add(Component.translatable("tfg.machine.higgs_emitter.idle").withStyle(ChatFormatting.GRAY));
        }

        // Max radius info
        textList.add(Component.translatable("tfg.machine.higgs_emitter.max_radius",
                FormattingUtil.formatNumbers(radius)).withStyle(ChatFormatting.AQUA));
    }

    // ==================== Lifecycle ====================

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(getLevel() instanceof ServerLevel serverLevel))
            return;

        TFGCore.LOGGER.info("HiggsEmitter onLoad, pos={}", getPos());

        manager = EnvironmentSystem.getManager(serverLevel);
        provider = manager.getOrCreateGravityProvider(getPos(), radius);
        provider.attach(this);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        TFGCore.LOGGER.info("HiggsEmitter onUnload, pos={}", getPos());
        if (provider != null) {
            provider.detach();
            provider = null;
        }
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        TFGCore.LOGGER.info("HiggsEmitter onMachineRemoved, pos={}", getPos());
        if (manager != null) {
            manager.removeGravityProvider(getPos());
            provider = null;
        }
    }

}
