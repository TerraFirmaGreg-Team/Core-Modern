package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.dries007.tfc.common.blockentities.QuernBlockEntity;
import net.dries007.tfc.common.blocks.devices.QuernBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mixin into {@link KineticBlockEntity} to add Create network handling for {@link IRotate} blocks.
 */
@Mixin(value = KineticBlockEntity.class, remap = false)
public abstract class KineticBlockEntityStressMixin {

    @Shadow
    protected float lastStressApplied;

    @Inject(method = "calculateStressApplied", at = @At("RETURN"), cancellable = true)
    private void onCalculateStressApplied(CallbackInfoReturnable<Float> cir) {
        KineticBlockEntity kbe = (KineticBlockEntity) (Object) this;
        Level level = kbe.getLevel();
        if (level == null)
            return;
        BlockPos pos = kbe.getBlockPos();
        float extraStress = 0;
        for (Direction d : Direction.values()) {
            BlockPos neighborPos = pos.relative(d);
            BlockState neighborState = level.getBlockState(neighborPos);
            Block neighborBlock = neighborState.getBlock();
            if (neighborBlock instanceof IRotate rotate && !(neighborBlock instanceof KineticBlock)) {
                if (rotate.hasShaftTowards(level, neighborPos, neighborState, d.getOpposite())
                        && RotationPropagatorAccessor.callGetAxisModifier(kbe, d) != 0) {
                    double impact = BlockStressValues.getImpact(neighborBlock);
                    if (impact > 0) {
                        if (neighborBlock instanceof QuernBlock) {
                            BlockEntity be = level.getBlockEntity(neighborPos);
                            if (be instanceof QuernBlockEntity quern && !quern.hasHandstone()) {
                                continue;
                            }
                        }
                        extraStress += (float) impact;
                    }
                }
            }
        }
        if (extraStress > 0) {
            float total = cir.getReturnValue() + extraStress;
            this.lastStressApplied = total;
            cir.setReturnValue(total);
        }
    }
}
