package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;

import su.terrafirmagreg.core.common.data.tfgt.machine.trait.GasWellRecipeLogic;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasWellMachine extends MultiblockControllerMachine implements IDisplayUIMachine {

    @Nullable
    private NotifiableFluidTank inputFluidTank;
    @Nullable
    private NotifiableFluidTank outputFluidTank;
    @Nullable
    private NotifiableItemStackHandler inputItemHandler;

    private final GasWellRecipeLogic logic;
    private com.gregtechceu.gtceu.api.machine.TickableSubscription tickSubscription;

    public GasWellMachine(IMachineBlockEntity holder) {
        super(holder);
        this.logic = new GasWellRecipeLogic(this);
    }

    public GasWellRecipeLogic getLogic() {
        return logic;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        inputFluidTank = null;
        outputFluidTank = null;
        inputItemHandler = null;

        for (IMultiPart part : getParts()) {
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

                if (!itemCap.isEmpty() && handlerList.getHandlerIO().support(IO.IN)
                        && inputItemHandler == null) {
                    inputItemHandler = (NotifiableItemStackHandler) itemCap.get(0);
                }
            }
        }

        tickSubscription = subscribeServerTick(logic::tick);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
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
        resetState();
    }

    private void resetState() {
        unsubscribe(tickSubscription);
        logic.reset();
        inputFluidTank = null;
        outputFluidTank = null;
        inputItemHandler = null;
    }

    @Nullable
    public NotifiableFluidTank getInputFluidTank() {
        return inputFluidTank;
    }

    @Nullable
    public NotifiableFluidTank getOutputFluidTank() {
        return outputFluidTank;
    }

    @Nullable
    public NotifiableItemStackHandler getInputItemHandler() {
        return inputItemHandler;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        if (isFormed()) {
            if (logic.isActive()) {
                textList.add(Component.translatable("tfg.machine.gas_well.active")
                        .withStyle(ChatFormatting.GREEN));
            } else {
                Component tooltip = Component.translatable("tfg.machine.gas_well.waiting_explosive.tooltip")
                        .withStyle(ChatFormatting.GRAY);
                textList.add(Component.translatable("tfg.machine.gas_well.waiting_explosive")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
            }

            Component waterInfo = Component.literal(GasWellRecipeLogic.FLUID_CONSUMPTION_PER_TICK + " mB/t")
                    .withStyle(ChatFormatting.BLUE);
            Component steamInfo = Component.literal(GasWellRecipeLogic.FLUID_CONSUMPTION_PER_TICK * 160 + " mB/t")
                    .withStyle(ChatFormatting.BLUE);
            textList.add(Component.translatable("tfg.machine.gas_well.fluid_consumption", waterInfo, steamInfo)
                    .withStyle(ChatFormatting.GRAY));

            if (logic.isActive()) {
                int remaining = (GasWellRecipeLogic.EXPLOSIVE_CONSUMPTION_INTERVAL * 20) - logic.getTimer();
                Component timeInfo = Component.literal(remaining / 20 + "s")
                        .withStyle(ChatFormatting.AQUA);
                textList.add(Component.translatable("tfg.machine.gas_well.next_explosive", timeInfo)
                        .withStyle(ChatFormatting.GRAY));
            }

            if (getLevel() instanceof ServerLevel serverLevel) {
                int chunkX = SectionPos.blockToSectionCoord(getPos().getX());
                int chunkZ = SectionPos.blockToSectionCoord(getPos().getZ());
                var savedData = BedrockFluidVeinSavedData.getOrCreate(serverLevel);
                var entry = savedData.getFluidVeinWorldEntry(chunkX, chunkZ);

                if (entry != null && entry.getDefinition() != null) {
                    var veinFluid = entry.getDefinition().getStoredFluid().get();
                    if (veinFluid != null) {
                        Component fluidInfo = veinFluid.getFluidType().getDescription().copy()
                                .withStyle(ChatFormatting.GREEN);
                        textList.add(Component.translatable("gtceu.multiblock.fluid_rig.drilled_fluid", fluidInfo)
                                .withStyle(ChatFormatting.GRAY));

                        int produced = Math.max(
                                entry.getDefinition().getDepletedYield(),
                                entry.getFluidYield() * entry.getOperationsRemaining() /
                                        BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS);
                        Component amountInfo = Component.literal(FormattingUtil.formatNumbers(produced) + " mB/s")
                                .withStyle(ChatFormatting.BLUE);
                        textList.add(Component.translatable("gtceu.multiblock.fluid_rig.fluid_amount", amountInfo)
                                .withStyle(ChatFormatting.GRAY));
                    }

                    int remainingOps = entry.getOperationsRemaining();
                    int maxOps = BedrockFluidVeinSavedData.MAXIMUM_VEIN_OPERATIONS;
                    int percent = remainingOps * 100 / maxOps;
                    Component veinInfo = Component.literal(percent + "%")
                            .withStyle(percent > 50 ? ChatFormatting.GREEN : percent > 20 ? ChatFormatting.YELLOW : ChatFormatting.RED);
                    textList.add(Component.translatable("tfg.machine.gas_well.vein_remaining", veinInfo)
                            .withStyle(ChatFormatting.GRAY));
                } else {
                    Component noFluid = Component.translatable("gtceu.multiblock.fluid_rig.no_fluid_in_area")
                            .withStyle(ChatFormatting.RED);
                    textList.add(Component.translatable("gtceu.multiblock.fluid_rig.drilled_fluid", noFluid)
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }
}
