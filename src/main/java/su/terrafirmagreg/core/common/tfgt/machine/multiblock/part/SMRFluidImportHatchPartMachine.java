package su.terrafirmagreg.core.common.tfgt.machine.multiblock.part;

import javax.annotation.ParametersAreNonnullByDefault;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;

import net.minecraftforge.fluids.FluidType;

import su.terrafirmagreg.core.common.data.TFGTags;

// Credit to https://github.com/Phoenixvine32908/PhoenixCore/

@ParametersAreNonnullByDefault
public class SMRFluidImportHatchPartMachine extends FluidHatchPartMachine {

    public static final int BASE_CAPACITY = 16 * FluidType.BUCKET_VOLUME;

    public SMRFluidImportHatchPartMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, IO.IN, new NotifiableFluidTank(1, getCapacityForTier(tier), IO.IN)
                .setFilter(stack -> stack.getFluid().builtInRegistryHolder().is(TFGTags.Fluids.SMR_FLUIDS)));
    }

    protected static int getCapacityForTier(int tier) {
        return BASE_CAPACITY * (1 << Math.min(6, tier));
    }

    @Override
    public boolean swapIO() {
        return false;
    }
}
