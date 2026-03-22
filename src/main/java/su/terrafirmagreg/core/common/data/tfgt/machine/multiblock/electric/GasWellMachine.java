package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import net.minecraft.MethodsReturnNonnullByDefault;

import su.terrafirmagreg.core.common.data.tfgt.machine.trait.GasWellRecipeLogic;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasWellMachine extends MultiblockControllerMachine {

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
}
