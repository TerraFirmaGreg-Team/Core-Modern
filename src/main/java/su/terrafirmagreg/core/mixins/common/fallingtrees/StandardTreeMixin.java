package su.terrafirmagreg.core.mixins.common.fallingtrees;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

@Pseudo
@Mixin(targets = "me.pandamods.fallingtrees.trees.StandardTree", remap = false)
public class StandardTreeMixin {
    @Inject(method = "loopLeaves", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", ordinal = 0), cancellable = true)
    private void checkTFCDistance(
            BlockGetter level, BlockPos blockPos, Set<BlockPos> blocks,
            Set<BlockPos> loopedBlocks, int recursionDistance,
            CallbackInfo ci,
            @Local BlockState blockState) {
        if (blockState.hasProperty(TFCBlockStateProperties.DISTANCE_10) &&
                blockState.getValue(TFCBlockStateProperties.DISTANCE_10) != recursionDistance) {
            ci.cancel();
        }
    }
}
