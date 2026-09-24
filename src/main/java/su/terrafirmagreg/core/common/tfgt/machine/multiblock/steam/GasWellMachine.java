package su.terrafirmagreg.core.common.tfgt.machine.multiblock.steam;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import lombok.Getter;

import su.terrafirmagreg.core.common.tfgt.machine.trait.GasWellRecipeLogic;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasWellMachine extends MultiblockControllerMachine {

    @Nullable
    private NotifiableFluidTank inputFluidTank;
    @Nullable
    private NotifiableFluidTank outputFluidTank;

    @Getter
    private final GasWellRecipeLogic logic;
    private TickableSubscription tickSubscription;

    public GasWellMachine(BlockEntityCreationInfo info) {
        super(info);
        this.logic = new GasWellRecipeLogic(this);
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        inputFluidTank = null;
        outputFluidTank = null;
        setActive(false);

        for (MultiblockPartMachine part : getParts()) {
            for (var handlerList : part.getRecipeHandlers()) {
                var fluidCap = handlerList.getCapability(FluidRecipeCapability.CAP);
                var itemCap = handlerList.getCapability(ItemRecipeCapability.CAP);

                if (!fluidCap.isEmpty()) {
                    if (handlerList.getHandlerIO().support(IO.IN) && inputFluidTank == null) {
                        inputFluidTank = (NotifiableFluidTank) fluidCap.get(0);
                    } else if (handlerList.getHandlerIO().support(IO.OUT) && outputFluidTank == null) {
                        outputFluidTank = (NotifiableFluidTank) fluidCap.get(0);
                    }
                }
            }
        }

        tickSubscription = subscribeServerTick(logic::tick);
    }

    @Override
    public void invalidateStructure(@NotNull String substructureName) {
        super.invalidateStructure(substructureName);
        resetState();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        resetState();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        logic.resetFull();
        unsubscribe(tickSubscription);
        tickSubscription = null;
        inputFluidTank = null;
        outputFluidTank = null;
    }

    private void resetState() {
        unsubscribe(tickSubscription);
        tickSubscription = null;
        logic.reset();
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
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);

        if (!isFormed())
            return widgets;

        if (logic.isActive()) {
            widgets.add(Text.lang("tfg.machine.gas_well.active")
                    .withStyle(ChatFormatting.GREEN).asWidget());
        } else {
            widgets.add(Text.lang("tfg.machine.gas_well.waiting_explosive").withStyle(ChatFormatting.RED).asWidget()
                    .tooltip(t -> t.addLine(Text.lang("tfg.machine.gas_well.waiting_explosive.tooltip")
                            .withStyle(ChatFormatting.GRAY))));
        }

        if (logic.isActive() && logic.isOutputBlocked()) {
            widgets.add(Text.lang("tfg.machine.gas_well.output_full").withStyle(ChatFormatting.RED).asWidget()
                    .tooltip(t -> t.addLine(Text.lang("tfg.machine.gas_well.output_full.tooltip")
                            .withStyle(ChatFormatting.GRAY))));

            Component waterInfo = Component.literal(GasWellRecipeLogic.FLUID_CONSUMPTION_PER_TICK + " mB/t")
                    .withStyle(ChatFormatting.BLUE);
            Component steamInfo = Component.literal(GasWellRecipeLogic.FLUID_CONSUMPTION_PER_TICK * 2 + " mB/t")
                    .withStyle(ChatFormatting.BLUE);

            widgets.add(Text.lang("tfg.machine.gas_well.fluid_consumption", waterInfo, steamInfo)
                    .withStyle(ChatFormatting.GRAY).asWidget());

            if (logic.isActive()) {
                int remaining = (GasWellRecipeLogic.EXPLOSIVE_CONSUMPTION_INTERVAL * 20) - logic.getTimer();
                Component timeInfo = Component.literal(remaining / 20 + "s")
                        .withStyle(ChatFormatting.AQUA);
                widgets.add(Text.lang("tfg.machine.gas_well.next_explosive", timeInfo)
                        .withStyle(ChatFormatting.GRAY).asWidget());
            }
        }

        if (getLevel() instanceof ServerLevel serverLevel) {
            int chunkX = SectionPos.blockToSectionCoord(getBlockPos().getX());
            int chunkZ = SectionPos.blockToSectionCoord(getBlockPos().getZ());
            var savedData = logic.getSavedData(serverLevel);
            var entry = savedData.getFluidVeinWorldEntry(chunkX, chunkZ);

            if (entry != null && entry.getDefinition() != null) {
                var veinFluid = entry.getDefinition().getStoredFluid().get();
                if (veinFluid != null) {
                    var naturalGas = GTMaterials.NaturalGas.getFluid();
                    boolean isPumpable = naturalGas != null && veinFluid.isSame(naturalGas);

                    Component fluidTooltip = Text.lang("tfg.machine.gas_well.fluid.tooltip")
                            .withStyle(ChatFormatting.GRAY);
                    Component fluidInfo = veinFluid.getFluidType().getDescription().copy()
                            .withStyle(isPumpable ? ChatFormatting.GREEN : ChatFormatting.RED);
                    widgets.add(Text.lang("gtceu.multiblock.fluid_rig.drilled_fluid", fluidInfo).asWidget()
                            .tooltip(t -> t.addLine(fluidTooltip)));

                    int produced = Math.max(
                            entry.getDefinition().getDepletedYield(),
                            entry.getFluidYield() * entry.getOperationsRemaining() /
                                    BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS);
                    Component amountInfo = Component.literal(FormattingUtil.formatNumbers(produced) + " mB/s")
                            .withStyle(ChatFormatting.BLUE);
                    widgets.add(Text.lang("gtceu.multiblock.fluid_rig.fluid_amount", amountInfo)
                            .withStyle(ChatFormatting.GRAY).asWidget());
                }

                int remainingOps = entry.getOperationsRemaining();
                int maxOps = BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS;
                int percent = remainingOps * 100 / maxOps;
                Component veinInfo = Component.literal(percent + "%")
                        .withStyle(percent > 50 ? ChatFormatting.GREEN
                                : percent > 20 ? ChatFormatting.YELLOW : ChatFormatting.RED);
                widgets.add(Text.lang("tfg.machine.gas_well.vein_remaining", veinInfo)
                        .withStyle(ChatFormatting.GRAY).asWidget());
            } else {
                Component noFluid = Component.translatable("gtceu.multiblock.fluid_rig.no_fluid_in_area")
                        .withStyle(ChatFormatting.RED);
                widgets.add(Text.lang("gtceu.multiblock.fluid_rig.drilled_fluid", noFluid)
                        .withStyle(ChatFormatting.GRAY).asWidget());
            }
        }

        return widgets;

    }

}
