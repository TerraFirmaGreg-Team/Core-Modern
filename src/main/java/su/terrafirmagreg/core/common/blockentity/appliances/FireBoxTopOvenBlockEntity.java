package su.terrafirmagreg.core.common.blockentity.appliances;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.eerussianguy.firmalife.common.recipes.WrappedHeatingRecipe;

import net.dries007.tfc.client.particle.TFCParticles;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.appliances.FireBoxTopOvenBlock;
import su.terrafirmagreg.core.common.data.TFGFoodTraits;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.config.TFGConfig;

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
        if (oven.temperature > TFGConfig.SERVER.fireBoxTopOvenHeatLoss.get()) {
            float spreadTemp = oven.temperature - TFGConfig.SERVER.fireBoxTopOvenHeatLoss.get();
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
                    // Base Speed
                    float modifier = 5.0F;
                    if (FoodCapability.hasTrait(inputStack, TFGFoodTraits.FIRE_BOX_BAKED)) {
                        // Already baked items continue to heat up much slower.
                        modifier = 1.1F;
                    } else if (FoodCapability.has(inputStack)) {
                        // Food items heat up slower than metals.
                        modifier = 1.4F;
                    }
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
                        boolean burnt = output.isEmpty();

                        if (!burnt) {
                            HeatCapability.setTemperature(output, inputHeat.getTemperature());
                            FoodCapability.applyTrait(output, TFGFoodTraits.FIRE_BOX_BAKED);
                            FoodCapability.setCreationDate(output, FoodCapability.getRoundedCreationDate());
                        }

                        oven.inventory.setStackInSlot(slot, ItemStack.EMPTY);
                        oven.inventory.setStackInSlot(SLOT_OUTPUT_START + slot, output);
                        oven.cachedRecipes[slot] = null;
                        oven.markForSync();

                        fanFareEffects(level, pos, state, slot, burnt);
                    }
                }
            }
        }
    }

    private static void fanFareEffects(Level level, BlockPos pos, BlockState state, int slot, boolean burnt) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Direction facing = state.hasProperty(FireBoxTopOvenBlock.FACING)
                ? state.getValue(FireBoxTopOvenBlock.FACING)
                : Direction.NORTH;
        Vec3 center = FireBoxTopOvenBlock.SLOT_CENTERS[facing.get2DDataValue()][slot];

        double x = pos.getX() + center.x;
        double y = pos.getY() + center.y + 0.05D;
        double z = pos.getZ() + center.z;

        if (burnt) {
            // Burnt Up
            serverLevel.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 4, 0.06D, 0.05D, 0.06D, 0.01D);
            serverLevel.sendParticles(ParticleTypes.ASH, x, y, z, 6, 0.08D, 0.06D, 0.08D, 0.01D);
        } else {
            // Recipe Success
            serverLevel.playSound(null, x, y, z, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.5F, 0.2F);
            serverLevel.sendParticles(TFCParticles.STEAM.get(), x, y, z, 3, 0.08D, 0.05D, 0.08D, 0.01D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, x, y, z, 3, 0.08D, 0.05D, 0.08D, 0.01D);
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
