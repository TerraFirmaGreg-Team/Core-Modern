package su.terrafirmagreg.core.common.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
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
import net.minecraftforge.items.ItemStackHandler;

import su.terrafirmagreg.core.client.util.TFGTooltipUtils;
import su.terrafirmagreg.core.common.blockentity.appliances.CopperPotBlockEntity;

/**
 * Base class for cooking appliance block items.
 * Handles storing inventories on break and managing their tooltips.
 */
@SuppressWarnings("NoTranslation")
public class CookingApplianceBlockItem extends BlockItem {

    public CookingApplianceBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new CookingApplianceItemStackFluidHandler(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag beTag = stack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
        if (beTag != null) {
            CompoundTag invTag = beTag.contains("inventory", Tag.TAG_COMPOUND) ? beTag.getCompound("inventory") : null;

            ListTag tanksTag = null;
            if (invTag != null && invTag.contains("tanks", Tag.TAG_LIST)) {
                tanksTag = invTag.getList("tanks", Tag.TAG_COMPOUND);
            } else if (beTag.contains("tanks", Tag.TAG_LIST)) {
                tanksTag = beTag.getList("tanks", Tag.TAG_COMPOUND);
            }

            if (tanksTag != null) {
                for (int i = 0; i < tanksTag.size(); i++) {
                    CompoundTag tankTag = tanksTag.getCompound(i);
                    FluidStack fluid = FluidStack.loadFluidStackFromNBT(tankTag);
                    if (!fluid.isEmpty()) {
                        tooltip.add(Component.translatable("tfg.tooltip.tank_slot", i + 1).append(": ").append(Tooltips.fluidUnitsOf(fluid)));
                    }
                }
            } else if (beTag.contains("tank", Tag.TAG_COMPOUND)) {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(beTag.getCompound("tank"));
                if (!fluid.isEmpty()) {
                    tooltip.add(Tooltips.fluidUnitsOf(fluid));
                }
            }

            ItemStackHandler itemHandler = getItemHandler(beTag);
            if (!Helpers.isEmpty(itemHandler) && !TFCConfig.CLIENT.displayItemContentsAsImages.get()) {
                tooltip.add(Component.translatable("tfc.tooltip.contents").withStyle(ChatFormatting.DARK_GREEN));
                Helpers.addInventoryTooltipInfo(itemHandler, tooltip);
            }
        }

        if (level != null) {
            if (Screen.hasShiftDown()) {
                tooltip.addAll(TFGTooltipUtils.normalize(Component.translatable("tfg.tooltip.cooking_appliance.flavor_text")));
            } else {
                tooltip.add(Component.translatable("tfg.tooltip.shift_hint").withStyle(ChatFormatting.GOLD));
            }
        }
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        if (TFCConfig.CLIENT.displayItemContentsAsImages.get()) {
            CompoundTag beTag = stack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
            if (beTag != null) {
                ItemStackHandler itemHandler = getItemHandler(beTag);
                if (!Helpers.isEmpty(itemHandler)) {
                    int slots = itemHandler.getSlots();
                    int width = 3;
                    int height = (slots + width - 1) / width;
                    return Helpers.getTooltipImage(itemHandler, width, height, 0, slots - 1);
                }
            }
        }
        return super.getTooltipImage(stack);
    }

    private static ItemStackHandler getItemHandler(CompoundTag beTag) {
        ItemStackHandler itemHandler = new ItemStackHandler();
        if (beTag.contains("inventory", Tag.TAG_COMPOUND)) {
            CompoundTag invTag = beTag.getCompound("inventory");
            if (invTag.contains("inventory", Tag.TAG_COMPOUND)) {
                itemHandler.deserializeNBT(invTag.getCompound("inventory"));
            } else if (invTag.contains("Items", Tag.TAG_LIST)) {
                itemHandler.deserializeNBT(invTag);
            }
        }
        return itemHandler;
    }

    private static class CookingApplianceItemStackFluidHandler implements ICapabilityProvider, IFluidHandlerItem {
        private final LazyOptional<IFluidHandlerItem> capability;
        private final ItemStack stack;

        CookingApplianceItemStackFluidHandler(ItemStack stack) {
            this.capability = LazyOptional.of(() -> this);
            this.stack = stack;
        }

        private List<FluidTank> getAllTanks() {
            List<FluidTank> result = new ArrayList<>();
            CompoundTag beTag = stack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
            ListTag tanksTag = null;
            if (beTag != null) {
                if (beTag.contains("inventory", Tag.TAG_COMPOUND)) {
                    CompoundTag invTag = beTag.getCompound("inventory");
                    if (invTag.contains("tanks", Tag.TAG_LIST)) {
                        tanksTag = invTag.getList("tanks", Tag.TAG_COMPOUND);
                    }
                }
                if (tanksTag == null && beTag.contains("tanks", Tag.TAG_LIST)) {
                    tanksTag = beTag.getList("tanks", Tag.TAG_COMPOUND);
                }
            }

            int inputCount = CopperPotBlockEntity.FLUID_INPUTS;
            int outputCount = CopperPotBlockEntity.FLUID_OUTPUTS;
            int totalTanks = inputCount + outputCount;

            for (int i = 0; i < totalTanks; i++) {
                int capacity = i < inputCount ? CopperPotBlockEntity.FLUID_INPUT_CAPACITY : CopperPotBlockEntity.FLUID_OUTPUT_CAPACITY;
                FluidTank tank = new FluidTank(capacity);
                if (tanksTag != null && i < tanksTag.size()) {
                    tank.readFromNBT(tanksTag.getCompound(i));
                }
                result.add(tank);
            }
            return result;
        }

        private void saveAllTanks(List<FluidTank> tanks) {
            boolean allEmpty = true;
            for (FluidTank tank : tanks) {
                if (!tank.isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }

            CompoundTag beTag = stack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
            if (allEmpty) {
                if (beTag != null) {
                    if (beTag.contains("inventory", Tag.TAG_COMPOUND)) {
                        CompoundTag invTag = beTag.getCompound("inventory");
                        invTag.remove("tanks");
                        if (invTag.isEmpty()) {
                            beTag.remove("inventory");
                        }
                    }
                    beTag.remove("tanks");
                    if (beTag.isEmpty()) {
                        stack.removeTagKey(Helpers.BLOCK_ENTITY_TAG);
                    }
                }
            } else {
                if (beTag == null) {
                    beTag = stack.getOrCreateTagElement(Helpers.BLOCK_ENTITY_TAG);
                }
                CompoundTag invTag;
                if (beTag.contains("inventory", Tag.TAG_COMPOUND)) {
                    invTag = beTag.getCompound("inventory");
                } else {
                    invTag = new CompoundTag();
                    beTag.put("inventory", invTag);
                }
                ListTag tanksTag = new ListTag();
                for (FluidTank tank : tanks) {
                    tanksTag.add(tank.writeToNBT(new CompoundTag()));
                }
                invTag.put("tanks", tanksTag);
                beTag.remove("tanks");
            }
        }

        @NotNull
        @Override
        public ItemStack getContainer() {
            return stack;
        }

        @Override
        public int getTanks() {
            return getAllTanks().size();
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            List<FluidTank> tanks = getAllTanks();
            return tank >= 0 && tank < tanks.size() ? tanks.get(tank).getFluid() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            List<FluidTank> tanks = getAllTanks();
            return tank >= 0 && tank < tanks.size() ? tanks.get(tank).getCapacity() : 8000;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack fluidStack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (stack.getCount() != 1 || resource.isEmpty()) {
                return 0;
            }
            List<FluidTank> tanks = getAllTanks();
            int inputCount = CopperPotBlockEntity.FLUID_INPUTS;
            for (int i = 0; i < Math.min(inputCount, tanks.size()); i++) {
                FluidTank tank = tanks.get(i);
                if (!tank.getFluid().isEmpty() && tank.getFluid().isFluidEqual(resource)) {
                    int filled = tank.fill(resource, action);
                    if (filled > 0) {
                        if (action.execute()) {
                            saveAllTanks(tanks);
                        }
                        return filled;
                    }
                }
            }
            for (int i = 0; i < Math.min(inputCount, tanks.size()); i++) {
                FluidTank tank = tanks.get(i);
                if (tank.getFluid().isEmpty()) {
                    int filled = tank.fill(resource, action);
                    if (filled > 0) {
                        if (action.execute()) {
                            saveAllTanks(tanks);
                        }
                        return filled;
                    }
                }
            }
            return 0;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (stack.getCount() != 1 || resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            List<FluidTank> tanks = getAllTanks();
            int outputStart = CopperPotBlockEntity.FLUID_INPUTS;
            for (int i = outputStart; i < tanks.size(); i++) {
                FluidStack drained = tanks.get(i).drain(resource, action);
                if (!drained.isEmpty()) {
                    if (action.execute()) {
                        saveAllTanks(tanks);
                    }
                    return drained;
                }
            }
            for (int i = 0; i < Math.min(outputStart, tanks.size()); i++) {
                FluidStack drained = tanks.get(i).drain(resource, action);
                if (!drained.isEmpty()) {
                    if (action.execute()) {
                        saveAllTanks(tanks);
                    }
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (stack.getCount() != 1 || maxDrain <= 0) {
                return FluidStack.EMPTY;
            }
            List<FluidTank> tanks = getAllTanks();
            int outputStart = CopperPotBlockEntity.FLUID_INPUTS;
            for (int i = outputStart; i < tanks.size(); i++) {
                FluidStack drained = tanks.get(i).drain(maxDrain, action);
                if (!drained.isEmpty()) {
                    if (action.execute()) {
                        saveAllTanks(tanks);
                    }
                    return drained;
                }
            }
            for (int i = 0; i < Math.min(outputStart, tanks.size()); i++) {
                FluidStack drained = tanks.get(i).drain(maxDrain, action);
                if (!drained.isEmpty()) {
                    if (action.execute()) {
                        saveAllTanks(tanks);
                    }
                    return drained;
                }
            }
            return FluidStack.EMPTY;
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
