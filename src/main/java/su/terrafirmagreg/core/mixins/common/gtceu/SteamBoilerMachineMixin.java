package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.machine.steam.SteamBoilerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.world.level.material.Fluids;

@Mixin(value = SteamBoilerMachine.class, remap = false)
public abstract class SteamBoilerMachineMixin {

    /**
     * Разрешает наливать в паровой бойлер речную или обычную ванильную воду. Хоть речная вода и является ванильной по
     * своей сути, но видимо имеет другую ссылку в памяти, поэтому они считаются разными.
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;setFilter(Ljava/util/function/Predicate;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableFluidTank;"), remap = false)
    private NotifiableFluidTank tfg$init$notifiableFluidTank$setFilter(NotifiableFluidTank instance,
            Predicate<FluidStack> fluidStackPredicate) {
        return instance.setFilter(fluidStack -> ((TFCFluids.RIVER_WATER.get() == fluidStack.getFluid())
                || (Fluids.WATER == fluidStack.getFluid())));
    }
}
