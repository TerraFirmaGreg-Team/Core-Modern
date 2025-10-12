package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.OpenEndedPipe;

import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

@Mixin(value = OpenEndedPipe.class, remap = false)
public class OpenEndedPipeMixin {

    @Inject(method = "removeFluidFromSpace", at = @At("RETURN"), cancellable = true, remap = false)
    private void tfg$removeFluidFromSpace(CallbackInfoReturnable<FluidStack> cir) {
        FluidStack stack = cir.getReturnValue();

        if (stack.getFluid() == TFCFluids.RIVER_WATER.get()) {
            //System.out.println("Found River Water");
            cir.setReturnValue(new FluidStack(Fluids.WATER.getSource(), stack.getAmount()));
        }
    }

}
