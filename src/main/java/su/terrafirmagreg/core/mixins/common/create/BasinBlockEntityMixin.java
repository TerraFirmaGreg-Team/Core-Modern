package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import net.dries007.tfc.common.blocks.devices.CharcoalForgeBlock;
import net.minecraft.world.level.block.state.BlockState;

@Pseudo
@Mixin(value = BasinBlockEntity.class, remap = false)
public abstract class BasinBlockEntityMixin {

    @Inject(method = "getHeatLevelOf", at = @At("HEAD"), cancellable = true)
    private static void tfg$getHeatLevelOf(BlockState state, CallbackInfoReturnable<BlazeBurnerBlock.HeatLevel> cir) {
        if (state.getBlock() instanceof CharcoalForgeBlock) {
            int heat = state.getValue(CharcoalForgeBlock.HEAT);
            if (heat >= 7) {
                cir.setReturnValue(BlazeBurnerBlock.HeatLevel.SEETHING);
            } else if (heat >= 3) {
                cir.setReturnValue(BlazeBurnerBlock.HeatLevel.KINDLED);
            } else {
                cir.setReturnValue(BlazeBurnerBlock.HeatLevel.NONE);
            }
        }

    }

}
