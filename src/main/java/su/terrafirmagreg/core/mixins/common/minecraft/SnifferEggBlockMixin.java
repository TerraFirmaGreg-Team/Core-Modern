package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import su.terrafirmagreg.core.common.data.TFGEntities;
import su.terrafirmagreg.core.common.data.entities.sniffer.TFCSniffer;

@Mixin(value = SnifferEggBlock.class)
public abstract class SnifferEggBlockMixin {

    @Shadow
    private boolean isReadyToHatch(BlockState pState) {
        return false;
    }

    /**
     * @author Pyritie
     * @reason Replace minecraft sniffer with TFG sniffer
     */
    @Overwrite
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {

        if (!isReadyToHatch(pState)) {
            pLevel.playSound(null, pPos, SoundEvents.SNIFFER_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + pRandom.nextFloat() * 0.2F);
            pLevel.setBlock(pPos, pState.setValue(SnifferEggBlock.HATCH, ((SnifferEggBlock) (Object) this).getHatchLevel(pState) + 1), 2);
        } else {
            pLevel.playSound(null, pPos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + pRandom.nextFloat() * 0.2F);
            pLevel.destroyBlock(pPos, false);
            TFCSniffer sniffa = TFGEntities.SNIFFER.get().create(pLevel);

            if (sniffa != null) {
                Vec3 center = pPos.getCenter();
                sniffa.setAge(0);
                sniffa.moveTo(center.x(), center.y(), center.z(), Mth.wrapDegrees(pLevel.random.nextFloat() * 360.0F), 0.0F);
                pLevel.addFreshEntity(sniffa);
            }
        }
    }
}
