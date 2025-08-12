package su.terrafirmagreg.core.common.data.entities.ai;

import net.dries007.tfc.common.entities.ai.pet.MoveOntoBlockBehavior;
import net.dries007.tfc.common.entities.misc.Seat;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.entities.TFCSniffer;

import java.util.Optional;

public class LayLargeEggBehavior extends MoveOntoBlockBehavior<TFCSniffer> {

    public LayLargeEggBehavior()
    {
        super(TFGBrain.LARGE_NEST_MEMORY.get(), true);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, TFCSniffer animal)
    {
        return animal.isReadyForAnimalProduct() && super.checkExtraStartConditions(level, animal);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, TFCSniffer animal, long time)
    {
        return animal.isReadyForAnimalProduct() && super.canStillUse(level, animal, time);
    }

    @Override
    protected void afterReached(TFCSniffer mob)
    {
        Seat.sit(mob.level(), mob.blockPosition(), mob);
    }

    @Override
    protected Optional<BlockPos> getNearestTarget(TFCSniffer mob)
    {
        return mob.getBrain().getMemory(TFGBrain.LARGE_NEST_MEMORY.get());
    }

    @Override
    protected boolean isTargetAt(ServerLevel level, BlockPos pos)
    {
        return Helpers.isBlock(level.getBlockState(pos), TFGBlocks.LARGE_NEST_BOX.get());
    }

}
