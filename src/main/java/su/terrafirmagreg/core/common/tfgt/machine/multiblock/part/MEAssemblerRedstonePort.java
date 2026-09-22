package su.terrafirmagreg.core.common.tfgt.machine.multiblock.part;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEAssemblerRedstonePort extends MultiblockPartMachine {

    @SaveField
    private int signal = 0;

    public MEAssemblerRedstonePort(BlockEntityCreationInfo info) {
        super(info);
    }

    public void trySetSignal(int newSignal) {
        if (newSignal == signal)
            return;
        signal = newSignal;
        notifyBlockUpdate();
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
