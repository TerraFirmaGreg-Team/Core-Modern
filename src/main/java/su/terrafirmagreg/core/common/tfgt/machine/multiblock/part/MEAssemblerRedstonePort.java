package su.terrafirmagreg.core.common.tfgt.machine.multiblock.part;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRedstoneSignalMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEAssemblerRedstonePort extends MultiblockPartMachine implements IRedstoneSignalMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEAssemblerRedstonePort.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Persisted
    private int signal = 0;

    public MEAssemblerRedstonePort(IMachineBlockEntity holder) {
        super(holder);
    }

    public void trySetSignal(int newSignal) {
        if (newSignal == signal)
            return;
        signal = newSignal;
        updateSignal();
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        return signal;
    }

    @Override
    public int getOutputDirectSignal(Direction direction) {
        return signal;
    }

    @Override
    public int getAnalogOutputSignal() {
        return signal;
    }

    @Override
    public boolean canConnectRedstone(Direction side) {
        return true;
    }
}
