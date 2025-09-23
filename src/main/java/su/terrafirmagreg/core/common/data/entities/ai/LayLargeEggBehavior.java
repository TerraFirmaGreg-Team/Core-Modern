package su.terrafirmagreg.core.common.data.entities.ai;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.entities.ai.pet.MoveOntoBlockBehavior;
import net.dries007.tfc.common.entities.misc.Seat;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.entities.TFGWoolEggProducingAnimal;

public class LayLargeEggBehavior extends MoveOntoBlockBehavior<TFGWoolEggProducingAnimal> {

    public LayLargeEggBehavior() {
        super(TFGBrain.LARGE_NEST_MEMORY.get(), true);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, TFGWoolEggProducingAnimal animal) {
        return animal.isReadyForAnimalProduct() && super.checkExtraStartConditions(level, animal);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, TFGWoolEggProducingAnimal animal, long time) {
        return animal.isReadyForAnimalProduct() && super.canStillUse(level, animal, time);
    }

    @Override
    protected void afterReached(TFGWoolEggProducingAnimal mob) {
        Seat.sit(mob.level(), mob.blockPosition(), mob);
    }

    @Override
    protected @NotNull Optional<BlockPos> getNearestTarget(TFGWoolEggProducingAnimal mob) {
        return mob.getBrain().getMemory(TFGBrain.LARGE_NEST_MEMORY.get());
    }

    @Override
    protected boolean isTargetAt(ServerLevel level, BlockPos pos) {
        return Helpers.isBlock(level.getBlockState(pos), TFGBlocks.LARGE_NEST_BOX.get())
                || Helpers.isBlock(level.getBlockState(pos), TFGBlocks.LARGE_NEST_BOX_WARPED.get());
    }

}
