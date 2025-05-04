package su.terrafirmagreg.core.common.data.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class GTGreenhousePortBlockEntity extends BlockEntity implements IFluidHandler {
    private final FluidTank tank = new FluidTank(4000); // 4 ведра

    public GTGreenhousePortBlockEntity(BlockPos pos, BlockState state) {
        super(TFGBlockEntities.GT_GREENHOUSE_PORT.get(), pos, state);
    }

    @Override
    public int getTanks() {
        return tank.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return this.tank.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.tank.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.tank.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return tank.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return tank.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return tank.drain(maxDrain, action);
    }

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> this);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        fluidHandler.invalidate();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GTGreenhousePortBlockEntity self) {
        if (level.isClientSide) return;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity neighbor = level.getBlockEntity(neighborPos);

            if (neighbor instanceof GTGreenhousePortBlockEntity other) {
                FluidStack toSend = self.tank.drain(1000, FluidAction.SIMULATE);
                if (!toSend.isEmpty()) {
                    int filled = other.tank.fill(toSend, FluidAction.EXECUTE);
                    if (filled > 0) {
                        self.tank.drain(filled, FluidAction.EXECUTE);
                        self.setChanged();
                        other.setChanged();
                    }
                }
            } else if (neighbor != null) {
                neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).ifPresent(handler -> {
                    FluidStack toSend = self.tank.drain(1000, FluidAction.SIMULATE);
                    if (!toSend.isEmpty()) {
                        int filled = handler.fill(toSend, FluidAction.EXECUTE);
                        if (filled > 0) {
                            self.tank.drain(filled, FluidAction.EXECUTE);
                            self.setChanged();
                            neighbor.setChanged();
                        }
                    }
                });
            }
        }
    }
}