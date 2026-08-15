package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gregtechceu.gtceu.api.fluids.forge.GTFluidImpl;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.fluids.FluidStack;

@Mixin(value = FluidDrainingBehaviour.class, remap = false)
public class FluidDrainingBehaviourMixin {

    // Fixes the hose pulley collecting in-world GT fluids as separate "flowing" and "source" fluids.
    // This converts them all into "source" ones

    @ModifyReturnValue(method = "getDrainableFluid", at = @At("RETURN"), remap = false)
    private FluidStack tfg$getDrainableFluid(FluidStack original) {
        if (original.getFluid() instanceof FlowingFluid flowingFluid
                && flowingFluid.getFlowing() instanceof GTFluidImpl
                && flowingFluid.getBucket() instanceof BucketItem bucket) {

            return new FluidStack(bucket.getFluid(), original.getAmount());
        }

        return original;
    }
}
