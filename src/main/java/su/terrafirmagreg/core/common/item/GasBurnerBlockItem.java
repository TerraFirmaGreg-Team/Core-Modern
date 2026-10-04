package su.terrafirmagreg.core.common.item;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import su.terrafirmagreg.core.client.util.TFGTooltipUtils;
import su.terrafirmagreg.core.common.blockentity.GasBurnerBlockEntity;
import su.terrafirmagreg.core.config.TFGConfig;

@SuppressWarnings("NoTranslation")
public class GasBurnerBlockItem extends BlockItem {

    public GasBurnerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new GasBurnerItemStackFluidHandler(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = stack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
        if (tag != null && tag.contains("tank", Tag.TAG_COMPOUND)) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("tank"));
            if (!fluid.isEmpty()) {
                tooltip.add(Tooltips.fluidUnitsOf(fluid));
            }
        }

        if (level != null) {
            if (Screen.hasShiftDown()) {
                tooltip.addAll(TFGTooltipUtils.normalize(Component.translatable("tfg.tooltip.gas_burner.flavor_text")));
            } else {
                tooltip.add(Component.translatable("tfg.tooltip.shift_hint").withStyle(ChatFormatting.GOLD));
            }
        }
    }

    private static class GasBurnerItemStackFluidHandler implements ICapabilityProvider, IFluidHandlerItem {
        private final LazyOptional<IFluidHandlerItem> capability;
        private final ItemStack stack;

        GasBurnerItemStackFluidHandler(ItemStack stack) {
            this.capability = LazyOptional.of(() -> this);
            this.stack = stack;
        }

        private FluidTank getTank() {
            FluidTank tank = new FluidTank(TFGConfig.SERVER.gasBurnerCapacity.get(), GasBurnerBlockEntity::isValidFluid);
            CompoundTag tag = stack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
            if (tag != null && tag.contains("tank", Tag.TAG_COMPOUND)) {
                tank.readFromNBT(tag.getCompound("tank"));
            }
            return tank;
        }

        private void saveTank(FluidTank tank) {
            if (tank.isEmpty()) {
                CompoundTag tag = stack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
                if (tag != null) {
                    tag.remove("tank");
                    if (tag.isEmpty()) {
                        stack.removeTagKey(Helpers.BLOCK_ENTITY_TAG);
                    }
                }
            } else {
                CompoundTag beTag = stack.getOrCreateTagElement(Helpers.BLOCK_ENTITY_TAG);
                beTag.put("tank", tank.writeToNBT(new CompoundTag()));
            }
        }

        @NotNull
        @Override
        public ItemStack getContainer() {
            return stack;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return getTank().getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return TFGConfig.SERVER.gasBurnerCapacity.get();
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack fluidStack) {
            return GasBurnerBlockEntity.isValidFluid(fluidStack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (stack.getCount() != 1 || resource.isEmpty() || !isFluidValid(0, resource)) {
                return 0;
            }
            FluidTank tank = getTank();
            int filled = tank.fill(resource, action);
            if (action.execute() && filled > 0) {
                saveTank(tank);
            }
            return filled;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (stack.getCount() != 1 || resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            FluidTank tank = getTank();
            FluidStack drained = tank.drain(resource, action);
            if (action.execute() && !drained.isEmpty()) {
                saveTank(tank);
            }
            return drained;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (stack.getCount() != 1 || maxDrain <= 0) {
                return FluidStack.EMPTY;
            }
            FluidTank tank = getTank();
            FluidStack drained = tank.drain(maxDrain, action);
            if (action.execute() && !drained.isEmpty()) {
                saveTank(tank);
            }
            return drained;
        }

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (stack.getCount() != 1) {
                return LazyOptional.empty();
            }
            if (cap == Capabilities.FLUID || cap == Capabilities.FLUID_ITEM || cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
                return capability.cast();
            }
            return LazyOptional.empty();
        }
    }
}
