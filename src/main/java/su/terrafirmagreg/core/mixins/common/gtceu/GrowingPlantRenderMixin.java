package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.*;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gregtechceu.gtceu.client.renderer.machine.impl.GrowingPlantRender;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.crop.DoubleCropBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@Mixin(value = GrowingPlantRender.class, remap = false)
public class GrowingPlantRenderMixin {

    // Add support for TFC's double crops

    @Unique
    private static final GrowingPlantRender.RenderFunction.ConfigureOnly TFC_DOUBLE_CROP_RENDER = (level, state, progress) -> {
        DoubleCropBlock block = (DoubleCropBlock) state.getBlock();
        IntegerProperty ageProp = block.getAgeProperty();
        int ageValue = (int) (progress * (block.getMaxAge() + 1));

        BlockState bottomState = state;
        bottomState = bottomState.trySetValue(ageProp, ageValue);

        if (progress > 0.5) {
            BlockState topState = state;
            topState = topState.trySetValue(TFCBlockStateProperties.DOUBLE_CROP_PART, DoubleCropBlock.Part.TOP);
            topState = topState.trySetValue(ageProp, ageValue);

            return Arrays.asList(
					new GrowingPlantRender.StateWithOffset(bottomState),
                    new GrowingPlantRender.StateWithOffset(topState, new Vector3f(0, 1, 0)));
        } else {
            return Collections.singleton(new GrowingPlantRender.StateWithOffset(bottomState));
        }
    };

    @Unique
    private static final GrowingPlantRender.GrowthMode TFC_DOUBLE_CROP_MODE = new GrowingPlantRender.GrowthMode("double_crop",
            block -> block instanceof DoubleCropBlock, TFC_DOUBLE_CROP_RENDER);

    @Inject(method = "getGrowthModeForBlock", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getGrowthModeForBlock(Block block, CallbackInfoReturnable<GrowingPlantRender.GrowthMode> cir) {
        if (block instanceof DoubleCropBlock) {
            cir.setReturnValue(TFC_DOUBLE_CROP_MODE);
        }
    }
}
