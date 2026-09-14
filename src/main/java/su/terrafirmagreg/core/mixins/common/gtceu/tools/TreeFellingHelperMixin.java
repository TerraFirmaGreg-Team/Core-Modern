package su.terrafirmagreg.core.mixins.common.gtceu.tools;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.item.tool.TreeFellingHelper;

import net.dries007.tfc.common.blocks.wood.BranchDirection;
import net.dries007.tfc.common.blocks.wood.LogBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = TreeFellingHelper.class, remap = false)
public abstract class TreeFellingHelperMixin {
    @Inject(method = "fellTree", at = @At("HEAD"), cancellable = true)
    private static void tfg$skipPlacedBrokenLog(ItemStack stack, Level level, BlockState origin, BlockPos originPos, LivingEntity miner, CallbackInfo ci) {
        if (isManuallyPlaced(origin)) {
            ci.cancel();
        }
    }

    @Redirect(method = "fellTree", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), remap = true)
    private static BlockState tfg$skipPlacedNeighbor(Level level, BlockPos pos) {
        BlockState neighbor = level.getBlockState(pos);
        if (isManuallyPlaced(neighbor)) {
            return Blocks.AIR.defaultBlockState();
        }
        return neighbor;
    }

    private static boolean isManuallyPlaced(BlockState blockState) {
        return blockState.hasProperty(LogBlock.BRANCH_DIRECTION) && blockState.getValue(LogBlock.BRANCH_DIRECTION) == BranchDirection.NONE;
    }
}
