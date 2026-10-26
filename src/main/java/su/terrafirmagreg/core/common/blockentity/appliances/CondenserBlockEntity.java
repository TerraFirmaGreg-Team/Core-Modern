package su.terrafirmagreg.core.common.blockentity.appliances;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.client.particle.FluidParticleOption;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.FluidTankCallback;
import net.dries007.tfc.common.capabilities.InventoryFluidTank;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import lombok.Getter;

import su.terrafirmagreg.core.common.block.appliances.CondenserBlock;
import su.terrafirmagreg.core.common.data.TFGTags;

public class CondenserBlockEntity extends TFCBlockEntity implements FluidTankCallback {
    public static final int CAPACITY = 4000;

    @Getter
    protected final InventoryFluidTank coolantTank;
    @Getter
    protected final InventoryFluidTank outputTank;
    protected final LazyOptional<IFluidHandler> fluidCapability;

    public CondenserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.coolantTank = new InventoryFluidTank(CAPACITY, CondenserBlockEntity::isCoolant, this);
        this.outputTank = new InventoryFluidTank(CAPACITY, stack -> false, this);
        this.fluidCapability = LazyOptional.of(SidedFluidHandler::new);
    }

    public static boolean isCoolant(FluidStack stack) {
        return !stack.isEmpty() && isCoolant(stack.getFluid());
    }

    @SuppressWarnings("deprecation")
    public static boolean isCoolant(Fluid fluid) {
        // TODO: water here is for testing!!! dont forget to remove it
        return fluid != null && fluid != Fluids.EMPTY && (fluid.is(TFGTags.Fluids.CONDENSER_COOLANTS) || fluid.is(FluidTags.WATER));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CondenserBlockEntity condenser) {
        condenser.tickOutputs(level, pos, state);
    }

    protected void tickOutputs(Level level, BlockPos pos, BlockState state) {
        if (outputTank.isEmpty()) {
            return;
        }

        Direction facing = state.getValue(CondenserBlock.FACING).getOpposite();
        BlockPos facingPos = pos.relative(facing);
        BlockEntity facingBE = level.getBlockEntity(facingPos);

        if (facingBE != null) {
            LazyOptional<IFluidHandler> cap = facingBE.getCapability(Capabilities.FLUID, facing.getOpposite());
            IFluidHandler target = cap.resolve().orElse(null);
            if (target != null) {
                FluidStack transferred = FluidUtil.tryFluidTransfer(target, outputTank, outputTank.getFluidAmount(), true);
                if (!transferred.isEmpty()) {
                    markDirty();
                    markForSync();
                }
                return;
            }
        }

        if (level.getBlockState(facingPos).isAir()) {
            tickPouring(level, pos, facingPos, facing);
        }
    }

    protected void tickPouring(Level level, BlockPos pos, BlockPos facingPos, Direction facing) {
        BlockPos pourPos = facingPos.below();
        BlockEntity belowBE = level.getBlockEntity(pourPos);
        boolean transferred = false;
        Fluid fluid = outputTank.getFluid().getFluid();

        if (belowBE != null) {
            LazyOptional<IFluidHandler> belowCap = belowBE.getCapability(Capabilities.FLUID, Direction.UP);
            IFluidHandler target = belowCap.resolve().orElse(null);
            if (target != null && FluidHelpers.couldTransferExact(outputTank, target, 1)) {
                FluidHelpers.transferExact(outputTank, target, 1);
                transferred = true;
            }
        }

        if (!transferred) {
            FluidStack drained = outputTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
            if (!drained.isEmpty()) {
                transferred = true;
            }
        }

        if (transferred) {
            markDirty();
            markForSync();

            if (level.getGameTime() % 12 == 0 && level instanceof ServerLevel server) {
                final double offset = 0.8;
                final double dx = facing.getStepX() > 0 ? offset : facing.getStepX() < 0 ? -offset : 0;
                final double dz = facing.getStepZ() > 0 ? offset : facing.getStepZ() < 0 ? -offset : 0;
                final double x = pos.getX() + 0.5f + dx;
                final double y = pos.getY() + 0.25f;
                final double z = pos.getZ() + 0.5f + dz;

                var rand = level.getRandom();
                level.playSound(null, pos, TFCSounds.BARREL_DRIP.get(), SoundSource.BLOCKS, 0.4F + rand.nextFloat(), rand.nextFloat() + 0.7F + 0.3F);
                server.sendParticles(new FluidParticleOption(TFCParticles.BARREL_DRIP.get(), fluid), x, y, z, 1, 0, 0, 0, 1f);
            }
        }
    }

    public boolean interactWithItem(ItemStack heldItem, Player player, InteractionHand hand) {
        if (heldItem.isEmpty() || level == null) {
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

        // Try to deposit coolant.
        if (!availableInItem.isEmpty() && isCoolant(availableInItem)) {
            FluidStack inTank = coolantTank.getFluid();
            int capacity = coolantTank.getCapacity();
            int space = capacity - inTank.getAmount();

            boolean isBucket = heldItem.getItem() instanceof BucketItem || singleHeld.getItem() == Items.BUCKET || availableInItem.getAmount() >= FluidType.BUCKET_VOLUME;
            int requiredSpace = (isBucket && heldItem.getCount() == 1) ? Math.min(FluidType.BUCKET_VOLUME, availableInItem.getAmount()) : 1;

            if (inTank.isEmpty() || (inTank.isFluidEqual(availableInItem) && space >= requiredSpace)) {
                if (!isBucket || space >= FluidType.BUCKET_VOLUME || heldItem.getCount() > 1) {
                    int toDrain = Math.min(space, availableInItem.getAmount());
                    if (toDrain > 0) {
                        boolean isCreative = player.isCreative();
                        IFluidHandler.FluidAction action = isCreative ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
                        FluidStack drained = fluidHandler.drain(toDrain, action);

                        if (!drained.isEmpty()) {
                            if (inTank.isEmpty()) {
                                coolantTank.setFluid(drained.copy());
                            } else {
                                inTank.grow(drained.getAmount());
                                coolantTank.setFluid(inTank);
                            }

                            if (!isCreative) {
                                ItemStack containerStack = fluidHandler.getContainer();
                                updateContainer(heldItem, containerStack, player, stack -> player.setItemInHand(hand, stack));
                            }

                            SoundEvent sound = drained.getFluid().getFluidType().getSound(drained, SoundActions.BUCKET_EMPTY);
                            if (sound == null) {
                                sound = SoundEvents.BUCKET_EMPTY;
                            }
                            level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 1.0f, 1.0f);

                            markDirty();
                            markForSync();
                            return true;
                        }
                    }
                }
            }
        }

        // Try to withdraw coolant or output.
        List<InventoryFluidTank> tanks = List.of(coolantTank, outputTank);
        for (InventoryFluidTank tank : tanks) {
            FluidStack inTank = tank.getFluid();
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
                tank.setFluid(inTank);

                ItemStack containerStack = fluidHandler.getContainer();
                updateContainer(heldItem, containerStack, player, stack -> player.setItemInHand(hand, stack));

                SoundEvent sound = inTank.getFluid().getFluidType().getSound(inTank, SoundActions.BUCKET_FILL);
                if (sound == null) {
                    sound = SoundEvents.BUCKET_FILL;
                }
                level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 1.0f, 1.0f);

                markDirty();
                markForSync();
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
    public void fluidTankChanged() {
        markDirty();
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
    public void loadAdditional(@NotNull CompoundTag nbt) {
        super.loadAdditional(nbt);
        if (nbt.contains("coolantTank")) {
            coolantTank.readFromNBT(nbt.getCompound("coolantTank"));
        } else if (nbt.contains("tank")) {
            coolantTank.readFromNBT(nbt.getCompound("tank"));
        }
        if (nbt.contains("outputTank")) {
            outputTank.readFromNBT(nbt.getCompound("outputTank"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.put("coolantTank", coolantTank.writeToNBT(new CompoundTag()));
        nbt.put("outputTank", outputTank.writeToNBT(new CompoundTag()));
        super.saveAdditional(nbt);
    }

    protected class SidedFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 2;
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return switch (tank) {
                case 0 -> coolantTank.getFluid();
                case 1 -> outputTank.getFluid();
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return switch (tank) {
                case 0 -> coolantTank.getCapacity();
                case 1 -> outputTank.getCapacity();
                default -> 0;
            };
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == 0 && isCoolant(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !isCoolant(resource)) {
                return 0;
            }
            return coolantTank.fill(resource, action);
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            return outputTank.drain(resource, action);
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) {
                return FluidStack.EMPTY;
            }
            return outputTank.drain(maxDrain, action);
        }
    }
}
