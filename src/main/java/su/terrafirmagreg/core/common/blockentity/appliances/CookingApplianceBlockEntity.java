package su.terrafirmagreg.core.common.blockentity.appliances;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blockentities.IHeatable;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.blockentities.TickableInventoryBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.DelegateFluidHandler;
import net.dries007.tfc.common.capabilities.DelegateItemHandler;
import net.dries007.tfc.common.capabilities.FluidTankCallback;
import net.dries007.tfc.common.capabilities.InventoryFluidTank;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.capabilities.PartialFluidHandler;
import net.dries007.tfc.common.capabilities.PartialItemHandler;
import net.dries007.tfc.common.capabilities.SidedHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeatBlock;
import net.dries007.tfc.common.container.ISlotCallback;
import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.dries007.tfc.util.IntArrayBuilder;
import net.dries007.tfc.util.calendar.ICalendarTickable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;

import lombok.Getter;

import su.terrafirmagreg.core.common.block.appliances.CookingApplianceBlock;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.recipe.CookingApplianceRecipe;

/**
 * Base class for cooking appliances like {@link CopperPotBlockEntity}, extending functionality from
 * heatable, calendar tickable, and tickable inventory block entities.
 * Provides mechanics for handling item and fluid inputs/outputs, managing recipes,
 * and temperature properties.
 */
@SuppressWarnings("unused")
public abstract class CookingApplianceBlockEntity extends TickableInventoryBlockEntity<CookingApplianceBlockEntity.CookingApplianceInventory>
        implements ICalendarTickable, IHeatable, IHeatBlock {

    public static final int TARGET_TEMPERATURE_STABILITY_TICKS = 5;

    public static final int ERROR_NONE = 0;
    public static final int ERROR_ROTTEN_INGREDIENT = 1 << 1;
    public static final int ERROR_NOT_ENOUGH_ITEM_SPACE = 1 << 2;
    public static final int ERROR_NOT_ENOUGH_FLUID_SPACE = 1 << 3;

    protected float temperature;
    @Getter
    protected float targetTemperature;
    protected int targetTemperatureStabilityTicks;
    @Getter
    protected int cookingTicks;
    protected long lastCalendarUpdateTick = Integer.MIN_VALUE;

    @Getter
    protected int recipeError;
    protected int maxDuration;

    @Getter
    protected final ContainerData syncableData;

    @Nullable
    protected CookingApplianceRecipe cachedRecipe;

    protected final SidedHandler.Builder<IFluidHandler> sidedFluidInventory;
    protected final SidedHandler.Builder<IHeatBlock> sidedHeat;

    public CookingApplianceBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            Component defaultName) {
        super(type, pos, state, CookingApplianceInventory::new, defaultName);

        this.temperature = 0.0f;
        this.targetTemperature = 0.0f;
        this.targetTemperatureStabilityTicks = 0;
        this.cookingTicks = 0;
        this.recipeError = ERROR_NONE;
        this.maxDuration = 100;
        this.cachedRecipe = null;

        this.sidedFluidInventory = new SidedHandler.Builder<>(inventory);
        this.sidedHeat = new SidedHandler.Builder<>(this);

        this.syncableData = new IntArrayBuilder()
                .add(() -> (int) this.temperature, val -> this.temperature = val)
                .add(() -> (int) this.targetTemperature, val -> this.targetTemperature = val)
                .add(() -> this.cookingTicks, val -> this.cookingTicks = val)
                .add(() -> this.recipeError, val -> this.recipeError = val)
                .add(() -> this.maxDuration, val -> this.maxDuration = val);

        configureSidedHandlers();
    }

    public abstract int getItemInputCount();

    public abstract int getFluidInputCount();

    public abstract int getItemOutputCount();

    public abstract int getFluidOutputCount();

    public abstract int getFluidInputCapacity();

    public abstract int getFluidOutputCapacity();

    public abstract RecipeType<? extends CookingApplianceRecipe> getRecipeType();

    public int getCatalystSlot() {
        return getItemInputCount();
    }

    public int getToolSlot() {
        return getItemInputCount() + 1;
    }

    public int getItemOutputStartSlot() {
        return getItemInputCount() + 2;
    }

    public int getTotalItemSlots() {
        return getItemInputCount() + 2 + getItemOutputCount();
    }

    public int getTotalFluidTanks() {
        return getFluidInputCount() + getFluidOutputCount();
    }

    public int getFluidOutputStartTank() {
        return getFluidInputCount();
    }

    /**
     * Configures the sided handlers for item, fluid, and heat logistics.
     */
    protected void configureSidedHandlers() {
        int[] inputSlots = new int[getItemInputCount() + 2];
        for (int i = 0; i < getItemInputCount() + 2; i++) {
            inputSlots[i] = i;
        }

        int[] outputSlots = new int[getItemOutputCount()];
        for (int i = 0; i < getItemOutputCount(); i++) {
            outputSlots[i] = getItemOutputStartSlot() + i;
        }

        sidedInventory
                .on(new PartialItemHandler(inventory).insert(inputSlots).extract(outputSlots), Direction.Plane.HORIZONTAL)
                .on(new PartialItemHandler(inventory).insert(inputSlots), Direction.UP)
                .on(new PartialItemHandler(inventory).extract(outputSlots), Direction.DOWN);

        sidedFluidInventory
                .on(new PartialFluidHandler(inventory).insert().extract(), Direction.Plane.HORIZONTAL)
                .on(new PartialFluidHandler(inventory).insert(), Direction.UP)
                .on(new PartialFluidHandler(inventory).extract(), Direction.DOWN);

        sidedHeat.on(this, Direction.DOWN);
        sidedHeat.on(this, Direction.Plane.HORIZONTAL);
        sidedHeat.on(this, Direction.UP);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CookingApplianceBlockEntity appliance) {
        appliance.checkForLastTickSync();
        appliance.checkForCalendarUpdate();

        // Temperature adjustments.
        if (appliance.temperature != appliance.targetTemperature) {
            appliance.temperature = HeatCapability.adjustTempTowards(appliance.temperature, appliance.targetTemperature);
            appliance.markForSync();
        }

        if (appliance.targetTemperatureStabilityTicks > 0) {
            appliance.targetTemperatureStabilityTicks--;
        }

        if (appliance.targetTemperature > 0 && appliance.targetTemperatureStabilityTicks == 0) {
            appliance.targetTemperature = HeatCapability.adjustTempTowards(appliance.targetTemperature, 0);
            appliance.markForSync();
        }

        // Heat blockstate handling.
        int heatLevel = Mth.clamp((int) (appliance.temperature / Heat.maxVisibleTemperature() * 6), 0, 4);
        if (heatLevel != state.getValue(CookingApplianceBlock.RECEIVED_HEAT)) {
            level.setBlockAndUpdate(pos, state.setValue(CookingApplianceBlock.RECEIVED_HEAT, heatLevel));
            appliance.markForSync();
        }

        appliance.updateAndTickRecipe(level);
    }

    /**
     * Updates the cooking appliance's recipe state and advances recipe ticks if a valid recipe is active.
     */
    protected void updateAndTickRecipe(Level level) {
        if (level == null || level.isClientSide)
            return;

        int prevError = this.recipeError;
        int prevDuration = this.maxDuration;

        if (cachedRecipe == null || !cachedRecipe.matches(this, level)) {
            CookingApplianceRecipe matching = level.getRecipeManager().getAllRecipesFor(getRecipeType()).stream()
                    .filter(r -> r instanceof CookingApplianceRecipe)
                    .map(r -> (CookingApplianceRecipe) r)
                    .sorted((r1, r2) -> Integer.compare(r2.getAttachments().size(), r1.getAttachments().size()))
                    .filter(r -> r.matches(this, level))
                    .findFirst()
                    .orElse(null);

            if (matching != null) {
                if (cachedRecipe != null && cachedRecipe != matching) {
                    cookingTicks = 0;
                }
                cachedRecipe = matching;
            }
        }

        if (cachedRecipe != null && cachedRecipe.matches(this, level)) {
            this.recipeError = ERROR_NONE;
            this.maxDuration = cachedRecipe.getDuration();
            cookingTicks++;
            if (cookingTicks >= cachedRecipe.getDuration()) {
                finishRecipe(cachedRecipe);
                cookingTicks = 0;
                cachedRecipe = null;
            }
            markForSync();
        } else {
            // Check if cachedRecipe still has its inputs in the appliance.
            if (cachedRecipe != null && !cachedRecipe.matchesInputs(this)) {
                // Fail if ingredients were removed in the middle of a recipe.
                cookingTicks = 0;
                cachedRecipe = null;
            }

            int detectedErrors = ERROR_NONE;
            CookingApplianceRecipe candidate = cachedRecipe;
            if (candidate == null) {
                candidate = level.getRecipeManager().getAllRecipesFor(getRecipeType()).stream()
                        .filter(r -> r instanceof CookingApplianceRecipe)
                        .map(r -> (CookingApplianceRecipe) r)
                        .sorted((r1, r2) -> Integer.compare(r2.getAttachments().size(), r1.getAttachments().size()))
                        .filter(r -> r.matchesInputs(this))
                        .findFirst()
                        .orElse(null);
            }

            if (candidate != null) {
                detectedErrors = candidate.getErrorFlags(this);
                this.maxDuration = candidate.getDuration();
            } else {
                if (hasAnyRottenInput()) {
                    detectedErrors = ERROR_ROTTEN_INGREDIENT;
                }
                this.maxDuration = 0;
                cookingTicks = 0;
            }

            this.recipeError = detectedErrors;

            if (cookingTicks > 0) {
                cookingTicks = Math.max(0, cookingTicks - 1);
                markForSync();
            } else if (prevError != this.recipeError || prevDuration != this.maxDuration) {
                markForSync();
            }
        }
    }

    public boolean hasAnyRottenInput() {
        for (int i = 0; i < getItemInputCount(); i++) {
            ItemStack stack = getItemInputStack(i);
            if (!stack.isEmpty()) {
                final IFood food = FoodCapability.get(stack);
                if (food != null && food.isRotten()) {
                    return true;
                }
            }
        }
        ItemStack catalyst = getCatalystStack();
        if (!catalyst.isEmpty()) {
            final IFood food = FoodCapability.get(catalyst);
            return food != null && food.isRotten();
        }
        return false;
    }

    protected void finishRecipe(CookingApplianceRecipe recipe) {
        assert level != null;

        // Assemble outputs first.
        recipe.assembleOutputs(this);

        // Consume item inputs.
        CookingApplianceRecipe.MatchResult matchResult = recipe.matchItemInputs(this);
        if (matchResult.matches()) {
            for (int i = 0; i < getItemInputCount(); i++) {
                int toShrink = matchResult.consumedPerSlot()[i];
                if (toShrink > 0) {
                    ItemStack stack = getItemInputStack(i);
                    stack.shrink(toShrink);
                    if (stack.isEmpty()) {
                        setItemInputStack(i, ItemStack.EMPTY);
                    }
                }
            }
        }

        // Consume fluid inputs.
        for (FluidStackIngredient fluidIngredient : recipe.getFluidIngredients()) {
            int remainingToDrain = fluidIngredient.amount();
            for (int i = 0; i < getFluidInputCount() && remainingToDrain > 0; i++) {
                FluidStack inTank = getFluidInputTank(i);
                if (!inTank.isEmpty() && fluidIngredient.test(inTank)) {
                    int drained = Math.min(inTank.getAmount(), remainingToDrain);
                    inTank.shrink(drained);
                    remainingToDrain -= drained;
                    if (inTank.isEmpty()) {
                        setFluidInputTank(i, FluidStack.EMPTY);
                    }
                }
            }
        }

        // Consume catalyst.
        if (!recipe.getCatalyst().isEmpty()) {
            ItemStack catalyst = getCatalystStack();
            if (!catalyst.isEmpty() && recipe.getCatalyst().test(catalyst)) {
                catalyst.shrink(1);
                if (catalyst.isEmpty()) {
                    setCatalystStack(ItemStack.EMPTY);
                }
            }
        }

        // Damage tool.
        if (!recipe.getTool().isEmpty()) {
            ItemStack tool = getToolStack();
            if (!tool.isEmpty() && recipe.getTool().test(tool)) {
                if (tool.isDamageableItem() && recipe.getToolDamage() > 0) {
                    int newDamage = tool.getDamageValue() + recipe.getToolDamage();
                    if (newDamage >= tool.getMaxDamage()) {
                        setToolStack(ItemStack.EMPTY);
                    } else {
                        tool.setDamageValue(newDamage);
                    }
                }
            }
        }

        markDirty();
        markForSync();
    }

    public int getMaxDuration() {
        if (cachedRecipe != null) {
            return cachedRecipe.getDuration();
        }
        return this.maxDuration > 0 ? this.maxDuration : 100;
    }

    @Nullable
    public CookingApplianceRecipe getCachedRecipe() {
        return cachedRecipe;
    }

    // ============== Item Slot Accessors =============

    public ItemStack getItemInputStack(int index) {
        if (index >= 0 && index < getItemInputCount()) {
            return inventory.getItemHandler().getStackInSlot(index);
        }
        return ItemStack.EMPTY;
    }

    public void setItemInputStack(int index, ItemStack stack) {
        if (index >= 0 && index < getItemInputCount()) {
            inventory.getItemHandler().setStackInSlot(index, stack);
            markForSync();
        }
    }

    public ItemStack getCatalystStack() {
        return inventory.getItemHandler().getStackInSlot(getCatalystSlot());
    }

    public void setCatalystStack(ItemStack stack) {
        inventory.getItemHandler().setStackInSlot(getCatalystSlot(), stack);
        markForSync();
    }

    public ItemStack getToolStack() {
        return inventory.getItemHandler().getStackInSlot(getToolSlot());
    }

    public void setToolStack(ItemStack stack) {
        inventory.getItemHandler().setStackInSlot(getToolSlot(), stack);
        markForSync();
    }

    public ItemStack getItemOutputStack(int index) {
        if (index >= 0 && index < getItemOutputCount()) {
            return inventory.getItemHandler().getStackInSlot(getItemOutputStartSlot() + index);
        }
        return ItemStack.EMPTY;
    }

    public void setItemOutputStack(int index, ItemStack stack) {
        if (index >= 0 && index < getItemOutputCount()) {
            inventory.getItemHandler().setStackInSlot(getItemOutputStartSlot() + index, stack);
            markForSync();
        }
    }

    // ============== Fluid Tank Accessors =============

    public FluidStack getFluidInputTank(int index) {
        if (index >= 0 && index < getFluidInputCount()) {
            return inventory.getTankArray()[index].getFluid();
        }
        return FluidStack.EMPTY;
    }

    public void setFluidInputTank(int index, FluidStack stack) {
        if (index >= 0 && index < getFluidInputCount()) {
            inventory.getTankArray()[index].setFluid(stack);
            markForSync();
        }
    }

    public FluidStack getFluidOutputTank(int index) {
        if (index >= 0 && index < getFluidOutputCount()) {
            return inventory.getTankArray()[getFluidOutputStartTank() + index].getFluid();
        }
        return FluidStack.EMPTY;
    }

    public void setFluidOutputTank(int index, FluidStack stack) {
        if (index >= 0 && index < getFluidOutputCount()) {
            inventory.getTankArray()[getFluidOutputStartTank() + index].setFluid(stack);
            markForSync();
        }
    }

    // Fluid transfer helper.
    public static boolean depositFluid(
            ItemStack heldItem,
            CookingApplianceBlockEntity appliance,
            Player player,
            InteractionHand hand,
            int targetTankIndex) {
        return interactFluid(heldItem, appliance, player, targetTankIndex, false, stack -> player.setItemInHand(hand, stack));
    }

    public static boolean interactFluid(
            ItemStack heldItem,
            CookingApplianceBlockEntity appliance,
            Player player,
            int targetTankIndex,
            boolean targetIsOutput,
            Consumer<ItemStack> containerSetter) {
        if (heldItem.isEmpty() || appliance == null) {
            return false;
        }

        ItemStack singleHeld = heldItem.copyWithCount(1);
        LazyOptional<IFluidHandlerItem> itemCap = singleHeld.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
        if (!itemCap.isPresent()) {
            return false;
        }

        IFluidHandlerItem fluidHandler = itemCap.resolve().orElse(null);
        if (fluidHandler == null) {
            return false;
        }

        FluidStack availableInItem = fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);

        // If held item has fluid, try to deposit into an input tank.
        if (!availableInItem.isEmpty() && (!targetIsOutput || targetTankIndex < 0)) {
            boolean isBucket = heldItem.getItem() instanceof BucketItem || singleHeld.getItem() == Items.BUCKET || availableInItem.getAmount() >= FluidType.BUCKET_VOLUME;
            int requiredSpace = (isBucket && heldItem.getCount() == 1) ? Math.min(FluidType.BUCKET_VOLUME, availableInItem.getAmount()) : 1;

            // Find the best tank to deposit into.
            int chosenTank = -1;
            if (targetTankIndex >= 0) {
                // Handling fluids in the screen class allows overflow into next tanks.
                for (int i = targetTankIndex; i < appliance.getFluidInputCount(); i++) {
                    FluidStack inTank = appliance.getFluidInputTank(i);
                    int capacity = appliance.getFluidInputCapacity();
                    int space = capacity - inTank.getAmount();
                    if (inTank.isEmpty() || (inTank.isFluidEqual(availableInItem) && space >= requiredSpace)) {
                        chosenTank = i;
                        break;
                    }
                }
            } else {
                // In-world interaction only tries to fill 1 input tank per fluid type.
                for (int i = 0; i < appliance.getFluidInputCount(); i++) {
                    FluidStack inTank = appliance.getFluidInputTank(i);
                    if (!inTank.isEmpty() && inTank.isFluidEqual(availableInItem)) {
                        int capacity = appliance.getFluidInputCapacity();
                        int space = capacity - inTank.getAmount();
                        if (space >= requiredSpace) {
                            chosenTank = i;
                        }
                        // Do not look for another tank even if full.
                        break;
                    }
                }
                // If no tank has this fluid, find first empty tank.
                if (chosenTank == -1) {
                    for (int i = 0; i < appliance.getFluidInputCount(); i++) {
                        FluidStack inTank = appliance.getFluidInputTank(i);
                        if (inTank.isEmpty()) {
                            int capacity = appliance.getFluidInputCapacity();
                            if (capacity >= requiredSpace) {
                                chosenTank = i;
                                break;
                            }
                        }
                    }
                }
            }

            // Deposit into chosen tank.
            if (chosenTank != -1) {
                FluidStack inTank = appliance.getFluidInputTank(chosenTank);
                int space = appliance.getFluidInputCapacity() - inTank.getAmount();

                if (!isBucket || space >= FluidType.BUCKET_VOLUME || heldItem.getCount() > 1) {
                    int toDrain = Math.min(space, availableInItem.getAmount());
                    if (toDrain > 0) {
                        boolean isCreative = player.isCreative();
                        // Simulate fluid draining if in creative to not actually drain the held item.
                        IFluidHandler.FluidAction action = isCreative ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
                        FluidStack drained = fluidHandler.drain(toDrain, action);

                        if (!drained.isEmpty()) {
                            if (inTank.isEmpty()) {
                                appliance.setFluidInputTank(chosenTank, drained.copy());
                            } else {
                                inTank.grow(drained.getAmount());
                                appliance.setFluidInputTank(chosenTank, inTank);
                            }

                            // Only update the held container if not in creative mode.
                            if (!isCreative) {
                                ItemStack containerStack = fluidHandler.getContainer();
                                updateContainer(heldItem, containerStack, player, containerSetter);
                            }

                            Level level = appliance.getLevel();
                            if (level != null) {
                                SoundEvent sound = drained.getFluid().getFluidType().getSound(drained, SoundActions.BUCKET_EMPTY);
                                if (sound == null)
                                    sound = SoundEvents.BUCKET_EMPTY;
                                level.playSound(null, appliance.getBlockPos(), sound, SoundSource.BLOCKS, 1.0f, 1.0f);
                            }

                            appliance.markDirty();
                            appliance.markForSync();
                            return true;
                        }
                    }
                }
            }
        }

        // If held item can be filled, try to pick up fluid from target tank or output/input tanks.
        record TankRef(boolean isOutput, int index) {
        }
        List<TankRef> tanksToCheck = new ArrayList<>();
        if (targetTankIndex >= 0) {
            if (targetIsOutput && targetTankIndex < appliance.getFluidOutputCount()) {
                tanksToCheck.add(new TankRef(true, targetTankIndex));
            } else if (!targetIsOutput && targetTankIndex < appliance.getFluidInputCount()) {
                tanksToCheck.add(new TankRef(false, targetTankIndex));
            }
        } else {
            // Check output tanks first, then input tanks
            for (int i = 0; i < appliance.getFluidOutputCount(); i++) {
                tanksToCheck.add(new TankRef(true, i));
            }
            for (int i = 0; i < appliance.getFluidInputCount(); i++) {
                tanksToCheck.add(new TankRef(false, i));
            }
        }

        for (TankRef ref : tanksToCheck) {
            FluidStack inTank = ref.isOutput ? appliance.getFluidOutputTank(ref.index) : appliance.getFluidInputTank(ref.index);
            if (inTank.isEmpty()) {
                continue;
            }

            int fillable = fluidHandler.fill(inTank, IFluidHandler.FluidAction.SIMULATE);
            if (fillable <= 0) {
                continue;
            }

            boolean isBucket = heldItem.getItem() instanceof BucketItem || singleHeld.getItem() == Items.BUCKET;
            if (isBucket && (inTank.getAmount() < FluidType.BUCKET_VOLUME || fillable < FluidType.BUCKET_VOLUME)) {
                continue;
            }

            int toFill = Math.min(inTank.getAmount(), fillable);
            int filled = fluidHandler.fill(new FluidStack(inTank, toFill), IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                inTank.shrink(filled);
                if (ref.isOutput) {
                    appliance.setFluidOutputTank(ref.index, inTank);
                } else {
                    appliance.setFluidInputTank(ref.index, inTank);
                }

                ItemStack containerStack = fluidHandler.getContainer();
                updateContainer(heldItem, containerStack, player, containerSetter);

                Level level = appliance.getLevel();
                if (level != null) {
                    SoundEvent sound = inTank.getFluid().getFluidType().getSound(inTank, SoundActions.BUCKET_FILL);
                    if (sound == null)
                        sound = SoundEvents.BUCKET_FILL;
                    level.playSound(null, appliance.getBlockPos(), sound, SoundSource.BLOCKS, 1.0f, 1.0f);
                }

                appliance.markDirty();
                appliance.markForSync();
                return true;
            }
        }

        return false;
    }

    private static void updateContainer(ItemStack heldItem, ItemStack containerStack, Player player, Consumer<ItemStack> containerSetter) {
        if (containerSetter != null) {
            if (heldItem.getCount() == 1) {
                containerSetter.accept(containerStack);
            } else {
                heldItem.shrink(1);
                containerSetter.accept(heldItem);
                if (!containerStack.isEmpty() && !player.getInventory().add(containerStack)) {
                    player.drop(containerStack, false);
                }
            }
        } else {
            if (heldItem.getCount() > 1) {
                heldItem.shrink(1);
                if (!containerStack.isEmpty() && !player.getInventory().add(containerStack)) {
                    player.drop(containerStack, false);
                }
            }
        }
    }

    @Override
    public float getTemperature() {
        return this.temperature;
    }

    @Override
    public void setTemperature(float temperature) {
        this.targetTemperature = temperature;
        this.targetTemperatureStabilityTicks = TARGET_TEMPERATURE_STABILITY_TICKS;
        markForSync();
    }

    @Override
    public void setTemperatureIfWarmer(float temperature) {
        if (temperature >= this.temperature) {
            this.targetTemperature = temperature;
            this.targetTemperatureStabilityTicks = TARGET_TEMPERATURE_STABILITY_TICKS;
            markForSync();
        }
    }

    public void resetHeat() {
        this.temperature = 0.0f;
        this.targetTemperature = 0.0f;
        this.targetTemperatureStabilityTicks = 0;
        markForSync();
    }

    @Override
    public void onCalendarUpdate(long ticks) {
        // Support decay updates over long skips.
    }

    @Override
    public long getLastCalendarUpdateTick() {
        return lastCalendarUpdateTick;
    }

    @Override
    public void setLastCalendarUpdateTick(long tick) {
        this.lastCalendarUpdateTick = tick;
    }

    @Override
    public void loadAdditional(CompoundTag nbt) {
        this.temperature = nbt.getFloat("temperature");
        this.targetTemperature = nbt.getFloat("targetTemperature");
        this.targetTemperatureStabilityTicks = nbt.getInt("targetTemperatureStabilityTicks");
        this.cookingTicks = nbt.getInt("cookingTicks");
        this.recipeError = nbt.getInt("recipeError");
        this.maxDuration = nbt.contains("maxDuration") ? nbt.getInt("maxDuration") : 100;
        this.lastCalendarUpdateTick = nbt.getLong("lastCalendarUpdateTick");
        super.loadAdditional(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.putFloat("temperature", this.temperature);
        nbt.putFloat("targetTemperature", this.targetTemperature);
        nbt.putInt("targetTemperatureStabilityTicks", this.targetTemperatureStabilityTicks);
        nbt.putInt("cookingTicks", this.cookingTicks);
        nbt.putInt("recipeError", this.recipeError);
        nbt.putInt("maxDuration", this.maxDuration);
        nbt.putLong("lastCalendarUpdateTick", this.lastCalendarUpdateTick);
        super.saveAdditional(nbt);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.FLUID) {
            return sidedFluidInventory.getSidedHandler(side).cast();
        }
        if (cap == HeatCapability.BLOCK_CAPABILITY) {
            return sidedHeat.getSidedHandler(side).cast();
        }
        return super.getCapability(cap, side);
    }

    public static class CookingApplianceInventory implements DelegateItemHandler, DelegateFluidHandler, INBTSerializable<CompoundTag>, FluidTankCallback, ISlotCallback {

        private final CookingApplianceBlockEntity appliance;
        private final InventoryItemHandler itemHandler;
        private final InventoryFluidTank[] tanks;

        public CookingApplianceInventory(InventoryBlockEntity<?> entity) {
            this.appliance = (CookingApplianceBlockEntity) entity;
            this.itemHandler = new InventoryItemHandler(this, this.appliance.getTotalItemSlots());

            int totalTanks = this.appliance.getTotalFluidTanks();
            this.tanks = new InventoryFluidTank[totalTanks];

            for (int i = 0; i < this.appliance.getFluidInputCount(); i++) {
                this.tanks[i] = new InventoryFluidTank(this.appliance.getFluidInputCapacity(), fluid -> true, this);
            }

            for (int i = 0; i < this.appliance.getFluidOutputCount(); i++) {
                int tankIndex = this.appliance.getFluidOutputStartTank() + i;
                this.tanks[tankIndex] = new InventoryFluidTank(this.appliance.getFluidOutputCapacity(), fluid -> false, this);
            }
        }

        public InventoryFluidTank[] getTankArray() {
            return tanks;
        }

        @Override
        public @NotNull IItemHandlerModifiable getItemHandler() {
            return itemHandler;
        }

        @Override
        public @NotNull IFluidHandler getFluidHandler() {
            return new IFluidHandler() {
                @Override
                public int getTanks() {
                    return tanks.length;
                }

                @NotNull
                @Override
                public FluidStack getFluidInTank(int tank) {
                    return tank >= 0 && tank < tanks.length ? tanks[tank].getFluid() : FluidStack.EMPTY;
                }

                @Override
                public int getTankCapacity(int tank) {
                    return tank >= 0 && tank < tanks.length ? tanks[tank].getCapacity() : 0;
                }

                @Override
                public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                    return tank >= 0 && tank < appliance.getFluidInputCount();
                }

                @Override
                public int fill(FluidStack resource, FluidAction action) {
                    if (resource.isEmpty())
                        return 0;
                    // Check if any input tank already contains this fluid.
                    for (int i = 0; i < appliance.getFluidInputCount(); i++) {
                        if (!tanks[i].getFluid().isEmpty() && tanks[i].getFluid().isFluidEqual(resource)) {
                            // Only fill this matching tank. Do not overflow into other tanks.
                            return tanks[i].fill(resource, action);
                        }
                    }
                    // If no tank has this fluid, fill the first empty tank.
                    for (int i = 0; i < appliance.getFluidInputCount(); i++) {
                        if (tanks[i].getFluid().isEmpty()) {
                            return tanks[i].fill(resource, action);
                        }
                    }
                    return 0;
                }

                @NotNull
                @Override
                public FluidStack drain(FluidStack resource, FluidAction action) {
                    if (resource.isEmpty())
                        return FluidStack.EMPTY;
                    for (int i = appliance.getFluidOutputStartTank(); i < tanks.length; i++) {
                        FluidStack drained = tanks[i].drain(resource, action);
                        if (!drained.isEmpty()) {
                            return drained;
                        }
                    }
                    return FluidStack.EMPTY;
                }

                @NotNull
                @Override
                public FluidStack drain(int maxDrain, FluidAction action) {
                    if (maxDrain <= 0)
                        return FluidStack.EMPTY;
                    for (int i = appliance.getFluidOutputStartTank(); i < tanks.length; i++) {
                        FluidStack drained = tanks[i].drain(maxDrain, action);
                        if (!drained.isEmpty()) {
                            return drained;
                        }
                    }
                    return FluidStack.EMPTY;
                }
            };
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == appliance.getCatalystSlot()) {
                return stack.is(TFGTags.Items.APPLIANCE_CATALYSTS);
            }
            if (slot == appliance.getToolSlot()) {
                return stack.is(TFGTags.Items.APPLIANCE_TOOLS);
            }
            return slot < appliance.getItemOutputStartSlot();
        }

        @Override
        public void setAndUpdateSlots(int slot) {
            appliance.markDirty();
            appliance.markForSync();
        }

        @Override
        public void fluidTankChanged() {
            appliance.markDirty();
            appliance.markForSync();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("inventory", itemHandler.serializeNBT());
            ListTag tanksTag = new ListTag();
            for (InventoryFluidTank tank : tanks) {
                tanksTag.add(tank.writeToNBT(new CompoundTag()));
            }
            nbt.put("tanks", tanksTag);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (nbt.contains("inventory", Tag.TAG_COMPOUND)) {
                itemHandler.deserializeNBT(nbt.getCompound("inventory"));
            }
            if (nbt.contains("tanks", Tag.TAG_LIST)) {
                ListTag tanksTag = nbt.getList("tanks", Tag.TAG_COMPOUND);
                for (int i = 0; i < Math.min(tanks.length, tanksTag.size()); i++) {
                    tanks[i].readFromNBT(tanksTag.getCompound(i));
                }
            }
        }
    }
}
