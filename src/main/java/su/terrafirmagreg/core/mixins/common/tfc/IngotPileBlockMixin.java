package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.dries007.tfc.common.blocks.devices.IngotPileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

@Mixin(value = IngotPileBlock.class, remap = false)
public abstract class IngotPileBlockMixin {

    /**
     * Не спавнит партиклы при разрушении блока (блоки слитков и двойных слитков)
     */
    @Redirect(method = "onDestroyedByPlayer", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/common/blocks/devices/IngotPileBlock;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V"), remap = true)
    private void tfg$onDestroyedByPlayer(IngotPileBlock instance, Level level, BlockPos blockPos, BlockState blockState,
            Player player) {
        if (blockState.is(BlockTags.GUARDED_BY_PIGLINS)) {
            PiglinAi.angerNearbyPiglins(player, false);
        }

        level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(player, blockState));
    }
}
