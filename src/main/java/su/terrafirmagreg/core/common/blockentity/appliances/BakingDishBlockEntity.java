package su.terrafirmagreg.core.common.blockentity.appliances;

import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
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
import net.dries007.tfc.common.capabilities.food.FoodTraits;
import net.dries007.tfc.common.capabilities.size.ItemSizeManager;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.common.container.ISlotCallback;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

import lombok.Getter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.appliances.BakingDishBlock;
import su.terrafirmagreg.core.common.container.BakingDishContainer;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class BakingDishBlockEntity extends InventoryBlockEntity<BakingDishBlockEntity.BakingDishInventory> {

    private static final Component NAME = Component.translatable(TFGCore.MOD_ID + ".block_entity.baking_dish");
    public static final int ITEM_SLOTS = 6;
    public static final int FLUID_SLOTS = 1;
    public static final int FLUID_CAPACITY = 1000;

    @Getter
    public final int slots = ITEM_SLOTS;
    @Getter
    public final int fluidCapacity = FLUID_CAPACITY;

    protected final SidedHandler.Builder<IFluidHandler> sidedFluidInventory;

    public BakingDishBlockEntity(BlockPos pos, BlockState state) {
        this(TFGBlockEntities.BAKING_DISH.get(), pos, state);
    }

    public BakingDishBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, BakingDishInventory::new, NAME);

        int[] itemSlots = IntStream.range(0, ITEM_SLOTS).toArray();
        sidedInventory
                .on(new PartialItemHandler(inventory).insert(itemSlots).extract(itemSlots), Direction.Plane.HORIZONTAL)
                .on(new PartialItemHandler(inventory).insert(itemSlots), Direction.UP)
                .on(new PartialItemHandler(inventory).extract(itemSlots), Direction.DOWN);

        this.sidedFluidInventory = new SidedHandler.Builder<>(inventory);
        this.sidedFluidInventory
                .on(new PartialFluidHandler(inventory).insert().extract(), Direction.Plane.HORIZONTAL)
                .on(new PartialFluidHandler(inventory).insert(), Direction.UP)
                .on(new PartialFluidHandler(inventory).extract(), Direction.DOWN);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, @NotNull Inventory inv, @NotNull Player player) {
        return BakingDishContainer.create(this, inv, windowID);
    }

    public FluidStack getFluidTank() {
        return inventory.getTank().getFluid();
    }

    public void setFluidTank(FluidStack stack) {
        inventory.getTank().setFluid(stack);
        markForSync();
    }

    public void onUnseal() {
        assert level != null;
        for (int i = 0; i < ITEM_SLOTS; i++) {
            inventory.getItemHandler().setStackInSlot(i, FoodCapability.removeTrait(inventory.getItemHandler().getStackInSlot(i).copy(), FoodTraits.PRESERVED));
        }
        Helpers.playSound(level, worldPosition, TFCSounds.OPEN_VESSEL.get());
    }

    public void onSeal() {
        assert level != null;
        for (int i = 0; i < ITEM_SLOTS; i++) {
            inventory.getItemHandler().setStackInSlot(i, FoodCapability.applyTrait(inventory.getItemHandler().getStackInSlot(i).copy(), FoodTraits.PRESERVED));
        }
        Helpers.playSound(level, worldPosition, TFCSounds.CLOSE_VESSEL.get());
    }

    public static boolean depositFluid(
            ItemStack heldItem,
            BakingDishBlockEntity appliance,
            Player player,
            InteractionHand hand) {
        return interactFluid(heldItem, appliance, player, stack -> player.setItemInHand(hand, stack));
    }

    public static boolean interactFluid(
            ItemStack heldItem,
            BakingDishBlockEntity appliance,
            Player player,
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

        // If held item has fluid, try to deposit into the tank.
        if (!availableInItem.isEmpty()) {
            boolean isBucket = heldItem.getItem() instanceof BucketItem || singleHeld.getItem() == Items.BUCKET || availableInItem.getAmount() >= FluidType.BUCKET_VOLUME;
            int requiredSpace = (isBucket && heldItem.getCount() == 1) ? Math.min(FluidType.BUCKET_VOLUME, availableInItem.getAmount()) : 1;

            FluidStack inTank = appliance.getFluidTank();
            int capacity = appliance.getFluidCapacity();
            int space = capacity - inTank.getAmount();

            if (inTank.isEmpty() || (inTank.isFluidEqual(availableInItem) && space >= requiredSpace)) {
                if (!isBucket || space >= FluidType.BUCKET_VOLUME || heldItem.getCount() > 1) {
                    int toDrain = Math.min(space, availableInItem.getAmount());
                    if (toDrain > 0) {
                        boolean isCreative = player.isCreative();
                        IFluidHandler.FluidAction action = isCreative ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
                        FluidStack drained = fluidHandler.drain(toDrain, action);

                        if (!drained.isEmpty()) {
                            if (inTank.isEmpty()) {
                                appliance.setFluidTank(drained.copy());
                            } else {
                                inTank.grow(drained.getAmount());
                                appliance.setFluidTank(inTank);
                            }

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

        // If held item can be filled, try to pick up fluid from the tank.
        FluidStack inTank = appliance.getFluidTank();
        if (!inTank.isEmpty()) {
            int fillable = fluidHandler.fill(inTank, IFluidHandler.FluidAction.SIMULATE);
            if (fillable > 0) {
                boolean isBucket = heldItem.getItem() instanceof BucketItem || singleHeld.getItem() == Items.BUCKET;
                if (!isBucket || (inTank.getAmount() >= FluidType.BUCKET_VOLUME && fillable >= FluidType.BUCKET_VOLUME)) {
                    int toFill = Math.min(inTank.getAmount(), fillable);
                    int filled = fluidHandler.fill(new FluidStack(inTank, toFill), IFluidHandler.FluidAction.EXECUTE);
                    if (filled > 0) {
                        inTank.shrink(filled);
                        appliance.setFluidTank(inTank);

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

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.ITEM || cap == ForgeCapabilities.ITEM_HANDLER) {
            return sidedInventory.getSidedHandler(side).cast();
        }
        if (cap == Capabilities.FLUID || cap == ForgeCapabilities.FLUID_HANDLER) {
            return sidedFluidInventory.getSidedHandler(side).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        sidedInventory.invalidate();
        sidedFluidInventory.invalidate();
    }

    public static class BakingDishInventory implements DelegateItemHandler, DelegateFluidHandler, INBTSerializable<CompoundTag>, FluidTankCallback, ISlotCallback {
        private final BakingDishBlockEntity bakingDishBlockEntity;
        private final InventoryItemHandler itemHandler;
        @Getter
        private final InventoryFluidTank tank;

        BakingDishInventory(InventoryBlockEntity<?> entity) {
            this.bakingDishBlockEntity = (BakingDishBlockEntity) entity;
            this.itemHandler = new InventoryItemHandler(entity, ITEM_SLOTS);
            this.tank = new InventoryFluidTank(FLUID_CAPACITY, fluid -> true, this);
        }

        private boolean canModify() {
            return !bakingDishBlockEntity.getBlockState().getOptionalValue(BakingDishBlock.SEALED).orElse(false);
        }

        @Override
        public @NotNull InventoryItemHandler getItemHandler() {
            return itemHandler;
        }

        @Override
        public @NotNull IFluidHandler getFluidHandler() {
            return tank;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return canModify() ? itemHandler.insertItem(slot, stack, simulate) : stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return canModify() ? itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlots() {
            return itemHandler.getSlots();
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return itemHandler.getStackInSlot(slot);
        }

        @Override
        public int getSlotLimit(int slot) {
            return itemHandler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return canModify() && ItemSizeManager.get(stack).getSize(stack).isSmallerThan(Size.LARGE) && itemHandler.isItemValid(slot, stack);
        }

        @Override
        public int getTanks() {
            return tank.getTanks();
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluidInTank(tankIndex);
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return tank.getTankCapacity(tankIndex);
        }

        @Override
        public boolean isFluidValid(int tankIndex, @NotNull FluidStack stack) {
            return canModify() && tank.isFluidValid(tankIndex, stack);
        }

        @Override
        public int fill(@NotNull FluidStack resource, IFluidHandler.@NotNull FluidAction action) {
            return canModify() ? tank.fill(resource, action) : 0;
        }

        @NotNull
        @Override
        public FluidStack drain(@NotNull FluidStack resource, IFluidHandler.@NotNull FluidAction action) {
            return canModify() ? tank.drain(resource, action) : FluidStack.EMPTY;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, IFluidHandler.@NotNull FluidAction action) {
            return canModify() ? tank.drain(maxDrain, action) : FluidStack.EMPTY;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("inventory", itemHandler.serializeNBT());
            ListTag tanksTag = new ListTag();
            tanksTag.add(tank.writeToNBT(new CompoundTag()));
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
                if (!tanksTag.isEmpty()) {
                    tank.readFromNBT(tanksTag.getCompound(0));
                }
            } else if (nbt.contains("tank", Tag.TAG_COMPOUND)) {
                tank.readFromNBT(nbt.getCompound("tank"));
            }
        }

        @Override
        public void setAndUpdateSlots(int slot) {
            bakingDishBlockEntity.setAndUpdateSlots(slot);
        }

        @Override
        public void fluidTankChanged() {
            bakingDishBlockEntity.markForSync();
        }
    }
}
