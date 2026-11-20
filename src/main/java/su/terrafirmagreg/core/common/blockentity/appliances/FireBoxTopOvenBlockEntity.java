package su.terrafirmagreg.core.common.blockentity.appliances;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.eerussianguy.firmalife.common.items.FLFoodTraits;
import com.eerussianguy.firmalife.common.recipes.WrappedHeatingRecipe;

import net.dries007.tfc.common.blockentities.IHeatable;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeat;
import net.dries007.tfc.common.capabilities.heat.IHeatBlock;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.dries007.tfc.util.IntArrayBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

public class FireBoxTopOvenBlockEntity extends TickableInventoryBlockEntity<ItemStackHandler> implements IHeatable, IHeatBlock {

    public static final int SLOTS = 18;
    public static final int SLOT_INPUT_START = 0;
    public static final int SLOT_INPUT_END = 8;
    public static final int SLOT_OUTPUT_START = 9;

    private static final Component NAME = Component.translatable(TFGCore.MOD_ID + ".block_entity.fire_box_top_oven");

    @Getter
    protected float temperature;
    @Getter
    protected float targetTemperature;
    protected int targetTemperatureStabilityTicks;

    @SuppressWarnings("unchecked")
    protected final WrappedHeatingRecipe[] cachedRecipes = new WrappedHeatingRecipe[9];

    @Getter
    protected final ContainerData syncableData;

    public FireBoxTopOvenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, defaultInventory(SLOTS), NAME);

        this.temperature = 0.0f;
        this.targetTemperature = 0.0f;
        this.targetTemperatureStabilityTicks = 0;

        int[] inputSlots = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
        int[] outputSlots = new int[] { 9, 10, 11, 12, 13, 14, 15, 16, 17 };

        sidedInventory
                .on(new PartialItemHandler(inventory).insert(inputSlots).extract(outputSlots), Direction.Plane.HORIZONTAL)
                .on(new PartialItemHandler(inventory).insert(inputSlots), Direction.UP)
                .on(new PartialItemHandler(inventory).extract(outputSlots), Direction.DOWN);

        this.syncableData = new IntArrayBuilder()
                .add(() -> (int) this.temperature, val -> this.temperature = val)
                .add(() -> (int) this.targetTemperature, val -> this.targetTemperature = val);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FireBoxTopOvenBlockEntity oven) {
        oven.checkForLastTickSync();

        // Temperature adjustment
        if (oven.temperature != oven.targetTemperature) {
            oven.temperature = HeatCapability.adjustTempTowards(oven.temperature, oven.targetTemperature);
            oven.markForSync();
        }

        if (oven.targetTemperatureStabilityTicks > 0) {
            oven.targetTemperatureStabilityTicks--;
        }

        if (oven.targetTemperature > 0 && oven.targetTemperatureStabilityTicks == 0) {
            oven.targetTemperature = HeatCapability.adjustTempTowards(oven.targetTemperature, 0);
            oven.markForSync();
        }

        // Heat spread
        if (oven.temperature > 300.0f) {
            float spreadTemp = oven.temperature - 300.0f;
            HeatCapability.provideHeatTo(level, pos.above(), spreadTemp);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockEntity be = level.getBlockEntity(pos.relative(dir));
                if (be instanceof FireBoxTopOvenBlockEntity otherOven) {
                    otherOven.setTemperatureIfWarmer(spreadTemp);
                }
            }
        }

        // Cooking handle
        for (int slot = 0; slot < 9; slot++) {
            ItemStack inputStack = oven.inventory.getStackInSlot(slot);
            if (inputStack.isEmpty()) {
                oven.cachedRecipes[slot] = null;
                continue;
            }

            IHeat inputHeat = HeatCapability.get(inputStack);
            if (inputHeat != null) {
                if (inputHeat.getTemperature() < oven.temperature) {
                    float modifier = FoodCapability.hasTrait(inputStack, FLFoodTraits.OVEN_BAKED) ? 1.1F : 1.4F;
                    HeatCapability.addTemp(inputHeat, oven.temperature, modifier);
                }

                WrappedHeatingRecipe recipe = oven.cachedRecipes[slot];
                if (recipe == null) {
                    recipe = WrappedHeatingRecipe.getRecipe(inputStack);
                    oven.cachedRecipes[slot] = recipe;
                }

                if (recipe != null && recipe.isValidTemperature(inputHeat.getTemperature())) {
                    if (oven.inventory.getStackInSlot(SLOT_OUTPUT_START + slot).isEmpty()) {
                        ItemStackInventory recipeInv = new ItemStackInventory(inputStack);
                        ItemStack output = recipe.assemble(recipeInv, level.registryAccess());
                        HeatCapability.setTemperature(output, inputHeat.getTemperature());
                        FoodCapability.applyTrait(output, FLFoodTraits.OVEN_BAKED);
                        FoodCapability.setCreationDate(output, FoodCapability.getRoundedCreationDate());
                        oven.inventory.setStackInSlot(slot, ItemStack.EMPTY);
                        oven.inventory.setStackInSlot(SLOT_OUTPUT_START + slot, output);
                        oven.cachedRecipes[slot] = null;
                        oven.markForSync();
                    }
                }
            }
        }
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot < SLOT_INPUT_START || slot > SLOT_INPUT_END) {
            return false;
        }
        if (stack.isEmpty() || stack.is(TFGTags.Items.OVEN_BLACKLISTED)) {
            return false;
        }
        if (!HeatCapability.maybeHas(stack) && HeatCapability.get(stack) == null) {
            return false;
        }
        final IFood food = FoodCapability.get(stack);
        if (food != null && food.isRotten()) {
            return false;
        }
        return inventory.getStackInSlot(SLOT_OUTPUT_START + slot).isEmpty();
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return 1;
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        temperature = nbt.getFloat("temperature");
        targetTemperature = nbt.getFloat("targetTemperature");
        targetTemperatureStabilityTicks = nbt.getInt("targetTemperatureStabilityTicks");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.putFloat("temperature", temperature);
        nbt.putFloat("targetTemperature", targetTemperature);
        nbt.putInt("targetTemperatureStabilityTicks", targetTemperatureStabilityTicks);
        super.saveAdditional(nbt);
    }

    @Override
    public void setTemperature(float temperature) {
        this.targetTemperature = temperature;
        this.targetTemperatureStabilityTicks = 5;
        markForSync();
    }

    @Override
    public void setTemperatureIfWarmer(float temperature) {
        if (temperature > this.targetTemperature) {
            this.targetTemperature = temperature;
            this.targetTemperatureStabilityTicks = 5;
            markForSync();
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == HeatCapability.BLOCK_CAPABILITY) {
            return LazyOptional.of(() -> this).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        sidedInventory.invalidate();
    }
}
