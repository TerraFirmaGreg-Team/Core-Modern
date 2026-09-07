package su.terrafirmagreg.core.common.item.wearable;

import java.util.Map;

import com.forsteri.createliquidfuel.util.Triplet;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

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

    public static void registerFuel(Map.Entry<Fluid, Pair<ResourceLocation, Triplet<Integer, Boolean, Integer>>> fuel) {
        FUELS.putIfAbsent(FluidRecipeCapability.CAP.of(new FluidStack(fuel.getKey(), 1)), 1);
    }
}
