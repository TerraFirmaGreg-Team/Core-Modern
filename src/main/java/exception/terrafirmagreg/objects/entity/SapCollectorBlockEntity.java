package exception.terrafirmagreg.objects.entity;

import exception.terrafirmagreg.TerraFirmaGreg;
import exception.terrafirmagreg.objects.recipe.TapExtractRecipe;
import exception.terrafirmagreg.util.ProgressFluidTank;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SapCollectorBlockEntity extends BlockEntity {
    public int progress = 0;
    public TapExtractRecipe currentRecipe;

    private LazyOptional<IItemHandlerModifiable> inventoryHandler = LazyOptional.of(() -> new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // No inserting
            return false;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack out = super.extractItem(slot, amount, simulate);
            if (!simulate && getStackInSlot(slot).isEmpty()) {
                progress = 0;
            }
            return out;
        }
    });

    private LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> new ProgressFluidTank(1000, this));

    public SapCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(TerraFirmaGreg.SAP_COLLECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    public void setCurrentRecipe(TapExtractRecipe recipe) {
        this.currentRecipe = recipe;
        // Reset fluid handler
        if (currentRecipe != null) {
            fluidHandler.invalidate();
            currentRecipe.getResultItem(getBlockState()).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(outHandler -> {
                fluidHandler = LazyOptional.of(() -> new ProgressFluidTank(outHandler.getTankCapacity(0), this));
                if (progress > 0) {
                    fluidHandler.ifPresent(h -> {
                        h.fill(new FluidStack(outHandler.getFluidInTank(0)
                                .getFluid(), (int) (outHandler.getTankCapacity(0) * ((float) progress / (float) currentRecipe.processingTime))), IFluidHandler.FluidAction.EXECUTE);
                    });
                }
            });
        }
    }

    public void addProgress(int progress) {
        if (currentRecipe != null) {
            this.progress += progress;

            var fluidCap = currentRecipe.getResultItem(getBlockState()).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
            if (fluidCap.isPresent()) {
                fluidCap.ifPresent(fluidHandlerItem -> {
                    fluidHandler.ifPresent(h -> {
                        if (h.getFluidInTank(0).isEmpty()) {
                            h.fill(new FluidStack(fluidHandlerItem.getFluidInTank(0), (int) (1000f * ((float) this.progress / (float) currentRecipe.processingTime))), IFluidHandler.FluidAction.EXECUTE);
                        } else {
                            h.getFluidInTank(0).setAmount((int) (1000f * ((float) this.progress / (float) currentRecipe.processingTime)));
                        }
                    });
                });
            } else if (this.progress >= currentRecipe.processingTime) {
                inventoryHandler.ifPresent(h -> {
                    h.setStackInSlot(0, currentRecipe.getResultItem(getBlockState()));
                });
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (currentRecipe != null) {
            if (cap.equals(ForgeCapabilities.FLUID_HANDLER) && currentRecipe.getResultItem(getBlockState())
                    .getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
                return fluidHandler.cast();
            } else if (cap.equals(ForgeCapabilities.ITEM_HANDLER)) {
                return inventoryHandler.cast();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.loadPacketNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.savePacketNBT(tag);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithId();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        this.loadPacketNBT(pkt.getTag());
        if (level instanceof ClientLevel) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 0);
        }
    }

    public void loadPacketNBT(CompoundTag tag) {
        this.progress = tag.getInt("progress");
        if (tag.contains("recipe") && level != null) {
            var recipe = level.getRecipeManager().byKey(new ResourceLocation(tag.getString("recipe")));
            recipe.ifPresent(value -> this.setCurrentRecipe((TapExtractRecipe) value));
        }
    }

    public void savePacketNBT(CompoundTag tag) {
        tag.putInt("progress", progress);
        if (this.currentRecipe != null) {
            tag.putString("recipe", this.currentRecipe.getId().toString());
        }
    }
}
