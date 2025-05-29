package su.terrafirmagreg.core.mixins.common.create;

import com.simibubi.create.content.fluids.tank.BoilerData.BoilerFluidHandler;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = BoilerFluidHandler.class, remap = false)
public class BoilerFluidHandlerMixin {
    @Overwrite
    public boolean isFluidValid(int tank, FluidStack stack){    
      return FluidHelper.isWater(stack.getFluid()) || (TFCFluids.RIVER_WATER.get() == stack.getFluid());
    }
}
