package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.simibubi.create.content.fluids.tank.BoilerData.BoilerFluidHandler;

import net.minecraft.tags.FluidTags;
import net.minecraftforge.fluids.FluidStack;

@Mixin(value = BoilerFluidHandler.class, remap = false)
public class BoilerFluidHandlerMixin {
    /**
     * @author Zeropol
     * @reason Allows create boilers to use TFC river water, mars water, or gregtech steam too
     */
    @Overwrite
    public boolean isFluidValid(int tank, FluidStack stack) {
        return stack.getFluid().is(FluidTags.WATER)
                || stack.getFluid().is(GTMaterials.Steam.getFluidTag());
    }
}
