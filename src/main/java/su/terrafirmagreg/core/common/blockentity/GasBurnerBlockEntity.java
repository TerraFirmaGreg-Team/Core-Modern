package su.terrafirmagreg.core.common.blockentity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blockentities.IHeatable;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.FluidTankCallback;
import net.dries007.tfc.common.capabilities.InventoryFluidTank;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.IntArrayBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.server.ServerLifecycleHooks;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.GasBurnerBlock;
import su.terrafirmagreg.core.common.container.GasBurnerBlockContainer;
import su.terrafirmagreg.core.common.data.TFGSounds;
import su.terrafirmagreg.core.common.environment.EnvironmentSystem;
import su.terrafirmagreg.core.common.recipe.GasBurnerFuelRecipe;
import su.terrafirmagreg.core.config.TFGConfig;

public class GasBurnerBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements FluidTankCallback, MenuProvider, IHeatable {
    public static final int SLOT_FLUID_CONTAINER_IN = 0;
    public static final int SLOT_FLUID_CONTAINER_OUT = 1;
    public static final int SLOTS = 2;
    public static final int CAPACITY = TFGConfig.SERVER.gasBurnerCapacity.get();

    private static final Component NAME = Component.translatable(TFGCore.MOD_ID + ".block_entity.gas_burner");
    private boolean isProcessingTankChange = false;
    public boolean isRedstoneIgnited = false;

    public static void serverTick(Level level, BlockPos pos, BlockState state, GasBurnerBlockEntity burner) {
        burner.checkForLastTickSync();

        // Handle delay scheduling for things like state changes.
        if (burner.stateDelayTicks > 0) {
            burner.stateDelayTicks--;
            if (burner.stateDelayTicks == 0 && burner.pendingState != null) {
                BlockState currentState = level.getBlockState(pos);
                BlockState targetState = currentState.setValue(GasBurnerBlock.HEAT, 2).setValue(GasBurnerBlock.LIT, true);
                burner.ignite(level, pos, targetState);
                level.setBlockAndUpdate(pos, targetState);

                burner.markForSync();
                burner.pendingState = null;
                burner.stateDelayTicks = -1;
            }
        }

        if (level.getGameTime() % 5 == 0) {
            burner.updateFluidIOSlots();
        }

        // Temperature state changes.
        if (state.getValue(GasBurnerBlock.HEAT) > 0) {
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

        // Temperature Providing.
        if (burner.temperature > 0 || burner.burnTemperature > 0) {
            float maxTemp = burner.getMaxTemperature();
            float effectiveMax = burner.burnTicks > 0 ? Math.min(maxTemp, burner.burnTemperature) : maxTemp;
            float target = HeatCapability.targetDeviceTemp(burner.burnTicks > 0 ? burner.burnTemperature : 0, 0, false);
            float positiveDelta = (float) Math.max(TFGConfig.SERVER.gasBurnerHeatDelta.get(), 0.1f);
            float negativeDelta = (float) Math.max(TFGConfig.SERVER.gasBurnerCoolDelta.get(), 0.1f);
            target = Math.min(target, effectiveMax);
            burner.temperature = HeatCapability.adjustTempTowards(burner.temperature, target, positiveDelta, negativeDelta);

            HeatCapability.provideHeatTo(level, pos.above(), burner.temperature);
            burner.markForSync();
        }
    }

    public int stateDelayTicks = -1;
    private BlockState pendingState = null;
    protected final InventoryFluidTank tank;
    public final LazyOptional<IFluidHandler> fluidCapability;

    @Getter
    protected final ContainerData syncableData;
    @Getter
    public float temperature;
    @Getter
    public int burnTicks;
    @Getter
    public int maxBurnTicks = 10;
    public float burnTemperature;
    private long lastPlayerTick;

    public GasBurnerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, defaultInventory(SLOTS), NAME);

        tank = new InventoryFluidTank(CAPACITY, this::isFluidValid, this);
        fluidCapability = LazyOptional.of(() -> tank);

        temperature = 0;
        burnTemperature = 0;
        burnTicks = 0;
        maxBurnTicks = 10;
        lastPlayerTick = Integer.MIN_VALUE;
        syncableData = new IntArrayBuilder()
                .add(() -> (int) temperature, value -> temperature = value)
                .add(() -> burnTicks, value -> burnTicks = value)
                .add(() -> maxBurnTicks, value -> maxBurnTicks = value)
                .add(() -> (int) burnTemperature, value -> burnTemperature = value);
    }

    /**
     * Checks if the input fluid is valid for the burner.
     */
    public boolean isFluidValid(FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (level != null) {
            return GasBurnerFuelRecipe.getRecipe(level, stack).isPresent();
        }
        return isValidFluid(stack);
    }

    public static boolean isValidFluid(FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return GasBurnerFuelRecipe.getRecipe(server.overworld(), stack).isPresent();
        }
        if (FMLEnvironment.dist.isClient()) {
            Level clientLevel = Minecraft.getInstance().level;
            if (clientLevel != null) {
                return GasBurnerFuelRecipe.getRecipe(clientLevel, stack).isPresent();
            }
        }
        return true;
    }

    @Override
    public void fluidTankChanged() {
        if (level == null || level.isClientSide) {
            markForSync();
            return;
        }

        if (isProcessingTankChange)
            return;

        BlockState state = level.getBlockState(worldPosition);

        if (state.hasProperty(GasBurnerBlock.HEAT) && state.getValue(GasBurnerBlock.HEAT) > 0) {
            GasBurnerFuelRecipe recipe = !tank.isEmpty() ? GasBurnerFuelRecipe.getRecipe(level, tank.getFluid()).orElse(null) : null;
            if (recipe == null || tank.getFluidAmount() < recipe.getFluid().amount()) {
                if (burnTicks <= 0 && state.getValue(GasBurnerBlock.LIT)) {
                    extinguish(state);
                    state = level.getBlockState(worldPosition);
                }
            }
        }
        try {
            isProcessingTankChange = true;
            GasBurnerBlock.updateBurnerRedstoneAndState(level, worldPosition, state);
        } finally {
            isProcessingTankChange = false;
        }

        level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
        markForSync();
    }

    public int getFuelLevel() {
        if (tank.isEmpty() || tank.getCapacity() <= 0) {
            return 0;
        }
        float fraction = (float) tank.getFluidAmount() / (float) tank.getCapacity();
        return Mth.floor(fraction * 14.0F) + (tank.getFluidAmount() > 0 ? 1 : 0);
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
        maxBurnTicks = nbt.contains("maxBurnTicks") ? nbt.getInt("maxBurnTicks") : 10;
        burnTemperature = nbt.getFloat("burnTemperature");
        lastPlayerTick = nbt.getLong("lastPlayerTick");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.put("tank", tank.writeToNBT(new CompoundTag()));
        nbt.putFloat("temperature", temperature);
        nbt.putInt("burnTicks", burnTicks);
        nbt.putInt("maxBurnTicks", maxBurnTicks);
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
     * Lighting event method. Changes state to LIT if fuel is available and runs ignite().
     */
    public boolean light(BlockState state) {
        assert level != null;
        if (burnTicks > 0) {
            return true;
        }
        if (consumeFuel()) {
            this.isRedstoneIgnited = false;
            this.stateDelayTicks = 0;

            BlockState targetState = state.setValue(GasBurnerBlock.HEAT, 2).setValue(GasBurnerBlock.LIT, true);
            this.ignite(level, worldPosition, targetState);

            level.setBlockAndUpdate(worldPosition, targetState);
            return true;
        }
        return false;
    }

    /**
     * Lighting event method for redstone lighting. Plays a click-click sound and then after a delay;
     * changes state to LIT.
     */
    public void autoLight(BlockState state) {
        assert level != null;
        if (burnTicks > 0) {
            return;
        }
        if (consumeFuel()) {
            this.pendingState = state.setValue(GasBurnerBlock.HEAT, 2).setValue(GasBurnerBlock.LIT, true);
            this.stateDelayTicks = 15;
            this.setChanged();

        }
        level.playSound(null, worldPosition, TFGSounds.FIRE_CLICK_CLICK.getMainEvent(), SoundSource.BLOCKS, 2, 0.8f);
    }

    /**
     * Plays the fire whoosh sound and then updates the block.
     */
    private void ignite(Level targetLevel, BlockPos targetPos, BlockState targetState) {
        RandomSource rand = targetLevel.random;
        targetLevel.playSound(null, targetPos, TFGSounds.FIRE_WHOOSH.getMainEvent(), SoundSource.BLOCKS, 2, 1 + rand.nextFloat());
        targetLevel.setBlockAndUpdate(targetPos, targetState);
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

    /**
     * Check for fuel availability against recipes and empties the tank.
     */
    private boolean consumeFuel() {
        if (tank.isEmpty() || level == null) {
            return false;
        }
        if (!EnvironmentSystem.hasOxygen(level, worldPosition)) {
            return false;
        }
        FluidStack current = tank.getFluid();
        GasBurnerFuelRecipe recipe = GasBurnerFuelRecipe.getRecipe(level, current).orElse(null);
        if (recipe == null) {
            return false;
        }
        int amount = recipe.getFluid().amount();
        if (tank.getFluidAmount() < amount) {
            return false;
        }
        FluidStack simulated = tank.drain(amount, IFluidHandler.FluidAction.SIMULATE);
        if (simulated.isEmpty() || simulated.getAmount() < amount) {
            return false;
        }
        FluidStack drained = tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
        if (!drained.isEmpty() && drained.getAmount() >= amount) {
            burnTemperature = recipe.getTemperature();
            burnTicks = recipe.getDuration();
            maxBurnTicks = recipe.getDuration();
            markForSync();
            return true;
        }
        return false;
    }

    public float getMaxTemperature() {
        if (level != null) {
            return getMaxTemperature(level.getBlockState(worldPosition));
        }
        return getMaxTemperature(getBlockState());
    }

    /**
     * Retrieves the maximum temperature based on the SET_LEVEL state.
     */
    public static float getMaxTemperature(BlockState state) {
        if (state != null && state.hasProperty(GasBurnerBlock.SET_LEVEL)) {
            int setLevel = state.getValue(GasBurnerBlock.SET_LEVEL);
            Heat[] values = Heat.values();
            if (setLevel >= 0 && setLevel < values.length) {
                return values[setLevel].getMax();
            }
        }
        return Heat.BRILLIANT_WHITE.getMax();
    }

    /**
     * Removes HEAT and LIT states and plays an extinguishing sound.
     */
    public void extinguish(BlockState state) {
        assert level != null;
        level.setBlockAndUpdate(worldPosition, state.setValue(GasBurnerBlock.HEAT, 0).setValue(GasBurnerBlock.LIT, false));
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2f, 2);
        burnTicks = 0;
        burnTemperature = 0;
        markForSync();
    }
}
