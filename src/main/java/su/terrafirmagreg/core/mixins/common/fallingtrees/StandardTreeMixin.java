package su.terrafirmagreg.core.mixins.common.fallingtrees;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.wood.BranchDirection;
import net.dries007.tfc.common.blocks.wood.LogBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import me.pandamods.fallingtrees.trees.StandardTree;

@Mixin(value = StandardTree.class, remap = false)
public class StandardTreeMixin {

    // Check TFC's distance property on the leaves
    @Inject(method = "loopLeaves", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", ordinal = 0), cancellable = true)
    private void checkTFCDistance(BlockGetter level, BlockPos blockPos, Set<BlockPos> blocks, Set<BlockPos> loopedBlocks, int recursionDistance, CallbackInfo ci, @Local BlockState blockState) {
        if (blockState.hasProperty(TFCBlockStateProperties.DISTANCE_10) &&
                blockState.getValue(TFCBlockStateProperties.DISTANCE_10) != recursionDistance) {
            ci.cancel();
        }
    }

    // Skip manually placed blocks if we can tell the difference
    @Inject(method = "loopLogs", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", remap = true), cancellable = true, remap = false)
    private void skipManuallyPlacedLogs(BlockGetter level, BlockPos blockPos, Set<BlockPos> blocks, Set<BlockPos> loopedBlocks, CallbackInfo ci, @Local BlockState blockState) {
        if (blockState.hasProperty(LogBlock.BRANCH_DIRECTION) && blockState.getValue(LogBlock.BRANCH_DIRECTION) == BranchDirection.NONE) {
            ci.cancel();
        }
    }

    // Allow leafless trees
    @Redirect(method = "getTreeData", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z", ordinal = 0))
    private boolean allowLeaflessTrees(Set<BlockPos> leavesBlocks) {
        return false; // false here means we don't care if the Set is empty, we allow the tree either way
    }
}
