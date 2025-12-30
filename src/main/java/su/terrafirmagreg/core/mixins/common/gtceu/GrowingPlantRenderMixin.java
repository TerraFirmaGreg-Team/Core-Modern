package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.Collection;
import java.util.List;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.client.renderer.machine.impl.GrowingPlantRender;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.crop.DoubleCropBlock;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@Mixin(value = GrowingPlantRender.RenderFunction.class, remap = false)
public interface GrowingPlantRenderMixin {

    // TODO: This doesn't work at the moment.
    // Revisit whenever the next version of GTM is out, because the target changed a bunch.
    // Basically, we need to tell it about TFC's double-crop-part block property, because it's
    // different from the one vanilla tall plants use

    /*@Inject(method = "lambda$static$4", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;trySetValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;", ordinal = 0, shift = At.Shift.AFTER), remap = false, cancellable = true)
    private static void tfg$addPartProperty(Integer min, Integer max, int minValue, IntegerProperty property, int maxValue, BlockAndTintGetter level, BlockState state, double progress,
            CallbackInfoReturnable<Collection> cir) {
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            final var topState = state.trySetValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            cir.setReturnValue(List.of(new GrowingPlantRender.StateWithOffset(state), new GrowingPlantRender.StateWithOffset(topState, new Vector3f(0, 1, 0))));
        }
        if (state.hasProperty(BlockStateProperties.HALF)) {
            final var topState = state.trySetValue(BlockStateProperties.HALF, Half.TOP);
            cir.setReturnValue(List.of(new GrowingPlantRender.StateWithOffset(state), new GrowingPlantRender.StateWithOffset(topState, new Vector3f(0, 1, 0))));
        }
        if (state.hasProperty(TFCBlockStateProperties.DOUBLE_CROP_PART)) {
            final var topState = state.trySetValue(TFCBlockStateProperties.DOUBLE_CROP_PART, DoubleCropBlock.Part.TOP);
            cir.setReturnValue(List.of(new GrowingPlantRender.StateWithOffset(state), new GrowingPlantRender.StateWithOffset(topState, new Vector3f(0, 1, 0))));
        }
    }*/
}
