package su.terrafirmagreg.core.common.blockentity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.FluidTankCallback;
import net.dries007.tfc.common.capabilities.InventoryFluidTank;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.SimpleFluid;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.IntArrayBuilder;
import net.dries007.tfc.util.calendar.ICalendarTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.GasBurnerBlock;
import su.terrafirmagreg.core.common.container.GasBurnerBlockContainer;

public class GasBurnerBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements ICalendarTickable, FluidTankCallback, MenuProvider {
    public static final int SLOT_FLUID_CONTAINER_IN = 0;
    public static final int SLOT_FLUID_CONTAINER_OUT = 1;
    public static final int SLOTS = 2;
    public static final int CAPACITY = 4000;
    public static final int BURN_TICKS_PER_CYCLE = 100;
    public static final int MB_PER_CYCLE = 3;

    private static final Component NAME = Component.translatable(TFGCore.MOD_ID + ".block_entity.gas_burner");

    public static void serverTick(Level level, BlockPos pos, BlockState state, GasBurnerBlockEntity burner) {
        burner.checkForLastTickSync();
        burner.checkForCalendarUpdate();

        if (level.getGameTime() % 5 == 0) {
            burner.updateFluidIOSlots();
        }

        boolean isRaining = level.isRainingAt(pos.above()) || level.isRainingAt(pos.above(2));
        if (state.getValue(GasBurnerBlock.HEAT) > 0) {
            if (isRaining && level.random.nextFloat() < 0.15F) {
                Helpers.playSound(level, pos, TFCSounds.ITEM_COOL.get());
            }
            int heatLevel = Mth.clamp((int) (burner.temperature / Heat.maxVisibleTemperature() * 6) + 1, 1, 7);
            if (heatLevel != state.getValue(GasBurnerBlock.HEAT)) {
                level.setBlockAndUpdate(pos, state.setValue(GasBurnerBlock.HEAT, heatLevel));
                burner.markForSync();
            }

            if (burner.burnTicks > 0) {
                burner.burnTicks--;
            }
            if (burner.burnTicks <= 0) {
                if (!burner.consumeFuel()) {
                    burner.extinguish(state);
                }
            }
        } else if (burner.burnTemperature > 0) {
            burner.extinguish(state);
        }

        if (burner.temperature > 0 || burner.burnTemperature > 0) {
            float target = HeatCapability.targetDeviceTemp(burner.burnTemperature, 0, isRaining);
            burner.temperature = HeatCapability.adjustTempTowards(burner.temperature, target, 10.0f, 1.0f);

            HeatCapability.provideHeatTo(level, pos.above(), burner.temperature);
            burner.markForSync();
        }
    }

    protected final InventoryFluidTank tank;
    private final LazyOptional<IFluidHandler> fluidCapability;

    @Getter
    protected final ContainerData syncableData;
    @Getter
    private float temperature;
    private int burnTicks;
    private float burnTemperature;
    private long lastPlayerTick;

    public GasBurnerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, defaultInventory(SLOTS), NAME);

        tank = new InventoryFluidTank(CAPACITY, stack -> !stack.isEmpty() && stack.getFluid().isSame(TFCFluids.SIMPLE_FLUIDS.get(SimpleFluid.OLIVE_OIL).getSource()), this);
        fluidCapability = LazyOptional.of(() -> tank);

        temperature = 0;
        burnTemperature = 0;
        burnTicks = 0;
        lastPlayerTick = Integer.MIN_VALUE;
        syncableData = new IntArrayBuilder().add(() -> (int) temperature, value -> temperature = value);
    }

    @Override
    public void fluidTankChanged() {
        if (level != null && !level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(GasBurnerBlock.HEAT) && state.getValue(GasBurnerBlock.HEAT) > 0) {
                Fluid oliveOil = TFCFluids.SIMPLE_FLUIDS.get(SimpleFluid.OLIVE_OIL).getSource();
                if (tank.isEmpty() || tank.getFluidAmount() < MB_PER_CYCLE || !tank.getFluid().getFluid().isSame(oliveOil)) {
                    if (burnTicks <= 0) {
                        extinguish(state);
                    }
                }
            }
        }
        markForSync();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == Capabilities.FLUID) {
            return fluidCapability.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidCapability.invalidate();
    }

    @Override
    public void onCalendarUpdate(long ticks) {
        assert level != null;
        final BlockState state = level.getBlockState(worldPosition);
    }

    @Override
    @Deprecated
    public long getLastCalendarUpdateTick() {
        return lastPlayerTick;
    }

    @Override
    @Deprecated
    public void setLastCalendarUpdateTick(long tick) {
        lastPlayerTick = tick;
    }

    public void onFirstCreation() {
        burnTicks = 0;
        burnTemperature = 0;
        markForSync();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, @NotNull Inventory playerInv, @NotNull Player player) {
        return GasBurnerBlockContainer.create(this, playerInv, windowID);
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        tank.readFromNBT(nbt.getCompound("tank"));
        temperature = nbt.getFloat("temperature");
        burnTicks = nbt.getInt("burnTicks");
        burnTemperature = nbt.getFloat("burnTemperature");
        lastPlayerTick = nbt.getLong("lastPlayerTick");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.put("tank", tank.writeToNBT(new CompoundTag()));
        nbt.putFloat("temperature", temperature);
        nbt.putInt("burnTicks", burnTicks);
        nbt.putFloat("burnTemperature", burnTemperature);
        nbt.putLong("lastPlayerTick", lastPlayerTick);
        super.saveAdditional(nbt);
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slot == SLOT_FLUID_CONTAINER_IN && Helpers.mightHaveCapability(stack, Capabilities.FLUID_ITEM);
    }

    /**
     * Attempts to light the burner.
     * @param state The current block state.
     * @return {@code true} if the burner was lit.
     */
    public boolean light(BlockState state) {
        assert level != null;
        if (burnTicks > 0) {
            return true;
        }
        if (consumeFuel()) {
            level.setBlockAndUpdate(worldPosition, state.setValue(GasBurnerBlock.HEAT, 2));
            return true;
        }
        return false;
    }

    private void updateFluidIOSlots() {
        assert level != null;
        final ItemStack input = inventory.getStackInSlot(SLOT_FLUID_CONTAINER_IN);
        if (!input.isEmpty() && inventory.getStackInSlot(SLOT_FLUID_CONTAINER_OUT).isEmpty()) {
            FluidHelpers.transferBetweenBlockEntityAndItem(input, this, level, worldPosition, (newOriginalStack, newContainerStack) -> {
                if (newContainerStack.isEmpty()) {
                    inventory.setStackInSlot(SLOT_FLUID_CONTAINER_IN, ItemStack.EMPTY);
                    inventory.setStackInSlot(SLOT_FLUID_CONTAINER_OUT, newOriginalStack);
                } else {
                    inventory.setStackInSlot(SLOT_FLUID_CONTAINER_IN, newOriginalStack);
                    inventory.setStackInSlot(SLOT_FLUID_CONTAINER_OUT, newContainerStack);
                }
            });
        }
    }

    private boolean consumeFuel() {
        if (tank.isEmpty() || tank.getFluidAmount() < MB_PER_CYCLE) {
            return false;
        }
        Fluid oliveOil = TFCFluids.SIMPLE_FLUIDS.get(SimpleFluid.OLIVE_OIL).getSource();
        if (!tank.getFluid().getFluid().isSame(oliveOil)) {
            return false;
        }
        FluidStack simulated = tank.drain(new FluidStack(oliveOil, MB_PER_CYCLE), IFluidHandler.FluidAction.SIMULATE);
        if (simulated.isEmpty() || simulated.getAmount() < MB_PER_CYCLE) {
            return false;
        }
        FluidStack drained = tank.drain(new FluidStack(oliveOil, MB_PER_CYCLE), IFluidHandler.FluidAction.EXECUTE);
        if (!drained.isEmpty() && drained.getAmount() >= MB_PER_CYCLE) {
            burnTemperature = 1000;
            burnTicks = BURN_TICKS_PER_CYCLE;
            markForSync();
            return true;
        }
        return false;
    }

    public void extinguish(BlockState state) {
        assert level != null;
        level.setBlockAndUpdate(worldPosition, state.setValue(GasBurnerBlock.HEAT, 0));
        burnTicks = 0;
        burnTemperature = 0;
        markForSync();
    }
}
