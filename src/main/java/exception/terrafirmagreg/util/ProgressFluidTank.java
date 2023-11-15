package exception.terrafirmagreg.util;

import exception.terrafirmagreg.objects.entity.SapCollectorBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class ProgressFluidTank extends FluidTank {
    private final SapCollectorBlockEntity blockEntity;

    public ProgressFluidTank(int capacity, SapCollectorBlockEntity blockEntity) {
        super(capacity);
        this.blockEntity = blockEntity;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        FluidStack drained = super.drain(maxDrain, action);
        if (this.blockEntity.currentRecipe != null) {
            int drainedProgress = Math.max(1, (int) (this.blockEntity.currentRecipe.processingTime / 1000f * drained.getAmount()));
            if (this.blockEntity.progress > drainedProgress) {
                if (action.equals(IFluidHandler.FluidAction.EXECUTE)) {
                    this.blockEntity.progress -= drainedProgress;
                    if (this.blockEntity.getLevel() != null) {
                        this.blockEntity.getLevel()
                                .sendBlockUpdated(this.blockEntity.getBlockPos(), this.blockEntity.getBlockState(), this.blockEntity.getBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            } else {
                drained.setAmount(0);
            }
        }
        return drained;
    }
}