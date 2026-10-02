package su.terrafirmagreg.core.mixins.common.create;

import static com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.KINDLED;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;

@Pseudo
@Mixin(value = BasinBlockEntity.class, remap = false)
public abstract class BasinBlockEntityMixin {

    @Inject(method = "getHeatLevelOf", at = @At("HEAD"), cancellable = true)
    private static void tfg$getHeatLevelOf(BlockState state, CallbackInfoReturnable<BlazeBurnerBlock.HeatLevel> cir) {
        if (state.hasProperty(TFCBlockStateProperties.HEAT_LEVEL)) {
            int heat = state.getValue(TFCBlockStateProperties.HEAT_LEVEL);

            if (heat >= 6) {
                cir.setReturnValue(BlazeBurnerBlock.HeatLevel.SEETHING);
            } else if (heat >= 3) {
                cir.setReturnValue(KINDLED);
            } else {
                cir.setReturnValue(BlazeBurnerBlock.HeatLevel.NONE);
            }
        }
    }

}
