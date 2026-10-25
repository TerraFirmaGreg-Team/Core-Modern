package su.terrafirmagreg.core.common.blockentity.appliances;

import net.dries007.tfc.common.capabilities.FluidTankCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.FluidHandlerBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.terrafirmagreg.core.config.TFGConfig;

public class CondenserBlockEntity extends FluidHandlerBlockEntity implements FluidTankCallback {
    public static final int SLOT_FLUID_CONTAINER_IN = 2;
    public static final int CAPACITY = TFGConfig.SERVER.gasBurnerCapacity.get();

    public CondenserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void fluidTankChanged() {
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.put("tank", tank.writeToNBT(new CompoundTag()));
        super.saveAdditional(nbt);
    }
}
