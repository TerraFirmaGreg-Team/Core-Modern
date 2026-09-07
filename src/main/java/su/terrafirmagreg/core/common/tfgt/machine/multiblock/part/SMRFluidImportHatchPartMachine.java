package su.terrafirmagreg.core.common.tfgt.machine.multiblock.part;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraftforge.fluids.FluidType;

import su.terrafirmagreg.core.common.data.TFGTags;

// Credit to https://github.com/Phoenixvine32908/PhoenixCore/

@ParametersAreNonnullByDefault
public class SMRFluidImportHatchPartMachine extends FluidHatchPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(SMRFluidImportHatchPartMachine.class,
            FluidHatchPartMachine.MANAGED_FIELD_HOLDER);

    public static final int BASE_CAPACITY = 16 * FluidType.BUCKET_VOLUME;

    public SMRFluidImportHatchPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.IN, BASE_CAPACITY, 1);
    }

    @NotNull
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        int capacity = getCapacityForTier(getTier());
        return new NotifiableFluidTank(this, 1, capacity, IO.IN)
                .setFilter(stack -> stack.getFluid().builtInRegistryHolder().is(TFGTags.Fluids.SMR_FLUIDS));

    }

    protected int getCapacityForTier(int tier) {
        return BASE_CAPACITY * (1 << Math.min(6, tier));
    }

    @Override
    public boolean swapIO() {
        return false;
    }
}
