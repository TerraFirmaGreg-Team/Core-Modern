package su.terrafirmagreg.core.common.tfgt.machine.multiblock.steam;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import lombok.Getter;

import su.terrafirmagreg.core.common.tfgt.machine.trait.GasWellTrait;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasWellMachine extends MultiblockControllerMachine implements IMuiMachine {

    @Nullable
    private NotifiableFluidTank inputFluidTank;
    @Nullable
    private NotifiableFluidTank outputFluidTank;

    @Getter
    @SaveField
    private final GasWellTrait logic;

    public GasWellMachine(BlockEntityCreationInfo info) {
        super(info);
        this.logic = attachTrait(new GasWellTrait());
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        inputFluidTank = null;
        outputFluidTank = null;

        for (MultiblockPartMachine part : getParts()) {
            for (var handlerList : part.getRecipeHandlers()) {
                var fluidCap = handlerList.getCapability(FluidRecipeCapability.CAP);
                if (fluidCap.isEmpty() || !(fluidCap.get(0) instanceof NotifiableFluidTank tank))
                    continue;

                if (handlerList.getHandlerIO().support(IO.IN) && inputFluidTank == null) {
                    inputFluidTank = tank;
                } else if (handlerList.getHandlerIO().support(IO.OUT) && outputFluidTank == null) {
                    outputFluidTank = tank;
                }
            }
        }

        setActive(logic.isActive());
        logic.start();
    }

    @Override
    public void invalidateStructure(@NotNull String substructureName) {
        super.invalidateStructure(substructureName);
        stopLogic();
        logic.reset();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        stopLogic();
    }

    private void stopLogic() {
        logic.stop();
        inputFluidTank = null;
        outputFluidTank = null;
        setActive(false);
    }

    @Nullable
    public NotifiableFluidTank getInputFluidTank() {
        return inputFluidTank;
    }

    @Nullable
    public NotifiableFluidTank getOutputFluidTank() {
        return outputFluidTank;
    }

    public void setActive(boolean active) {
        MachineRenderState renderState = getRenderState();
        if (renderState == null)
            return;
        if (renderState.hasProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS)) {
            setRenderState(renderState.setValue(
                    GTMachineModelProperties.RECIPE_LOGIC_STATUS,
                    active ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE));
        }
    }

    @Nullable
    public NotifiableItemStackHandler getInputItemHandler() {
        for (MultiblockPartMachine part : getParts()) {
            if (!(part instanceof ItemBusPartMachine bus))
                continue;
            var inventory = bus.getInventory();
            if (inventory.getHandlerIO() == IO.IN) {
                return inventory;
            }
        }
        return null;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
            UISettings settings) {
        mainWidget.child(getMainTextPanel(syncManager).margin(4, 2));
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);

        BooleanSyncValue activeSync = new BooleanSyncValue(logic::isActive);
        BooleanSyncValue outputBlockedSync = new BooleanSyncValue(logic::isOutputBlocked);
        IntSyncValue timerSync = new IntSyncValue(logic::getTimer);
        BooleanSyncValue hasVeinSync = new BooleanSyncValue(() -> logic.getVeinFluid() != null);
        GenericSyncValue<Component> veinFluidSync = GenericSyncValue.builder(Component.class)
                .adapter(ByteBufAdapters.COMPONENT)
                .getter(() -> {
                    Fluid fluid = logic.getVeinFluid();
                    if (fluid == null)
                        return Component.translatable("gtceu.multiblock.fluid_rig.no_fluid_in_area")
                                .withStyle(ChatFormatting.RED);
                    var naturalGas = GTMaterials.NaturalGas.getFluid();
                    boolean isPumpable = naturalGas != null && fluid.isSame(naturalGas);
                    return fluid.getFluidType().getDescription().copy()
                            .withStyle(isPumpable ? ChatFormatting.GREEN : ChatFormatting.RED);
                }).build();
        IntSyncValue producedSync = new IntSyncValue(logic::getFluidToProduce);
        IntSyncValue veinPercentSync = new IntSyncValue(logic::getVeinPercent);
        syncManager.syncValue("active", activeSync);
        syncManager.syncValue("outputBlocked", outputBlockedSync);
        syncManager.syncValue("timer", timerSync);
        syncManager.syncValue("hasVein", hasVeinSync);
        syncManager.syncValue("veinFluid", veinFluidSync);
        syncManager.syncValue("produced", producedSync);
        syncManager.syncValue("veinPercent", veinPercentSync);

        if (!isFormed())
            return widgets;

        widgets.add(Text.lang("tfg.machine.gas_well.active").withStyle(ChatFormatting.GREEN).asWidget()
                .setEnabledIf(w -> activeSync.getBoolValue()));

        widgets.add(Text.lang("tfg.machine.gas_well.waiting_explosive").withStyle(ChatFormatting.RED).asWidget()
                .tooltip(t -> t.addLine(Text.lang("tfg.machine.gas_well.waiting_explosive.tooltip")
                        .withStyle(ChatFormatting.GRAY)))
                .setEnabledIf(w -> !activeSync.getBoolValue()));

        widgets.add(Text.lang("tfg.machine.gas_well.output_full").withStyle(ChatFormatting.RED).asWidget()
                .tooltip(t -> t.addLine(Text.lang("tfg.machine.gas_well.output_full.tooltip")
                        .withStyle(ChatFormatting.GRAY)))
                .setEnabledIf(w -> activeSync.getBoolValue() && outputBlockedSync.getBoolValue()));

        Component waterInfo = Component.literal(GasWellTrait.FLUID_CONSUMPTION_PER_TICK + " mB/t")
                .withStyle(ChatFormatting.BLUE);
        Component steamInfo = Component.literal(GasWellTrait.FLUID_CONSUMPTION_PER_TICK * 2 + " mB/t")
                .withStyle(ChatFormatting.BLUE);
        widgets.add(Text.lang("tfg.machine.gas_well.fluid_consumption", waterInfo, steamInfo)
                .withStyle(ChatFormatting.GRAY).asWidget()
                .setEnabledIf(w -> activeSync.getBoolValue()));

        widgets.add(Text.dynamic(() -> {
            int remaining = GasWellTrait.EXPLOSIVE_CONSUMPTION_INTERVAL * 20 - timerSync.getIntValue();
            return Text.lang("tfg.machine.gas_well.next_explosive",
                    Component.literal(remaining / 20 + "s").withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GRAY);
        }).asWidget()
                .setEnabledIf(w -> activeSync.getBoolValue()));

        widgets.add(Text.dynamic(() -> Text.lang("gtceu.multiblock.fluid_rig.drilled_fluid",
                veinFluidSync.getValue())).asWidget()
                .tooltip(t -> t.addLine(Text.lang("tfg.machine.gas_well.fluid.tooltip")
                        .withStyle(ChatFormatting.GRAY))));

        widgets.add(Text.dynamic(() -> Text.lang("gtceu.multiblock.fluid_rig.fluid_amount",
                Component.literal(FormattingUtil.formatNumbers(producedSync.getIntValue()) + " mB/s")
                        .withStyle(ChatFormatting.BLUE))
                .withStyle(ChatFormatting.GRAY)).asWidget()
                .setEnabledIf(w -> hasVeinSync.getBoolValue()));

        widgets.add(Text.dynamic(() -> {
            int percent = veinPercentSync.getIntValue();
            return Text.lang("tfg.machine.gas_well.vein_remaining",
                    Component.literal(percent + "%").withStyle(percent > 50 ? ChatFormatting.GREEN
                            : percent > 20 ? ChatFormatting.YELLOW : ChatFormatting.RED))
                    .withStyle(ChatFormatting.GRAY);
        }).asWidget()
                .setEnabledIf(w -> hasVeinSync.getBoolValue()));

        return widgets;
    }
}
