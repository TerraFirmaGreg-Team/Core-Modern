package su.terrafirmagreg.core.common.blockentity.appliances;

import static net.dries007.tfc.common.capabilities.heat.HeatCapability.targetDeviceTemp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blockentities.BellowsBlockEntity;
import net.dries007.tfc.common.blockentities.IHeatable;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.util.Fuel;
import net.dries007.tfc.util.IntArrayBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.items.ItemStackHandler;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.appliances.FireBoxBlock;
import su.terrafirmagreg.core.common.container.FireBoxContainer;
import su.terrafirmagreg.core.common.data.TFGSounds;
import su.terrafirmagreg.core.common.environment.EnvironmentSystem;
import su.terrafirmagreg.core.config.TFGConfig;

public class FireBoxBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements MenuProvider, IHeatable {
    public static final int SLOT_FUEL_IN = 0;
    public static final int SLOTS = 1;

    private static final Component NAME = Component.translatable(TFGCore.MOD_ID + ".block_entity.fire_box");
    public boolean isRedstoneIgnited = false;

    public static void serverTick(Level level, BlockPos pos, BlockState state, FireBoxBlockEntity fireBox) {
        fireBox.checkForLastTickSync();

        // Delay scheduling for ignition.
        if (fireBox.stateDelayTicks > 0) {
            fireBox.stateDelayTicks--;
            if (fireBox.stateDelayTicks == 0 && fireBox.pendingState != null) {
                if (fireBox.consumeFuel()) {
                    BlockState currentState = level.getBlockState(pos);
                    BlockState targetState = currentState.setValue(FireBoxBlock.HEAT, 2).setValue(FireBoxBlock.LIT, true);
                    fireBox.ignite(level, pos, targetState);
                    state = targetState;
                }

                fireBox.markForSync();
                fireBox.pendingState = null;
                fireBox.stateDelayTicks = -1;
            }
        }

        // Temperature Changes.
        if (state.getValue(FireBoxBlock.HEAT) > 0) {
            int heatLevel = Mth.clamp((int) (fireBox.temperature / Heat.maxVisibleTemperature() * 6) + 1, 1, 7);
            if (heatLevel != state.getValue(FireBoxBlock.HEAT)) {
                level.setBlockAndUpdate(pos, state.setValue(FireBoxBlock.HEAT, heatLevel));
                fireBox.markForSync();
            }

            if (fireBox.burnTicks > 0) {
                // Fuel burns twice as fast when using bellows.
                fireBox.burnTicks -= fireBox.airTicks > 0 ? 2 : 1;
            }
            if (fireBox.burnTicks <= 0) {
                if (!fireBox.consumeFuel()) {
                    fireBox.extinguish(state);
                }
            }
        } else if (fireBox.burnTemperature > 0) {
            fireBox.extinguish(state);
        }

        if (fireBox.airTicks > 0) {
            fireBox.airTicks--;
        }

        // Temperature Providing.
        if (fireBox.temperature > 0 || fireBox.burnTemperature > 0) {
            float heatDelta = fireBox.airTicks > 0 ? (float) (TFGConfig.SERVER.fireBoxHeatDelta.get() * 2.0f) : (float) (TFGConfig.SERVER.fireBoxHeatDelta.get() * 1.0f);
            float coolDelta = fireBox.airTicks > 0 ? (float) (TFGConfig.SERVER.fireBoxCoolDelta.get() * 0.5f) : (float) (TFGConfig.SERVER.fireBoxCoolDelta.get() * 1.0f);
            float targetTemp = targetDeviceTemp(fireBox.burnTemperature, fireBox.airTicks, false);
            fireBox.temperature = HeatCapability.adjustTempTowards(fireBox.temperature, targetTemp, heatDelta, coolDelta);

            HeatCapability.provideHeatTo(level, pos.above(), fireBox.temperature);
            fireBox.markForSync();
        }
    }

    public int stateDelayTicks = -1;
    private BlockState pendingState = null;

    @Getter
    protected final ContainerData syncableData;
    @Getter
    public float temperature;
    @Getter
    public int burnTicks;
    @Getter
    public int airTicks;
    @Getter
    public int maxBurnTicks;
    public float burnTemperature;
    private long lastPlayerTick;

    public FireBoxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, defaultInventory(SLOTS), NAME);

        temperature = 0;
        burnTemperature = 0;
        burnTicks = 0;
        airTicks = 0;
        maxBurnTicks = 10;
        lastPlayerTick = Integer.MIN_VALUE;
        syncableData = new IntArrayBuilder()
                .add(() -> (int) temperature, value -> temperature = value)
                .add(() -> burnTicks, value -> burnTicks = value)
                .add(() -> maxBurnTicks, value -> maxBurnTicks = value)
                .add(() -> (int) burnTemperature, value -> burnTemperature = value);

        sidedInventory
                .on(new PartialItemHandler(inventory).insert(SLOT_FUEL_IN), Direction.Plane.HORIZONTAL)
                .on(new PartialItemHandler(inventory).insert(SLOT_FUEL_IN), Direction.UP)
                .on(new PartialItemHandler(inventory).insert(SLOT_FUEL_IN), Direction.DOWN);
    }

    public void intakeAir(int amount) {
        airTicks = (int) (airTicks + (amount * TFGConfig.SERVER.fireBoxEfficiencyMultiplier.get()));
        int maxTicks = BellowsBlockEntity.MAX_DEVICE_AIR_TICKS * 2;
        if (airTicks > maxTicks) {
            airTicks = maxTicks;
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        sidedInventory.invalidate();
    }

    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return inventory.insertItem(slot, stack, simulate);
    }

    @NotNull
    public ItemStack insertItem(@NotNull ItemStack stack, boolean simulate) {
        return inventory.insertItem(SLOT_FUEL_IN, stack, simulate);
    }

    @NotNull
    public ItemStack insertItem(@NotNull ItemStack stack) {
        return inventory.insertItem(SLOT_FUEL_IN, stack, false);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, @NotNull Inventory playerInv, @NotNull Player player) {
        return FireBoxContainer.create(this, playerInv, windowID);
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        temperature = nbt.getFloat("temperature");
        burnTicks = nbt.getInt("burnTicks");
        airTicks = nbt.getInt("airTicks");
        maxBurnTicks = nbt.contains("maxBurnTicks") ? nbt.getInt("maxBurnTicks") : 10;
        burnTemperature = nbt.getFloat("burnTemperature");
        lastPlayerTick = nbt.getLong("lastPlayerTick");
        isRedstoneIgnited = nbt.getBoolean("isRedstoneIgnited");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.putFloat("temperature", temperature);
        nbt.putInt("burnTicks", burnTicks);
        nbt.putInt("airTicks", airTicks);
        nbt.putInt("maxBurnTicks", maxBurnTicks);
        nbt.putFloat("burnTemperature", burnTemperature);
        nbt.putLong("lastPlayerTick", lastPlayerTick);
        nbt.putBoolean("isRedstoneIgnited", isRedstoneIgnited);
        super.saveAdditional(nbt);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slot == 0 && Fuel.get(stack) != null;
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
            this.stateDelayTicks = -1;
            this.pendingState = null;

            BlockState targetState = state.setValue(FireBoxBlock.HEAT, 2).setValue(FireBoxBlock.LIT, true);
            this.ignite(level, worldPosition, targetState);
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
        if (burnTicks > 0 || stateDelayTicks > 0) {
            return;
        }
        if (hasFuel()) {
            this.pendingState = state.setValue(FireBoxBlock.HEAT, 2).setValue(FireBoxBlock.LIT, true);
            this.stateDelayTicks = 15;
            this.setChanged();
            level.playSound(null, worldPosition, TFGSounds.FIRE_CLICK_CLICK.getMainEvent(), SoundSource.BLOCKS, 2, 0.8f);
        }
    }

    /**
     * Plays the fire whoosh sound and then updates the block.
     */
    private void ignite(Level targetLevel, BlockPos targetPos, BlockState targetState) {
        RandomSource rand = targetLevel.random;
        targetLevel.playSound(null, targetPos, TFGSounds.FIRE_WHOOSH.getMainEvent(), SoundSource.BLOCKS, 2, 1 + rand.nextFloat());
        targetLevel.setBlockAndUpdate(targetPos, targetState);
    }

    public boolean hasFuel() {
        final ItemStack fuelStack = inventory.getStackInSlot(SLOT_FUEL_IN);
        if (!fuelStack.isEmpty()) {
            return Fuel.get(fuelStack) != null;
        }
        return false;
    }

    /**
     * Check for fuel availability.
     */
    private boolean consumeFuel() {
        final ItemStack fuelStack = inventory.getStackInSlot(SLOT_FUEL_IN);
        if (!fuelStack.isEmpty() && EnvironmentSystem.hasOxygen(level, worldPosition)) {
            Fuel fuel = Fuel.get(fuelStack);
            if (fuel != null) {
                int duration = (int) (fuel.getDuration() * TFGConfig.SERVER.fireBoxEfficiencyMultiplier.get());
                burnTicks += duration;
                maxBurnTicks = duration;
                burnTemperature = fuel.getTemperature();
                fuelStack.shrink(1);
                if (fuelStack.isEmpty()) {
                    inventory.setStackInSlot(SLOT_FUEL_IN, ItemStack.EMPTY);
                }
                markForSync();
                return true;
            }
        }
        return false;
    }

    /**
     * Removes HEAT and LIT states and plays an extinguishing sound.
     */
    public void extinguish(BlockState state) {
        assert level != null;
        level.setBlockAndUpdate(worldPosition, state.setValue(FireBoxBlock.HEAT, 0).setValue(FireBoxBlock.LIT, false));
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2f, 2);
        burnTicks = 0;
        burnTemperature = 0;
        stateDelayTicks = -1;
        pendingState = null;
        markForSync();
    }
}
