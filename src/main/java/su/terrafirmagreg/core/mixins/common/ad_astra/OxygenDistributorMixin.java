package su.terrafirmagreg.core.mixins.common.ad_astra;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

import earth.terrarium.adastra.common.blockentities.machines.OxygenDistributorBlockEntity;
import earth.terrarium.adastra.common.blockentities.machines.OxygenLoaderBlockEntity;
import earth.terrarium.adastra.common.config.MachineConfig;
import earth.terrarium.adastra.common.registry.ModSoundEvents;
import earth.terrarium.botarium.common.fluid.FluidConstants;

@Mixin(value = OxygenDistributorBlockEntity.class, remap = false)
public abstract class OxygenDistributorMixin extends OxygenLoaderBlockEntity {

    public OxygenDistributorMixin(BlockPos pos, BlockState state, int containerSize, Set<BlockPos> lastDistributedBlocks) {
        super(pos, state, containerSize);
    }

    @Shadow
    private int shutDownTicks;

    @Shadow
    protected abstract long calculateFluidPerTick();

    @Shadow
    protected abstract boolean canCraftDistribution(long fluidAmount);

    @Shadow
    protected abstract long calculateEnergyPerTick();

    @Shadow
    protected abstract void consumeDistribution(long fluidAmount);

    @Shadow
    protected abstract void tickOxygen(ServerLevel level, BlockPos pos, BlockState state);

    @Final
    @Shadow
    private Set<BlockPos> lastDistributedBlocks;

    @Shadow
    protected abstract void clearOxygenBlocks();

    @Shadow
    private long energyPerTick;

    @Shadow
    private float fluidPerTick;

    @Shadow
    private int distributedBlocksCount;

    /**
     * @author Bumperdo09
     * @reason the logic that handled consuming gas didn't work and always consumed 1mb/tick regardless of room size
     * This patches out the broken code and replaces it with simpler code
     * Overwriting the whole function isn't necessary I just couldn't figure out how to do it with more flexible mixins.
     */
    @Overwrite()
    public void serverTick(ServerLevel level, long time, BlockState state, BlockPos pos) {
        super.serverTick(level, time, state, pos);
        if (shutDownTicks > 0) {
            shutDownTicks--;
            return;
        }

        long fluidPerTick = calculateFluidPerTick();
        boolean canDistribute = canCraftDistribution(Math.max(FluidConstants.fromMillibuckets(1), fluidPerTick));
        if (canFunction() && canDistribute) {
            getEnergyStorage().internalExtract(calculateEnergyPerTick(), false);
            setLit(true);

            // This is the only change. Removing these statements and replacing them with one call to consumeDistribution()
            //accumulatedFluid += fluidPerTick;
            //int wholeBuckets = (int) (accumulatedFluid / 1000f);
            //if (wholeBuckets > 0) {
            //    consumeDistribution(FluidConstants.fromMillibuckets(Math.max(1, wholeBuckets / 1000)));
            //    accumulatedFluid -= wholeBuckets;
            //}
            consumeDistribution(FluidConstants.fromMillibuckets(Math.max(1, fluidPerTick)));

            if (time % MachineConfig.distributionRefreshRate == 0)
                tickOxygen(level, pos, state);

            if (time % 200 == 0) {
                level.playSound(null, pos, ModSoundEvents.OXYGEN_OUTTAKE.get(), SoundSource.BLOCKS, 0.2f, 1);
            } else if (time % 100 == 0) {
                level.playSound(null, pos, ModSoundEvents.OXYGEN_INTAKE.get(), SoundSource.BLOCKS, 0.2f, 1);
            }
        } else if (!lastDistributedBlocks.isEmpty()) {
            clearOxygenBlocks();
            shutDownTicks = 60;
            setLit(false);
        } else if (time % 10 == 0)
            setLit(false);

        energyPerTick = (recipe != null && canCraft() ? recipe.energy() : 0) + (canDistribute ? calculateEnergyPerTick() : 0);
        this.fluidPerTick = canDistribute ? fluidPerTick : 0;
        distributedBlocksCount = canDistribute ? lastDistributedBlocks.size() : 0;
    }

}
