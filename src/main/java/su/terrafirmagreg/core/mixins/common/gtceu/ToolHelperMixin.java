package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import net.dries007.tfc.common.blocks.CharcoalPileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
}
