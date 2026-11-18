package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.negodya1.vintageimprovements.content.kinetics.helve_hammer.HelveBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = HelveBlockEntity.class, remap = false)
public abstract class HelveBlockEntityMixin extends SmartBlockEntity {
    public HelveBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "getProcessingSpeed", at = @At("HEAD"), cancellable = true)
    private void tfg$getProcessingSpeed(float speed, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Mth.clamp((int) Math.abs(speed / 4f), 1, 512));
    }
}
