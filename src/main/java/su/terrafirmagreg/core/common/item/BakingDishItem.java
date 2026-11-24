package su.terrafirmagreg.core.common.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.HeatHandler;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.ItemStackHandler;

import su.terrafirmagreg.core.common.blockentity.appliances.BakingDishBlockEntity;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;
import su.terrafirmagreg.core.common.recipe.BakingDishRecipe;

public class BakingDishItem extends CookingApplianceBlockItem {

    private static final Cache<CompoundTag, Optional<Component>> TOOLTIP_CACHE = CacheBuilder.newBuilder()
            .maximumSize(500)
            .build();

    public BakingDishItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new BakingDishItemStackCapabilityProvider(stack);
    }

    public static class BakingDishItemStackCapabilityProvider implements ICapabilitySerializable<CompoundTag> {

        private final CookingApplianceItemStackFluidHandler fluidHandler;
        private final HeatHandler heatHandler;

        public BakingDishItemStackCapabilityProvider(ItemStack stack) {
            this.fluidHandler = new CookingApplianceItemStackFluidHandler(
                    stack,
                    BakingDishBlockEntity.FLUID_SLOTS,
                    0,
                    BakingDishBlockEntity.FLUID_CAPACITY,
                    0);
            this.heatHandler = new HeatHandler(HeatCapability.POTTERY_HEAT_CAPACITY, 0, 0);
        }

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == HeatCapability.CAPABILITY || cap == HeatCapability.NETWORK_CAPABILITY) {
                return heatHandler.getCapability(cap, side);
            }
            return fluidHandler.getCapability(cap, side);
        }

        @Override
        public CompoundTag serializeNBT() {
            return heatHandler.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            heatHandler.deserializeNBT(nbt);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        addBakingTooltip(stack, level, tooltip);
    }

    private static void addBakingTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        CompoundTag beTag = stack.getTagElement(Helpers.BLOCK_ENTITY_TAG);
        if (beTag == null) {
            return;
        }

        Optional<Component> cached = TOOLTIP_CACHE.getIfPresent(beTag);
        if (cached != null) {
            cached.ifPresent(tooltip::add);
            return;
        }

        Component computed = computeBakingTooltip(beTag, level);
        TOOLTIP_CACHE.put(beTag, Optional.ofNullable(computed));
        if (computed != null) {
            tooltip.add(computed);
        }
    }

    private static @Nullable Component computeBakingTooltip(CompoundTag beTag, @Nullable Level level) {
        Level lvl = level;
        if (lvl == null && FMLEnvironment.dist == Dist.CLIENT) {
            lvl = Minecraft.getInstance().level;
        }
        if (lvl == null) {
            return null;
        }

        ItemStackHandler items = new ItemStackHandler(BakingDishBlockEntity.ITEM_SLOTS);
        FluidTank tank = new FluidTank(BakingDishBlockEntity.FLUID_CAPACITY);

        if (beTag.contains("inventory", Tag.TAG_COMPOUND)) {
            CompoundTag invTag = beTag.getCompound("inventory");
            if (invTag.contains("inventory", Tag.TAG_COMPOUND)) {
                items.deserializeNBT(invTag.getCompound("inventory"));
            } else if (invTag.contains("Items", Tag.TAG_LIST)) {
                items.deserializeNBT(invTag);
            }
            if (invTag.contains("tanks", Tag.TAG_LIST)) {
                ListTag tanksTag = invTag.getList("tanks", Tag.TAG_COMPOUND);
                if (!tanksTag.isEmpty()) {
                    tank.readFromNBT(tanksTag.getCompound(0));
                }
            }
        }
        if (tank.isEmpty()) {
            if (beTag.contains("tanks", Tag.TAG_LIST)) {
                ListTag tanksTag = beTag.getList("tanks", Tag.TAG_COMPOUND);
                if (!tanksTag.isEmpty()) {
                    tank.readFromNBT(tanksTag.getCompound(0));
                }
            } else if (beTag.contains("tank", Tag.TAG_COMPOUND)) {
                tank.readFromNBT(beTag.getCompound("tank"));
            }
        }

        boolean hasItems = false;
        List<ItemStack> nonNullInputs = new ArrayList<>();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack inSlot = items.getStackInSlot(i);
            if (!inSlot.isEmpty()) {
                hasItems = true;
                nonNullInputs.add(inSlot);
            }
        }
        if (!hasItems && tank.isEmpty()) {
            return null;
        }

        for (BakingDishRecipe recipe : lvl.getRecipeManager().getAllRecipesFor(TFGRecipeTypes.BAKING_DISH.get())) {
            if (recipe.matchItemInputs(items, false).matches() && recipe.matchFluidInputs(tank) && recipe.canFitOutputs(items, tank)) {
                List<Component> outputNames = new ArrayList<>();
                for (int outIdx = 0; outIdx < recipe.getItemOutputs().size(); outIdx++) {
                    ItemStack in = nonNullInputs.isEmpty() ? ItemStack.EMPTY : nonNullInputs.get(Math.min(outIdx, nonNullInputs.size() - 1));
                    ItemStack out = recipe.getItemOutputs().get(outIdx).getStack(in);
                    if (!out.isEmpty()) {
                        if (out.getCount() > 1) {
                            outputNames.add(Component.literal(out.getCount() + "x ").append(out.getHoverName()));
                        } else {
                            outputNames.add(out.getHoverName().copy());
                        }
                    }
                }
                for (FluidStack fluid : recipe.getFluidOutputs()) {
                    if (!fluid.isEmpty()) {
                        outputNames.add(Component.literal(fluid.getAmount() + " mB ").append(fluid.getDisplayName()));
                    }
                }

                if (outputNames.isEmpty()) {
                    continue;
                }

                MutableComponent outputsText = Component.empty();
                for (int i = 0; i < outputNames.size(); i++) {
                    if (i > 0) {
                        outputsText.append(Component.literal(", "));
                    }
                    outputsText.append(outputNames.get(i));
                }

                float temp = recipe.getMinTemp();
                MutableComponent formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temp);
                if (formattedTemp == null) {
                    formattedTemp = TFCConfig.CLIENT.heatTooltipStyle.get().formatRange(temp);
                }
                if (formattedTemp == null) {
                    formattedTemp = Component.literal(String.format("%.0f°C", temp));
                }

                return Component.translatable("tfg.tooltip.baking_dish.bakes_into", outputsText, formattedTemp).withStyle(ChatFormatting.GRAY);
            }
        }
        return null;
    }
}
