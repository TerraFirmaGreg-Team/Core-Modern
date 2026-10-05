package su.terrafirmagreg.core.common.item.wearable;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;

import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.data.fuel_type.FuelType;

public class LiquidFuelJetpack extends PowerlessJetpack {
    @Override
    public double getVerticalHoverSlowSpeed() {
        return 0.005D;
    }

    @Override
    public double getVerticalHoverSpeed() {
        return 0.15D;
    }

    @Override
    public double getVerticalSpeed() {
        return 0.18D;
    }

    @Override
    public void drainEnergy(ItemStack stack, int amount) {
        super.drainEnergy(stack, amount * 2);
    }

    public static void registerFuel(FuelType fuel) {
        FUELS.putIfAbsent(FluidRecipeCapability.CAP.of(fuel.fluid()), 1);
    }
}
