package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import net.dries007.tfc.common.blocks.CharcoalPileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Mixin(value = ToolHelper.class, remap = false)
public abstract class ToolHelperMixin {

    /**
     * Исправляет баг при ломании AOE инстрами кучи угля.
     * Возможно нужно добавить ? (я забыл, что, но это было где-то)
     * */
    @Redirect(method = "removeBlockRoutine", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;destroy(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"), remap = true)
    private static void tfg$removeBlockRoutine$block$destroy(Block instance, LevelAccessor pLevel, BlockPos pPos, BlockState pState, BlockState state, Level world, ServerPlayer player, BlockPos pos, boolean playSound) {
        if (instance instanceof CharcoalPileBlock charcoalPileBlock) {
            charcoalPileBlock.onDestroyedByPlayer(state, world, pPos, player, true, state.getFluidState());
        }
    }

    /*
        Changed the order of blocks broken from random to top to bottom
        Very specific things needed to be changed for this functions that are not able to be changes using mixins, therefore overwriting the entire thing is easier
     */
    @Overwrite
    public static Set<BlockPos> iterateAoE(ItemStack stack, AoESymmetrical aoeDefinition, Level world,
                                         Player player, HitResult rayTraceResult,
                                         ToolHelper.AOEFunction function){
        if (aoeDefinition != AoESymmetrical.none() && rayTraceResult instanceof BlockHitResult blockHit) {
            blockHit.getDirection();
            int column = aoeDefinition.column;
            int row = aoeDefinition.row;
            int layer = aoeDefinition.layer;
            Direction playerFacing = player.getDirection();
            Direction.Axis playerAxis = playerFacing.getAxis();
            Direction.Axis sideHitAxis = blockHit.getDirection().getAxis();
            Direction.AxisDirection sideHitAxisDir = blockHit.getDirection().getAxisDirection();
            Set<BlockPos> validPositions = new LinkedHashSet<>();
            if (sideHitAxis.isVertical()) {
                boolean isX = playerAxis == Direction.Axis.X;
                boolean isDown = sideHitAxisDir == Direction.AxisDirection.NEGATIVE;
                for (int y = 0; y <= layer; y++) {
                    for (int x = isX ? -row : -column; x <= (isX ? row : column); x++) {
                        for (int z = isX ? -column : -row; z <= (isX ? column : row); z++) {
                            if (!(x == 0 && y == 0 && z == 0)) {
                                BlockPos pos = blockHit.getBlockPos().offset(x, isDown ? y : -y, z);
                                if (player.mayUseItemAt(pos.relative(blockHit.getDirection()), blockHit.getDirection(), stack)) {
                                    if (function.apply(stack, world, player, pos, new UseOnContext(player.level(), player, player.getUsedItemHand(), stack, blockHit))) {
                                        validPositions.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                boolean isX = sideHitAxis == Direction.Axis.X;
                boolean isNegative = sideHitAxisDir == Direction.AxisDirection.NEGATIVE;
                // Special case for any additional column > 1: https://i.imgur.com/Dvcx7Vg.png
                // Same behaviour as the Flux Bore
                for (int y = (row == 0 ? 0 : row * 2 - 1); y >= (row == 0 ? 0 : -1); y--) {
                    for (int x = 0; x <= layer; x++) {
                        for (int z = -column; z <= column; z++) {
                            if (!(x == 0 && y == 0 && z == 0)) {
                                BlockPos pos = blockHit.getBlockPos().offset(
                                        isX ? (isNegative ? x : -x) : (isNegative ? z : -z), y,
                                        isX ? (isNegative ? z : -z) : (isNegative ? x : -x));
                                if (function.apply(stack, world, player, pos, new UseOnContext(player.level(), player, player.getUsedItemHand(), stack, blockHit))) {
                                    validPositions.add(pos);
                                }
                            }
                        }
                    }
                }
            }
            return validPositions;
        }
        return Collections.emptySet();

    }

}
