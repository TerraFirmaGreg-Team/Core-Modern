package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.tags.FluidTags;

@Mixin(value = SteamBoilerMachine.class, remap = false)
public abstract class SteamBoilerMachineMixin {

    /**
     * Allow TFC river water in steam boilers
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;setFilter(Ljava/util/function/Predicate;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;"), remap = false)
    private NotifiableFluidTank tfg$init$notifiableFluidTank$setFilter(NotifiableFluidTank instance,
            Predicate<FluidStack> fluidStackPredicate) {
        return instance.setFilter(fluidStack -> fluidStack.getFluid().is(FluidTags.WATER));
    }
}
