package su.terrafirmagreg.core.common.item;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labellum.mc.waterflasks.item.FlaskItem;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.CombinedCapabilityProvider;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.food.TFCFoodData;
import net.dries007.tfc.util.Drinkable;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ElectricFlaskItem extends FlaskItem {

    public static final int CAPACITY = 4000;
    public static final long MAX_CHARGE = 100_000L; // Only matter for Creative item
    public static final int TIER = GTValues.LV;
    public static final long EU_PER_DRINK = 1_000L;

    public ElectricFlaskItem(Properties properties) {
        super(properties, () -> CAPACITY, DEFAULT_DRINK, () -> Items.AIR);
    }

    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable CompoundTag nbt) {
        final LazyOptional<IElectricItem> electric = LazyOptional.of(() -> new ElectricItem(stack, MAX_CHARGE, TIER, true, false));
        return new CombinedCapabilityProvider(super.initCapabilities(stack, nbt), new ICapabilityProvider() {

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                return GTCapability.CAPABILITY_ELECTRIC_ITEM.orEmpty(cap, electric);
            }
        });
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || level.getGameTime() % 20 != 0 || !(entity instanceof Player player) || !(player.getFoodData() instanceof TFCFoodData food)) {
            return;
        }

        final IElectricItem electric = GTCapabilityHelper.getElectricItem(stack);
        final IFluidHandler handler = Helpers.getCapability(stack, Capabilities.FLUID_ITEM);
        if (electric == null || handler == null || !electric.canUse(EU_PER_DRINK)) {
            return;
        }

        final FluidStack sip = handler.drain(DEFAULT_DRINK, IFluidHandler.FluidAction.SIMULATE);
        final Drinkable drinkable = Drinkable.get(sip.getFluid());
        if (drinkable == null || sip.getAmount() < DEFAULT_DRINK) {
            return;
        }

        final float gain = drinkable.getThirst() * DEFAULT_DRINK / 25f;
        if (gain <= 0 || TFCFoodData.MAX_THIRST - food.getThirst() < gain) {
            return;
        }

        electric.discharge(EU_PER_DRINK, TIER, true, false, false);
        drinkable.onDrink(player, handler.drain(DEFAULT_DRINK, IFluidHandler.FluidAction.EXECUTE).getAmount());
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        final IElectricItem electric = GTCapabilityHelper.getElectricItem(stack);
        if (electric != null) {
            ElectricStats.addCurrentChargeTooltip(tooltip, electric.getCharge(), electric.getMaxCharge(), electric.getTier(), false);
        }
    }
}
